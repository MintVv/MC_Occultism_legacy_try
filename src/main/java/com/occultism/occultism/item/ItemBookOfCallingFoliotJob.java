package com.occultism.occultism.item;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.occultism.occultism.entity.EntityFoliotJob;
import com.occultism.occultism.Occultism;
import com.occultism.occultism.gui.ModGuiHandler;
import com.occultism.occultism.spirit.MachineReference;
import com.occultism.occultism.spirit.SpiritJobType;
import com.occultism.occultism.spirit.SpiritTier;
import com.occultism.occultism.tileentity.TileEntityStorageController;
import com.occultism.occultism.util.InventoryAccess;
import com.occultism.occultism.util.ItemNBTUtil;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;

public class ItemBookOfCallingFoliotJob extends Item {

    private static final String TAG_STORED_SPIRIT = "StoredSpiritEntity";
    private static final String TAG_STORED_SPIRIT_ID = "StoredSpiritEntityId";
    private static final String TAG_STORED_SPIRIT_NAME = "StoredSpiritEntityName";
    private static final String LANG_PREFIX = "item.occultism.book_of_calling";

    private final SpiritJobType jobType;
    private final SpiritTier spiritTier;

    public ItemBookOfCallingFoliotJob(SpiritJobType jobType) {
        this(jobType, SpiritTier.FOLIOT);
    }

    public ItemBookOfCallingFoliotJob(SpiritJobType jobType, SpiritTier spiritTier) {
        this.jobType = jobType;
        this.spiritTier = spiritTier == null ? SpiritTier.FOLIOT : spiritTier;
    }

    public SpiritJobType getJobType() {
        return this.jobType;
    }

    public SpiritTier getSpiritTier() {
        return this.spiritTier;
    }

    public boolean bindToSpirit(ItemStack stack, EntityFoliotJob spirit) {
        if (stack.isEmpty() || spirit == null || !this.matchesSpirit(spirit)) {
            return false;
        }

        ItemNBTUtil.setSpiritEntityUUID(stack, spirit.getUniqueID());
        this.applyStoredConfigurationToSpirit(stack, spirit);
        String spiritName = getSpiritDisplayName(spirit);
        if (!spiritName.isEmpty() && !ItemNBTUtil.hasBoundSpiritName(stack)) {
            ItemNBTUtil.setBoundSpiritName(stack, spiritName);
        } else if (spiritName.isEmpty() && ItemNBTUtil.hasBoundSpiritName(stack)) {
            setSpiritName(spirit, ItemNBTUtil.getBoundSpiritName(stack));
        }
        return true;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack held = playerIn.getHeldItem(handIn);
        if (handIn != EnumHand.MAIN_HAND || hasStoredSpirit(held) || playerIn.isSneaking()) {
            return new ActionResult<>(EnumActionResult.PASS, held);
        }
        if (!worldIn.isRemote) {
            playerIn.openGui(Occultism.INSTANCE, ModGuiHandler.BOOK_OF_CALLING, worldIn,
                    playerIn.inventory.currentItem, 0, 0);
        }
        playerIn.swingArm(handIn);
        return new ActionResult<>(EnumActionResult.SUCCESS, held);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand,
            EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        if (hand != EnumHand.MAIN_HAND || !hasStoredSpirit(stack)) {
            if (hand == EnumHand.MAIN_HAND && player.isSneaking() && !hasStoredSpirit(stack)) {
                return this.configurePosition(stack, player, worldIn, pos, facing);
            }
            if (hand == EnumHand.MAIN_HAND && !player.isSneaking() && !hasStoredSpirit(stack)) {
                if (!worldIn.isRemote) {
                    player.openGui(Occultism.INSTANCE, ModGuiHandler.BOOK_OF_CALLING, worldIn,
                            player.inventory.currentItem, 0, 0);
                }
                player.swingArm(hand);
                return EnumActionResult.SUCCESS;
            }
            return EnumActionResult.PASS;
        }
        if (worldIn.isRemote) {
            return EnumActionResult.SUCCESS;
        }

        NBTTagCompound storedData = getStoredSpiritData(stack);
        String storedId = getStoredSpiritId(stack);
        String storedName = getStoredSpiritName(stack);
        Entity entity = createStoredSpirit(stack, worldIn);
        if (!(entity instanceof EntityFoliotJob)) {
            player.sendStatusMessage(new TextComponentTranslation(LANG_PREFIX + ".message.release_failed"), true);
            return EnumActionResult.FAIL;
        }

        EntityFoliotJob spirit = (EntityFoliotJob) entity;
        if (!this.matchesSpirit(spirit)) {
            player.sendStatusMessage(new TextComponentTranslation(LANG_PREFIX + ".message.wrong_job"), true);
            return EnumActionResult.FAIL;
        }

        clearStoredSpirit(stack);
        BlockPos spawnPos = getSpawnPos(worldIn, pos, facing);
        float yaw = player.rotationYaw + 180.0F;
        spirit.dimension = worldIn.provider.getDimension();
        spirit.setPositionAndRotation(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, yaw, 0.0F);
        spirit.rotationYaw = yaw;
        spirit.rotationPitch = 0.0F;
        spirit.setRotationYawHead(yaw);
        spirit.setRenderYawOffset(yaw);
        this.applyBookNameToSpirit(stack, spirit);
        this.applyStoredConfigurationToSpirit(stack, spirit);

        if (!worldIn.spawnEntity(spirit)) {
            setStoredSpirit(stack, storedData, storedId, storedName);
            player.sendStatusMessage(new TextComponentTranslation(LANG_PREFIX + ".message.release_failed"), true);
            return EnumActionResult.FAIL;
        }

        ItemNBTUtil.setSpiritEntityUUID(stack, spirit.getUniqueID());
        player.swingArm(hand);
        worldIn.playSound(null, spawnPos, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.PLAYERS, 0.75F, 1.0F);
        player.sendStatusMessage(new TextComponentTranslation(LANG_PREFIX + ".message.released"), true);
        return EnumActionResult.SUCCESS;
    }

