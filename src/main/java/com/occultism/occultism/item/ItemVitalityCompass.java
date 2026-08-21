package com.occultism.occultism.item;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.occultism.occultism.Tags;
import com.occultism.occultism.util.ItemNBTUtil;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemVitalityCompass extends Item {

    public ItemVitalityCompass() {
        this.setMaxStackSize(1);
        this.addPropertyOverride(new ResourceLocation(Tags.MOD_ID, "angle"), new IItemPropertyGetter() {
            @SideOnly(Side.CLIENT)
            private double rotation;
            @SideOnly(Side.CLIENT)
            private double rota;
            @SideOnly(Side.CLIENT)
            private long lastUpdateTick;

            @Override
            @SideOnly(Side.CLIENT)
            public float apply(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn) {
                if (entityIn == null && !stack.isOnItemFrame()) {
                    return 0.0F;
                }

                boolean heldByEntity = entityIn != null;
                Entity compassHolder = heldByEntity ? entityIn : stack.getItemFrame();
                if (worldIn == null) {
                    worldIn = compassHolder.world;
                }

                Entity target = getLinkedEntity(stack, worldIn);
                double angle;
                if (target != null) {
                    double holderYaw = heldByEntity ? compassHolder.rotationYaw
                            : this.getFrameRotation((EntityItemFrame) compassHolder);
                    holderYaw = MathHelper.positiveModulo(holderYaw / 360.0D, 1.0D);
                    double targetAngle = this.getAngleToTarget(compassHolder, target) / (Math.PI * 2D);
                    angle = 0.5D - (holderYaw - 0.25D - targetAngle);
                } else {
                    angle = Math.random();
                }

                if (heldByEntity) {
                    angle = this.wobble(worldIn, angle);
                }

                return MathHelper.positiveModulo((float) angle, 1.0F);
            }

            @SideOnly(Side.CLIENT)
            private double wobble(World worldIn, double targetRotation) {
                if (worldIn.getTotalWorldTime() != this.lastUpdateTick) {
                    this.lastUpdateTick = worldIn.getTotalWorldTime();
                    double delta = targetRotation - this.rotation;
                    delta = MathHelper.positiveModulo(delta + 0.5D, 1.0D) - 0.5D;
                    this.rota += delta * 0.1D;
                    this.rota *= 0.8D;
                    this.rotation = MathHelper.positiveModulo(this.rotation + this.rota, 1.0D);
                }
                return this.rotation;
            }

            @SideOnly(Side.CLIENT)
            private double getFrameRotation(EntityItemFrame frame) {
                return MathHelper.wrapDegrees(180 + frame.facingDirection.getHorizontalIndex() * 90);
            }

            @SideOnly(Side.CLIENT)
            private double getAngleToTarget(Entity holder, Entity target) {
                return Math.atan2(target.posZ - holder.posZ, target.posX - holder.posX);
            }
        });
    }

    public EnumActionResult tryLink(ItemStack stack, EntityPlayer player, Entity target, EnumHand hand) {
        if (hand != EnumHand.MAIN_HAND || !(target instanceof EntityLivingBase)
                || !((EntityLivingBase) target).isEntityAlive()) {
            return EnumActionResult.PASS;
        }

        if (player.world.isRemote) {
            return EnumActionResult.SUCCESS;
        }

        ItemNBTUtil.setSpiritEntityUUID(stack, target.getUniqueID());
        ItemNBTUtil.setBoundSpiritName(stack, target.getDisplayName().getUnformattedText());
        player.sendStatusMessage(new TextComponentTranslation(this.getTranslationKey() + ".message.target_linked",
                target.getDisplayName()), false);
        player.swingArm(hand);
        player.inventoryContainer.detectAndSendChanges();
        return EnumActionResult.SUCCESS;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || oldStack.getItem() != newStack.getItem();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        String name = ItemNBTUtil.getBoundSpiritName(stack);
        if (name.isEmpty()) {
            name = I18n.format("item.occultism.bound_spirit.unknown");
        } else {
            name = ItemNBTUtil.formatSpiritName(name);
        }
        tooltip.add(I18n.format(this.getTranslationKey() + ".tooltip", name));
    }

    @Nullable
    private static Entity getLinkedEntity(ItemStack stack, World world) {
        UUID uuid = ItemNBTUtil.getSpiritEntityUUID(stack);
        if (uuid == null) {
            return null;
        }
        for (Entity entity : world.getLoadedEntityList()) {
            if (uuid.equals(entity.getUniqueID())) {
                return entity;
            }
        }
        return null;
    }
}
