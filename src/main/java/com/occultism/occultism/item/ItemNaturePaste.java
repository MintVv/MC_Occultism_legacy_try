package com.occultism.occultism.item;

import com.occultism.occultism.block.IOtherworldBlock;
import com.occultism.occultism.registry.ModBlocks;
import com.occultism.occultism.registry.ModItems;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCactus;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockNetherWart;
import net.minecraft.block.BlockReed;
import net.minecraft.block.BlockStem;
import net.minecraft.block.IGrowable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemNaturePaste extends Item {

    private static final int DURABILITY = 64;
    private static final int ORE_EXTRACTION_GRAY_PASTE_DAMAGE = 8;
    private static final int ORE_EXTRACTION_COOLDOWN = 50;

    public ItemNaturePaste() {
        this.setMaxDamage(DURABILITY);
        this.setMaxStackSize(1);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand,
            EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        IBlockState state = world.getBlockState(pos);
        boolean applied = false;

        if (world.isRemote) {
            return this.canApplyClient(player, state) ? EnumActionResult.SUCCESS : EnumActionResult.PASS;
        }

        Block block = state.getBlock();
        if (this.extractOre(world, pos, state, player, stack)) {
            return EnumActionResult.SUCCESS;
        } else if (block instanceof BlockCrops) {
            applied = this.growCrop(world, pos, state);
        } else if (block instanceof BlockStem) {
            applied = this.growStem(world, pos, state);
        } else if (block instanceof BlockNetherWart) {
            applied = this.growNetherWart(world, pos, state);
        } else if (block instanceof BlockReed || block instanceof BlockCactus) {
            applied = this.growColumnPlant(world, pos, state);
        } else if (block instanceof IGrowable) {
            applied = this.forceGrow(world, pos, state, (IGrowable) block);
        }

        if (!applied) {
            return EnumActionResult.PASS;
        }

        this.damagePaste(stack, player);
        this.spawnEffects(world, pos);
        return EnumActionResult.SUCCESS;
    }

    @Override
    public boolean hasContainerItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getContainerItem(ItemStack itemStack) {
        ItemStack copy = itemStack.copy();
        copy.setCount(1);
        int nextDamage = copy.getItemDamage() + 1;
        if (nextDamage >= copy.getMaxDamage()) {
            return ItemStack.EMPTY;
        }
        copy.setItemDamage(nextDamage);
        return copy;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        for (String line : I18n.format("item.occultism.nature_paste.auto_tooltip").split("\\\\n|\\n")) {
            if (!line.isEmpty()) {
                tooltip.add(line);
            }
        }
    }

    private boolean canApplyClient(EntityPlayer player, IBlockState state) {
        Block block = state.getBlock();
        return this.canExtractOre(state, player.getHeldItemOffhand())
                || block instanceof BlockCrops
                || block instanceof BlockStem
                || block instanceof BlockNetherWart
                || block instanceof BlockReed
                || block instanceof BlockCactus
                || block instanceof IGrowable;
    }

    private boolean extractOre(World world, BlockPos pos, IBlockState state, EntityPlayer player,
            ItemStack naturePaste) {
        ItemStack grayPaste = player.getHeldItemOffhand();
        if (!this.canExtractOre(state, grayPaste)) {
            return false;
        }

        if (!world.destroyBlock(pos, true)) {
            return false;
        }
        world.setBlockState(pos, state, 4);
        world.playEvent(2001, pos, Block.getStateId(state));
        this.damagePaste(naturePaste, player);
        this.damagePaste(grayPaste, player, ORE_EXTRACTION_GRAY_PASTE_DAMAGE);
        player.getCooldownTracker().setCooldown(this, ORE_EXTRACTION_COOLDOWN);
        this.spawnEffects(world, pos);
        return true;
    }

    private boolean canExtractOre(IBlockState state, ItemStack grayPaste) {
        return !grayPaste.isEmpty()
                && grayPaste.getItem() == ModItems.GRAY_PASTE
                && this.isSupportedOre(state);
    }

    private boolean isSupportedOre(IBlockState state) {
        Block block = state.getBlock();
        if (block == ModBlocks.IESNIUM_ORE_NATURAL) {
            return state.getValue(IOtherworldBlock.UNCOVERED);
        }
        return block == Blocks.COAL_ORE
                || block == Blocks.IRON_ORE
                || block == Blocks.GOLD_ORE
                || block == Blocks.REDSTONE_ORE
                || block == Blocks.LIT_REDSTONE_ORE
                || block == Blocks.LAPIS_ORE
                || block == Blocks.DIAMOND_ORE
                || block == Blocks.EMERALD_ORE
                || block == Blocks.QUARTZ_ORE
                || block == ModBlocks.COPPER_ORE
                || block == ModBlocks.SILVER_ORE
                || block == ModBlocks.IESNIUM_ORE;
    }

    private boolean growCrop(World world, BlockPos pos, IBlockState state) {
        if (!state.getProperties().containsKey(BlockCrops.AGE)) {
            return false;
        }
        int age = state.getValue(BlockCrops.AGE);
        if (age >= 7) {
            return false;
        }
        world.setBlockState(pos, state.withProperty(BlockCrops.AGE, 7), 2);
        return true;
    }

    private boolean growStem(World world, BlockPos pos, IBlockState state) {
        if (!state.getProperties().containsKey(BlockStem.AGE)) {
            return false;
        }
        int age = state.getValue(BlockStem.AGE);
        if (age >= 7) {
            state.getBlock().updateTick(world, pos, state, new Random());
            return true;
        }
        world.setBlockState(pos, state.withProperty(BlockStem.AGE, 7), 2);
        return true;
    }

    private boolean growNetherWart(World world, BlockPos pos, IBlockState state) {
        if (!state.getProperties().containsKey(BlockNetherWart.AGE)) {
            return false;
        }
        int age = state.getValue(BlockNetherWart.AGE);
        if (age >= 3) {
            return false;
        }
        world.setBlockState(pos, state.withProperty(BlockNetherWart.AGE, 3), 2);
        return true;
    }

    private boolean growColumnPlant(World world, BlockPos pos, IBlockState state) {
        Block block = state.getBlock();
        BlockPos top = pos;
        while (world.getBlockState(top.up()).getBlock() == block) {
            top = top.up();
        }

        BlockPos placePos = top.up();
        if (!world.isAirBlock(placePos) || !block.canPlaceBlockAt(world, placePos)) {
            return false;
        }
        world.setBlockState(placePos, state.getBlock().getDefaultState(), 2);
        return true;
    }

    private boolean forceGrow(World world, BlockPos pos, IBlockState state, IGrowable growable) {
        if (!growable.canGrow(world, pos, state, false)) {
            return false;
        }
        growable.grow(world, world.rand, pos, state);
        return true;
    }

    private void damagePaste(ItemStack stack, EntityPlayer player) {
        this.damagePaste(stack, player, 1);
    }

    private void damagePaste(ItemStack stack, EntityPlayer player, int amount) {
        if (!player.capabilities.isCreativeMode && !stack.isEmpty()) {
            stack.damageItem(amount, player);
        }
    }

    private void spawnEffects(World world, BlockPos pos) {
        world.playEvent(2005, pos, 0);
        world.playSound(null, pos, SoundEvents.BLOCK_GRASS_BREAK, SoundCategory.BLOCKS, 0.8F, 1.0F);
        if (world instanceof WorldServer) {
            ((WorldServer) world).spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, pos.getX() + 0.5D,
                    pos.getY() + 0.8D, pos.getZ() + 0.5D, 12, 0.35D, 0.35D, 0.35D, 0.02D);
        }
    }
}
