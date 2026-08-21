package com.occultism.occultism.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.occultism.occultism.entity.EntityBatFamiliar;
import com.occultism.occultism.handler.FamiliarEventHandler;
import com.occultism.occultism.registry.ModPotions;

import net.minecraft.entity.Entity;
import net.minecraft.init.Bootstrap;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.junit.jupiter.api.Test;

class BatUpgradePortContractTest {

    private static final Path ROOT = Paths.get("src", "main");
    private static final Path JAVA_ROOT = ROOT.resolve("java").resolve("com").resolve("occultism")
            .resolve("occultism");
    private static final Path ASSET_ROOT = ROOT.resolve("resources").resolve("assets").resolve("occultism");
    private static final String BAT_TEXTURE_SHA256 =
            "35a7cae4f7be53158eb28323b194bc5da81cd3fca856fe039b537ee41b356386";

    @Test
    void batAcceptsExactlyOneBlacksmithUpgrade() {
        Bootstrap.register();

        EntityBatFamiliar bat = new EntityBatFamiliar(null);
        assertFalse(bat.hasBlacksmithUpgrade());
        assertTrue(bat.canBlacksmithUpgrade());
        assertSame(bat, bat.getFamiliarEntity());
        assertNull(bat.getFamiliarOwner());

        bat.blacksmithUpgrade();
        assertTrue(bat.hasBlacksmithUpgrade());
        assertFalse(bat.canBlacksmithUpgrade());

        bat.blacksmithUpgrade();
        assertTrue(bat.hasBlacksmithUpgrade());
    }

    @Test
    void batStateAndOwnerEffectsMatchThe1814Contract() throws IOException {
        String entity = readUtf8(JAVA_ROOT.resolve("entity").resolve("EntityBatFamiliar.java"));
        assertTrue(entity.contains("this.setBlacksmithUpgrade(compound.getBoolean(\"hasBlacksmithUpgrade\"));"));
        assertTrue(entity.contains("compound.setBoolean(\"hasBlacksmithUpgrade\", this.hasBlacksmithUpgrade());"));

        String livingUpdate = section(entity, "public void onLivingUpdate() {", "public boolean processInteract");
        assertTrue(livingUpdate.indexOf("this.updateOwnerEffect();")
                        < livingUpdate.indexOf("if (this.isSitting())"),
                "Sitting must stop movement without disabling the upstream owner effects");

        String ownerEffect = section(entity, "private void updateOwnerEffect() {", "private void updateFlight() {");
        assertTrue(ownerEffect.contains("new PotionEffect(MobEffects.NIGHT_VISION, 300, 1, false, false)"),
                "The 1.81.4 Bat grants hidden Night Vision II for 300 ticks");
        assertTrue(ownerEffect.contains("if (this.hasBlacksmithUpgrade())"));
        assertTrue(ownerEffect.contains("new PotionEffect(ModPotions.BAT_LIFESTEAL, 300, 0, false, false)"),
                "Only an upgraded Bat may grant the hidden lifesteal effect");
    }

    @Test
    void lifestealPotionUsesTheUpstreamRegistryContract() throws IOException {
        Bootstrap.register();

        assertEquals(new ResourceLocation("occultism", "bat_lifesteal"),
                ModPotions.BAT_LIFESTEAL.getRegistryName());
        assertEquals("effect.occultism.bat_lifesteal", ModPotions.BAT_LIFESTEAL.getName());
        assertEquals(0x960201, ModPotions.BAT_LIFESTEAL.getLiquidColor());
        assertTrue(ModPotions.BAT_LIFESTEAL.isBeneficial());

        String registry = readUtf8(JAVA_ROOT.resolve("registry").resolve("ModPotions.java"));
        assertTrue(registry.contains("MUMMY_DODGE, BAT_LIFESTEAL"),
                "The new effect must be appended after existing potion registrations");

        String english = readUtf8(ASSET_ROOT.resolve("lang").resolve("en_us.lang"));
        String chinese = readUtf8(ASSET_ROOT.resolve("lang").resolve("zh_cn.lang"));
        assertTrue(english.contains("effect.occultism.bat_lifesteal=Lifesteal"));
        assertTrue(chinese.contains("effect.occultism.bat_lifesteal=生命窃取"));
        assertTrue(english.contains("A Blacksmith can upgrade it once to also grant Lifesteal"));
        assertTrue(chinese.contains("铁匠使魔可将它升级一次并额外给予生命窃取"));
    }

