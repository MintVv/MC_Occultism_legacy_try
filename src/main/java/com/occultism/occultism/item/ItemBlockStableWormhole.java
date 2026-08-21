package com.occultism.occultism.item;

import java.util.List;

import javax.annotation.Nullable;

import com.occultism.occultism.Tags;
import com.occultism.occultism.api.common.data.GlobalBlockPos;
import com.occultism.occultism.block.BlockStableWormhole;
import com.occultism.occultism.tileentity.TileEntityStableWormhole;
import com.occultism.occultism.tileentity.TileEntityStorageController;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemBlockStableWormhole extends ItemBlock {

    public ItemBlockStableWormhole(Block block) {
        super(block);
        this.setMaxStackSize(1);
        this.addPropertyOverride(new ResourceLocation(Tags.MOD_ID, "linked"), new IItemPropertyGetter() {
            @Override
            @SideOnly(Side.CLIENT)
            public float apply(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn) {
                return TileEntityStableWormhole.hasLinkedController(stack) ? 1.0F : 0.0F;
            }
        });
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand,
            EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        TileEntity tile = worldIn.getTileEntity(pos);
        if (player.isSneaking() && tile instanceof TileEntityStorageController) {
            if (!worldIn.isRemote) {
                TileEntityStableWormhole.writeLinkToStack(stack, worldIn, pos);
                player.sendStatusMessage(new TextComponentTranslation(this.getTranslationKey()
                        + ".message.set_storage_controller"), true);
            }
            return EnumActionResult.SUCCESS;
        }
        return super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side,
            float hitX, float hitY, float hitZ, IBlockState newState) {
        IBlockState placedState = newState;
        if (this.block instanceof BlockStableWormhole) {
            placedState = placedState.withProperty(BlockStableWormhole.LINKED,
                    TileEntityStableWormhole.hasLinkedController(stack));
        }

        boolean placed = super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, placedState);
        if (!placed) {
            return false;
        }

        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityStableWormhole) {
            ((TileEntityStableWormhole) tile).readLinkFromStack(stack);
            world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
        }
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        GlobalBlockPos linked = TileEntityStableWormhole.getLinkedControllerPosition(stack);
        if (linked == null || !linked.isPresent()) {
            tooltip.add(I18n.format(this.getTranslationKey() + ".tooltip.unlinked"));
            return;
        }
        tooltip.add(I18n.format(this.getTranslationKey() + ".tooltip.linked",
                linked.getDimension(), linked.getPos().getX(), linked.getPos().getY(), linked.getPos().getZ()));
    }
}
