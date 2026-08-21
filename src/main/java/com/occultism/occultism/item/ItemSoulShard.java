package com.occultism.occultism.item;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemSoulShard extends Item {

    private static final String TAG_ENTITY = "entityData";
    private static final String TAG_LEGACY_ENTITY = "OccultismEntity";
    private static final String TAG_LEGACY_ENTITY_ID = "EntityId";
    private static final String TAG_LEGACY_ENTITY_NAME = "EntityName";

    private final boolean dropsLootOnUse;

    public ItemSoulShard(boolean dropsLootOnUse) {
        this.dropsLootOnUse = dropsLootOnUse;
        this.setMaxStackSize(1);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);
        if (!this.dropsLootOnUse || !hasStoredEntity(stack)) {
            return new ActionResult<>(EnumActionResult.PASS, stack);
        }
        if (worldIn.isRemote) {
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }

        Entity entity = createStoredEntity(stack, worldIn);
        if (!(entity instanceof EntityLivingBase)) {
            return new ActionResult<>(EnumActionResult.FAIL, stack);
        }

        EntityLivingBase living = (EntityLivingBase) entity;
        living.setPositionAndRotation(playerIn.posX, playerIn.posY, playerIn.posZ, playerIn.rotationYaw, 0.0F);
        living.onDeath(DamageSource.causePlayerDamage(playerIn));

        playerIn.getCooldownTracker().setCooldown(this, 10);
        if (!playerIn.capabilities.isCreativeMode) {
            stack.shrink(1);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        String key = this.getTranslationKey() + (hasStoredEntity(stack) ? ".tooltip_filled" : ".tooltip_empty");
        String text = hasStoredEntity(stack)
                ? I18n.format(key, getStoredEntityTooltipName(stack))
                : I18n.format(key);
        if (!text.equals(key)) {
            for (String line : text.split("\\\\n|\\r?\\n")) {
                if (!line.isEmpty()) {
                    tooltip.add(line);
                }
            }
        }

        String autoTooltipKey = this.getTranslationKey() + ".auto_tooltip";
        if (I18n.hasKey(autoTooltipKey)) {
            for (String line : I18n.format(autoTooltipKey).split("\\\\n|\\r?\\n")) {
                if (!line.isEmpty()) {
                    tooltip.add(line);
                }
            }
        }
    }

    public static boolean hasStoredEntity(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTagCompound()) {
            return false;
        }
        NBTTagCompound tag = stack.getTagCompound();
        return tag.hasKey(TAG_ENTITY, 10) || tag.hasKey(TAG_LEGACY_ENTITY, 10);
    }

    /**
     * Soul shards retain the complete entity snapshot on the server, but only
     * the entity id is needed by the client tooltip.  Do not broadcast UUIDs,
     * position, motion, owner data, or other mutable entity state.
     */
    @Override
    @Nullable
    public NBTTagCompound getNBTShareTag(ItemStack stack) {
        NBTTagCompound tag = super.getNBTShareTag(stack);
        if (tag == null) {
            return null;
        }

        NBTTagCompound share = tag.copy();
        if (share.hasKey(TAG_ENTITY, 10)) {
            NBTTagCompound entityData = share.getCompoundTag(TAG_ENTITY);
            NBTTagCompound filtered = new NBTTagCompound();
            String id = entityData.getString("id");
            if (!id.isEmpty()) {
                filtered.setString("id", id);
            }
            share.setTag(TAG_ENTITY, filtered);
        }

        // Keep old saves readable without exposing their complete entity snapshot.
        if (share.hasKey(TAG_LEGACY_ENTITY, 10)) {
            NBTTagCompound legacyData = share.getCompoundTag(TAG_LEGACY_ENTITY);
            NBTTagCompound filtered = new NBTTagCompound();
            String id = legacyData.getString("id");
            if (id.isEmpty()) {
                id = share.getString(TAG_LEGACY_ENTITY_ID);
            }
            if (!id.isEmpty()) {
                filtered.setString("id", id);
            }
            share.setTag(TAG_LEGACY_ENTITY, filtered);
        }
        return share;
    }

    public static boolean storeEntity(ItemStack stack, EntityLivingBase target) {
        ResourceLocation id = EntityList.getKey(target);
        if (id == null) {
            return false;
        }

        NBTTagCompound entityData = new NBTTagCompound();
        float health = target.getHealth();
        target.setHealth(target.getMaxHealth());
        try {
            target.writeToNBT(entityData);
        } finally {
            target.setHealth(health);
        }

        entityData.setString("id", id.toString());
        setStoredEntity(stack, entityData, id.toString(), "");
        return true;
    }

    public static void setStoredEntity(ItemStack stack, @Nullable NBTTagCompound entityData, String id, String name) {
        if (entityData == null || entityData.isEmpty()) {
            return;
        }
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
        }
        NBTTagCompound storedData = entityData.copy();
        if (id != null && !id.isEmpty()) {
            storedData.setString("id", id);
        }
        tag.setTag(TAG_ENTITY, storedData);
        tag.removeTag(TAG_LEGACY_ENTITY);
        tag.removeTag(TAG_LEGACY_ENTITY_ID);
        tag.removeTag(TAG_LEGACY_ENTITY_NAME);
        stack.setTagCompound(tag);
    }

    @Nullable
    public static NBTTagCompound getStoredEntityData(ItemStack stack) {
        if (!hasStoredEntity(stack)) {
            return null;
        }
        NBTTagCompound tag = stack.getTagCompound();
        if (tag.hasKey(TAG_ENTITY, 10)) {
            return tag.getCompoundTag(TAG_ENTITY).copy();
        }
        return tag.getCompoundTag(TAG_LEGACY_ENTITY).copy();
    }

    public static String getStoredEntityId(ItemStack stack) {
        if (!stack.hasTagCompound()) {
            return "";
        }
        NBTTagCompound tag = stack.getTagCompound();
        if (tag.hasKey(TAG_ENTITY, 10)) {
            String id = tag.getCompoundTag(TAG_ENTITY).getString("id");
            if (!id.isEmpty()) {
                return id;
            }
        }
        return tag.getString(TAG_LEGACY_ENTITY_ID);
    }

    public static String getStoredEntityDisplayName(ItemStack stack) {
        if (!stack.hasTagCompound()) {
            return "";
        }
        String name = stack.getTagCompound().getString(TAG_LEGACY_ENTITY_NAME);
        return name.isEmpty() ? getStoredEntityId(stack) : name;
    }

    @SideOnly(Side.CLIENT)
    private static String getStoredEntityTooltipName(ItemStack stack) {
        if (stack.hasTagCompound()) {
            String legacyName = stack.getTagCompound().getString(TAG_LEGACY_ENTITY_NAME);
            if (!legacyName.isEmpty()) {
                return legacyName;
            }
        }
        String id = getStoredEntityId(stack);
        if (id.isEmpty()) {
            return id;
        }
        String translationName = EntityList.getTranslationName(new ResourceLocation(id));
        if (translationName == null) {
            return id;
        }
        String key = "entity." + translationName + ".name";
        String translated = I18n.format(key);
        return translated.equals(key) ? id : translated;
    }

    @Nullable
    public static Entity createStoredEntity(ItemStack stack, World world) {
        NBTTagCompound entityData = getStoredEntityData(stack);
        if (entityData == null) {
            return null;
        }
        String id = getStoredEntityId(stack);
        if (!id.isEmpty()) {
            entityData.setString("id", id);
        }
        entityData.removeTag("Pos");
        return EntityList.createEntityFromNBT(entityData, world);
    }
}
