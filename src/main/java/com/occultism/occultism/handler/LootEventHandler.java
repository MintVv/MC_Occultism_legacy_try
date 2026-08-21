package com.occultism.occultism.handler;

import java.util.List;

import com.occultism.occultism.Tags;
import com.occultism.occultism.config.ModConfig;
import com.occultism.occultism.entity.EntityBlacksmithFamiliar;
import com.occultism.occultism.registry.ModPotions;
import com.occultism.occultism.util.FamiliarUtil;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class LootEventHandler {

    private LootEventHandler() {
    }

    @SubscribeEvent
    public static void onExperienceDrop(LivingExperienceDropEvent event) {
        if (event.getDroppedExperience() == 0) {
            return;
        }
        EntityPlayer player = event.getAttackingPlayer();
        if (player == null) {
            return;
        }
        PotionEffect greed = player.getActivePotionEffect(ModPotions.DRAGON_GREED);
        if (greed != null) {
            event.setDroppedExperience(event.getDroppedExperience() + greed.getAmplifier() + 1);
        }
    }

    @SubscribeEvent
    public static void giveStoneToBlacksmith(EntityItemPickupEvent event) {
        EntityPlayer player = event.getEntityPlayer();
        EntityItem entity = event.getItem();
        if (player == null || entity == null || player.world.isRemote) {
            return;
        }

        ItemStack stone = entity.getItem();
        if (!matchesOre(stone, "stone") && !matchesOre(stone, "cobblestone")) {
            return;
        }
        if (!hasNearbyBlacksmith(player)) {
            return;
        }

        if (player.getRNG().nextDouble()
                < ModConfig.SPIRIT_JOBS.blacksmithFamiliarRepairChance * stone.getCount()) {
            repairEquipment(player);
        }

        event.setCanceled(true);
        entity.setDead();
    }

    private static boolean hasNearbyBlacksmith(EntityPlayer player) {
        List<EntityBlacksmithFamiliar> familiars = FamiliarUtil.getAllFamiliars(player,
                EntityBlacksmithFamiliar.class,
                familiar -> familiar != null && familiar.isEntityAlive() && familiar.isTamed()
                        && familiar.isOwner(player));
        return !familiars.isEmpty();
    }

    private static void repairEquipment(EntityPlayer player) {
        for (ItemStack stack : player.getEquipmentAndArmor()) {
            if (!stack.isEmpty() && stack.isItemDamaged()) {
                stack.setItemDamage(Math.max(0, stack.getItemDamage() - 2));
                return;
            }
        }
    }

    private static boolean matchesOre(ItemStack stack, String oreName) {
        if (stack.isEmpty()) {
            return false;
        }
        for (int id : OreDictionary.getOreIDs(stack)) {
            if (oreName.equals(OreDictionary.getOreName(id))) {
                return true;
            }
        }
        return false;
    }
}
