package com.occultism.occultism.item;

import java.util.List;

import javax.annotation.Nullable;

import com.occultism.occultism.entity.EntityBatFamiliar;
import com.occultism.occultism.entity.EntityBeholderFamiliar;
import com.occultism.occultism.entity.EntityBeaverFamiliar;
import com.occultism.occultism.entity.EntityBlacksmithFamiliar;
import com.occultism.occultism.entity.EntityCthulhuFamiliar;
import com.occultism.occultism.entity.EntityChimeraFamiliar;
import com.occultism.occultism.entity.EntityDeerFamiliar;
import com.occultism.occultism.entity.EntityDevilFamiliar;
import com.occultism.occultism.entity.EntityDragonFamiliar;
import com.occultism.occultism.entity.EntityFairyFamiliar;
import com.occultism.occultism.entity.EntityGreedyFamiliar;
import com.occultism.occultism.entity.EntityGoatFamiliar;
import com.occultism.occultism.entity.EntityGuardianFamiliar;
import com.occultism.occultism.entity.EntityHeadlessFamiliar;
import com.occultism.occultism.entity.EntityMercyGoat;
import com.occultism.occultism.entity.EntityMummyFamiliar;
import com.occultism.occultism.entity.EntityPossessedBee;
import com.occultism.occultism.entity.EntityPurifiedAllay;
import com.occultism.occultism.entity.EntityShubNiggurathFamiliar;
import com.occultism.occultism.entity.IFamiliar;
import com.occultism.occultism.capability.FamiliarRingCapability;
import com.occultism.occultism.advancement.FamiliarTrigger;
import com.occultism.occultism.handler.FamiliarRingHandler;
import com.occultism.occultism.registry.ModAdvancements;
import com.occultism.occultism.util.ItemNBTUtil;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityParrot;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ActionResult;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class ItemFamiliarRing extends Item {

    private static final String TAG_STORED_FAMILIAR = "StoredFamiliarEntity";
    private static final String TAG_STORED_FAMILIAR_ID = "StoredFamiliarEntityId";
    private static final String TAG_STORED_FAMILIAR_NAME = "StoredFamiliarEntityName";
    public static final String TAG_FAMILIAR_TYPE = "familiarType";
    public static final String TAG_RITUAL_FAMILIAR_PARROT = "OccultismFamiliarParrot";

    public ItemFamiliarRing() {
        this.setMaxStackSize(1);
        this.addPropertyOverride(new ResourceLocation("occultism", "has_familiar"), new IItemPropertyGetter() {
            @Override
            public float apply(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn) {
                return hasStoredFamiliar(stack) ? 1.0F : 0.0F;
            }
        });
    }

    /** Materializes loot-table familiarType tags once a server world is available. */
    @Override
    public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if (!worldIn.isRemote) {
            materializeFamiliarType(stack, worldIn);
        }
    }

    /**
     * Native 1.12 equivalent of equipping a filled Curios ring. Right-clicking
     * air equips it into the player's persistent one-slot capability; block
     * right-click remains the release action handled by {@link #onItemUse}.
     */
    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);
        if (!worldIn.isRemote) {
            materializeFamiliarType(stack, worldIn);
        }
        if (handIn != EnumHand.MAIN_HAND || !hasStoredFamiliar(stack)) {
            return new ActionResult<>(EnumActionResult.PASS, stack);
        }
        if (worldIn.isRemote) {
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }

        FamiliarRingCapability capability = FamiliarRingHandler.get(playerIn);
        if (capability == null) {
            playerIn.sendStatusMessage(new TextComponentTranslation(getTranslationKey() + ".message.equip_failed"),
                    true);
            return new ActionResult<>(EnumActionResult.FAIL, stack);
        }
        if (capability.isEquipped()) {
            playerIn.sendStatusMessage(new TextComponentTranslation(getTranslationKey() + ".message.already_equipped"),
                    true);
            return new ActionResult<>(EnumActionResult.FAIL, stack);
        }
        if (!capability.equip(playerIn, stack)) {
            playerIn.sendStatusMessage(new TextComponentTranslation(getTranslationKey() + ".message.equip_failed"),
                    true);
            return new ActionResult<>(EnumActionResult.FAIL, stack);
        }

        playerIn.setHeldItem(handIn, ItemStack.EMPTY);
        if (playerIn instanceof net.minecraft.entity.player.EntityPlayerMP) {
            com.occultism.occultism.network.ModNetworking.syncFamiliarRing(
                    (net.minecraft.entity.player.EntityPlayerMP) playerIn);
        }
        playerIn.sendStatusMessage(new TextComponentTranslation(getTranslationKey() + ".message.equipped"), true);
        return new ActionResult<>(EnumActionResult.SUCCESS, ItemStack.EMPTY);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand,
            EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        if (!worldIn.isRemote) {
            materializeFamiliarType(stack, worldIn);
        }
        if (hand != EnumHand.MAIN_HAND || !hasStoredFamiliar(stack)) {
            return EnumActionResult.PASS;
        }
        if (worldIn.isRemote) {
            return EnumActionResult.SUCCESS;
        }

        NBTTagCompound storedData = getStoredFamiliarData(stack);
        String storedId = getStoredFamiliarId(stack);
        String storedName = getStoredFamiliarName(stack);
        Entity entity = createStoredFamiliar(stack, worldIn);
        clearStoredFamiliar(stack);

        if (!(entity instanceof IFamiliar) && !isTransportOnlyFamiliar(entity)) {
            setStoredFamiliar(stack, storedData, storedId, storedName);
            player.sendStatusMessage(new TextComponentTranslation(getTranslationKey() + ".message.release_failed"),
                    true);
            return EnumActionResult.FAIL;
        }

        BlockPos spawnPos = getSpawnPos(worldIn, pos, facing);
        float yaw = player.rotationYaw + 180.0F;
        entity.dimension = worldIn.provider.getDimension();
        entity.setPositionAndRotation(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, yaw, 0.0F);
        entity.rotationYaw = yaw;
        entity.rotationPitch = 0.0F;
        entity.setRotationYawHead(yaw);
        if (entity instanceof EntityLivingBase) {
            ((EntityLivingBase) entity).setRenderYawOffset(yaw);
        }
        applyReleasedOwner(entity, player);
        if (!storedName.isEmpty() && entity instanceof EntityLivingBase) {
            ((EntityLivingBase) entity).setCustomNameTag(storedName);
        }

        if (!worldIn.spawnEntity(entity)) {
            setStoredFamiliar(stack, storedData, storedId, storedName);
            player.sendStatusMessage(new TextComponentTranslation(getTranslationKey() + ".message.release_failed"),
                    true);
            return EnumActionResult.FAIL;
        }

        player.swingArm(hand);
        player.setHeldItem(hand, stack);
        worldIn.playSound(null, spawnPos, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.PLAYERS, 0.7F, 1.1F);
        player.sendStatusMessage(new TextComponentTranslation(getTranslationKey() + ".message.released"), true);
        return EnumActionResult.SUCCESS;
    }

    @Override
    public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer playerIn, EntityLivingBase target,
            EnumHand hand) {
        return this.tryCapture(stack, playerIn, target, hand) == EnumActionResult.SUCCESS;
    }

    public EnumActionResult tryCapture(ItemStack stack, EntityPlayer player, Entity target, EnumHand hand) {
        if (!player.world.isRemote) {
            materializeFamiliarType(stack, player.world);
        }
        if (hand != EnumHand.MAIN_HAND || !canCaptureFamiliar(target, player)) {
            return EnumActionResult.PASS;
        }
        if (hasStoredFamiliar(stack)) {
            if (!player.world.isRemote) {
                player.sendStatusMessage(new TextComponentTranslation(getTranslationKey() + ".message.already_stored"),
                        true);
            }
            return EnumActionResult.FAIL;
        }
        EntityLivingBase familiar = (EntityLivingBase) target;
        if (!familiar.isEntityAlive()) {
            return EnumActionResult.PASS;
        }
        if (player.world.isRemote) {
            return EnumActionResult.SUCCESS;
        }

        familiar.dismountRidingEntity();
        familiar.removePassengers();
        if (!storeFamiliar(stack, familiar)) {
            player.sendStatusMessage(new TextComponentTranslation(getTranslationKey() + ".message.store_failed"), true);
            return EnumActionResult.FAIL;
        }

        if (player instanceof net.minecraft.entity.player.EntityPlayerMP) {
            ModAdvancements.FAMILIAR.trigger((net.minecraft.entity.player.EntityPlayerMP) player,
                    FamiliarTrigger.Type.CAPTURE);
        }
        familiar.setDead();
        player.swingArm(hand);
        player.setHeldItem(hand, stack);
        player.world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT,
                SoundCategory.PLAYERS, 0.55F, 1.35F);
        player.sendStatusMessage(new TextComponentTranslation(getTranslationKey() + ".message.stored",
                getStoredFamiliarName(stack)), true);
        return EnumActionResult.SUCCESS;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        String key = getTranslationKey() + (hasStoredFamiliar(stack) ? ".tooltip_filled" : ".tooltip_empty");
        String text = hasStoredFamiliar(stack)
                ? I18n.format(key, getStoredFamiliarDisplayName(stack))
                : I18n.format(key);
        for (String line : text.split("\\\\n|\\r?\\n")) {
            if (!line.isEmpty()) {
                tooltip.add(line);
            }
        }
        if (hasStoredFamiliar(stack)) {
            String familiarId = getStoredFamiliarId(stack);
            if (familiarId.isEmpty() && stack.hasTagCompound()) {
                familiarId = stack.getTagCompound().getString(TAG_FAMILIAR_TYPE);
            }
            if (!familiarId.isEmpty()) {
                tooltip.add(I18n.format(getTranslationKey() + ".tooltip.familiar_type",
                        getStoredFamiliarTypeTooltipName(familiarId)));
            }
            if (worldIn != null && isTransportOnlyStoredFamiliar(stack, worldIn)) {
                tooltip.add(I18n.format(getTranslationKey() + ".tooltip.transport_only"));
            }
        }
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        if (!hasStoredFamiliar(stack) || FMLCommonHandler.instance().getSide() != Side.CLIENT) {
            return false;
        }
        return hasClientFamiliarEffect(stack);
    }

    /**
     * Curios uses the familiar's effect setting to decide whether the ring is
     * foil. The native 1.12 slot keeps that visual rule on the client and does
     * not make every filled inventory ring glow unconditionally.
     */
    @SideOnly(Side.CLIENT)
    private static boolean hasClientFamiliarEffect(ItemStack stack) {
        Minecraft minecraft = Minecraft.getMinecraft();
        EntityPlayer player = minecraft.player;
        if (player == null || player.world == null) {
            return false;
        }

        FamiliarRingCapability capability = FamiliarRingHandler.get(player);
        if (capability != null && capability.isEquippedStack(stack)) {
            IFamiliar active = capability.getActiveFamiliar(player);
            return active != null && active.isEffectEnabled(player);
        }

        // A held or displayed filled ring has no Curios capability in 1.12;
        // reconstruct its supported IFamiliar only for the client-side foil
        // query. Non-IFamiliar transport entities deliberately return false.
        EntityLivingBase preview = createStoredFamiliarForEquipment(stack, player.world);
        return preview instanceof IFamiliar && ((IFamiliar) preview).isEffectEnabled(player);
    }

    @SideOnly(Side.CLIENT)
    private static String getStoredFamiliarTypeTooltipName(String id) {
        if (id == null || id.isEmpty()) {
            return "";
        }
        try {
            String translationName = EntityList.getTranslationName(new ResourceLocation(id));
            if (translationName != null) {
                String key = "entity." + translationName + ".name";
                String translated = I18n.format(key);
                if (!translated.equals(key)) {
                    return translated;
                }
            }
        } catch (RuntimeException ignored) {
            // Keep malformed/unknown IDs visible in the tooltip for debugging.
        }
        return id;
    }

    public static boolean hasStoredFamiliar(ItemStack stack) {
        return !stack.isEmpty() && stack.hasTagCompound()
                && (stack.getTagCompound().hasKey(TAG_STORED_FAMILIAR, 10)
                        || stack.getTagCompound().hasKey(TAG_FAMILIAR_TYPE, 8));
    }

    /**
     * Converts a loot-table familiarType into the same canonical entity NBT
     * used by captured familiars. Unknown types stay untouched for debugging
     * and can be retried when a valid server registry becomes available.
     */
    public static boolean materializeFamiliarType(ItemStack stack, @Nullable World world) {
        if (stack.isEmpty() || !stack.hasTagCompound()) {
            return false;
        }
        NBTTagCompound tag = stack.getTagCompound();
        if (tag.hasKey(TAG_STORED_FAMILIAR, 10) || !tag.hasKey(TAG_FAMILIAR_TYPE, 8)
                || world == null || world.isRemote) {
            return false;
        }

        String rawId = tag.getString(TAG_FAMILIAR_TYPE);
        if (rawId.isEmpty()) {
            return false;
        }
        ResourceLocation id;
        try {
            id = new ResourceLocation(rawId);
        } catch (RuntimeException ignored) {
            return false;
        }

        Entity entity = EntityList.createEntityByIDFromName(id, world);
        if (!(entity instanceof EntityLivingBase) || !isSupportedStoredFamiliar(entity)) {
            return false;
        }
        NBTTagCompound entityData = new NBTTagCompound();
        if (!((EntityLivingBase) entity).writeToNBTOptional(entityData)) {
            return false;
        }
        entityData.setString("id", id.toString());
        entityData.removeTag("Pos");
        entityData.removeTag("Motion");
        entityData.removeTag("Rotation");
        String name = ItemNBTUtil.getOrCreateBoundSpiritName(stack);
        setStoredFamiliar(stack, entityData, id.toString(), name);
        NBTTagCompound updated = stack.getTagCompound();
        updated.removeTag(TAG_FAMILIAR_TYPE);
        stack.setTagCompound(updated);
        return true;
    }

    private static boolean storeFamiliar(ItemStack stack, EntityLivingBase familiar) {
        NBTTagCompound entityData = new NBTTagCompound();
        if (!familiar.writeToNBTOptional(entityData)) {
            return false;
        }
        ResourceLocation id = EntityList.getKey(familiar);
        if (id == null) {
            return false;
        }

        entityData.setString("id", id.toString());
        entityData.removeTag("Pos");
        entityData.removeTag("Motion");
        entityData.removeTag("Rotation");
        setStoredFamiliar(stack, entityData, id.toString(), getFamiliarDisplayName(familiar));
        return true;
    }

    private static void setStoredFamiliar(ItemStack stack, @Nullable NBTTagCompound entityData, String id, String name) {
        if (stack.isEmpty() || entityData == null || entityData.isEmpty()) {
            return;
        }
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
        }
        tag.setTag(TAG_STORED_FAMILIAR, entityData.copy());
        tag.setString(TAG_STORED_FAMILIAR_ID, id == null ? "" : id);
        tag.setString(TAG_STORED_FAMILIAR_NAME, name == null ? "" : name);
        stack.setTagCompound(tag);
    }

    private static void clearStoredFamiliar(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTagCompound()) {
            return;
        }
        NBTTagCompound tag = stack.getTagCompound();
        tag.removeTag(TAG_STORED_FAMILIAR);
        tag.removeTag(TAG_STORED_FAMILIAR_ID);
        tag.removeTag(TAG_STORED_FAMILIAR_NAME);
        if (tag.isEmpty()) {
            stack.setTagCompound(null);
        }
    }

    @Nullable
    private static NBTTagCompound getStoredFamiliarData(ItemStack stack) {
        if (!hasStoredFamiliar(stack)) {
            return null;
        }
        return stack.getTagCompound().getCompoundTag(TAG_STORED_FAMILIAR).copy();
    }

    private static String getStoredFamiliarId(ItemStack stack) {
        return stack.hasTagCompound() ? stack.getTagCompound().getString(TAG_STORED_FAMILIAR_ID) : "";
    }

    private static String getStoredFamiliarName(ItemStack stack) {
        return stack.hasTagCompound() ? stack.getTagCompound().getString(TAG_STORED_FAMILIAR_NAME) : "";
    }

    private static String getStoredFamiliarDisplayName(ItemStack stack) {
        String name = getStoredFamiliarName(stack);
        return name.isEmpty() ? getStoredFamiliarId(stack) : name;
    }

    @Nullable
    public static EntityLivingBase createStoredFamiliarForEquipment(ItemStack stack, World world) {
        materializeFamiliarType(stack, world);
        Entity entity = createStoredFamiliar(stack, world);
        return canEquipFamiliar(entity)
                ? (EntityLivingBase) entity : null;
    }

    /**
     * Only entities implementing IFamiliar can run inside the native slot.
     * This is intentionally narrower than the transport list below.
     */
    public static boolean canEquipFamiliar(@Nullable Entity entity) {
        return entity instanceof EntityLivingBase && entity instanceof IFamiliar;
    }

    /**
     * Supported entities that can be captured/released as ring cargo but do
     * not implement IFamiliar. They never receive effects, curioTick, or an
     * equipped-slot entry.
     */
    public static boolean isTransportOnlyFamiliar(@Nullable Entity entity) {
        return entity instanceof EntityLivingBase && isSupportedStoredFamiliar(entity)
                && !(entity instanceof IFamiliar);
    }

    /** Returns whether a stored cargo entity cannot participate in the slot. */
    public static boolean isTransportOnlyStoredFamiliar(ItemStack stack, @Nullable World world) {
        if (world == null || !hasStoredFamiliar(stack)) {
            return false;
        }
        return isTransportOnlyFamiliar(createStoredFamiliar(stack, world));
    }

    @Nullable
    private static Entity createStoredFamiliar(ItemStack stack, World world) {
        NBTTagCompound entityData = getStoredFamiliarData(stack);
        if (entityData == null) {
            return null;
        }
        String id = getStoredFamiliarId(stack);
        if (id.isEmpty()) {
            id = entityData.getString("id");
        }
        if (id.isEmpty()) {
            return null;
        }
        if (!id.isEmpty()) {
            entityData.setString("id", id);
        }
        entityData.removeTag("Pos");
        entityData.removeTag("Motion");
        entityData.removeTag("Rotation");
        try {
            return EntityList.createEntityFromNBT(entityData, world);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    /** Writes mutable state from an off-world equipped familiar back to its ring. */
    public static boolean updateStoredFamiliarState(ItemStack stack, EntityLivingBase familiar) {
        if (!hasStoredFamiliar(stack) || familiar == null) {
            return false;
        }
        NBTTagCompound entityData = new NBTTagCompound();
        if (!familiar.writeToNBTOptional(entityData)) {
            return false;
        }
        ResourceLocation id = EntityList.getKey(familiar);
        if (id == null) {
            String storedId = getStoredFamiliarId(stack);
            if (storedId.isEmpty()) {
                return false;
            }
            try {
                id = new ResourceLocation(storedId);
            } catch (RuntimeException ignored) {
                return false;
            }
        }
        entityData.setString("id", id.toString());
        entityData.removeTag("Pos");
        entityData.removeTag("Motion");
        entityData.removeTag("Rotation");
        NBTTagCompound tag = stack.getTagCompound();
        tag.setTag(TAG_STORED_FAMILIAR, entityData);
        tag.setString(TAG_STORED_FAMILIAR_ID, id.toString());
        tag.setString(TAG_STORED_FAMILIAR_NAME, getFamiliarDisplayName(familiar));
        stack.setTagCompound(tag);
        return true;
    }

    private static boolean isSupportedStoredFamiliar(@Nullable Entity entity) {
        // The upstream ring is interface-driven: new familiars automatically
        // participate once they implement IFamiliar. Keep the explicit list
        // only for legacy transport-only entities that cannot run ring hooks.
        return entity instanceof IFamiliar || entity instanceof EntityPurifiedAllay || entity instanceof EntityPossessedBee
                || entity instanceof EntityMercyGoat || entity instanceof EntityGreedyFamiliar
                || entity instanceof EntityBatFamiliar || entity instanceof EntityDeerFamiliar
                || entity instanceof EntityBeaverFamiliar
                || entity instanceof EntityMummyFamiliar
                || entity instanceof EntityDevilFamiliar
                || entity instanceof EntityGuardianFamiliar
                || entity instanceof EntityHeadlessFamiliar
                || entity instanceof EntityCthulhuFamiliar
                || entity instanceof EntityBlacksmithFamiliar
                || entity instanceof EntityDragonFamiliar
                || entity instanceof EntityFairyFamiliar
                || entity instanceof EntityGoatFamiliar
                || entity instanceof EntityShubNiggurathFamiliar
                || entity instanceof EntityBeholderFamiliar
                || entity instanceof EntityChimeraFamiliar
                || isRitualFamiliarParrot(entity);
    }

    private static boolean canCaptureFamiliar(@Nullable Entity entity, EntityPlayer player) {
        if (isTransportOnlyFamiliar(entity)) {
            return entity.isEntityAlive();
        }
        if (!(entity instanceof IFamiliar)) {
            return false;
        }
        IFamiliar familiar = (IFamiliar) entity;
        EntityLivingBase owner = familiar.getFamiliarOwner();
        // Match FamiliarRingItem: an unbound familiar may be claimed, while
        // an already bound familiar may only be captured by its owner.
        return owner == null || owner == player;
    }

    private static boolean isRitualFamiliarParrot(@Nullable Entity entity) {
        return entity instanceof EntityParrot
                && entity.getEntityData().getBoolean(TAG_RITUAL_FAMILIAR_PARROT);
    }

    private static void applyReleasedOwner(Entity entity, EntityPlayer player) {
        if (entity instanceof IFamiliar) {
            ((IFamiliar) entity).setFamiliarOwner(player);
            return;
        }
        if (!(entity instanceof EntityTameable)) {
            return;
        }
        EntityTameable tameable = (EntityTameable) entity;
        tameable.setTamed(true);
        tameable.setOwnerId(player.getUniqueID());
    }

    private static String getFamiliarDisplayName(EntityLivingBase familiar) {
        return familiar.hasCustomName() ? familiar.getCustomNameTag() : familiar.getDisplayName().getUnformattedText();
    }

    private static BlockPos getSpawnPos(World world, BlockPos pos, EnumFacing facing) {
        EnumFacing side = facing == null ? EnumFacing.UP : facing;
        IBlockState state = world.getBlockState(pos);
        AxisAlignedBB collision = state.getBlock().getCollisionBoundingBox(state, world, pos);
        return collision == null || collision == Block.NULL_AABB || state.getBlock().isReplaceable(world, pos)
                ? pos
                : pos.offset(side);
    }
}