    @Test
    void lifestealHealsOnePlusAmplifierAndIgnoresAttackersWithoutTheEffect() throws Exception {
        Bootstrap.register();

        Method lifesteal = FamiliarEventHandler.class.getDeclaredMethod("applyBatLifesteal", Entity.class);
        lifesteal.setAccessible(true);
        TestBatFamiliar attacker = new TestBatFamiliar();
        attacker.setHealth(5.0F);

        lifesteal.invoke(null, attacker);
        assertEquals(5.0F, attacker.getHealth(), 0.0001F);

        attacker.addPotionEffect(new PotionEffect(ModPotions.BAT_LIFESTEAL, 300, 2, false, false));
        lifesteal.invoke(null, attacker);
        assertEquals(8.0F, attacker.getHealth(), 0.0001F,
                "Amplifier two must heal 1 + 2 health points on a kill");

        String handler = readUtf8(JAVA_ROOT.resolve("handler").resolve("FamiliarEventHandler.java"));
        String death = section(handler, "public static void onLivingDeath(LivingDeathEvent event) {",
                "private static void applyBatLifesteal");
        assertTrue(death.contains("source.getTrueSource()"),
                "Projectile kills must resolve the living attacker through the 1.12 true source");
        assertTrue(death.contains("applyBatLifesteal(sourceEntity);"));
    }

    @Test
    void upgradedBatShowsTheUpstreamGoblet() throws IOException, NoSuchAlgorithmException {
        String model = readUtf8(JAVA_ROOT.resolve("client").resolve("model").resolve("ModelBatFamiliar.java"));
        assertTrue(model.contains("this.goblet1 = new ModelRenderer(this, 10, 30);"));
        assertTrue(model.contains("this.goblet1.setRotationPoint(-3.5F, 3.0F, 0.0F);"));
        assertTrue(model.contains("goblet3.setRotationPoint(5.01F, 0.0F, 0.0F);"));
        assertEquals(4, countOccurrences(model, "addGobletCorner(goblet3,"));
        assertTrue(model.contains("this.goblet1.showModel = bat.hasBlacksmithUpgrade() && !bat.isSitting();"));
        assertEquals(BAT_TEXTURE_SHA256,
                sha256(ASSET_ROOT.resolve("textures").resolve("entity").resolve("bat_familiar.png")));
    }

    private static String section(String source, String start, String end) {
        int startIndex = source.indexOf(start);
        assertTrue(startIndex >= 0, "Missing source marker: " + start);
        int endIndex = source.indexOf(end, startIndex + start.length());
        assertTrue(endIndex >= 0, "Missing source marker after " + start + ": " + end);
        return source.substring(startIndex, endIndex);
    }

    private static int countOccurrences(String source, String token) {
        int count = 0;
        int index = 0;
        while ((index = source.indexOf(token, index)) >= 0) {
            count++;
            index += token.length();
        }
        return count;
    }

    private static String sha256(Path path) throws IOException, NoSuchAlgorithmException {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path));
        StringBuilder hex = new StringBuilder(digest.length * 2);
        for (byte value : digest) {
            hex.append(String.format("%02x", value & 0xff));
        }
        return hex.toString();
    }

    private static String readUtf8(Path path) throws IOException {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }

    private static final class TestBatFamiliar extends EntityBatFamiliar {

        private TestBatFamiliar() {
            super((World) null);
        }

        @Override
        protected void onNewPotionEffect(PotionEffect effect) {
        }
    }
}
