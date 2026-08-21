package com.occultism.occultism.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import com.occultism.occultism.entity.EntityBatFamiliar;
import com.occultism.occultism.entity.EntityBeaverFamiliar;
import com.occultism.occultism.entity.EntityCthulhuFamiliar;
import com.occultism.occultism.entity.EntityDeerFamiliar;
import com.occultism.occultism.entity.EntityDevilFamiliar;
import com.occultism.occultism.entity.EntityDragonFamiliar;
import com.occultism.occultism.entity.EntityFamiliarBase;
import com.occultism.occultism.entity.EntityMummyFamiliar;
import com.occultism.occultism.entity.EntityOtherworldBird;
import com.occultism.occultism.entity.IFamiliar;
import com.occultism.occultism.capability.FamiliarSettings;
import com.occultism.occultism.util.FamiliarUtil;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class FamiliarEffectsPortContractTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        Bootstrap.register();
    }

    @Test
    void interfaceExposesUpstreamEffectAndRingHooksWithoutForcingInventoryRingsActive() {
        assertTrue(IFamiliar.class.isAssignableFrom(EntityFamiliarBase.class));
        assertTrue(new EntityBatFamiliar(null).getFamiliarEffects().iterator().hasNext());
        new EntityBatFamiliar(null).curioTick(null);
        assertFalse(FamiliarUtil.isFamiliarEnabled(null, EntityBatFamiliar.class));
        assertTrue(FamiliarUtil.getAllNearbyFamiliars(null, EntityBatFamiliar.class, bat -> true).isEmpty());
        assertTrue(FamiliarUtil.getAllEquippedFamiliars(null, EntityBatFamiliar.class, bat -> true).isEmpty());
    }

    @Test
    void familiarEffectsMatchTheFixedUpstreamPotionContract() {
        assertEffect(new EntityBatFamiliar(null), MobEffects.NIGHT_VISION, 300, 1);
        assertEffect(new EntityBeaverFamiliar(null), com.occultism.occultism.registry.ModPotions.BEAVER_HARVEST,
                300, 0);
        assertEffect(new EntityCthulhuFamiliar(null), MobEffects.WATER_BREATHING, 300, 0);
        assertEffect(new EntityDevilFamiliar(null), MobEffects.FIRE_RESISTANCE, 300, 0);
        assertEffect(new EntityDragonFamiliar(null), com.occultism.occultism.registry.ModPotions.DRAGON_GREED,
                300, 0);
        assertEffect(new EntityMummyFamiliar(null), com.occultism.occultism.registry.ModPotions.MUMMY_DODGE,
                300, 0);

        EntityDeerFamiliar deer = new EntityDeerFamiliar(null);
        List<PotionEffect> deerEffects = effects(deer);
        assertEquals(2, deerEffects.size());
        assertContains(deerEffects, MobEffects.JUMP_BOOST, 300, 0);
        assertContains(deerEffects, MobEffects.SPEED, 300, 0);
    }

    @Test
    void familiarUtilKeepsNearbyQueriesOwnerScopedAndExplicitlyLeavesEquippedQueriesEmpty() throws Exception {
        String source = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(
                "src", "main", "java", "com", "occultism", "occultism", "util", "FamiliarUtil.java")),
                java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(source.contains("getAllNearbyFamiliars"));
        assertTrue(source.contains("familiar.getFamiliarOwner() == owner"));
        assertTrue(source.contains("getAllEquippedFamiliars"));
        assertTrue(source.contains("return Collections.emptyList();"));
        assertTrue(source.contains("getOwnerEnemies"));
    }

    @Test
    void familiarSettingsDefaultToEnabledAndRoundTripKnownTypes() {
        FamiliarSettings settings = new FamiliarSettings();
        assertEquals(17, settings.getValues().size());
        for (Class<?> familiarClass : FamiliarSettings.getFamiliarClasses()) {
            assertTrue(settings.isFamiliarEnabled(familiarClass), familiarClass.getSimpleName());
        }

        settings.setFamiliarEnabled(EntityBatFamiliar.class, false);
        assertFalse(settings.isFamiliarEnabled(EntityBatFamiliar.class));
        settings.setFamiliarEnabled(EntityOtherworldBird.class, false);
        assertFalse(settings.isFamiliarEnabled(EntityOtherworldBird.class));

        FamiliarSettings restored = new FamiliarSettings();
        restored.deserializeNBT(settings.serializeNBT());
        assertFalse(restored.isFamiliarEnabled(EntityBatFamiliar.class));
        assertFalse(restored.isFamiliarEnabled(EntityOtherworldBird.class));
        assertTrue(restored.isFamiliarEnabled(EntityDragonFamiliar.class));

        FamiliarSettings copied = new FamiliarSettings();
        copied.copyFrom(settings);
        assertFalse(copied.isFamiliarEnabled(EntityBatFamiliar.class));
        assertFalse(copied.isFamiliarEnabled(EntityOtherworldBird.class));
    }

    @Test
    void settingsCapabilityAndNetworkUseFixedServerOwnedState() throws Exception {
        String capability = readSource("src", "main", "java", "com", "occultism", "occultism", "registry",
                "ModCapabilities.java");
        String handler = readSource("src", "main", "java", "com", "occultism", "occultism", "handler",
                "FamiliarSettingsHandler.java");
        String networking = readSource("src", "main", "java", "com", "occultism", "occultism", "network",
                "MessageToggleFamiliarSettings.java");
        assertTrue(capability.contains("CapabilityInject(FamiliarSettings.class)"));
        assertTrue(handler.contains("AttachCapabilitiesEvent<Entity>"));
        assertTrue(handler.contains("event.isWasDeath()"));
        assertTrue(handler.contains("onLogin"));
        assertTrue(handler.contains("onChangedDimension"));
        assertTrue(networking.contains("FamiliarSettings.getFamiliarClasses()"));
        assertTrue(networking.contains("settings.toggleFamiliar"));
        assertTrue(networking.contains("ModNetworking.syncFamiliarSettings(player)"));

        String english = readSource("src", "main", "resources", "assets", "occultism", "lang", "en_us.lang");
        String chinese = readSource("src", "main", "resources", "assets", "occultism", "lang", "zh_cn.lang");
        assertTrue(english.contains("message.occultism.familiar.bat_familiar.enabled="));
        assertTrue(english.contains("message.occultism.familiar.otherworld_bird.enabled="));
        assertTrue(english.contains("message.occultism.familiar.shub_niggurath_familiar.disabled="));
        assertTrue(chinese.contains("message.occultism.familiar.bat_familiar.enabled="));
        assertTrue(chinese.contains("message.occultism.familiar.otherworld_bird.enabled="));
        assertTrue(chinese.contains("message.occultism.familiar.shub_niggurath_familiar.disabled="));
    }

    @Test
    void familiarSettingsHaveOneClientKeyHandlerAndUniqueBilingualKeyLabels() throws Exception {
        String client = readSource("src", "main", "java", "com", "occultism", "occultism", "client",
                "ClientFamiliarSettingsHandler.java");
        String occultism = readSource("src", "main", "java", "com", "occultism", "occultism", "Occultism.java");
        assertTrue(client.contains("ClientRegistry.registerKeyBinding"));
        assertTrue(client.contains("Keyboard.KEY_NONE"));
        assertTrue(client.contains("InputEvent.KeyInputEvent"));
        assertTrue(client.contains("InputEvent.MouseInputEvent"));
        assertTrue(client.contains("minecraft.currentScreen != null"));
        assertTrue(client.contains("new MessageToggleFamiliarSettings(pressed)"));
        assertEquals(1, count(occultism, "ClientFamiliarSettingsHandler.register()"));

        String english = readSource("src", "main", "resources", "assets", "occultism", "lang", "en_us.lang");
        String chinese = readSource("src", "main", "resources", "assets", "occultism", "lang", "zh_cn.lang");
        assertEquals(18, keyLineCount(english, "key.occultism."));
        assertEquals(18, uniqueKeyCount(english, "key.occultism."));
        assertEquals(18, keyLineCount(chinese, "key.occultism."));
        assertEquals(18, uniqueKeyCount(chinese, "key.occultism."));
    }

    private static void assertEffect(IFamiliar familiar, Potion potion, int duration, int amplifier) {
        assertContains(effects(familiar), potion, duration, amplifier);
    }

    private static List<PotionEffect> effects(IFamiliar familiar) {
        List<PotionEffect> effects = new ArrayList<>();
        for (PotionEffect effect : familiar.getFamiliarEffects()) {
            effects.add(effect);
        }
        return effects;
    }

    private static void assertContains(List<PotionEffect> effects, Potion potion, int duration, int amplifier) {
        assertTrue(effects.stream().anyMatch(effect -> effect.getPotion() == potion
                && effect.getDuration() == duration && effect.getAmplifier() == amplifier),
                "Missing effect " + potion.getRegistryName());
    }

    private static String readSource(String... path) throws Exception {
        return new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(path[0],
                java.util.Arrays.copyOfRange(path, 1, path.length))),
                java.nio.charset.StandardCharsets.UTF_8);
    }

    private static int count(String source, String needle) {
        int count = 0;
        int index = 0;
        while ((index = source.indexOf(needle, index)) >= 0) {
            count++;
            index += needle.length();
        }
        return count;
    }

    private static int uniqueKeyCount(String source, String prefix) {
        java.util.Set<String> keys = new java.util.HashSet<>();
        for (String line : source.split("\\R")) {
            if (!line.startsWith(prefix)) {
                continue;
            }
            int separator = line.indexOf('=');
            if (separator > 0) {
                keys.add(line.substring(0, separator));
            }
        }
        return keys.size();
    }

    private static int keyLineCount(String source, String prefix) {
        int count = 0;
        for (String line : source.split("\\R")) {
            if (line.startsWith(prefix) && line.indexOf('=') > 0) {
                count++;
            }
        }
        return count;
    }
}
