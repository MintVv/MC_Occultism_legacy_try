package com.occultism.occultism.registry;

import com.occultism.occultism.Tags;
import com.occultism.occultism.enchantment.EnchantmentFractureSoul;

import net.minecraft.enchantment.Enchantment;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class ModEnchantments {

    public static final Enchantment FRACTURE_SOUL = new EnchantmentFractureSoul()
            .setRegistryName(Tags.MOD_ID, "fracture_soul");

    private ModEnchantments() {
    }

    @SubscribeEvent
    public static void registerEnchantments(RegistryEvent.Register<Enchantment> event) {
        event.getRegistry().register(FRACTURE_SOUL);
    }
}
