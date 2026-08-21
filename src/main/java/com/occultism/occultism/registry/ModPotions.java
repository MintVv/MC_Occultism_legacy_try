package com.occultism.occultism.registry;

import com.occultism.occultism.Tags;
import com.occultism.occultism.potion.PotionBatLifesteal;
import com.occultism.occultism.potion.PotionBeaverHarvest;
import com.occultism.occultism.potion.PotionDragonGreed;
import com.occultism.occultism.potion.PotionFairySlowFall;
import com.occultism.occultism.potion.PotionMummyDodge;
import com.occultism.occultism.potion.PotionThirdEye;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class ModPotions {

    public static final Potion THIRD_EYE = new PotionThirdEye()
            .setRegistryName(Tags.MOD_ID, "third_eye");
    public static final Potion BEAVER_HARVEST = new PotionBeaverHarvest()
            .setRegistryName(Tags.MOD_ID, "beaver_harvest");
    public static final Potion MUMMY_DODGE = new PotionMummyDodge()
            .setRegistryName(Tags.MOD_ID, "mummy_dodge");
    public static final Potion BAT_LIFESTEAL = new PotionBatLifesteal()
            .setRegistryName(Tags.MOD_ID, "bat_lifesteal");
    public static final Potion DRAGON_GREED = new PotionDragonGreed()
            .setRegistryName(Tags.MOD_ID, "dragon_greed");
    public static final Potion FAIRY_SLOW_FALL = new PotionFairySlowFall()
            .setRegistryName(Tags.MOD_ID, "fairy_slow_fall");
    public static final PotionType THIRD_EYE_POTION = new PotionType("third_eye",
            new PotionEffect(THIRD_EYE, 60 * 20, 1))
                    .setRegistryName(Tags.MOD_ID, "third_eye_potion");
    public static final PotionType LONG_THIRD_EYE_POTION = new PotionType("third_eye",
            new PotionEffect(THIRD_EYE, 5 * 60 * 20, 1))
                    .setRegistryName(Tags.MOD_ID, "long_third_eye_potion");

    private ModPotions() {
    }

    @SubscribeEvent
    public static void registerPotions(RegistryEvent.Register<Potion> event) {
        event.getRegistry().registerAll(THIRD_EYE, BEAVER_HARVEST, MUMMY_DODGE, BAT_LIFESTEAL, DRAGON_GREED,
                FAIRY_SLOW_FALL);
    }

    @SubscribeEvent
    public static void registerPotionTypes(RegistryEvent.Register<PotionType> event) {
        event.getRegistry().registerAll(THIRD_EYE_POTION, LONG_THIRD_EYE_POTION);
    }
}
