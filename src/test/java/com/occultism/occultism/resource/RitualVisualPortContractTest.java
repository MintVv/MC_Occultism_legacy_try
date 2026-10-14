package com.occultism.occultism.resource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

/** Keeps the 1.12 ritual feedback aligned with the upstream visual contract. */
class RitualVisualPortContractTest {

    @Test
    void runningFeedbackUsesServerPortalParticlesAtTheGoldenBowl() throws Exception {
        String bowl = readSource("src", "main", "java", "com", "occultism", "occultism",
                "tileentity", "TileEntitySacrificialBowl.java");

        assertTrue(bowl.contains("this.world.rand.nextInt(16) != 0"));
        assertTrue(bowl.contains("EnumParticleTypes.PORTAL"));
        assertTrue(bowl.contains("EnumParticleTypes.SMOKE_LARGE"));
        assertTrue(bowl.contains("this.pos.getY() + 0.5D"));
        assertFalse(bowl.contains("spawnRitualMarkerParticles"),
                "ingredient-bowl marker particles are not part of the upstream ritual feedback");
        assertFalse(bowl.contains("EnumParticleTypes.SPELL_MOB"),
                "waiting feedback must not be duplicated by a second vanilla particle stream");
    }

    @Test
    void floatingItemMatchesUpstreamRotationAndCrystalScale() throws Exception {
        String renderer = readSource("src", "main", "java", "com", "occultism", "occultism",
                "client", "render", "TileEntitySacrificialBowlRenderer.java");

        assertTrue(renderer.contains("System.currentTimeMillis() / 16L"));
        assertTrue(renderer.contains("y + 0.5D + bob"));
        assertTrue(renderer.contains("gameTime - te.getLastChangeTime()"));
        assertTrue(renderer.contains("? 1.5F : 0.5F"));
        assertTrue(renderer.contains("instanceof BlockGoldenSacrificialBowl"));
        assertTrue(renderer.contains("te.getWorld().rand.nextInt(16) != 0"));
        assertFalse(renderer.contains("EnumParticleTypes.PORTAL"),
                "running portal particles are server-driven, as in upstream");
    }

    @Test
    void waitingParticleKeepsTheUpstreamPortalTrajectoryAndGrayColor() throws Exception {
        String particle = readSource("src", "main", "java", "com", "occultism", "occultism",
                "client", "render", "ParticleRitualWaiting.java");

        assertTrue(particle.contains("this.particleMaxAge = this.rand.nextInt(10) + 40"));
        assertTrue(particle.contains("this.particleRed = shade * 0.3F"));
        assertTrue(particle.contains("this.posY = this.portalPosY"));
        assertTrue(particle.contains("this.setParticleTextureIndex(this.rand.nextInt(8))"));
    }

    private static String readSource(String... path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path[0],
                java.util.Arrays.copyOfRange(path, 1, path.length))), StandardCharsets.UTF_8);
    }
}
