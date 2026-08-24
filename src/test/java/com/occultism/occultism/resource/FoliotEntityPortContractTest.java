package com.occultism.occultism.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.occultism.occultism.entity.EntityFoliot;
import com.occultism.occultism.spirit.SpiritTier;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Bootstrap;

/** Contract coverage for the upstream occultism:foliot entity id. */
class FoliotEntityPortContractTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        Bootstrap.register();
    }

    @Test
    void standaloneFoliotKeepsUpstreamTierAttributesAndLifetime() {
        EntityFoliot foliot = new EntityFoliot(null);

        assertEquals(SpiritTier.FOLIOT, foliot.getTier());
        assertEquals(0.6F, foliot.width, 0.000001F);
        assertEquals(0.9F, foliot.height, 0.000001F);
        assertEquals(8.0D, foliot.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getBaseValue());
        assertEquals(0.30D, foliot.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getBaseValue());
        assertEquals(1.0D, foliot.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getBaseValue());
        assertEquals(2.0D, foliot.getEntityAttribute(SharedMonsterAttributes.ARMOR).getBaseValue());
        assertEquals(1.0D, foliot.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getBaseValue());
        assertEquals(50.0D, foliot.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).getBaseValue());
        assertEquals(60 * 60 * 24, foliot.getSpiritMaxAge());

        foliot.setTier(SpiritTier.MARID);
        assertEquals(SpiritTier.FOLIOT, foliot.getTier());
    }

    @Test
    void ritualResourceIdHasARegisteredEntityAndRenderer() throws Exception {
        String rituals = readSource("src", "main", "resources", "assets", "occultism", "rituals",
                "rituals.json");
        String entities = readSource("src", "main", "java", "com", "occultism", "occultism", "registry",
                "ModEntities.java");
        String renderers = readSource("src", "main", "java", "com", "occultism", "occultism", "client",
                "render", "ModEntityRenderers.java");

        assertTrue(rituals.contains("\"entity_to_summon\": \"occultism:foliot\""));
        assertTrue(entities.contains("new ResourceLocation(Tags.MOD_ID, \"foliot\")"));
        assertTrue(entities.contains("EntityFoliot.class"));
        assertTrue(renderers.contains("registerEntityRenderingHandler(EntityFoliot.class"));
    }

    private static String readSource(String... path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path[0], java.util.Arrays.copyOfRange(path, 1, path.length))),
                StandardCharsets.UTF_8);
    }
}
