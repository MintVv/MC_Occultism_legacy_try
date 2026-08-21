package com.occultism.occultism.item;

import java.util.List;

import javax.annotation.Nullable;

import com.occultism.occultism.entity.EntityBoundSpirit;
import com.occultism.occultism.entity.EntityFoliotJob;
import com.occultism.occultism.spirit.SpiritJobType;
import com.occultism.occultism.spirit.SpiritTier;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemDebugSummonSpirit extends Item {

    private final SpiritJobType jobType;
    private final SpiritTier tier;
    private final String debugName;

    public ItemDebugSummonSpirit(@Nullable SpiritJobType jobType, SpiritTier tier, String debugName) {
        this.jobType = jobType;
        this.tier = tier == null ? SpiritTier.FOLIOT : tier;
        this.debugName = debugName;
        this.setMaxStackSize(1);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand,
            EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (hand != EnumHand.MAIN_HAND) {
            return EnumActionResult.PASS;
        }
        if (worldIn.isRemote) {
            return EnumActionResult.SUCCESS;
        }

        BlockPos spawnPos = pos.offset(facing);
        EntityLiving spirit = this.createSpirit(worldIn);
        spirit.setPositionAndRotation(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D,
                player.rotationYaw + 180.0F, 0.0F);
        if (this.debugName != null && !this.debugName.isEmpty()) {
            spirit.setCustomNameTag(this.debugName);
            spirit.setAlwaysRenderNameTag(true);
        }

        worldIn.spawnEntity(spirit);
        worldIn.playSound(null, spawnPos, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.PLAYERS, 0.75F, 1.0F);
        player.swingArm(hand);
        return EnumActionResult.SUCCESS;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip,
            ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        String key = this.getTranslationKey() + ".tooltip";
        if (!I18n.hasKey(key)) {
            return;
        }

        for (String line : I18n.format(key).split("\\\\n|\\r?\\n")) {
            if (!line.isEmpty()) {
                tooltip.add(line);
            }
        }
    }

    private EntityLiving createSpirit(World world) {
        if (this.jobType == null) {
            return new EntityBoundSpirit(world, this.tier);
        }
        EntityFoliotJob spirit = new EntityFoliotJob(world, this.jobType, this.tier);
        spirit.setSpiritMaxAge(-1);
        return spirit;
    }
}
