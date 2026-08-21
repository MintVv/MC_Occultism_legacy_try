package com.occultism.occultism.item;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nullable;

import com.occultism.occultism.util.ItemNBTUtil;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemKnowledgeTablet extends Item {

    private static final String TAG_STORED_XP = "StoredXP";

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);
        if (worldIn.isRemote) {
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }

        if (playerIn.isSneaking()) {
            withdrawExperience(stack, playerIn);
        } else {
            storeExperience(stack, playerIn);
        }
        playerIn.swingArm(handIn);
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        addTranslatedTooltip(tooltip, getTranslationKey() + ".auto_tooltip");
        String spiritName = getBoundSpiritNameForDisplay(stack);
        String storedXp = NumberFormat.getIntegerInstance(Locale.ROOT).format(getStoredXP(stack));
        String text = I18n.format(getTranslationKey() + ".tooltip", spiritName, "\u00a7a" + storedXp + "\u00a7r");
        for (String line : text.split("\\\\n|\\r?\\n")) {
            if (!line.isEmpty()) {
                tooltip.add(line);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    private static void addTranslatedTooltip(List<String> tooltip, String key) {
        if (!I18n.hasKey(key)) {
            return;
        }
        String text = I18n.format(key);
        for (String line : text.split("\\\\n|\\r?\\n")) {
            if (!line.isEmpty()) {
                tooltip.add(line);
            }
        }
    }

    private static void storeExperience(ItemStack stack, EntityPlayer player) {
        int playerXp = getPlayerExperience(player);
        if (playerXp <= 0) {
            player.sendStatusMessage(new TextComponentTranslation("item.occultism.knowledge_tablet.message.no_xp"),
                    true);
            return;
        }

        int stored = getStoredXP(stack);
        int capacity = Integer.MAX_VALUE - stored;
        if (capacity <= 0) {
            player.sendStatusMessage(new TextComponentTranslation("item.occultism.knowledge_tablet.message.full"),
                    true);
            return;
        }

        int toStore = Math.min(playerXp, capacity);
        setStoredXP(stack, stored + toStore);
        setPlayerExperience(player, playerXp - toStore);
        player.world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP,
                SoundCategory.PLAYERS, 0.7F, 1.1F);
        player.sendStatusMessage(new TextComponentTranslation("item.occultism.knowledge_tablet.message.stored",
                toStore), true);
    }

    private static void withdrawExperience(ItemStack stack, EntityPlayer player) {
        int stored = getStoredXP(stack);
        if (stored <= 0) {
            player.sendStatusMessage(new TextComponentTranslation("item.occultism.knowledge_tablet.message.empty"),
                    true);
            return;
        }

        int capacity = Integer.MAX_VALUE - player.experienceTotal;
        if (capacity <= 0) {
            player.sendStatusMessage(new TextComponentTranslation("item.occultism.knowledge_tablet.message.no_room"),
                    true);
            return;
        }

        int toGive = Math.min(stored, capacity);
        player.addExperience(toGive);
        setStoredXP(stack, stored - toGive);
        player.world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP,
                SoundCategory.PLAYERS, 0.7F, 1.2F);
        player.sendStatusMessage(new TextComponentTranslation("item.occultism.knowledge_tablet.message.withdrawn",
                toGive), true);
    }

    private static int getPlayerExperience(EntityPlayer player) {
        return getExperienceForLevel(player.experienceLevel)
                + Math.round(player.experience * player.xpBarCap());
    }

    private static void setPlayerExperience(EntityPlayer player, int experience) {
        player.experience = 0.0F;
        player.experienceLevel = 0;
        player.experienceTotal = 0;
        if (experience > 0) {
            player.addExperience(experience);
        }
    }

    private static int getExperienceForLevel(int level) {
        if (level <= 0) {
            return 0;
        }
        if (level <= 16) {
            return level * level + 6 * level;
        }
        if (level <= 31) {
            return (int) (2.5D * level * level - 40.5D * level + 360.0D);
        }
        return (int) (4.5D * level * level - 162.5D * level + 2220.0D);
    }

    public static int getStoredXP(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        return tag == null ? 0 : Math.max(0, tag.getInteger(TAG_STORED_XP));
    }

    public static void setStoredXP(ItemStack stack, int xp) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
        }
        tag.setInteger(TAG_STORED_XP, Math.max(0, xp));
        stack.setTagCompound(tag);
    }

    private static String getBoundSpiritNameForDisplay(ItemStack stack) {
        String name = ItemNBTUtil.getBoundSpiritName(stack);
        if (name.isEmpty()) {
            return I18n.format("item.occultism.bound_spirit.unknown");
        }
        return ItemNBTUtil.formatSpiritName(name);
    }
}
