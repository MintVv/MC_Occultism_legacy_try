package com.occultism.occultism.resource;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.EnumMap;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.occultism.occultism.config.ModConfig;
import com.occultism.occultism.entity.EntityAfritJob;
import com.occultism.occultism.entity.EntityDjinniJob;
import com.occultism.occultism.entity.EntityFoliotJob;
import com.occultism.occultism.spirit.SpiritJobType;
import com.occultism.occultism.spirit.SpiritTier;
import com.occultism.occultism.spirit.WorldEffectJob;
import net.minecraft.init.Bootstrap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.storage.WorldInfo;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class WorldEffectJobPortTest {
    @BeforeAll
    static void bootstrap() {
        Bootstrap.register();
    }

    @Test
    void weatherJobsWaitForTheirExactUpstreamCastDurations() {
        Map<SpiritJobType, Integer> durations = new EnumMap<>(SpiritJobType.class);
        durations.put(SpiritJobType.CLEAR_WEATHER, 300);
        durations.put(SpiritJobType.RAIN_WEATHER, 600);
        durations.put(SpiritJobType.THUNDER_WEATHER, 1200);
        for (Map.Entry<SpiritJobType, Integer> entry : durations.entrySet()) {
            WorldInfo info = worldInfo(6000);
            info.setRaining(entry.getKey() == SpiritJobType.CLEAR_WEATHER);
            info.setThundering(info.isRaining());
            boolean originalRain = info.isRaining();
            WorldEffectJob job = new WorldEffectJob(entry.getKey());
            assertEquals(entry.getValue().intValue(), job.getRequiredChangeTicks());
            for (int tick = 1; tick < entry.getValue(); tick++) {
                assertFalse(job.tick(info, true));
                assertEquals(originalRain, info.isRaining(), "weather must not change early");
            }
            assertTrue(job.tick(info, true));
            boolean clear = entry.getKey() == SpiritJobType.CLEAR_WEATHER;
            assertEquals(!clear, info.isRaining());
            assertEquals(entry.getKey() == SpiritJobType.THUNDER_WEATHER, info.isThundering());
            assertEquals(clear ? 6000 : 0, info.getCleanWeatherTime());
            assertEquals(clear ? 0 : 6000, info.getRainTime());
            assertEquals(clear ? 0 : 6000, info.getThunderTime());
        }
    }

    @Test
    void timeUsesTheNextDawnAndDuskRatherThanTheOldImmediate1000TickTarget() {
        assertEquals(24000L, WorldEffectJob.nextTime(0, true));
        assertEquals(24000L, WorldEffectJob.nextTime(13000, true));
        assertEquals(48000L, WorldEffectJob.nextTime(24000, true));
        assertEquals(13000L, WorldEffectJob.nextTime(0, false));
        assertEquals(13000L, WorldEffectJob.nextTime(12999, false));
        assertEquals(37000L, WorldEffectJob.nextTime(13000, false));
        assertEquals(37000L, WorldEffectJob.nextTime(24000, false));
    }

    @Test
    void interpolationConvergesOver100TicksEvenWhenDaylightContinues() {
        for (SpiritJobType type : new SpiritJobType[] {SpiritJobType.DAY_TIME, SpiritJobType.NIGHT_TIME}) {
            WorldInfo info = worldInfo(6000);
            WorldEffectJob job = new WorldEffectJob(type);
            long target = WorldEffectJob.nextTime(6000, type == SpiritJobType.DAY_TIME);
            for (int tick = 1; tick <= 100; tick++) {
                long before = info.getWorldTime();
                assertEquals(tick == 100, job.tick(info, true));
                long expected = Math.min(before + (target - before) / Math.max(100 - tick, 1), target);
                assertEquals(expected, info.getWorldTime(), "each step must match ChangeTimeJob.updateTime");
                // Upstream reaches the target at tick 99 and clamps a final natural daylight tick back.
                assertTrue(info.getWorldTime() >= Math.min(before, target));
                if (tick == 1) {
                    assertNotEquals(target, info.getWorldTime(), "time change must be gradual");
                }
                if (tick < 100) {
                    info.setWorldTime(info.getWorldTime() + 1);
                }
            }
            assertEquals(target, info.getWorldTime());
        }
    }

    @Test
    void disabledWeatherWaitsButDisabledTimeExitsWithoutChangingTheWorld() {
        WorldInfo info = worldInfo(6000);
        info.setRainTime(123);
        WorldEffectJob weather = new WorldEffectJob(SpiritJobType.RAIN_WEATHER);
        for (int tick = 1; tick <= 600; tick++) {
            assertEquals(tick == 600, weather.tick(info, false));
        }
        assertFalse(info.isRaining());
        assertEquals(123, info.getRainTime());
        WorldEffectJob day = new WorldEffectJob(SpiritJobType.DAY_TIME);
        assertTrue(day.tick(info, false));
        assertEquals(0, day.getCurrentChangeTicks());
        assertEquals(6000, info.getWorldTime());
    }

    @Test
    void completedOrReloadedJobsNeverApplyTheirEffectAgain() {
        WorldInfo info = worldInfo(1000);
        WorldEffectJob job = new WorldEffectJob(SpiritJobType.CLEAR_WEATHER);
        for (int tick = 0; tick < 300; tick++) {
            job.tick(info, true);
        }
        NBTTagCompound saved = job.writeToNBT();
        WorldEffectJob restored = new WorldEffectJob(SpiritJobType.CLEAR_WEATHER);
        restored.readFromNBT(saved);
        assertEquals(300, restored.getCurrentChangeTicks());
        info.setRaining(true);
        info.setRainTime(333);
        assertTrue(restored.tick(info, true));
        assertTrue(info.isRaining());
        assertEquals(333, info.getRainTime());
    }

    @Test
    void savedWeatherProgressDoesNotRestartItsCast() {
        WorldInfo info = worldInfo(0);
        WorldEffectJob job = new WorldEffectJob(SpiritJobType.RAIN_WEATHER);
        for (int tick = 0; tick < 350; tick++) {
            assertFalse(job.tick(info, true));
        }
        WorldEffectJob restored = new WorldEffectJob(SpiritJobType.RAIN_WEATHER);
        restored.readFromNBT(job.writeToNBT());
        assertEquals(350, restored.getCurrentChangeTicks());
        for (int tick = 351; tick < 600; tick++) {
            assertFalse(restored.tick(info, true));
            assertFalse(info.isRaining());
        }
        assertTrue(restored.tick(info, true));
        assertTrue(info.isRaining());
    }

    @Test
    void disablingWeatherDuringTheCastStillPreventsItsFinalWorldChange() {
        WorldInfo info = worldInfo(0);
        info.setRaining(true);
        WorldEffectJob job = new WorldEffectJob(SpiritJobType.CLEAR_WEATHER);
        for (int tick = 0; tick < 299; tick++) {
            job.tick(info, true);
        }
        assertTrue(job.tick(info, false));
        assertTrue(info.isRaining());
        // A subsequent config enable cannot replay a completed worker.
        assertTrue(job.tick(info, true));
        assertTrue(info.isRaining());
    }

    @Test
    void savedInterpolationKeepsTheOriginalTargetInsteadOfStartingAnotherDay() {
        WorldInfo info = worldInfo(12000);
        WorldEffectJob job = new WorldEffectJob(SpiritJobType.DAY_TIME);
        for (int tick = 0; tick < 40; tick++) {
            job.tick(info, true);
        }
        NBTTagCompound saved = job.writeToNBT();
        assertEquals(40, saved.getInteger("currentChangeTicks"));
        assertEquals(100, saved.getInteger("requiredChangeTicks"));
        assertEquals(24000L, saved.getLong("newTime"));
        WorldEffectJob restored = new WorldEffectJob(SpiritJobType.DAY_TIME);
        restored.readFromNBT(saved);
        for (int tick = 41; tick <= 100; tick++) {
            assertEquals(tick == 100, restored.tick(info, true));
        }
        assertEquals(24000L, info.getWorldTime());
    }

    @Test
    void malformedCountersAreClampedAndMissingDataUsesTheJobDefaults() {
        WorldEffectJob job = new WorldEffectJob(SpiritJobType.THUNDER_WEATHER);
        job.readFromNBT(new NBTTagCompound());
        assertEquals(1200, job.getRequiredChangeTicks());
        NBTTagCompound malformed = new NBTTagCompound();
        malformed.setInteger("requiredChangeTicks", -1);
        malformed.setInteger("currentChangeTicks", Integer.MAX_VALUE);
        job.readFromNBT(malformed);
        assertEquals(1, job.getRequiredChangeTicks());
        assertEquals(0, job.getCurrentChangeTicks());
        assertTrue(job.tick(worldInfo(0), false));
        assertThrows(IllegalArgumentException.class, () -> new WorldEffectJob(SpiritJobType.CRUSHER));
    }

    @Test
    void dedicatedTierWorkersSaveTheirJobStateAndChangingProfessionClearsIt() {
        EntityFoliotJob djinni = EntityFoliotJob.createForTier(null, SpiritJobType.DAY_TIME, SpiritTier.DJINNI);
        assertTrue(djinni instanceof EntityDjinniJob);
        WorldInfo info = worldInfo(6000);
        for (int tick = 0; tick < 35; tick++) {
            djinni.getWorldEffectJob().tick(info, true);
        }
        NBTTagCompound saved = new NBTTagCompound();
        djinni.writeEntityToNBT(saved);
        assertEquals("occultism:day_time", saved.getCompoundTag("spiritJob").getString("factoryId"));
        EntityDjinniJob loaded = new EntityDjinniJob(null);
        loaded.readEntityFromNBT(saved);
        assertEquals(SpiritJobType.DAY_TIME, loaded.getJobType());
        assertEquals(35, loaded.getWorldEffectJob().getCurrentChangeTicks());
        assertEquals(24000, loaded.getWorldEffectJob().getNewTime());
        loaded.setJobType(SpiritJobType.CRUSHER);
        assertNull(loaded.getWorldEffectJob());
        loaded.writeEntityToNBT(saved);
        assertFalse(saved.hasKey("spiritJob"), "reused NBT must not keep an old weather job");
        EntityFoliotJob afrit = EntityFoliotJob.createForTier(null, SpiritJobType.RAIN_WEATHER, SpiritTier.AFRIT);
        assertTrue(afrit instanceof EntityAfritJob);
        assertEquals(600, afrit.getWorldEffectJob().getRequiredChangeTicks());
    }

    @Test
    void eachPermissionIsIndependentAndEnabledByDefault() {
        ModConfig.RitualSettings settings = new ModConfig.RitualSettings();
        for (SpiritJobType type : SpiritJobType.values()) {
            assertEquals(type.isWorldEffectJob(), settings.isEnabled(type));
        }
        settings.enableRainWeatherRitual = false;
        assertFalse(settings.isEnabled(SpiritJobType.RAIN_WEATHER));
        assertTrue(settings.isEnabled(SpiritJobType.THUNDER_WEATHER));
        assertTrue(settings.isEnabled(SpiritJobType.DAY_TIME));
    }

    @Test
    void allFiveRitualsDescribeRealTieredWorkersAndBowlHasNoImmediateWorldMutation() throws Exception {
        String rituals = read("src/main/resources/assets/occultism/rituals/rituals.json");
        int checked = 0;
        for (JsonElement element : new JsonParser().parse(rituals).getAsJsonArray()) {
            JsonObject ritual = element.getAsJsonObject();
            if (!ritual.has("spirit_job_type")) {
                continue;
            }
            SpiritJobType type = SpiritJobType.fromId(ritual.get("spirit_job_type").getAsString());
            if (!type.isWorldEffectJob()) {
                continue;
            }
            boolean afrit = type == SpiritJobType.RAIN_WEATHER || type == SpiritJobType.THUNDER_WEATHER;
            assertEquals(afrit ? "occultism:afrit" : "occultism:djinni", ritual.get("spirit_tier").getAsString());
            assertEquals(afrit ? "occultism:afrit_job" : "occultism:djinni_job",
                    ritual.get("entity_to_summon").getAsString());
            assertEquals(1200, ritual.get("duration_ticks").getAsInt());
            assertTrue(ritual.get("spirit_max_age").getAsInt() > 0);
            checked++;
        }
        assertEquals(5, checked);
        String bowl = read("src/main/java/com/occultism/occultism/tileentity/TileEntitySacrificialBowl.java");
        assertFalse(bowl.contains("applyWorldEffectJob"));
        assertFalse(bowl.contains(".setWorldTime("));
        assertFalse(bowl.contains(".setRaining("));
        assertTrue(bowl.contains("this.spawnFoliotJobEntity(definition.getSpiritJobType(), definition)"));
        assertTrue(bowl.contains("spirit.setWorldEffectCaster(this.findCastingPlayer())"));
    }

    private static WorldInfo worldInfo(long time) {
        WorldInfo info = new WorldInfo(new NBTTagCompound());
        info.setWorldTime(time);
        return info;
    }

    private static String read(String path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
    }
}
