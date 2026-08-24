package com.occultism.occultism.util;

import com.occultism.occultism.capability.DoubleJumpCapability;
import com.occultism.occultism.registry.ModCapabilities;
import com.occultism.occultism.registry.ModPotions;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

/** Server-authoritative movement helpers shared by the client prediction path. */
public final class MovementUtil {

    private MovementUtil() {
    }

    public static boolean doubleJump(EntityPlayer player) {
        if (!allowDoubleJump(player) || ModCapabilities.DOUBLE_JUMP == null) {
            return false;
        }
        DoubleJumpCapability capability = player.getCapability(ModCapabilities.DOUBLE_JUMP, null);
        if (capability == null || capability.getJumps() >= getMaxJumps(player)) {
            return false;
        }
        player.jump();
        player.velocityChanged = true;
        capability.addJump();
        return true;
    }

    public static boolean allowDoubleJump(EntityPlayer player) {
        if (player == null || !player.isEntityAlive() || player.onGround || player.isRiding()
                || player.capabilities.isFlying || player.isInWater() || player.isInLava()
                || player.isElytraFlying()) {
            return false;
        }
        return ModPotions.DOUBLE_JUMP != null && player.isPotionActive(ModPotions.DOUBLE_JUMP);
    }

    public static int getMaxJumps(EntityPlayer player) {
        if (player == null || ModPotions.DOUBLE_JUMP == null) {
            return 0;
        }
        net.minecraft.potion.PotionEffect effect = player.getActivePotionEffect(ModPotions.DOUBLE_JUMP);
        return effect == null ? 0 : Math.max(0, 1 + effect.getAmplifier());
    }

    public static void syncJumps(EntityPlayer player) {
        if (player instanceof EntityPlayerMP) {
            com.occultism.occultism.network.ModNetworking.syncDoubleJump((EntityPlayerMP) player);
        }
    }
}
