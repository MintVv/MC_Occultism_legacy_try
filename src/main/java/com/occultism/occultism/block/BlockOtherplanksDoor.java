package com.occultism.occultism.block;

import net.minecraft.block.BlockDoor;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.world.World;

import com.occultism.occultism.registry.ModBlocks;

public class BlockOtherplanksDoor extends BlockDoor {

    public BlockOtherplanksDoor() {
        super(Material.WOOD);
        this.setSoundType(SoundType.WOOD);
    }

    @Override
    public Item getItemDropped(IBlockState state, java.util.Random rand, int fortune) {
        return state.getValue(HALF) == EnumDoorHalf.UPPER ? null
                : Item.getItemFromBlock(ModBlocks.OTHERPLANKS_DOOR);
    }

    @Override
    public net.minecraft.item.ItemStack getItem(World worldIn, net.minecraft.util.math.BlockPos pos, IBlockState state) {
        return new net.minecraft.item.ItemStack(Item.getItemFromBlock(ModBlocks.OTHERPLANKS_DOOR));
    }
}