    @Override
    public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer playerIn, EntityLivingBase target,
            EnumHand hand) {
        return this.tryInteractWithSpirit(stack, playerIn, target, hand) == EnumActionResult.SUCCESS;
    }

    public EnumActionResult tryInteractWithSpirit(ItemStack stack, EntityPlayer player, Entity target, EnumHand hand) {
        if (hand != EnumHand.MAIN_HAND || !(target instanceof EntityFoliotJob)) {
            return EnumActionResult.PASS;
        }
        EntityFoliotJob spirit = (EntityFoliotJob) target;
        if (!this.matchesSpirit(spirit)) {
            if (!player.world.isRemote) {
                player.sendStatusMessage(new TextComponentTranslation(LANG_PREFIX + ".message.wrong_job"), true);
            }
            return EnumActionResult.FAIL;
        }
        if (hasStoredSpirit(stack)) {
            if (!player.world.isRemote) {
                player.sendStatusMessage(new TextComponentTranslation(LANG_PREFIX + ".message.already_stored"), true);
            }
            return EnumActionResult.FAIL;
        }
        if (player.world.isRemote) {
            return EnumActionResult.SUCCESS;
        }

        UUID linkedUuid = ItemNBTUtil.getSpiritEntityUUID(stack);
        if (linkedUuid == null) {
            if (!player.isSneaking()) {
                player.sendStatusMessage(new TextComponentTranslation(LANG_PREFIX + ".message_target_uuid_no_match"), true);
                return EnumActionResult.FAIL;
            }
            this.bindToSpirit(stack, spirit);
            player.swingArm(hand);
            player.sendStatusMessage(new TextComponentTranslation(LANG_PREFIX + ".message.linked",
                    this.getDisplayNameForMessage(stack, spirit)), true);
            player.world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP,
                    SoundCategory.PLAYERS, 0.6F, 1.4F);
            return EnumActionResult.SUCCESS;
        }

        if (!linkedUuid.equals(spirit.getUniqueID()) && player.isSneaking()) {
            this.bindToSpirit(stack, spirit);
            player.swingArm(hand);
            player.sendStatusMessage(new TextComponentTranslation(LANG_PREFIX + ".message.linked",
                    this.getDisplayNameForMessage(stack, spirit)), true);
            player.world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP,
                    SoundCategory.PLAYERS, 0.6F, 1.4F);
            return EnumActionResult.SUCCESS;
        }

        if (!linkedUuid.equals(spirit.getUniqueID())) {
            player.sendStatusMessage(new TextComponentTranslation(LANG_PREFIX + ".message_target_uuid_no_match"), true);
            return EnumActionResult.FAIL;
        }

        if (!this.storeSpirit(stack, spirit)) {
            player.sendStatusMessage(new TextComponentTranslation(LANG_PREFIX + ".message.store_failed"), true);
            return EnumActionResult.FAIL;
        }

        spirit.setDead();
        player.swingArm(hand);
        player.setHeldItem(hand, stack);
        player.world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT,
                SoundCategory.PLAYERS, 0.6F, 1.2F);
        player.sendStatusMessage(new TextComponentTranslation(LANG_PREFIX + ".message.stored",
                this.getDisplayNameForMessage(stack, spirit)), true);
        return EnumActionResult.SUCCESS;
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
        tooltip.add(I18n.format("item.occultism.bound_spirit.tooltip", name));

        if (hasStoredSpirit(stack)) {
            String storedName = getStoredSpiritName(stack);
            tooltip.add(I18n.format(LANG_PREFIX + ".tooltip.stored",
                    storedName.isEmpty() ? I18n.format("item.occultism.bound_spirit.unknown") : storedName));
        } else if (ItemNBTUtil.hasSpiritEntityUUID(stack)) {
            tooltip.add(I18n.format(LANG_PREFIX + ".tooltip.linked"));
        } else {
            tooltip.add(I18n.format(LANG_PREFIX + ".tooltip.unlinked"));
        }
        BookOfCallingMode mode = this.getCurrentMode(stack);
        tooltip.add(I18n.format(LANG_PREFIX + ".tooltip.mode",
                I18n.format(mode.getTranslationKey())));
        tooltip.add(I18n.format(LANG_PREFIX + ".tooltip.work_area_size",
                I18n.format(this.getCurrentWorkAreaSize(stack).getTranslationKey())));
        this.addPositionTooltip(tooltip, LANG_PREFIX + ".tooltip.work_area", ItemNBTUtil.getWorkAreaPosition(stack));
        this.addPositionTooltip(tooltip, LANG_PREFIX + ".tooltip.deposit", ItemNBTUtil.getDepositPosition(stack));
        this.addPositionTooltip(tooltip, LANG_PREFIX + ".tooltip.extract", ItemNBTUtil.getExtractPosition(stack));
        tooltip.add(I18n.format(LANG_PREFIX + ".tooltip.controls"));
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        return ItemNBTUtil.hasSpiritEntityUUID(stack) || hasStoredSpirit(stack);
    }

    private boolean storeSpirit(ItemStack stack, EntityFoliotJob spirit) {
        NBTTagCompound entityData = new NBTTagCompound();
        if (!spirit.writeToNBTOptional(entityData)) {
            return false;
        }
        ResourceLocation id = EntityList.getKey(spirit);
        if (id == null) {
            return false;
        }

        entityData.setString("id", id.toString());
        entityData.removeTag("Pos");
        entityData.removeTag("Motion");
        entityData.removeTag("Rotation");
        this.syncConfigurationFromSpirit(stack, spirit);
        setStoredSpirit(stack, entityData, id.toString(), this.getDisplayNameForMessage(stack, spirit));
        ItemNBTUtil.setSpiritEntityUUID(stack, spirit.getUniqueID());
        String spiritName = getSpiritDisplayName(spirit);
        if (!spiritName.isEmpty()) {
            ItemNBTUtil.setBoundSpiritName(stack, spiritName);
        }
        return true;
    }

    private void applyBookNameToSpirit(ItemStack stack, EntityFoliotJob spirit) {
        String name = ItemNBTUtil.getBoundSpiritName(stack);
        if (!name.isEmpty()) {
            setSpiritName(spirit, name);
        }
    }

    private EnumActionResult configurePosition(ItemStack stack, EntityPlayer player, World world, BlockPos pos,
            EnumFacing facing) {
        if (world.isRemote) {
            return EnumActionResult.SUCCESS;
        }

        BookOfCallingMode mode = this.getCurrentMode(stack);
        TileEntity tile = world.getTileEntity(pos);
        if (this.jobType == SpiritJobType.MANAGE_MACHINE) {
            return this.configureManageMachinePosition(stack, player, world, pos, facing, mode);
        }
        if ((mode == BookOfCallingMode.SET_DEPOSIT || mode == BookOfCallingMode.SET_EXTRACT)
                && !InventoryAccess.isInventory(tile)) {
            player.sendStatusMessage(new TextComponentTranslation(LANG_PREFIX + ".message.not_inventory"), true);
            return EnumActionResult.FAIL;
        }
        if (mode == BookOfCallingMode.SET_EXTRACT && !this.supportsMode(BookOfCallingMode.SET_EXTRACT)) {
            player.sendStatusMessage(new TextComponentTranslation(LANG_PREFIX + ".message.extract_not_supported"), true);
            return EnumActionResult.FAIL;
        }

        if (mode == BookOfCallingMode.SET_BASE) {
            ItemNBTUtil.setWorkAreaPosition(stack, pos);
        } else if (mode == BookOfCallingMode.SET_DEPOSIT) {
            ItemNBTUtil.setDepositPosition(stack, pos);
        } else {
            ItemNBTUtil.setExtractPosition(stack, pos);
        }

        EntityFoliotJob spirit = this.findLinkedSpirit(stack, world);
        if (spirit != null) {
            this.applyStoredConfigurationToSpirit(stack, spirit);
        }

        player.swingArm(EnumHand.MAIN_HAND);
        player.sendStatusMessage(new TextComponentTranslation(LANG_PREFIX + ".message.position_set",
                new TextComponentTranslation(mode.getTranslationKey()), formatBlockPos(pos)), true);
        world.playSound(null, pos, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.PLAYERS, 0.5F, 1.4F);
        return EnumActionResult.SUCCESS;
    }

    private EnumActionResult configureManageMachinePosition(ItemStack stack, EntityPlayer player, World world,
            BlockPos pos, EnumFacing facing, BookOfCallingMode mode) {
        if (world.isRemote) {
            return EnumActionResult.SUCCESS;
        }
        EntityFoliotJob spirit = this.findLinkedSpirit(stack, world);
        if (spirit == null || spirit.getJobType() != SpiritJobType.MANAGE_MACHINE) {
            player.sendStatusMessage(new TextComponentTranslation(LANG_PREFIX + ".message.spirit_not_found"), true);
            return EnumActionResult.FAIL;
        }
        TileEntity tile = world.getTileEntity(pos);
        int dimension = world.provider.getDimension();
        String registry = tile == null || tile.getBlockType().getRegistryName() == null ? ""
                : tile.getBlockType().getRegistryName().toString();
        if (mode == BookOfCallingMode.SET_STORAGE_CONTROLLER) {
            if (!(tile instanceof TileEntityStorageController)) {
                player.sendStatusMessage(new TextComponentTranslation(LANG_PREFIX + ".message.not_storage_controller"),
                        true);
                return EnumActionResult.FAIL;
            }
            ItemNBTUtil.setStorageControllerPosition(stack, pos, dimension);
            spirit.setStorageControllerPos(pos, dimension);
        } else if (mode == BookOfCallingMode.SET_MANAGED_MACHINE) {
            if (!hasItemHandler(tile, facing)) {
                player.sendStatusMessage(new TextComponentTranslation(LANG_PREFIX + ".message.not_inventory"), true);
                return EnumActionResult.FAIL;
            }
            MachineReference reference = MachineReference.from(tile, facing);
            reference.setInsertPosition(pos, dimension, facing, registry);
            // A new machine starts with its own output side. A separate extract
            // point can be configured afterwards with SET_EXTRACT.
            reference.setExtractPosition(pos, dimension, EnumFacing.DOWN, registry);
            ItemNBTUtil.setManagedMachine(stack, reference);
            spirit.setManagedMachine(reference);
        } else if (mode == BookOfCallingMode.SET_EXTRACT) {
            MachineReference reference = ItemNBTUtil.getManagedMachine(stack);
            if (reference == null || !hasItemHandler(tile, facing)) {
                player.sendStatusMessage(new TextComponentTranslation(LANG_PREFIX + ".message.extract_not_supported"),
                        true);
                return EnumActionResult.FAIL;
            }
            reference.setExtractPosition(pos, dimension, facing, registry);
            ItemNBTUtil.setManagedMachine(stack, reference);
            spirit.setManagedMachine(reference);
        } else {
            return EnumActionResult.FAIL;
        }
        this.applyStoredConfigurationToSpirit(stack, spirit);
        player.swingArm(EnumHand.MAIN_HAND);
        player.sendStatusMessage(new TextComponentTranslation(LANG_PREFIX + ".message.position_set",
                new TextComponentTranslation(mode.getTranslationKey()), formatBlockPos(pos)), true);
        return EnumActionResult.SUCCESS;
    }

    private static boolean hasItemHandler(@Nullable TileEntity tile, @Nullable EnumFacing facing) {
        return tile != null && tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing)
                && tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing) != null;
    }

    private void applyStoredConfigurationToSpirit(ItemStack stack, EntityFoliotJob spirit) {
        spirit.setWorkAreaPos(ItemNBTUtil.getWorkAreaPosition(stack));
        spirit.setWorkAreaSize(this.getCurrentWorkAreaSize(stack));
        spirit.setDepositPos(ItemNBTUtil.getDepositPosition(stack));
        spirit.setExtractPos(ItemNBTUtil.getExtractPosition(stack));
        spirit.setStorageControllerPos(ItemNBTUtil.getStorageControllerPosition(stack),
                ItemNBTUtil.getStorageControllerDimension(stack));
        spirit.setManagedMachine(ItemNBTUtil.getManagedMachine(stack));
    }

    private void syncConfigurationFromSpirit(ItemStack stack, EntityFoliotJob spirit) {
        ItemNBTUtil.setBookOfCallingWorkAreaSize(stack, spirit.getWorkAreaSize().ordinal());
        if (spirit.getWorkAreaPos() != null) {
            ItemNBTUtil.setWorkAreaPosition(stack, spirit.getWorkAreaPos());
        }
        if (spirit.getDepositPos() != null) {
            ItemNBTUtil.setDepositPosition(stack, spirit.getDepositPos());
        }
        if (spirit.getExtractPos() != null) {
            ItemNBTUtil.setExtractPosition(stack, spirit.getExtractPos());
        }
        if (spirit.getStorageControllerPos() != null) {
            ItemNBTUtil.setStorageControllerPosition(stack, spirit.getStorageControllerPos(),
                    spirit.getStorageControllerDimension());
        }
        if (spirit.getManagedMachine() != null) {
            ItemNBTUtil.setManagedMachine(stack, spirit.getManagedMachine());
        }
    }

    @Nullable
    private EntityFoliotJob findLinkedSpirit(ItemStack stack, World world) {
        UUID uuid = ItemNBTUtil.getSpiritEntityUUID(stack);
        if (uuid == null) {
            return null;
        }
        Entity entity = world instanceof net.minecraft.world.WorldServer
                ? ((net.minecraft.world.WorldServer) world).getEntityFromUuid(uuid)
                : null;
        if (entity instanceof EntityFoliotJob && this.matchesSpirit((EntityFoliotJob) entity)) {
            return (EntityFoliotJob) entity;
        }
        return null;
    }

    private boolean matchesSpirit(EntityFoliotJob spirit) {
        return spirit.getJobType() == this.jobType && spirit.getTier() == this.spiritTier;
    }

    public BookOfCallingMode getCurrentMode(ItemStack stack) {
        BookOfCallingMode mode = BookOfCallingMode.byOrdinal(ItemNBTUtil.getBookOfCallingMode(stack));
        if (!this.supportsMode(mode)) {
            mode = this.getAvailableModes()[0];
            ItemNBTUtil.setBookOfCallingMode(stack, mode.ordinal());
        }
        return mode;
    }

    public WorkAreaSize getCurrentWorkAreaSize(ItemStack stack) {
        WorkAreaSize size = WorkAreaSize.byOrdinal(ItemNBTUtil.getBookOfCallingWorkAreaSize(stack));
        ItemNBTUtil.setBookOfCallingWorkAreaSize(stack, size.ordinal());
        return size;
    }

    public BookOfCallingMode[] getAvailableModes() {
        return BookOfCallingMode.getModesForJob(this.jobType);
    }

    public boolean supportsMode(BookOfCallingMode mode) {
        for (BookOfCallingMode availableMode : this.getAvailableModes()) {
            if (availableMode == mode) {
                return true;
            }
        }
        return false;
    }

    public void setBookConfiguration(ItemStack stack, BookOfCallingMode mode, WorkAreaSize workAreaSize) {
        if (!this.supportsMode(mode)) {
            mode = this.getAvailableModes()[0];
        }
        ItemNBTUtil.setBookOfCallingMode(stack, mode.ordinal());
        if (mode.hasSize() && workAreaSize != null) {
            ItemNBTUtil.setBookOfCallingWorkAreaSize(stack, workAreaSize.ordinal());
        }
    }

    public void setBookConfiguration(ItemStack stack, BookOfCallingMode mode, WorkAreaSize workAreaSize,
            @Nullable World world) {
        this.setBookConfiguration(stack, mode, workAreaSize);
        if (world == null) {
            return;
        }
        EntityFoliotJob spirit = this.findLinkedSpirit(stack, world);
        if (spirit != null) {
            this.applyStoredConfigurationToSpirit(stack, spirit);
        }
    }

    public boolean supportsExtractMode() {
        return this.jobType == SpiritJobType.TRANSPORT_ITEMS || this.jobType == SpiritJobType.SMELTER
                || this.jobType == SpiritJobType.CRUSHER || this.jobType == SpiritJobType.CRYSTALLIZER
                || this.jobType == SpiritJobType.TRADER_OTHERSTONE || this.jobType == SpiritJobType.TRADER_OTHERROCK
                || this.jobType == SpiritJobType.TRADER_SAPLING || this.jobType == SpiritJobType.MANAGE_MACHINE;
    }

    @SideOnly(Side.CLIENT)
    private void addPositionTooltip(List<String> tooltip, String key, @Nullable BlockPos pos) {
        if (pos != null) {
            tooltip.add(I18n.format(key, formatBlockPos(pos)));
        }
    }

    private static String formatBlockPos(BlockPos pos) {
        return pos.getX() + " " + pos.getY() + " " + pos.getZ();
    }

    private String getDisplayNameForMessage(ItemStack stack, EntityFoliotJob spirit) {
        String name = ItemNBTUtil.getBoundSpiritName(stack);
        if (!name.isEmpty()) {
            return name;
        }
        name = getSpiritDisplayName(spirit);
        if (!name.isEmpty()) {
            return name;
        }
        return spirit.getDisplayName().getUnformattedText();
    }

    private static String getSpiritDisplayName(EntityFoliotJob spirit) {
        return spirit.hasCustomName() ? spirit.getCustomNameTag() : "";
    }

    private static void setSpiritName(EntityFoliotJob spirit, String name) {
        if (name == null || name.isEmpty()) {
            return;
        }
        spirit.setCustomNameTag(name);
        spirit.setAlwaysRenderNameTag(true);
    }

    public static boolean hasStoredSpirit(ItemStack stack) {
        return !stack.isEmpty() && stack.hasTagCompound() && stack.getTagCompound().hasKey(TAG_STORED_SPIRIT, 10);
    }

    private static void setStoredSpirit(ItemStack stack, @Nullable NBTTagCompound entityData, String id, String name) {
        if (stack.isEmpty() || entityData == null || entityData.isEmpty()) {
            return;
        }
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
        }
        tag.setTag(TAG_STORED_SPIRIT, entityData.copy());
        tag.setString(TAG_STORED_SPIRIT_ID, id == null ? "" : id);
        tag.setString(TAG_STORED_SPIRIT_NAME, name == null ? "" : name);
        stack.setTagCompound(tag);
    }

    private static void clearStoredSpirit(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTagCompound()) {
            return;
        }
        NBTTagCompound tag = stack.getTagCompound();
        tag.removeTag(TAG_STORED_SPIRIT);
        tag.removeTag(TAG_STORED_SPIRIT_ID);
        tag.removeTag(TAG_STORED_SPIRIT_NAME);
        if (tag.isEmpty()) {
            stack.setTagCompound(null);
        }
    }

    @Nullable
    private static NBTTagCompound getStoredSpiritData(ItemStack stack) {
        if (!hasStoredSpirit(stack)) {
            return null;
        }
        return stack.getTagCompound().getCompoundTag(TAG_STORED_SPIRIT).copy();
    }

    private static String getStoredSpiritId(ItemStack stack) {
        return stack.hasTagCompound() ? stack.getTagCompound().getString(TAG_STORED_SPIRIT_ID) : "";
    }

    private static String getStoredSpiritName(ItemStack stack) {
        return stack.hasTagCompound() ? stack.getTagCompound().getString(TAG_STORED_SPIRIT_NAME) : "";
    }

    @Nullable
    private static Entity createStoredSpirit(ItemStack stack, World world) {
        NBTTagCompound entityData = getStoredSpiritData(stack);
        if (entityData == null) {
            return null;
        }
        String id = getStoredSpiritId(stack);
        if (!id.isEmpty()) {
            entityData.setString("id", id);
        }
        entityData.removeTag("Pos");
        entityData.removeTag("Motion");
        entityData.removeTag("Rotation");
        return EntityList.createEntityFromNBT(entityData, world);
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
