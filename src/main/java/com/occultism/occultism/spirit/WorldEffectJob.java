package com.occultism.occultism.spirit;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.storage.WorldInfo;

/**
 * 1.12 state for the pinned upstream ChangeWeatherJob/ChangeTimeJob.
 * Rendering and entity removal stay in the worker; saved progress survives transport.
 */
public final class WorldEffectJob {
    private final SpiritJobType type;
    private int currentChangeTicks;
    private int requiredChangeTicks;
    private long newTime;
    private boolean initialized;
    private boolean completed;

    public WorldEffectJob(SpiritJobType type) {
        if (type == null || !type.isWorldEffectJob()) {
            throw new IllegalArgumentException("Not a weather/time job: " + type);
        }
        this.type = type;
        switch (type) {
            case CLEAR_WEATHER: this.requiredChangeTicks = 300; break;
            case RAIN_WEATHER: this.requiredChangeTicks = 600; break;
            case THUNDER_WEATHER: this.requiredChangeTicks = 1200; break;
            default: this.requiredChangeTicks = 100;
        }
    }

    public boolean isTimeJob() {
        return this.type == SpiritJobType.DAY_TIME || this.type == SpiritJobType.NIGHT_TIME;
    }

    public int getCurrentChangeTicks() {
        return this.currentChangeTicks;
    }

    public int getRequiredChangeTicks() {
        return this.requiredChangeTicks;
    }

    public long getNewTime() {
        return this.newTime;
    }

    public boolean isCompleted() {
        return this.completed;
    }

    /** Returns true once the spirit should disappear; completing twice never reapplies the effect. */
    public boolean tick(WorldInfo info, boolean enabled) {
        if (this.completed) {
            return true;
        }
        if (!this.initialized) {
            this.initialized = true;
            if (this.isTimeJob()) {
                this.newTime = nextTime(info.getWorldTime(), this.type == SpiritJobType.DAY_TIME);
                if (!enabled) {
                    this.completed = true;
                    return true;
                }
            }
        }
        this.currentChangeTicks++;
        if (this.isTimeJob()) {
            if (enabled) {
                long current = info.getWorldTime();
                long remaining = this.newTime - current;
                int ticks = Math.max(this.requiredChangeTicks - this.currentChangeTicks, 1);
                // Match upstream interpolation, including convergence when normal daylight also advances.
                long interpolated = current + remaining / ticks;
                info.setWorldTime(Math.min(interpolated, this.newTime));
            }
        } else if (enabled && this.currentChangeTicks >= this.requiredChangeTicks) {
            applyWeather(info);
        }
        this.completed = this.currentChangeTicks >= this.requiredChangeTicks;
        return this.completed;
    }

    private void applyWeather(WorldInfo info) {
        boolean clear = this.type == SpiritJobType.CLEAR_WEATHER;
        info.setCleanWeatherTime(clear ? 6000 : 0);
        info.setRainTime(clear ? 0 : 6000);
        info.setThunderTime(clear ? 0 : 6000);
        info.setRaining(!clear);
        info.setThundering(this.type == SpiritJobType.THUNDER_WEATHER);
    }

    public static long nextTime(long current, boolean dawn) {
        long offset = dawn ? 24000L : 11000L;
        long base = current > Long.MAX_VALUE - offset ? Long.MAX_VALUE : current + offset;
        long target = Math.floorDiv(base, 24000L) * 24000L;
        if (!dawn) {
            target = target > Long.MAX_VALUE - 13000L ? Long.MAX_VALUE : target + 13000L;
        }
        return target;
    }

    public NBTTagCompound writeToNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("factoryId", this.type.getId().toString());
        tag.setInteger("currentChangeTicks", this.currentChangeTicks);
        tag.setInteger("requiredChangeTicks", this.requiredChangeTicks);
        if (this.isTimeJob() && this.initialized) {
            tag.setLong("newTime", this.newTime);
        }
        tag.setBoolean("completed", this.completed);
        return tag;
    }

    public void readFromNBT(NBTTagCompound tag) {
        if (tag.hasKey("requiredChangeTicks", 3)) {
            this.requiredChangeTicks = Math.max(1, tag.getInteger("requiredChangeTicks"));
        }
        this.completed = tag.getBoolean("completed");
        this.currentChangeTicks = Math.max(0,
                Math.min(tag.getInteger("currentChangeTicks"),
                        this.completed ? this.requiredChangeTicks : this.requiredChangeTicks - 1));
        this.initialized = this.isTimeJob() && tag.hasKey("newTime", 4);
        this.newTime = this.initialized ? tag.getLong("newTime") : 0L;
    }
}
