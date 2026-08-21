package com.occultism.occultism.item;

import java.util.List;

import javax.annotation.Nullable;

import com.occultism.occultism.spirit.SpiritJobRecipe;
import com.occultism.occultism.spirit.SpiritJobType;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemFoliotJobToken extends Item {

    private static final double WORK_RANGE = 3.0D;

    private final SpiritJobType jobType;

    public ItemFoliotJobToken(SpiritJobType jobType) {
        this.jobType = jobType;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack held = playerIn.getHeldItem(handIn);
        if (worldIn.isRemote) {
            return new ActionResult<>(EnumActionResult.SUCCESS, held);
        }

        EntityItem target = this.findMatchingItem(worldIn, playerIn);
        if (target == null) {
            playerIn.sendStatusMessage(new net.minecraft.util.text.TextComponentTranslation(
                    this.getTranslationKey() + ".no_recipe"), true);
            return new ActionResult<>(EnumActionResult.PASS, held);
        }

        SpiritJobRecipe recipe = SpiritJobRecipe.findFirst(this.jobType, target.getItem());
        this.convertItem(worldIn, target, recipe);
        worldIn.playSound(null, playerIn.posX, playerIn.posY, playerIn.posZ,
                SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.7F, 1.2F);
        playerIn.swingArm(handIn);
        return new ActionResult<>(EnumActionResult.SUCCESS, held);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip,
            ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        this.addTranslatedTooltip(tooltip, this.getTranslationKey() + ".tooltip");
    }

    private EntityItem findMatchingItem(World world, EntityPlayer player) {
        AxisAlignedBB bounds = player.getEntityBoundingBox().grow(WORK_RANGE);
        List<EntityItem> items = world.getEntitiesWithinAABB(EntityItem.class, bounds);
        EntityItem best = null;
        double bestDistance = Double.MAX_VALUE;
        for (EntityItem item : items) {
            if (item.isDead || item.getItem().isEmpty()) {
                continue;
            }
            if (SpiritJobRecipe.findFirst(this.jobType, item.getItem()) == null) {
                continue;
            }
            double distance = player.getDistanceSq(item);
            if (distance < bestDistance) {
                best = item;
                bestDistance = distance;
            }
        }
        return best;
    }

    private void convertItem(World world, EntityItem entityItem, SpiritJobRecipe recipe) {
        ItemStack input = entityItem.getItem();
        ItemStack output = recipe.createOutput(input.getCount());
        double x = entityItem.posX;
        double y = entityItem.posY;
        double z = entityItem.posZ;
        entityItem.setDead();
        this.spawnStacks(world, x, y, z, output);
        world.playEvent(2004, (new net.minecraft.util.math.BlockPos(x, y, z)), 0);
    }

    private void spawnStacks(World world, double x, double y, double z, ItemStack stack) {
        int remaining = stack.getCount();
        int maxStackSize = stack.getMaxStackSize();
        while (remaining > 0) {
            ItemStack split = stack.copy();
            split.setCount(Math.min(remaining, maxStackSize));
            EntityItem result = new EntityItem(world, x, y, z, split);
            result.motionY = 0.1D;
            world.spawnEntity(result);
            remaining -= split.getCount();
        }
    }

    @SideOnly(Side.CLIENT)
    private void addTranslatedTooltip(List<String> tooltip, String key) {
        if (!I18n.hasKey(key)) {
            return;
        }

        String text = I18n.format(key);
        if (text == null || text.isEmpty()) {
            return;
        }

        for (String line : text.split("\\\\n|\\r?\\n")) {
            if (!line.isEmpty()) {
                tooltip.add(line);
            }
        }
    }
}
