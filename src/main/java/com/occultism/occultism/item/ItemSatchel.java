package com.occultism.occultism.item;

import java.util.List;

import javax.annotation.Nullable;

import com.occultism.occultism.Occultism;
import com.occultism.occultism.block.BlockChalkGlyph;
import com.occultism.occultism.container.SatchelInventory;
import com.occultism.occultism.gui.ModGuiHandler;
import com.occultism.occultism.registry.ModItems;
import com.occultism.occultism.ritual.PentacleDetector;
import com.occultism.occultism.ritual.PentacleDetector.PentacleSymbol;
import com.occultism.occultism.ritual.PentacleMatch;
import com.occultism.occultism.util.ItemNBTUtil;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemSatchel extends Item {

    public static boolean opensSatchelInventory(ItemStack stack) {
        return !stack.isEmpty()
                && (stack.getItem() == ModItems.SATCHEL
                || stack.getItem() == ModItems.RITUAL_SATCHEL_T1
                || stack.getItem() == ModItems.RITUAL_SATCHEL_T2);
    }

    public static boolean isSatchelLike(ItemStack stack) {
        return opensSatchelInventory(stack) || !stack.isEmpty() && stack.getItem() == ModItems.ENDER_SATCHEL;
    }

    public static boolean tryCleanPentacle(World world, BlockPos center, EntityPlayer player, ItemStack satchelStack) {
        if (satchelStack.isEmpty() || satchelStack.getItem() != ModItems.RITUAL_SATCHEL_T2) {
            return false;
        }

        List<PentacleMatch> matches = PentacleDetector.findMatchesAt(world, center);
        if (matches.isEmpty()) {
            return false;
        }

        if (!world.isRemote) {
            cleanPentacle(world, center, player, satchelStack, matches.get(0));
        }
        return true;
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand,
            EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        if (stack.getItem() != ModItems.RITUAL_SATCHEL_T2) {
            return EnumActionResult.PASS;
        }

        if (tryCleanPentacle(worldIn, pos, player, stack)) {
            return EnumActionResult.SUCCESS;
        }

        return facing == EnumFacing.UP && tryCleanPentacle(worldIn, pos.up(), player, stack)
                ? EnumActionResult.SUCCESS : EnumActionResult.PASS;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);
        if (!worldIn.isRemote) {
            int slot = handIn == EnumHand.MAIN_HAND ? playerIn.inventory.currentItem : 40;
            playerIn.openGui(Occultism.INSTANCE, ModGuiHandler.SATCHEL, worldIn, slot, 0, 0);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        String name = ItemNBTUtil.getBoundSpiritName(stack);
        if (!name.isEmpty()) {
            tooltip.add(I18n.format("item.occultism.bound_spirit.tooltip", ItemNBTUtil.formatSpiritName(name)));
        }
    }

    private static void cleanPentacle(World world, BlockPos center, EntityPlayer player, ItemStack satchelStack,
            PentacleMatch match) {
        SatchelInventory satchel = new SatchelInventory(satchelStack);
        int storedItems = 0;
        int droppedItems = 0;

        for (PentacleSymbol symbol : PentacleDetector.getRequiredSymbols(match.getDefinition(), center)) {
            BlockPos target = symbol.getPos();
            if (target.equals(center)) {
                continue;
            }

            IBlockState state = world.getBlockState(target);
            if (!PentacleDetector.matchesRequiredSymbol(world, target, symbol.getSymbol(), state)) {
                continue;
            }

            NonNullList<ItemStack> drops = getPentacleDrops(world, target, state, symbol, player);
            world.setBlockToAir(target);

            for (ItemStack drop : drops) {
                if (drop.isEmpty()) {
                    continue;
                }
                ItemStack remaining = satchel.insert(drop);
                storedItems += drop.getCount() - remaining.getCount();
                if (!remaining.isEmpty()) {
                    droppedItems += remaining.getCount();
                    dropRemainder(world, player, target, remaining);
                }
            }
        }

        satchel.writeToStack();
        player.sendStatusMessage(new TextComponentTranslation("occultism.ritual_satchel.cleaned",
                match.getDefinition().getDisplayName(), storedItems, droppedItems), false);
        world.playSound(null, center, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 0.4F, 1.1F);
    }

    private static NonNullList<ItemStack> getPentacleDrops(World world, BlockPos pos, IBlockState state,
            PentacleSymbol symbol, EntityPlayer player) {
        NonNullList<ItemStack> drops = NonNullList.create();
        if (symbol.isGlyph() && state.getBlock() instanceof BlockChalkGlyph) {
            ItemStack glyphStack = state.getBlock().getPickBlock(state, null, world, pos, player);
            if (!glyphStack.isEmpty()) {
                drops.add(glyphStack);
            }
            return drops;
        }

        state.getBlock().getDrops(drops, world, pos, state, 0);
        return drops;
    }

    private static void dropRemainder(World world, EntityPlayer player, BlockPos source, ItemStack stack) {
        InventoryHelper.spawnItemStack(world,
                player == null ? source.getX() + 0.5D : player.posX,
                player == null ? source.getY() + 0.5D : player.posY,
                player == null ? source.getZ() + 0.5D : player.posZ,
                stack);
    }
}
