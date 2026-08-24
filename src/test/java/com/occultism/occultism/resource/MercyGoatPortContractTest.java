package com.occultism.occultism.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.occultism.occultism.entity.EntityMercyGoat;

import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.init.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class MercyGoatPortContractTest {

    private static final Path ROOT = Paths.get("src", "main");
    private static final Path JAVA_ROOT = ROOT.resolve("java").resolve("com").resolve("occultism")
            .resolve("occultism");
    private static final Path ASSET_ROOT = ROOT.resolve("resources").resolve("assets").resolve("occultism");

    @BeforeAll
    static void bootstrapMinecraft() {
        Bootstrap.register();
    }

    @Test
    void entityKeepsTheSheepCompatibilityBaseAndRamProfile() {
        EntityMercyGoat goat = new EntityMercyGoat(null);
        assertTrue(goat instanceof EntitySheep);
        assertEquals(60, EntityMercyGoat.RAM_COOLDOWN_TICKS);
        assertEquals(2.25D, EntityMercyGoat.RAM_RANGE_SQ, 0.000001D);
        assertEquals(4.0F, EntityMercyGoat.RAM_DAMAGE, 0.000001F);
        assertEquals(0.7F, EntityMercyGoat.RAM_KNOCKBACK, 0.000001F);
        assertTrue(goat.isNoDespawnRequired());
    }

    @Test
    void ramUsesServerSidePlayerTargetingDamageKnockbackAndCooldown() throws IOException {
        String source = readUtf8(JAVA_ROOT.resolve("entity").resolve("EntityMercyGoat.java"));
        assertTrue(source.contains("getNearestPlayerNotCreative(this, 12.0D)"));
        assertTrue(source.contains("tryMoveToEntityLiving(target, 1.35D)"));
        assertTrue(source.contains("DamageSource.causeMobDamage(this), RAM_DAMAGE"));
        assertTrue(source.contains("target.knockBack(this, RAM_KNOCKBACK"));
        assertTrue(source.contains("this.ramCooldown = RAM_COOLDOWN_TICKS"));
        assertTrue(source.contains("this.world.isRemote"));
    }

    @Test
    void ritualAndLanguagesExposeTheCompatibilityBehavior() throws IOException {
        String rituals = readUtf8(ASSET_ROOT.resolve("rituals").resolve("rituals.json"));
        assertTrue(rituals.contains("\"id\": \"possess_goat\""));
        assertTrue(rituals.contains("\"entity_to_summon\": \"occultism:mercy_goat\""));
        String english = readUtf8(ASSET_ROOT.resolve("lang").resolve("en_us.lang"));
        String chinese = readUtf8(ASSET_ROOT.resolve("lang").resolve("zh_cn.lang"));
        assertTrue(english.contains("charges nearby non-creative players"));
        assertTrue(chinese.contains("冲撞附近的非创造玩家"));
        assertTrue(english.contains("Modern goat horns"));
        assertTrue(chinese.contains("现代山羊角"));
    }

    private static String readUtf8(Path path) throws IOException {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}
