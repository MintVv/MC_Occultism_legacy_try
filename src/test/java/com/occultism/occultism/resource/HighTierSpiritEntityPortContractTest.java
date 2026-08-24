package com.occultism.occultism.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;

import com.occultism.occultism.entity.EntityAfrit;
import com.occultism.occultism.entity.EntityBoundSpirit;
import com.occultism.occultism.entity.EntityDjinni;
import com.occultism.occultism.entity.EntityMarid;
import com.occultism.occultism.spirit.SpiritTier;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Bootstrap;

/** Contract coverage for the dedicated 1.12 high-tier bound spirit entities. */
class HighTierSpiritEntityPortContractTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        Bootstrap.register();
    }

    @Test
    void factoryUsesDedicatedClassesAndKeepsLegacyFallback() {
        EntityBoundSpirit djinni = EntityBoundSpirit.createForTier(null, SpiritTier.DJINNI);
        EntityBoundSpirit afrit = EntityBoundSpirit.createForTier(null, SpiritTier.AFRIT);
        EntityBoundSpirit marid = EntityBoundSpirit.createForTier(null, SpiritTier.MARID);
        EntityBoundSpirit ancient = EntityBoundSpirit.createForTier(null, SpiritTier.ANCIENT);

        assertTrue(djinni instanceof EntityDjinni);
        assertTrue(afrit instanceof EntityAfrit);
        assertTrue(marid instanceof EntityMarid);
        assertTrue(ancient.getClass() == EntityBoundSpirit.class);
        assertNotSame(djinni.getClass(), afrit.getClass());
        assertNotSame(afrit.getClass(), marid.getClass());
        assertEquals(SpiritTier.DJINNI, djinni.getTier());
        assertEquals(SpiritTier.AFRIT, afrit.getTier());
        assertEquals(SpiritTier.MARID, marid.getTier());
    }

    @Test
    void highTierAttributesMatchTheUpstreamSpiritTiers() {
        EntityBoundSpirit djinni = EntityBoundSpirit.createForTier(null, SpiritTier.DJINNI);
        EntityBoundSpirit afrit = EntityBoundSpirit.createForTier(null, SpiritTier.AFRIT);
        EntityBoundSpirit marid = EntityBoundSpirit.createForTier(null, SpiritTier.MARID);

        assertAttributes(djinni, 100.0D, 0.30D, 3.0D, 4.0D, 5.0D, 50.0D);
        assertAttributes(afrit, 200.0D, 0.40D, 8.0D, 8.0D, 50.0D, 50.0D);
        assertAttributes(marid, 300.0D, 0.40D, 16.0D, 16.0D, 100.0D, 50.0D);

        djinni.setTier(SpiritTier.MARID);
        afrit.setTier(SpiritTier.DJINNI);
        marid.setTier(SpiritTier.AFRIT);
        assertEquals(SpiritTier.DJINNI, djinni.getTier());
        assertEquals(SpiritTier.AFRIT, afrit.getTier());
        assertEquals(SpiritTier.MARID, marid.getTier());
    }

    @Test
    void ritualAndDebugPathsUseTheTierFactoryAndAppendRegistrations() throws Exception {
        String bowl = readSource("src", "main", "java", "com", "occultism", "occultism", "tileentity",
                "TileEntitySacrificialBowl.java");
        String debug = readSource("src", "main", "java", "com", "occultism", "occultism", "item",
                "ItemDebugSummonSpirit.java");
        String entities = readSource("src", "main", "java", "com", "occultism", "occultism", "registry",
                "ModEntities.java");

        assertTrue(bowl.contains("EntityBoundSpirit.createForTier"));
        assertTrue(debug.contains("EntityBoundSpirit.createForTier"));
        assertTrue(entities.contains("\"djinni\""));
        assertTrue(entities.contains("\"afrit\""));
        assertTrue(entities.contains("\"marid\""));
        assertTrue(entities.contains("Append specialized high-tier spirits"));
    }

    private static void assertAttributes(EntityBoundSpirit spirit, double health, double speed, double attack,
            double armor, double toughness, double followRange) {
        assertEquals(health, spirit.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getBaseValue());
        assertEquals(speed, spirit.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getBaseValue());
        assertEquals(attack, spirit.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getBaseValue());
        assertEquals(armor, spirit.getEntityAttribute(SharedMonsterAttributes.ARMOR).getBaseValue());
        assertEquals(toughness, spirit.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getBaseValue());
        assertEquals(followRange, spirit.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).getBaseValue());
    }

    private static String readSource(String... path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path[0], java.util.Arrays.copyOfRange(path, 1, path.length))),
                StandardCharsets.UTF_8);
    }
}
