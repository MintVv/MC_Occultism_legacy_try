package com.occultism.occultism.block;

import com.occultism.occultism.item.ItemIesniumPickaxe;
import com.occultism.occultism.item.ItemInfusedPickaxe;
import com.occultism.occultism.registry.ModItems;
import com.occultism.occultism.registry.ModPotions;

import net.minecraft.block.Block;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import net.minecraft.util.math.BlockPos;

public interface IOtherworldBlock {

    PropertyBool UNCOVERED = PropertyBool.create("uncovered");

    Block getUncoveredBlock();

    Block getCoveredBlock();

    OtherworldBlockTier getTier();

    default IBlockState getHarvestState(IBlockState state, EntityPlayer player, ItemStack tool) {
        return this.getPlayerHarvestTier(player, tool).canReveal(this.getTier())
                ? state.withProperty(UNCOVERED, Boolean.TRUE) : state.withProperty(UNCOVERED, Boolean.FALSE);
    }

    default OtherworldBlockTier getPlayerHarvestTier(EntityPlayer player, ItemStack tool) {
        OtherworldBlockTier tier = getToolTier(tool);
        if (player != null && (isTrueSightStaff(player.getHeldItemMainhand())
                || isTrueSightStaff(player.getHeldItemOffhand()))) {
            tier = OtherworldBlockTier.max(tier, OtherworldBlockTier.TWO);
        }
        if (player != null && player.isPotionActive(ModPotions.THIRD_EYE)) {
            tier = OtherworldBlockTier.max(tier, OtherworldBlockTier.ONE);
        }
        return tier;
    }

    default ItemStack getPickBlockStack(IBlockAccess world, BlockPos pos, IBlockState state) {
        return new ItemStack(state.getValue(UNCOVERED) ? this.getUncoveredBlock() : this.getCoveredBlock());
    }

    static OtherworldBlockTier getToolTier(ItemStack tool) {
        if (tool == null || tool.isEmpty()) {
            return OtherworldBlockTier.NONE;
        }
        Item item = tool.getItem();
        if (item instanceof ItemInfusedPickaxe || item instanceof ItemIesniumPickaxe || item == ModItems.TRUE_SIGHT_STAFF) {
            return OtherworldBlockTier.TWO;
        }
        return OtherworldBlockTier.NONE;
    }

    static boolean isTrueSightStaff(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.getItem() == ModItems.TRUE_SIGHT_STAFF;
    }
}
