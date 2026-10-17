package com.occultism.occultism.entity;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;

import com.occultism.occultism.Occultism;
import com.occultism.occultism.config.ModConfig;
import com.occultism.occultism.gui.ModGuiHandler;
import com.occultism.occultism.registry.ModBlocks;
import com.occultism.occultism.registry.ModItems;
import com.occultism.occultism.item.ItemSoulShard;
import com.occultism.occultism.item.WorkAreaSize;
import com.occultism.occultism.spirit.DepositOrder;
import com.occultism.occultism.spirit.MachineReference;
import com.occultism.occultism.spirit.SpiritJobRecipe;
import com.occultism.occultism.spirit.SpiritTradeRecipeRegistry;
import com.occultism.occultism.spirit.WorldEffectJob;
import com.occultism.occultism.spirit.SpiritJobType;
import com.occultism.occultism.spirit.SpiritTier;
import com.occultism.occultism.tileentity.TileEntityStorageController;
import com.occultism.occultism.util.InventoryAccess;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockFarmland;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockLog;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.oredict.OreDictionary;

public class EntityFoliotJob extends EntityCreature {

    public static final int MAX_FILTER_SLOTS = 14;
    public static final int MAX_TAG_FILTER_LENGTH = 90;

    private static final DataParameter<Integer> JOB_TYPE =
            EntityDataManager.createKey(EntityFoliotJob.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> TIER =
            EntityDataManager.createKey(EntityFoliotJob.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> SPIRIT_AGE =
            EntityDataManager.createKey(EntityFoliotJob.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> SPIRIT_MAX_AGE =
            EntityDataManager.createKey(EntityFoliotJob.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> FILTER_BLACKLIST =
            EntityDataManager.createKey(EntityFoliotJob.class, DataSerializers.BOOLEAN);
    private static final DataParameter<NBTTagCompound> FILTER_ITEMS =
            EntityDataManager.createKey(EntityFoliotJob.class, DataSerializers.COMPOUND_TAG);
    private static final DataParameter<String> TAG_FILTER =
            EntityDataManager.createKey(EntityFoliotJob.class, DataSerializers.STRING);

    private static final String DROP_JOB_KEY = "OccultismFoliotJobDrop";
    private static final String DROP_TICK_KEY = "OccultismFoliotJobDropTick";
    private static final int PROCESS_TIME = 100;
    private static final int RESULT_PICKUP_DELAY = 100;
    private static final int SEARCH_INTERVAL = 10;
    private static final int MANAGE_MACHINE_INTERVAL = 10;
    private static final int MANAGE_MACHINE_TRANSFER_SIZE = 16;
    private static final int MANAGE_MACHINE_MAX_ORDERS = 64;
    private static final double MANAGE_MACHINE_ACCESS_DISTANCE_SQ = 4.0D;
    private static final int SPIRIT_AGE_INTERVAL = 20;
    private static final int LUMBERJACK_MAX_LOGS = 96;
    private static final EnumFacing[] HORIZONTAL_DIRECTIONS =
            { EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST };
    private static final int TRANSPORTER_STACK_SIZE = 16;
    private static final double PICKUP_RANGE_SQ = 2.25D;
    private static final int GAMBLER_TOTAL_WEIGHT = 55;

    private int workTimer;
    private SpiritJobRecipe currentRecipe;
    private String spiritTradeId;
    private int tradeTimeOverride = -1;
    private int tradeLimitOverride = -1;
    private SpiritTradeRecipeRegistry.Recipe currentTrade;
    private WorldEffectJob worldEffectJob;
    private UUID worldEffectCaster;
    private BlockPos transporterSourcePos;
    private BlockPos transporterTargetPos;
    private BlockPos workAreaPos;
    private BlockPos depositPos;
    private BlockPos extractPos;
    private BlockPos storageControllerPos;
    private int storageControllerDimension;
    private MachineReference managedMachine;
    private DepositOrder currentDepositOrder;
    private Queue<DepositOrder> depositOrderQueue = new ArrayDeque<>();
    private WorkAreaSize workAreaSize = WorkAreaSize.SMALL;
    private boolean loadingFilterItems;
    private final ItemStackHandler filterItems = new ItemStackHandler(MAX_FILTER_SLOTS) {
        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        public void setStackInSlot(int slot, ItemStack stack) {
            ItemStack normalized = stack == null || stack.isEmpty()
                    ? ItemStack.EMPTY : stack.copy();
            if (!normalized.isEmpty()) {
                normalized.setCount(1);
            }
            super.setStackInSlot(slot, normalized);
        }

        @Override
        protected void onContentsChanged(int slot) {
            if (!EntityFoliotJob.this.loadingFilterItems
                    && (EntityFoliotJob.this.world == null || !EntityFoliotJob.this.world.isRemote)) {
                EntityFoliotJob.this.syncFilterItems();
                EntityFoliotJob.this.transporterSourcePos = null;
            }
        }
    };
    private boolean droppedSoulShard;
    private int lastDebugMessageTick = -1000;
    private String lastDebugMessage = "";

    /** Server-side state for the bounded, same-dimension machine route. */
    private ManageMachineState manageMachineState = ManageMachineState.IDLE;

    private enum ManageMachineState {
        IDLE,
        NAVIGATING_TO_MACHINE_OUTPUT,
        NAVIGATING_TO_STORAGE_OUTPUT,
        NAVIGATING_TO_STORAGE_INPUT,
        NAVIGATING_TO_MACHINE_INPUT,
        BLOCKED_DIMENSION,
        BLOCKED_ENDPOINT
    }

    public EntityFoliotJob(World worldIn) {
        super(worldIn);
        this.setSize(0.55F, 1.1F);
        this.setCanPickUpLoot(false);
        this.enablePersistence();
        this.setDropChance(EntityEquipmentSlot.MAINHAND, 0.0F);
    }

    public EntityFoliotJob(World worldIn, SpiritJobType jobType) {
        this(worldIn);
        this.setJobType(jobType);
    }

    public EntityFoliotJob(World worldIn, SpiritJobType jobType, SpiritTier tier) {
        this(worldIn, jobType);
        this.setTier(tier);
    }

    /** Creates the dedicated high-tier job entity while retaining the legacy fallback. */
    public static EntityFoliotJob createForTier(World worldIn, SpiritJobType jobType, SpiritTier tier) {
        if (tier == SpiritTier.DJINNI) {
            return new EntityDjinniJob(worldIn, jobType);
        }
        if (tier == SpiritTier.AFRIT) {
            return new EntityAfritJob(worldIn, jobType);
        }
        if (tier == SpiritTier.MARID) {
            return new EntityMaridJob(worldIn, jobType);
        }
        return new EntityFoliotJob(worldIn, jobType, tier);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(JOB_TYPE, Integer.valueOf(SpiritJobType.CRUSHER.ordinal()));
        this.dataManager.register(TIER, Integer.valueOf(SpiritTier.FOLIOT.ordinal()));
        this.dataManager.register(SPIRIT_AGE, Integer.valueOf(0));
        this.dataManager.register(SPIRIT_MAX_AGE, Integer.valueOf(-1));
        this.dataManager.register(FILTER_BLACKLIST, Boolean.FALSE);
        this.dataManager.register(FILTER_ITEMS, new NBTTagCompound());
        this.dataManager.register(TAG_FILTER, "");
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        if (this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE) == null) {
            this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
        }
        if (this.getEntityAttribute(SharedMonsterAttributes.ARMOR) == null) {
            this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ARMOR);
        }
        if (this.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS) == null) {
            this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS);
        }
        this.applyTierAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(16.0D);
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (this.world == null) {
            return;
        }
        if (this.world.isRemote) {
            this.spawnClientWorkParticles();
            return;
        }
        if (this.updateSpiritAge()) {
            return;
        }
        this.updateJob();
    }

    @Override
    protected boolean canDespawn() {
        return false;
    }

    @Override
    public boolean isEntityInvulnerable(DamageSource source) {
        return super.isEntityInvulnerable(source) || source == DamageSource.IN_WALL
                || source == DamageSource.FLY_INTO_WALL;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (this.isEntityInvulnerable(source)) {
            return false;
        }
        Entity trueSource = source == null ? null : source.getTrueSource();
        Entity immediateSource = source == null ? null : source.getImmediateSource();
        if (trueSource != null && !(trueSource instanceof EntityPlayer)
                && !(trueSource instanceof EntityArrow) && !(immediateSource instanceof EntityArrow)) {
            amount = (amount + 1.0F) / 2.0F;
        }
        return super.attackEntityFrom(source, amount);
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        if (player != null && player.isSneaking() && player.getHeldItem(hand).isEmpty() && this.isEntityAlive()) {
            if (!this.world.isRemote) {
                player.openGui(Occultism.INSTANCE, ModGuiHandler.SPIRIT, this.world, this.getEntityId(), 0, 0);
            }
            return true;
        }
        return super.processInteract(player, hand);
    }

    @Override
    public void onDeath(DamageSource cause) {
        this.unregisterFromStorageController();
        super.onDeath(cause);
        if (!this.world.isRemote && !this.droppedSoulShard) {
            this.dropHeldItem();
            this.dropSoulShard();
        }
    }

    public SpiritJobType getJobType() {
        int index = this.dataManager.get(JOB_TYPE).intValue();
        SpiritJobType[] values = SpiritJobType.values();
        if (index < 0 || index >= values.length) {
            return SpiritJobType.CRUSHER;
        }
        return values[index];
    }

    public void setJobType(SpiritJobType jobType) {
        if (this.getJobType() != jobType) {
            this.workTimer = 0;
            this.currentRecipe = null;
            this.spiritTradeId = null;
            this.tradeTimeOverride = -1;
            this.tradeLimitOverride = -1;
            this.currentTrade = null;
            this.worldEffectJob = jobType.isWorldEffectJob() ? new WorldEffectJob(jobType) : null;
            this.worldEffectCaster = null;
        }
        this.dataManager.set(JOB_TYPE, Integer.valueOf(jobType.ordinal()));
        this.updateEntitySizeForJob();
    }

    public void setWorldEffectCaster(@Nullable EntityPlayer player) {
        this.worldEffectCaster = player == null ? null : player.getUniqueID();
    }

    @Nullable
    public WorldEffectJob getWorldEffectJob() {
        if (this.getJobType().isWorldEffectJob() && this.worldEffectJob == null) {
            this.worldEffectJob = new WorldEffectJob(this.getJobType());
        }
        return this.worldEffectJob;
    }

    public String getSpiritTradeId() {
        return this.spiritTradeId == null
                ? SpiritTradeRecipeRegistry.getDefaultRecipeId(this.getJobType()) : this.spiritTradeId;
    }

    /** Unknown IDs remain selected and stop processing until the recipe becomes available. */
    public void setTradeRecipeId(String id) {
        String canonical = SpiritTradeRecipeRegistry.canonicalId(id);
        if (canonical == null || !SpiritTradeRecipeRegistry.isTradeJob(this.getJobType())) {
            throw new IllegalArgumentException("Invalid trade ID or non-trader job");
        }
        this.spiritTradeId = canonical;
        this.currentTrade = null;
        this.workTimer = 0;
    }

    @Nullable
    private SpiritTradeRecipeRegistry.Recipe getTradeRecipe() {
        return SpiritTradeRecipeRegistry.isTradeJob(this.getJobType())
                ? SpiritTradeRecipeRegistry.find(this.getSpiritTradeId()) : null;
    }

    public int getTimeToConvert() {
        SpiritTradeRecipeRegistry.Recipe recipe = this.getTradeRecipe();
        return this.tradeTimeOverride > 0 ? this.tradeTimeOverride
                : recipe == null ? 20 : recipe.getConversionSteps();
    }

    public void setTimeToConvert(int seconds) {
        if (seconds < 1 || seconds > SpiritTradeRecipeRegistry.MAX_CONVERSION_STEPS) {
            throw new IllegalArgumentException("Invalid trader conversion time");
        }
        this.tradeTimeOverride = seconds;
        this.workTimer = 0;
    }

    public void setMaxTradesPerRound(int count) {
        if (count < 1 || count > SpiritTradeRecipeRegistry.MAX_TRADES_PER_ROUND) {
            throw new IllegalArgumentException("Invalid trader batch size");
        }
        this.tradeLimitOverride = count;
    }

    public SpiritTier getTier() {
        int index = this.dataManager.get(TIER).intValue();
        SpiritTier[] values = SpiritTier.values();
        if (index < 0 || index >= values.length) {
            return SpiritTier.FOLIOT;
        }
        return values[index];
    }

    public void setTier(SpiritTier tier) {
        this.dataManager.set(TIER, Integer.valueOf((tier == null ? SpiritTier.FOLIOT : tier).ordinal()));
        this.applyTierAttributes();
        this.updateEntitySizeForJob();
    }

    protected void applyTierAttributes() {
        if (this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH) == null) {
            return;
        }
        SpiritTier tier = this.getTier();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(tier.getMaxHealth());
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(tier.getMovementSpeed());
        if (this.getHealth() > this.getMaxHealth()) {
            this.setHealth(this.getMaxHealth());
        }
    }

    /** Hook for dedicated job entities whose physical dimensions depend on the job. */
    protected void updateEntitySizeForJob() {
    }

    public void setSpiritMaxAge(int spiritMaxAge) {
        this.dataManager.set(SPIRIT_MAX_AGE, Integer.valueOf(spiritMaxAge > 0 ? spiritMaxAge : -1));
    }

    public int getSpiritMaxAge() {
        return this.dataManager.get(SPIRIT_MAX_AGE).intValue();
    }

    public int getSpiritAge() {
        return this.dataManager.get(SPIRIT_AGE).intValue();
    }

    public void setSpiritAge(int spiritAge) {
        this.dataManager.set(SPIRIT_AGE, Integer.valueOf(Math.max(0, spiritAge)));
    }

    public boolean supportsTransporterFilter() {
        return this.getJobType() == SpiritJobType.CLEANER
                || this.getJobType() == SpiritJobType.TRANSPORT_ITEMS;
    }

    public boolean isFilterBlacklist() {
        return this.dataManager.get(FILTER_BLACKLIST).booleanValue();
    }

    public void setFilterBlacklist(boolean blacklist) {
        this.dataManager.set(FILTER_BLACKLIST, Boolean.valueOf(blacklist));
        this.transporterSourcePos = null;
    }

    public String getTagFilter() {
        return this.dataManager.get(TAG_FILTER);
    }

    public void setTagFilter(@Nullable String tagFilter) {
        this.dataManager.set(TAG_FILTER, normalizeTagFilter(tagFilter));
        this.transporterSourcePos = null;
    }

    public ItemStackHandler getFilterItems() {
        return this.filterItems;
    }

    public boolean isAllowedByTransportFilter(ItemStack stack) {
        boolean matches = matchesItemFilter(stack) || matchesTagFilter(stack, this.getTagFilter());
        return this.isFilterBlacklist() ? !matches : matches;
    }

    private boolean matchesItemFilter(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        for (int slot = 0; slot < this.filterItems.getSlots(); slot++) {
            ItemStack filter = this.filterItems.getStackInSlot(slot);
            if (!filter.isEmpty() && ItemStack.areItemsEqual(filter, stack)) {
                return true;
            }
        }
        return false;
    }

    static boolean matchesTagFilter(ItemStack stack, @Nullable String tagFilter) {
        if (stack == null || stack.isEmpty() || tagFilter == null || tagFilter.isEmpty()) {
            return false;
        }
        for (String rawFilter : tagFilter.split(";")) {
            String filter = rawFilter.trim();
            if (filter.isEmpty()) {
                continue;
            }
            if (filter.regionMatches(true, 0, "item:", 0, 5)) {
                String itemFilter = filter.substring(5);
                if (stack.getItem().getRegistryName() != null
                        && FilenameUtils.wildcardMatch(stack.getItem().getRegistryName().toString(), itemFilter,
                                IOCase.INSENSITIVE)) {
                    return true;
                }
                continue;
            }
            if (filter.regionMatches(true, 0, "tag:", 0, 4)) {
                filter = filter.substring(4);
            }
            for (String alias : getOreDictionaryAliases(stack)) {
                if (FilenameUtils.wildcardMatch(alias, filter, IOCase.INSENSITIVE)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static List<String> getOreDictionaryAliases(ItemStack stack) {
        List<String> aliases = new ArrayList<>();
        for (int oreId : OreDictionary.getOreIDs(stack)) {
            String oreName = OreDictionary.getOreName(oreId);
            if (oreName == null || oreName.isEmpty()) {
                continue;
            }
            aliases.add(oreName);
            String[] parts = oreName.split("(?<=[a-z0-9])(?=[A-Z])");
            String category = pluralizeTagCategory(parts[0].toLowerCase(Locale.ROOT));
            StringBuilder path = new StringBuilder(category);
            if (parts.length > 1) {
                path.append('/');
                for (int index = 1; index < parts.length; index++) {
                    if (index > 1) {
                        path.append('_');
                    }
                    path.append(parts[index].toLowerCase(Locale.ROOT));
                }
            }
            aliases.add("c:" + category);
            aliases.add("forge:" + category);
            aliases.add("c:" + path);
            aliases.add("forge:" + path);
        }
        return aliases;
    }

    private static String pluralizeTagCategory(String category) {
        if ("ore".equals(category)) {
            return "ores";
        }
        if ("log".equals(category)) {
            return "logs";
        }
        if ("plank".equals(category)) {
            return "planks";
        }
        if ("block".equals(category)) {
            return "storage_blocks";
        }
        if (category.endsWith("s")) {
            return category;
        }
        return category + "s";
    }

    public static String normalizeTagFilter(@Nullable String value) {
        if (value == null) {
            return "";
        }
        String normalized = value.replace('\r', ' ').replace('\n', ' ').trim();
        return normalized.length() <= MAX_TAG_FILTER_LENGTH
                ? normalized : normalized.substring(0, MAX_TAG_FILTER_LENGTH);
    }

    private void syncFilterItems() {
        NBTTagCompound serialized = this.filterItems.serializeNBT();
        serialized.setInteger("Size", MAX_FILTER_SLOTS);
        this.dataManager.set(FILTER_ITEMS, serialized);
    }

    private void loadFilterItems(NBTTagCompound serialized) {
        NBTTagCompound bounded = serialized == null ? new NBTTagCompound() : serialized.copy();
        bounded.setInteger("Size", MAX_FILTER_SLOTS);
        this.loadingFilterItems = true;
        try {
            this.filterItems.deserializeNBT(bounded);
            for (int slot = 0; slot < this.filterItems.getSlots(); slot++) {
                ItemStack stack = this.filterItems.getStackInSlot(slot);
                if (!stack.isEmpty() && stack.getCount() != 1) {
                    ItemStack single = stack.copy();
                    single.setCount(1);
                    this.filterItems.setStackInSlot(slot, single);
                }
            }
        } finally {
            this.loadingFilterItems = false;
        }
        if (this.world != null && !this.world.isRemote) {
            this.syncFilterItems();
        }
    }

    private boolean updateSpiritAge() {
        if (this.getSpiritMaxAge() < 0) {
            return false;
        }
        if (this.ticksExisted % SPIRIT_AGE_INTERVAL != 0) {
            return false;
        }
        this.setSpiritAge(this.getSpiritAge() + 1);
        if (this.getSpiritAge() <= this.getSpiritMaxAge()) {
            return false;
        }
        this.debugAI("Spirit max age reached, dropping held item and despawning", this.getPosition(),
                EnumParticleTypes.SMOKE_NORMAL);
        this.dropHeldItem();
        this.setDead();
        return true;
    }

    public void setWorkAreaPos(@Nullable BlockPos workAreaPos) {
        this.workAreaPos = workAreaPos;
    }

    @Nullable
    public BlockPos getWorkAreaPos() {
        return this.workAreaPos;
    }

    public void setWorkAreaSize(WorkAreaSize workAreaSize) {
        this.workAreaSize = workAreaSize == null ? WorkAreaSize.SMALL : workAreaSize;
    }

    public WorkAreaSize getWorkAreaSize() {
        return this.workAreaSize;
    }

    private double getWorkRange() {
        return this.workAreaSize.getValue() / 2.0D;
    }

    private int getWorkRangeBlocks() {
        return (int) this.getWorkRange();
    }

    public void setDepositPos(@Nullable BlockPos depositPos) {
        this.depositPos = depositPos;
        if (depositPos != null && depositPos.equals(this.transporterSourcePos)) {
            this.transporterSourcePos = null;
        }
        this.transporterTargetPos = null;
    }

    @Nullable
    public BlockPos getDepositPos() {
        return this.depositPos;
    }

    public void setExtractPos(@Nullable BlockPos extractPos) {
        this.extractPos = extractPos;
        this.transporterSourcePos = null;
        if (extractPos != null && extractPos.equals(this.transporterTargetPos)) {
            this.transporterTargetPos = null;
        }
    }

    @Nullable
    public BlockPos getExtractPos() {
        return this.extractPos;
    }

    @Nullable
    public BlockPos getStorageControllerPos() {
        return this.storageControllerPos;
    }

    public int getStorageControllerDimension() {
        return this.storageControllerDimension;
    }

    public void setStorageControllerPos(@Nullable BlockPos position, int dimension) {
        boolean changed = this.storageControllerPos == null ? position != null
                : !this.storageControllerPos.equals(position) || this.storageControllerDimension != dimension;
        if (changed) {
            this.unregisterFromStorageController();
        }
        this.storageControllerPos = position;
        this.storageControllerDimension = dimension;
        if (changed) {
            this.clearDepositOrders();
            this.resetManageMachineRoute();
            this.registerWithStorageController();
        }
    }

    @Nullable
    public MachineReference getManagedMachine() {
        return this.managedMachine;
    }

    /** Returns true when the supplied reference identifies this spirit's route. */
    public boolean isMachineReferenceFor(@Nullable MachineReference reference) {
        if (reference == null || this.managedMachine == null) {
            return false;
        }
        return sameEndpoint(this.managedMachine.getInsertPosition(), reference.getInsertPosition())
                && this.managedMachine.getInsertDimension() == reference.getInsertDimension()
                && sameEndpoint(this.managedMachine.getExtractPosition(), reference.getExtractPosition())
                && this.managedMachine.getExtractDimension() == reference.getExtractDimension()
                && this.managedMachine.getInsertFacing() == reference.getInsertFacing()
                && this.managedMachine.getExtractFacing() == reference.getExtractFacing()
                && sameString(this.managedMachine.getInsertRegistryName(), reference.getInsertRegistryName())
                && sameString(this.managedMachine.getExtractRegistryName(), reference.getExtractRegistryName())
                && sameString(this.managedMachine.getCustomName(), reference.getCustomName());
    }

    private static boolean sameEndpoint(@Nullable BlockPos left, @Nullable BlockPos right) {
        return left == null ? right == null : left.equals(right);
    }

    private static boolean sameString(@Nullable String left, @Nullable String right) {
        return left == null ? right == null : left.equals(right);
    }

    public void setManagedMachine(@Nullable MachineReference reference) {
        this.unregisterFromStorageController();
        this.managedMachine = reference;
        this.clearDepositOrders();
        this.resetManageMachineRoute();
        this.registerWithStorageController();
    }

    /**
     * Upstream ManageMachineJob registers its machine with the selected storage
     * controller. The 1.12 port performs the same operation when both endpoint
     * references are available, without loading another dimension or chunk.
     */
    private void registerWithStorageController() {
        if (this.world == null || this.world.isRemote || this.storageControllerPos == null
                || this.managedMachine == null || !this.managedMachine.isStrictlyConfigured()
                || this.storageControllerDimension != this.world.provider.getDimension()
                || !this.world.isBlockLoaded(this.storageControllerPos)) {
            return;
        }
        TileEntity tile = this.world.getTileEntity(this.storageControllerPos);
        if (tile instanceof TileEntityStorageController) {
            ((TileEntityStorageController) tile).registerMachineManager(this.managedMachine, this.getUniqueID());
        }
    }

    private void unregisterFromStorageController() {
        if (this.world == null || this.world.isRemote || this.storageControllerPos == null
                || this.managedMachine == null || this.storageControllerDimension != this.world.provider.getDimension()
                || !this.world.isBlockLoaded(this.storageControllerPos)) {
            return;
        }
        TileEntity tile = this.world.getTileEntity(this.storageControllerPos);
        if (tile instanceof TileEntityStorageController) {
            ((TileEntityStorageController) tile).unregisterMachineManager(this.managedMachine, this.getUniqueID());
        }
    }

    /** Returns the in-flight machine input request, if any. */
    @Nullable
    public DepositOrder getCurrentDepositOrder() {
        return this.currentDepositOrder;
    }

    /** Returns the number of queued machine input requests. */
    public int getPendingDepositOrderCount() {
        return this.depositOrderQueue.size();
    }

    public void setCurrentDepositOrder(@Nullable DepositOrder order) {
        this.currentDepositOrder = order;
    }

    /** Adds a bounded request; invalid or completed requests are ignored. */
    public void addDepositOrder(@Nullable DepositOrder order) {
        if (order == null || order.isComplete() || this.depositOrderQueue.size() >= MANAGE_MACHINE_MAX_ORDERS) {
            return;
        }
        this.depositOrderQueue.add(order);
    }

    /** Spelling retained for parity with the upstream job API. */
    public void addDepsitOrder(@Nullable DepositOrder order) {
        this.addDepositOrder(order);
    }

    public void clearDepositOrders() {
        this.currentDepositOrder = null;
        this.depositOrderQueue.clear();
    }

    private void resetManageMachineRoute() {
        this.manageMachineState = ManageMachineState.IDLE;
        if (this.world != null && !this.world.isRemote) {
            this.getNavigator().clearPath();
        }
    }

    private void updateJob() {
        if (this.getJobType().isWorldEffectJob()) {
            this.updateWorldEffectJob();
            return;
        }
        if (this.getJobType() == SpiritJobType.MANAGE_MACHINE) {
            this.updateManageMachine();
            return;
        }
        if (this.getJobType() == SpiritJobType.CLEANER) {
            this.updateCleaner();
            return;
        }
        if (this.getJobType() == SpiritJobType.FARMER) {
            this.updateFarmer();
            return;
        }
        if (this.getJobType() == SpiritJobType.TRANSPORT_ITEMS) {
            this.updateTransporter();
            return;
        }
        if (this.getJobType() == SpiritJobType.LUMBERJACK) {
            this.updateLumberjack();
            return;
        }

        ItemStack held = this.getHeldItemMainhand();
        if (held.isEmpty()) {
            this.workTimer = 0;
            this.currentRecipe = null;
            if (this.ticksExisted % SEARCH_INTERVAL == 0) {
                this.pickupProcessingInput();
            }
            return;
        }

        SpiritJobRecipe recipe = this.findSpiritJobRecipe(held);
        ItemStack smeltingResult = ItemStack.EMPTY;
        if (recipe == null && this.getJobType() == SpiritJobType.SMELTER) {
            smeltingResult = this.findSmeltingResult(held);
        }
        ItemStack tradeResult = ItemStack.EMPTY;
        if (recipe == null && smeltingResult.isEmpty() && this.isTrader()) {
            tradeResult = this.findTradeResult(held);
        }
        if (recipe == null && smeltingResult.isEmpty() && tradeResult.isEmpty()) {
            if (SpiritTradeRecipeRegistry.isTradeJob(this.getJobType())) {
                // TraderJob keeps invalid/unavailable input in hand and resets conversion progress.
                this.workTimer = 0;
                this.currentTrade = this.getTradeRecipe();
                if (this.currentTrade != null && this.currentTrade.acceptsStackType(held)
                        && held.getCount() < this.currentTrade.getIngredients().size()
                        && this.ticksExisted % SEARCH_INTERVAL == 0) {
                    this.pickupProcessingInput();
                }
                return;
            }
            this.debugAI("Cannot process held item, dropping " + describeStack(held), this.getPosition(),
                    EnumParticleTypes.SMOKE_NORMAL);
            this.dropHeldItem();
            return;
        }

        boolean fixedTrade = SpiritTradeRecipeRegistry.isTradeJob(this.getJobType());
        if (fixedTrade && this.currentTrade != this.getTradeRecipe()) {
            // Replacing a recipe must not inherit progress accumulated for its previous input/output.
            this.currentTrade = this.getTradeRecipe();
            this.workTimer = 0;
        }
        if (recipe != this.currentRecipe) {
            this.currentRecipe = recipe;
            this.workTimer = 0;
            if (!fixedTrade) {
                this.playWorkSound();
            }
        } else if (!fixedTrade && recipe == null && this.workTimer == 0) {
            this.playWorkSound();
        }

        if (fixedTrade) {
            // TraderJob advances conversionTimer once per world second, independent of spawn time.
            if (this.world.getTotalWorldTime() % 20L == 0L) {
                this.workTimer += 20;
            }
            if (this.world.getTotalWorldTime() % 10L == 0L) {
                this.spawnServerWorkParticles();
            }
        } else {
            this.workTimer++;
            if (this.workTimer % 40 == 0) {
                this.playWorkSound();
            }
            if (this.workTimer % 10 == 0) {
                this.spawnServerWorkParticles();
            }
        }
        if (this.workTimer >= this.getProcessTime()) {
            if (this.getJobType() == SpiritJobType.SMELTER) {
                this.completeSmelting(smeltingResult, held);
            } else if (this.isTrader()) {
                this.completeTrade(tradeResult, held);
            } else {
                this.completeWork(recipe, held);
            }
        }
    }

    private void updateWorldEffectJob() {
        if (this.world == null || this.world.isRemote) {
            return;
        }
        WorldEffectJob job = this.getWorldEffectJob();
        boolean enabled = ModConfig.RITUALS.isEnabled(this.getJobType());
        boolean finished = job.tick(this.world.getWorldInfo(), enabled);
        if (!this.isSwingInProgress) {
            this.swingArm(EnumHand.MAIN_HAND);
        }
        if (this.world instanceof WorldServer && this.world.getTotalWorldTime() % 2L == 0L) {
            ((WorldServer) this.world).spawnParticle(
                    job.isTimeJob() ? EnumParticleTypes.PORTAL : EnumParticleTypes.SMOKE_NORMAL,
                    this.posX, this.posY + 0.5D, this.posZ, 3, 0.5D, 0.0D, 0.0D, 0.0D);
        }
        if (!finished) {
            return;
        }
        if (!enabled && this.worldEffectCaster != null && this.world.getMinecraftServer() != null) {
            EntityPlayer caster = this.world.getMinecraftServer().getPlayerList().getPlayerByUUID(this.worldEffectCaster);
            if (caster != null) {
                caster.sendMessage(new TextComponentTranslation("ritual.occultism.disabled"));
            }
        }
        if (this.world instanceof WorldServer) {
            for (int index = 0; index < 5; index++) {
                ((WorldServer) this.world).spawnParticle(
                        job.isTimeJob() ? EnumParticleTypes.PORTAL : EnumParticleTypes.SMOKE_LARGE,
                        this.posX + this.rand.nextGaussian(), this.posY + 0.5D + this.rand.nextGaussian(),
                        this.posZ + this.rand.nextGaussian(), 5, 0.0D, 0.0D, 0.0D, 0.0D);
            }
        }
        if (job.isTimeJob()) {
            // Beacon activation sounds were added after 1.12; use the local ritual completion sound.
            this.world.playSound(null, this.getPosition(), SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE,
                    SoundCategory.NEUTRAL, 1.0F, 1.0F);
        }
        // Successful one-shot jobs must not yield a shard that can replay the world change.
        this.dropHeldItem();
        this.droppedSoulShard = true;
        this.onDeath(job.isTimeJob() ? DamageSource.OUT_OF_WORLD : DamageSource.LIGHTNING_BOLT);
        this.setDead();
    }

    private void updateCleaner() {
        ItemStack held = this.getHeldItemMainhand();
        if (held.isEmpty()) {
            this.workTimer = 0;
            this.currentRecipe = null;
            if (this.ticksExisted % SEARCH_INTERVAL == 0) {
                this.pickupNearestInput();
            }
            return;
        }

        TileEntity target = this.getDepositInventoryWithSpace(held);
        if (target == null) {
            this.debugAI("No deposit inventory with space for " + describeStack(held), this.getWorkOrigin(),
                    EnumParticleTypes.SMOKE_NORMAL);
            this.getNavigator().clearPath();
            return;
        }

        BlockPos targetPos = target.getPos();
        this.getLookHelper().setLookPosition(targetPos.getX() + 0.5D, targetPos.getY() + 0.5D,
                targetPos.getZ() + 0.5D, 20.0F, 20.0F);
        this.getNavigator().tryMoveToXYZ(targetPos.getX() + 0.5D, targetPos.getY() + 0.5D,
                targetPos.getZ() + 0.5D, 0.6D);
        if (this.getDistanceSq(targetPos.getX() + 0.5D, targetPos.getY() + 0.5D,
                targetPos.getZ() + 0.5D) > 4.0D) {
            return;
        }

        ItemStack remaining = InventoryAccess.insert(target, held);
        this.setHeldItem(EnumHand.MAIN_HAND, remaining.isEmpty() ? ItemStack.EMPTY : remaining);
        this.setDropChance(EntityEquipmentSlot.MAINHAND, 0.0F);
        this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.BLOCK_CHEST_OPEN,
                SoundCategory.NEUTRAL, 0.35F, 1.3F);
        this.debugAI("Deposited item stack into inventory", targetPos, EnumParticleTypes.VILLAGER_HAPPY);
        this.spawnServerWorkParticles();
        this.swingArm(EnumHand.MAIN_HAND);
    }

    private void updateFarmer() {
        if (this.ticksExisted % SEARCH_INTERVAL != 0) {
            return;
        }

        BlockPos targetPos = this.findNearestMatureCrop();
        if (targetPos == null) {
            this.debugAI("No mature crops in work area", this.getWorkOrigin(), EnumParticleTypes.SMOKE_NORMAL);
            this.getNavigator().clearPath();
            return;
        }

        this.debugAI("Moving to mature crop", targetPos, EnumParticleTypes.VILLAGER_HAPPY);
        this.getLookHelper().setLookPosition(targetPos.getX() + 0.5D, targetPos.getY() + 0.5D,
                targetPos.getZ() + 0.5D, 20.0F, 20.0F);
        this.getNavigator().tryMoveToXYZ(targetPos.getX() + 0.5D, targetPos.getY() + 0.5D,
                targetPos.getZ() + 0.5D, 0.6D);
        if (this.getDistanceSq(targetPos.getX() + 0.5D, targetPos.getY() + 0.5D,
                targetPos.getZ() + 0.5D) <= 4.0D) {
            this.harvestMatureCrop(targetPos);
        }
    }

    @Nullable
    private BlockPos findNearestMatureCrop() {
        BlockPos origin = this.getWorkOrigin();
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;
        int range = this.getWorkRangeBlocks();
        for (int x = -range; x <= range; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -range; z <= range; z++) {
                    BlockPos pos = origin.add(x, y, z);
                    if (!this.isMatureCrop(pos)) {
                        continue;
                    }
                    double distance = this.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
                    if (distance < bestDistance) {
                        best = pos;
                        bestDistance = distance;
                    }
                }
            }
        }
        return best;
    }

    private boolean isMatureCrop(BlockPos pos) {
        IBlockState state = this.world.getBlockState(pos);
        Block block = state.getBlock();
        return block instanceof BlockCrops && ((BlockCrops) block).isMaxAge(state)
                && this.world.getBlockState(pos.down()).getBlock() instanceof BlockFarmland
                && this.hasHorizontalStandingSpace(pos);
    }

    private void harvestMatureCrop(BlockPos center) {
        int harvested = 0;
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                BlockPos pos = center.add(x, 0, z);
                if (this.harvestSingleCrop(pos)) {
                    harvested++;
                }
            }
        }
        if (harvested <= 0) {
            return;
        }
        this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.BLOCK_GRASS_BREAK,
                SoundCategory.NEUTRAL, 0.5F, 1.1F);
        this.debugAI("Harvested " + harvested + " crops", center, EnumParticleTypes.VILLAGER_HAPPY);
        this.spawnServerWorkParticles();
        this.swingArm(EnumHand.MAIN_HAND);
    }

    private boolean harvestSingleCrop(BlockPos pos) {
        IBlockState state = this.world.getBlockState(pos);
        Block block = state.getBlock();
        if (!(block instanceof BlockCrops)) {
            return false;
        }
        BlockCrops crop = (BlockCrops) block;
        if (!crop.isMaxAge(state)) {
            return false;
        }
        if (!(this.world.getBlockState(pos.down()).getBlock() instanceof BlockFarmland)
                || !this.hasHorizontalStandingSpace(pos)) {
            return false;
        }
        this.outputHarvestDrops(pos, state);
        this.world.setBlockState(pos, crop.withAge(0), 3);
        return true;
    }

    /**
     * Bounded 1.12 machine-manager loop. The modern job uses separate goals;
     * this port keeps the same route as a small server-side state machine.
     * Every transfer waits for a loaded endpoint in the active dimension.
     */
    private void updateManageMachine() {
        // Re-register after an entity/chunk reload; the controller owns the
        // linked-machine view and intentionally does not trust stale entity NBT.
        this.registerWithStorageController();
        if (this.world == null || this.world.isRemote || this.ticksExisted % MANAGE_MACHINE_INTERVAL != 0
                || this.storageControllerPos == null || this.managedMachine == null
                || !this.managedMachine.isStrictlyConfigured()) {
            return;
        }
        if (!this.isManageMachineSameDimension()) {
            this.getNavigator().clearPath();
            this.manageMachineState = ManageMachineState.BLOCKED_DIMENSION;
            return;
        }
        if (!this.isManageMachineEndpointsLoaded()) {
            this.getNavigator().clearPath();
            this.manageMachineState = ManageMachineState.BLOCKED_ENDPOINT;
            return;
        }

        IItemHandler storage = this.getHandler(this.storageControllerPos, this.storageControllerDimension, null);
        IItemHandler machine = this.getMachineHandler(true);
        IItemHandler output = this.getMachineHandler(false);
        if (storage == null || machine == null || output == null) {
            this.getNavigator().clearPath();
            this.manageMachineState = ManageMachineState.BLOCKED_ENDPOINT;
            return;
        }

        // Match the upstream priority: collect machine output before requesting
        // another input stack. Navigation is intentionally local and bounded.
        if (this.hasExtractableItems(output)) {
            if (this.manageMachineState != ManageMachineState.NAVIGATING_TO_STORAGE_OUTPUT) {
                if (!this.navigateToEndpoint(this.managedMachine.getExtractPosition(),
                        ManageMachineState.NAVIGATING_TO_MACHINE_OUTPUT)) {
                    return;
                }
            }
            if (!this.navigateToEndpoint(this.storageControllerPos,
                    ManageMachineState.NAVIGATING_TO_STORAGE_OUTPUT)) {
                return;
            }
            boolean movedOutput = this.transferItems(output, storage, MANAGE_MACHINE_TRANSFER_SIZE);
            if (!movedOutput) {
                this.debugAI("Machine output could not be deposited", this.storageControllerPos,
                        EnumParticleTypes.SMOKE_NORMAL);
            }
            this.manageMachineState = ManageMachineState.IDLE;
            return;
        }

        if (this.currentDepositOrder == null || this.currentDepositOrder.isComplete()) {
            this.currentDepositOrder = this.depositOrderQueue.poll();
        }
        if (this.currentDepositOrder == null) {
            this.generateDepositOrders(storage, machine);
            this.currentDepositOrder = this.depositOrderQueue.poll();
        }
        if (this.currentDepositOrder == null) {
            this.manageMachineState = ManageMachineState.IDLE;
            return;
        }
        if (this.manageMachineState != ManageMachineState.NAVIGATING_TO_MACHINE_INPUT) {
            if (!this.navigateToEndpoint(this.storageControllerPos,
                    ManageMachineState.NAVIGATING_TO_STORAGE_INPUT)) {
                return;
            }
        }
        if (!this.navigateToEndpoint(this.managedMachine.getInsertPosition(),
                ManageMachineState.NAVIGATING_TO_MACHINE_INPUT)) {
            return;
        }

        int moved = this.transferMatchingItems(storage, machine, this.currentDepositOrder,
                MANAGE_MACHINE_TRANSFER_SIZE);
        if (moved > 0) {
            this.currentDepositOrder.recordSuccess(moved);
        } else {
            this.currentDepositOrder.recordFailure();
        }
        if (this.currentDepositOrder.isComplete()) {
            this.currentDepositOrder = null;
        }
        this.manageMachineState = ManageMachineState.IDLE;
    }

    public String getManageMachineState() {
        return this.manageMachineState.name();
    }

    private boolean navigateToEndpoint(@Nullable BlockPos target, ManageMachineState state) {
        if (target == null || this.world == null || this.world.isRemote
                || !this.world.isBlockLoaded(target)) {
            this.getNavigator().clearPath();
            this.manageMachineState = ManageMachineState.BLOCKED_ENDPOINT;
            return false;
        }
        double targetX = target.getX() + 0.5D;
        double targetY = target.getY() + 0.5D;
        double targetZ = target.getZ() + 0.5D;
        this.getLookHelper().setLookPosition(targetX, targetY, targetZ, 20.0F, 20.0F);
        if (this.getDistanceSq(targetX, targetY, targetZ) > MANAGE_MACHINE_ACCESS_DISTANCE_SQ) {
            this.manageMachineState = state;
            this.getNavigator().tryMoveToXYZ(targetX, targetY, targetZ, 0.6D);
            return false;
        }
        this.getNavigator().clearPath();
        return true;
    }

    private boolean hasExtractableItems(IItemHandler handler) {
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            if (!handler.getStackInSlot(slot).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private boolean isManageMachineEndpointsLoaded() {
        if (this.world == null || this.storageControllerPos == null || this.managedMachine == null
                || !this.world.isBlockLoaded(this.storageControllerPos)) {
            return false;
        }
        return this.managedMachine.isValidFor(this.world);
    }

    private IItemHandler getMachineHandler(boolean input) {
        if (this.world == null || this.managedMachine == null) {
            return null;
        }
        BlockPos position = input ? this.managedMachine.getInsertPosition()
                : this.managedMachine.getExtractPosition();
        int dimension = input ? this.managedMachine.getInsertDimension()
                : this.managedMachine.getExtractDimension();
        EnumFacing facing = input ? this.managedMachine.getInsertFacing()
                : this.managedMachine.getExtractFacing();
        if (position == null || facing == null || this.world.provider.getDimension() != dimension
                || !this.world.isBlockLoaded(position)) {
            return null;
        }
        TileEntity tile = this.world.getTileEntity(position);
        boolean matches = input ? this.managedMachine.matchesInsertTile(tile)
                : this.managedMachine.matchesExtractTile(tile);
        if (!matches || !tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing)) {
            return null;
        }
        return tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing);
    }

    /** The 1.12 port rejects unloaded or cross-dimension endpoints. */
    private boolean isManageMachineSameDimension() {
        if (this.world == null || this.managedMachine == null || !this.managedMachine.isStrictlyConfigured()) {
            return false;
        }
        int activeDimension = this.world.provider.getDimension();
        if (this.storageControllerDimension != activeDimension
                || this.managedMachine.getInsertDimension() != activeDimension) {
            return false;
        }
        return this.managedMachine.getExtractDimension() == activeDimension;
    }

    /** Builds deterministic orders from machine slot capacity and storage contents. */
    private void generateDepositOrders(IItemHandler storage, IItemHandler machine) {
        if (storage == null || machine == null || this.depositOrderQueue.size() >= MANAGE_MACHINE_MAX_ORDERS) {
            return;
        }
        for (int machineSlot = 0; machineSlot < machine.getSlots(); machineSlot++) {
            int capacity = machine.getSlotLimit(machineSlot);
            ItemStack existing = machine.getStackInSlot(machineSlot);
            if (!existing.isEmpty()) {
                capacity -= existing.getCount();
            }
            if (capacity <= 0) {
                continue;
            }
            for (int storageSlot = 0; storageSlot < storage.getSlots(); storageSlot++) {
                ItemStack candidate = storage.getStackInSlot(storageSlot);
                if (candidate.isEmpty()) {
                    continue;
                }
                int requested = Math.min(Math.min(capacity, candidate.getCount()), MANAGE_MACHINE_TRANSFER_SIZE);
                ItemStack probe = candidate.copy();
                probe.setCount(requested);
                ItemStack remainder = machine.insertItem(machineSlot, probe, true);
                int accepted = requested - remainder.getCount();
                if (accepted > 0) {
                    this.addDepositOrder(new DepositOrder(candidate, accepted));
                    break;
                }
            }
            if (this.depositOrderQueue.size() >= MANAGE_MACHINE_MAX_ORDERS) {
                return;
            }
        }
    }

    private int transferMatchingItems(IItemHandler source, IItemHandler target, DepositOrder order, int maxAmount) {
        if (source == null || target == null || order == null || order.isComplete() || maxAmount <= 0) {
            return 0;
        }
        int limit = Math.min(maxAmount, order.getAmount());
        for (int sourceSlot = 0; sourceSlot < source.getSlots(); sourceSlot++) {
            ItemStack available = source.getStackInSlot(sourceSlot);
            if (available.isEmpty() || !order.matches(available)) {
                continue;
            }
            int requested = Math.min(limit, available.getCount());
            ItemStack probe = source.extractItem(sourceSlot, requested, true);
            if (probe.isEmpty()) {
                continue;
            }
            ItemStack targetRemainder = insertIntoHandler(target, probe, true);
            int accepted = probe.getCount() - targetRemainder.getCount();
            if (accepted <= 0) {
                continue;
            }
            ItemStack extracted = source.extractItem(sourceSlot, accepted, false);
            ItemStack remainder = insertIntoHandler(target, extracted, false);
            int moved = extracted.getCount() - remainder.getCount();
            if (!remainder.isEmpty()) {
                source.insertItem(sourceSlot, remainder, false);
            }
            return moved;
        }
        return 0;
    }

    @Nullable
    private IItemHandler getHandler(@Nullable BlockPos position, int dimension, @Nullable EnumFacing facing) {
        if (position == null || this.world == null || this.world.provider.getDimension() != dimension
                || !this.world.isBlockLoaded(position)) {
            return null;
        }
        TileEntity tile = this.world.getTileEntity(position);
        if (tile == null || !tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing)) {
            return null;
        }
        return tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing);
    }

    private boolean transferItems(IItemHandler source, IItemHandler target, int maxAmount) {
        return transferItemHandlers(source, target, maxAmount);
    }

    /** Exposes the deterministic capability transaction for contract tests and integrations. */
    public static boolean transferItemHandlers(IItemHandler source, IItemHandler target, int maxAmount) {
        if (source == null || target == null || maxAmount <= 0) {
            return false;
        }
        for (int sourceSlot = 0; sourceSlot < source.getSlots(); sourceSlot++) {
            ItemStack probe = source.extractItem(sourceSlot, maxAmount, true);
            if (probe.isEmpty()) {
                continue;
            }
            ItemStack targetRemainder = insertIntoHandler(target, probe, true);
            int accepted = probe.getCount() - targetRemainder.getCount();
            if (accepted <= 0) {
                continue;
            }
            ItemStack extracted = source.extractItem(sourceSlot, accepted, false);
            ItemStack remainder = insertIntoHandler(target, extracted, false);
            if (!remainder.isEmpty()) {
                source.insertItem(sourceSlot, remainder, false);
            }
            return true;
        }
        return false;
    }

    private static ItemStack insertIntoHandler(IItemHandler handler, ItemStack stack, boolean simulate) {
        ItemStack remaining = stack;
        for (int slot = 0; slot < handler.getSlots() && !remaining.isEmpty(); slot++) {
            remaining = handler.insertItem(slot, remaining, simulate);
        }
        return remaining;
    }

    private void updateTransporter() {
        ItemStack held = this.getHeldItemMainhand();
        if (held.isEmpty()) {
            if (this.ticksExisted % SEARCH_INTERVAL == 0) {
                this.pickupFromSourceInventory();
            }
            return;
        }

        TileEntity target = this.getTransporterTargetInventory(held);
        if (!InventoryAccess.isInventory(target)) {
            this.debugAI("No transporter target inventory with space for " + describeStack(held),
                    this.getWorkOrigin(), EnumParticleTypes.SMOKE_NORMAL);
            this.getNavigator().clearPath();
            return;
        }

        BlockPos targetPos = target.getPos();
        this.getLookHelper().setLookPosition(targetPos.getX() + 0.5D, targetPos.getY() + 0.5D,
                targetPos.getZ() + 0.5D, 20.0F, 20.0F);
        this.getNavigator().tryMoveToXYZ(targetPos.getX() + 0.5D, targetPos.getY() + 0.5D,
                targetPos.getZ() + 0.5D, 0.6D);
        if (this.getDistanceSq(targetPos.getX() + 0.5D, targetPos.getY() + 0.5D,
                targetPos.getZ() + 0.5D) > 4.0D) {
            return;
        }

        ItemStack remaining = InventoryAccess.insert(target, held);
        this.setHeldItem(EnumHand.MAIN_HAND, remaining.isEmpty() ? ItemStack.EMPTY : remaining);
        this.setDropChance(EntityEquipmentSlot.MAINHAND, 0.0F);
        target.markDirty();
        this.debugAI("Transported item stack into target inventory", targetPos, EnumParticleTypes.VILLAGER_HAPPY);
        this.playTransporterEffects();
        this.swingArm(EnumHand.MAIN_HAND);
    }

    private void pickupFromSourceInventory() {
        TileEntity source = this.getTransporterSourceInventory();
        if (!InventoryAccess.isInventory(source)) {
            this.debugAI("No transporter source inventory with extractable items", this.getWorkOrigin(),
                    EnumParticleTypes.SMOKE_NORMAL);
            this.getNavigator().clearPath();
            return;
        }

        BlockPos sourcePos = source.getPos();
        this.getLookHelper().setLookPosition(sourcePos.getX() + 0.5D, sourcePos.getY() + 0.5D,
                sourcePos.getZ() + 0.5D, 20.0F, 20.0F);
        this.getNavigator().tryMoveToXYZ(sourcePos.getX() + 0.5D, sourcePos.getY() + 0.5D,
                sourcePos.getZ() + 0.5D, 0.6D);
        if (this.getDistanceSq(sourcePos.getX() + 0.5D, sourcePos.getY() + 0.5D,
                sourcePos.getZ() + 0.5D) > 4.0D) {
            return;
        }

        ItemStack extracted = this.extractFirstMovableStack(source);
        if (extracted.isEmpty()) {
            return;
        }
        this.setHeldItem(EnumHand.MAIN_HAND, extracted);
        this.setDropChance(EntityEquipmentSlot.MAINHAND, 0.0F);
        source.markDirty();
        this.debugAI("Extracted " + describeStack(extracted) + " from source inventory", sourcePos,
                EnumParticleTypes.VILLAGER_HAPPY);
        this.playTransporterEffects();
        this.swingArm(EnumHand.MAIN_HAND);
    }

    @Nullable
    private TileEntity getTransporterSourceInventory() {
        if (this.transporterSourcePos != null) {
            TileEntity source = this.world.getTileEntity(this.transporterSourcePos);
            if (InventoryAccess.isInventory(source)
                    && InventoryAccess.hasAnyExtractable(source, this::isAllowedByTransportFilter)
                    && !this.isTransporterDepositPos(source.getPos())) {
                return source;
            }
            this.transporterSourcePos = null;
        }
        if (this.extractPos != null) {
            TileEntity configuredSource = this.getConfiguredExtractInventory();
            if (configuredSource != null
                    && !this.isTransporterDepositPos(configuredSource.getPos())
                    && InventoryAccess.hasAnyExtractable(configuredSource, this::isAllowedByTransportFilter)) {
                this.transporterSourcePos = configuredSource.getPos();
                return configuredSource;
            }
            this.transporterSourcePos = null;
        }

        TileEntity source = this.findNearestInventoryWithItems(this.depositPos);
        this.transporterSourcePos = source == null ? null : source.getPos();
        return source;
    }

    @Nullable
    private TileEntity getTransporterTargetInventory(ItemStack stack) {
        if (this.transporterTargetPos != null) {
            TileEntity target = this.world.getTileEntity(this.transporterTargetPos);
            if (InventoryAccess.isInventory(target) && !target.getPos().equals(this.transporterSourcePos)
                    && !this.isTransporterExtractPos(target.getPos())
                    && InventoryAccess.hasSpace(target, stack)) {
                return target;
            }
        }
        if (this.depositPos != null) {
            TileEntity configuredTarget = this.getConfiguredDepositInventory();
            if (InventoryAccess.isInventory(configuredTarget)
                    && !configuredTarget.getPos().equals(this.transporterSourcePos)
                    && InventoryAccess.hasSpace(configuredTarget, stack)) {
                this.transporterTargetPos = configuredTarget.getPos();
                return configuredTarget;
            }
            this.transporterTargetPos = null;
            return null;
        }
        BlockPos excludedPos = this.transporterSourcePos == null ? this.extractPos : this.transporterSourcePos;
        TileEntity target = this.findNearestInventoryWithSpace(stack, excludedPos);
        this.transporterTargetPos = target == null ? null : target.getPos();
        return target;
    }

    private boolean isTransporterDepositPos(BlockPos pos) {
        return this.depositPos != null && this.depositPos.equals(pos);
    }

    private boolean isTransporterExtractPos(BlockPos pos) {
        return this.extractPos != null && this.extractPos.equals(pos);
    }

    private ItemStack extractFirstMovableStack(TileEntity inventory) {
        return InventoryAccess.extractFirst(inventory,
                stack -> Math.min(stack.getCount(), TRANSPORTER_STACK_SIZE),
                stack -> this.isAllowedByTransportFilter(stack)
                        && InventoryAccess.hasSpace(this.getTransporterTargetInventory(stack), stack));
    }

    @Nullable
    private TileEntity findNearestInventoryWithItems(@Nullable BlockPos excludedPos) {
        TileEntity best = null;
        double bestDistance = Double.MAX_VALUE;
        int range = this.getWorkRangeBlocks();
        BlockPos origin = this.getWorkOrigin();
        for (int x = -range; x <= range; x++) {
            for (int y = -4; y <= 4; y++) {
                for (int z = -range; z <= range; z++) {
                    BlockPos pos = origin.add(x, y, z);
                    if (excludedPos != null && excludedPos.equals(pos)) {
                        continue;
                    }
                    TileEntity tile = this.world.getTileEntity(pos);
                    if (!InventoryAccess.hasAnyExtractable(tile, this::isAllowedByTransportFilter)) {
                        continue;
                    }
                    double distance = this.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
                    if (distance < bestDistance) {
                        best = tile;
                        bestDistance = distance;
                    }
                }
            }
        }
        return best;
    }

    private void playTransporterEffects() {
        this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT,
                SoundCategory.NEUTRAL, 0.25F, 1.6F);
        this.spawnServerWorkParticles();
    }

    private void updateLumberjack() {
        if (this.ticksExisted % SEARCH_INTERVAL != 0) {
            return;
        }

        BlockPos targetPos = this.findNearestTreeLog();
        if (targetPos == null) {
            this.debugAI("No reachable tree in work area", this.getWorkOrigin(), EnumParticleTypes.SMOKE_NORMAL);
            this.getNavigator().clearPath();
            return;
        }

        this.debugAI("Moving to tree", targetPos, EnumParticleTypes.VILLAGER_HAPPY);
        this.getLookHelper().setLookPosition(targetPos.getX() + 0.5D, targetPos.getY() + 0.5D,
                targetPos.getZ() + 0.5D, 20.0F, 20.0F);
        this.getNavigator().tryMoveToXYZ(targetPos.getX() + 0.5D, targetPos.getY() + 0.5D,
                targetPos.getZ() + 0.5D, 0.6D);
        if (this.getDistanceSq(targetPos.getX() + 0.5D, targetPos.getY() + 0.5D,
                targetPos.getZ() + 0.5D) <= 6.25D) {
            this.harvestTree(targetPos);
        }
    }

    @Nullable
    private BlockPos findNearestTreeLog() {
        BlockPos origin = this.getWorkOrigin();
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;
        int range = this.getWorkRangeBlocks();
        for (int x = -range; x <= range; x++) {
            for (int y = -2; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    BlockPos pos = origin.add(x, y, z);
                    if (!this.isTreeStump(pos)) {
                        continue;
                    }
                    double distance = this.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
                    if (distance < bestDistance) {
                        best = pos;
                        bestDistance = distance;
                    }
                }
            }
        }
        return best;
    }

    private void harvestTree(BlockPos startPos) {
        Set<BlockPos> logs = this.collectConnectedLogs(startPos);
        if (logs.isEmpty()) {
            return;
        }
        for (BlockPos logPos : logs) {
            IBlockState state = this.world.getBlockState(logPos);
            this.outputHarvestDrops(logPos, state);
            this.world.setBlockToAir(logPos);
            this.markNearbyLeavesForDecay(logPos);
        }
        this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.BLOCK_WOOD_BREAK,
                SoundCategory.NEUTRAL, 0.55F, 1.0F);
        this.debugAI("Harvested tree logs: " + logs.size(), startPos, EnumParticleTypes.VILLAGER_HAPPY);
        this.spawnServerWorkParticles();
        this.swingArm(EnumHand.MAIN_HAND);
    }

    private Set<BlockPos> collectConnectedLogs(BlockPos startPos) {
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();
        queue.add(startPos);
        while (!queue.isEmpty() && visited.size() < LUMBERJACK_MAX_LOGS) {
            BlockPos pos = queue.remove();
            if (visited.contains(pos) || !this.isHarvestableLog(pos)) {
                continue;
            }
            visited.add(pos);
            for (EnumFacing direction : HORIZONTAL_DIRECTIONS) {
                BlockPos next = pos.offset(direction);
                if (!visited.contains(next) && this.isHarvestableLog(next)) {
                    queue.add(next);
                }
            }
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos next = pos.add(x, 1, z);
                    if (!visited.contains(next) && this.isHarvestableLog(next)) {
                        queue.add(next);
                    }
                }
            }
        }
        return visited;
    }

    private boolean isHarvestableLog(BlockPos pos) {
        IBlockState state = this.world.getBlockState(pos);
        Block block = state.getBlock();
        if (block == ModBlocks.OTHERWORLD_LOG || block == ModBlocks.OTHERWORLD_LOG_NATURAL
                || block == ModBlocks.OTHERWORLD_WOOD
                || block == ModBlocks.STRIPPED_OTHERWORLD_LOG
                || block == ModBlocks.STRIPPED_OTHERWORLD_LOG_NATURAL
                || block == ModBlocks.STRIPPED_OTHERWORLD_WOOD) {
            return true;
        }
        if (block instanceof BlockLog) {
            return true;
        }
        ItemStack stack = new ItemStack(block, 1, OreDictionary.WILDCARD_VALUE);
        for (int id : OreDictionary.getOreIDs(stack)) {
            String name = OreDictionary.getOreName(id);
            if ("logWood".equals(name) || "logs".equals(name)) {
                return true;
            }
        }
        return state.getMaterial() == Material.WOOD && block.getRegistryName() != null
                && block.getRegistryName().getPath().contains("log");
    }

    private boolean isTreeStump(BlockPos logPos) {
        if (!this.isHarvestableLog(logPos) || !this.isTreeSoil(logPos.down())
                || !this.hasHorizontalStandingSpace(logPos)) {
            return false;
        }
        return this.canReachLeafCrown(logPos);
    }

    private boolean isTreeSoil(BlockPos pos) {
        IBlockState state = this.world.getBlockState(pos);
        return state.getBlock().canSustainPlant(state, this.world, pos, EnumFacing.UP, (IPlantable) Blocks.SAPLING);
    }

    private boolean canReachLeafCrown(BlockPos logPos) {
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();
        queue.add(logPos);
        while (!queue.isEmpty() && visited.size() < LUMBERJACK_MAX_LOGS) {
            BlockPos pos = queue.remove();
            if (visited.contains(pos) || !this.isHarvestableLog(pos)) {
                continue;
            }
            visited.add(pos);
            if (this.hasNearbyLeaves(pos)) {
                return true;
            }
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos next = pos.add(x, 1, z);
                    if (!visited.contains(next) && this.isHarvestableLog(next)) {
                        queue.add(next);
                    }
                }
            }
        }
        return false;
    }

    private boolean hasNearbyLeaves(BlockPos logPos) {
        for (int x = -3; x <= 3; x++) {
            for (int y = 0; y <= 5; y++) {
                for (int z = -3; z <= 3; z++) {
                    BlockPos pos = logPos.add(x, y, z);
                    IBlockState state = this.world.getBlockState(pos);
                    if (state.getBlock().isLeaves(state, this.world, pos)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean hasHorizontalStandingSpace(BlockPos targetPos) {
        for (EnumFacing direction : HORIZONTAL_DIRECTIONS) {
            if (this.isStandingSpace(targetPos.offset(direction))) {
                return true;
            }
        }
        return false;
    }

    private boolean isStandingSpace(BlockPos feetPos) {
        IBlockState groundState = this.world.getBlockState(feetPos.down());
        if (groundState.getBlock().getCollisionBoundingBox(groundState, this.world, feetPos.down()) == null) {
            return false;
        }
        return this.hasNoCollision(feetPos) && this.hasNoCollision(feetPos.up());
    }

    private boolean hasNoCollision(BlockPos pos) {
        IBlockState state = this.world.getBlockState(pos);
        AxisAlignedBB collision = state.getBlock().getCollisionBoundingBox(state, this.world, pos);
        return collision == null || collision == Block.NULL_AABB;
    }

    private void markNearbyLeavesForDecay(BlockPos logPos) {
        for (int x = -4; x <= 4; x++) {
            for (int y = -4; y <= 4; y++) {
                for (int z = -4; z <= 4; z++) {
                    BlockPos pos = logPos.add(x, y, z);
                    IBlockState state = this.world.getBlockState(pos);
                    if (!state.getBlock().isLeaves(state, this.world, pos)
                            || !state.getProperties().containsKey(BlockLeaves.CHECK_DECAY)) {
                        continue;
                    }
                    this.world.setBlockState(pos, state.withProperty(BlockLeaves.CHECK_DECAY, Boolean.TRUE), 4);
                }
            }
        }
    }

    private void pickupNearestInput() {
        EntityItem target = this.findNearestInput();
        if (target == null) {
            this.debugAI("No processable dropped item in work area", this.getWorkOrigin(),
                    EnumParticleTypes.SMOKE_NORMAL);
            this.getNavigator().clearPath();
            return;
        }

        this.debugAI("Moving to dropped input " + describeStack(target.getItem()), target.getPosition(),
                EnumParticleTypes.VILLAGER_HAPPY);
        this.getLookHelper().setLookPositionWithEntity(target, 20.0F, 20.0F);
        this.getNavigator().tryMoveToEntityLiving(target, 0.6D);
        if (this.getDistanceSq(target) <= PICKUP_RANGE_SQ) {
            this.pickupOne(target);
        }
    }

    private void pickupProcessingInput() {
        if (this.extractPos != null && this.getJobType() != SpiritJobType.CLEANER
                && this.getJobType() != SpiritJobType.FARMER && this.getJobType() != SpiritJobType.LUMBERJACK
                && this.getJobType() != SpiritJobType.TRANSPORT_ITEMS) {
            this.pickupFromConfiguredProcessingInventory();
            return;
        }
        this.pickupNearestInput();
    }

    private void pickupFromConfiguredProcessingInventory() {
        TileEntity source = this.getConfiguredExtractInventory();
        if (!InventoryAccess.isInventory(source)) {
            this.debugAI("Configured extract inventory missing or unusable",
                    this.extractPos == null ? this.getWorkOrigin() : this.extractPos, EnumParticleTypes.SMOKE_NORMAL);
            this.getNavigator().clearPath();
            return;
        }

        BlockPos sourcePos = source.getPos();
        this.getLookHelper().setLookPosition(sourcePos.getX() + 0.5D, sourcePos.getY() + 0.5D,
                sourcePos.getZ() + 0.5D, 20.0F, 20.0F);
        this.getNavigator().tryMoveToXYZ(sourcePos.getX() + 0.5D, sourcePos.getY() + 0.5D,
                sourcePos.getZ() + 0.5D, 0.6D);
        if (this.getDistanceSq(sourcePos.getX() + 0.5D, sourcePos.getY() + 0.5D,
                sourcePos.getZ() + 0.5D) > 4.0D) {
            return;
        }

        ItemStack extracted = this.extractFirstProcessableStack(source);
        if (extracted.isEmpty()) {
            this.debugAI("No processable stack in configured extract inventory", sourcePos,
                    EnumParticleTypes.SMOKE_NORMAL);
            return;
        }
        this.mergePickedTradeInput(extracted);
        this.setDropChance(EntityEquipmentSlot.MAINHAND, 0.0F);
        this.currentRecipe = this.findSpiritJobRecipe(extracted);
        this.workTimer = 0;
        source.markDirty();
        this.debugAI("Extracted processable stack " + describeStack(extracted), sourcePos,
                EnumParticleTypes.VILLAGER_HAPPY);
        this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ITEM_PICKUP,
                SoundCategory.NEUTRAL, 0.4F, 1.2F);
        this.swingArm(EnumHand.MAIN_HAND);
    }

    private ItemStack extractFirstProcessableStack(TileEntity inventory) {
        return InventoryAccess.extractFirst(inventory,
                stack -> Math.min(stack.getCount(), this.getPickupCountForJob(stack)),
                this::canPickUpProcessingStack);
    }

    private boolean canPickUpProcessingStack(ItemStack stack) {
        if (stack.isEmpty() || this.getPickupCountForJob(stack) <= 0) {
            return false;
        }
        SpiritTradeRecipeRegistry.Recipe trade = this.getTradeRecipe();
        ItemStack held = this.getHeldItemMainhand();
        if (trade != null && !held.isEmpty()) {
            return trade.acceptsStackType(stack) && this.canMergeStacks(held, stack);
        }
        return this.canProcess(stack);
    }

    @Nullable
    private EntityItem findNearestInput() {
        BlockPos origin = this.getWorkOrigin();
        AxisAlignedBB bounds = new AxisAlignedBB(origin).grow(this.getWorkRange());
        List<EntityItem> items = this.world.getEntitiesWithinAABB(EntityItem.class, bounds);
        EntityItem best = null;
        double bestDistance = Double.MAX_VALUE;
        for (EntityItem item : items) {
            if (item.isDead || item.getItem().isEmpty() || this.shouldIgnoreDrop(item)) {
                continue;
            }
            if (!this.canPickUpProcessingStack(item.getItem())) {
                continue;
            }
            double distance = this.getDistanceSq(item);
            if (distance < bestDistance) {
                best = item;
                bestDistance = distance;
            }
        }
        return best;
    }

    private boolean shouldIgnoreDrop(EntityItem item) {
        NBTTagCompound data = item.getEntityData();
        if (!data.hasKey(DROP_TICK_KEY)) {
            return false;
        }
        return this.world.getTotalWorldTime() - data.getLong(DROP_TICK_KEY) < RESULT_PICKUP_DELAY;
    }

    private void pickupOne(EntityItem entityItem) {
        ItemStack stack = entityItem.getItem();
        if (!this.canPickUpProcessingStack(stack)) {
            return;
        }
        ItemStack picked = stack.copy();
        int pickupCount = Math.min(stack.getCount(), this.getPickupCountForJob(stack));
        if (pickupCount <= 0) {
            return;
        }
        picked.setCount(pickupCount);
        stack.shrink(pickupCount);
        if (stack.isEmpty()) {
            entityItem.setDead();
        } else {
            entityItem.setItem(stack);
        }

        this.mergePickedTradeInput(picked);
        this.setDropChance(EntityEquipmentSlot.MAINHAND, 0.0F);
        this.currentRecipe = this.findSpiritJobRecipe(picked);
        this.workTimer = 0;
        this.debugAI("Picked up " + describeStack(picked), entityItem.getPosition(), EnumParticleTypes.VILLAGER_HAPPY);
        this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ITEM_PICKUP,
                SoundCategory.NEUTRAL, 0.4F, 1.2F);
    }

    private void mergePickedTradeInput(ItemStack picked) {
        ItemStack held = this.getHeldItemMainhand();
        if (this.getTradeRecipe() != null && !held.isEmpty()) {
            ItemStack combined = held.copy();
            combined.grow(picked.getCount());
            this.setHeldItem(EnumHand.MAIN_HAND, combined);
        } else {
            this.setHeldItem(EnumHand.MAIN_HAND, picked);
        }
    }

    private void completeWork(SpiritJobRecipe recipe, ItemStack held) {
        int operationCount = Math.min(held.getCount(), this.getProcessingOperationCount());
        ItemStack output = recipe.createOutput(operationCount);
        output.setCount((int) (output.getCount() * this.getProcessingOutputMultiplier(recipe)));
        this.debugAI("Completed job recipe -> " + describeStack(output), this.getPosition(),
                EnumParticleTypes.VILLAGER_HAPPY);
        held.shrink(operationCount);
        this.setHeldItem(EnumHand.MAIN_HAND, held.isEmpty() ? ItemStack.EMPTY : held);
        this.outputProcessedStack(output);
        this.world.playEvent(2004, this.getPosition(), 0);
        this.swingArm(EnumHand.MAIN_HAND);
        this.workTimer = 0;
        this.currentRecipe = null;
    }

    private void completeSmelting(ItemStack smeltingResult, ItemStack held) {
        int operationCount = Math.min(held.getCount(), this.getProcessingOperationCount());
        ItemStack output = smeltingResult.copy();
        output.setCount(output.getCount() * operationCount);
        this.debugAI("Completed smelting -> " + describeStack(output), this.getPosition(),
                EnumParticleTypes.VILLAGER_HAPPY);
        held.shrink(operationCount);
        this.setHeldItem(EnumHand.MAIN_HAND, held.isEmpty() ? ItemStack.EMPTY : held);
        this.outputProcessedStack(output);
        this.world.playEvent(2004, this.getPosition(), 0);
        this.swingArm(EnumHand.MAIN_HAND);
        this.workTimer = 0;
        this.currentRecipe = null;
    }

    private void completeTrade(ItemStack tradeResult, ItemStack held) {
        int inputCount = Math.min(held.getCount(), this.getTraderOperationCount());
        SpiritTradeRecipeRegistry.Recipe recipe = this.getTradeRecipe();
        ItemStack output;
        if (recipe != null) {
            inputCount = Math.min(held.getCount() / recipe.getIngredients().size(), this.getTraderOperationCount());
            output = recipe.trade(held, this.getTraderOperationCount());
            if (output.isEmpty()) {
                this.workTimer = 0;
                return;
            }
        } else if (this.getJobType() == SpiritJobType.GAMBLER && !tradeResult.isEmpty()) {
            // Gambling retains its separate weighted-result behavior.
            output = tradeResult.copy();
            output.setCount(output.getCount() * inputCount);
            held.shrink(inputCount);
        } else {
            this.workTimer = 0;
            return;
        }
        this.debugAI("Completed trade x" + inputCount + " -> " + describeStack(output), this.getPosition(),
                EnumParticleTypes.VILLAGER_HAPPY);
        this.setHeldItem(EnumHand.MAIN_HAND, held.isEmpty() ? ItemStack.EMPTY : held);
        this.outputProcessedStack(output);
        this.world.playEvent(2004, this.getPosition(), 0);
        this.swingArm(EnumHand.MAIN_HAND);
        this.workTimer = 0;
        this.currentRecipe = null;
    }

    private boolean canProcess(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (this.getJobType() == SpiritJobType.CLEANER) {
            return this.isAllowedByTransportFilter(stack);
        }
        if (this.getJobType() == SpiritJobType.SMELTER) {
            return !this.findSmeltingResult(stack).isEmpty();
        }
        if (this.isTrader()) {
            return !this.findTradeResult(stack).isEmpty();
        }
        return this.findSpiritJobRecipe(stack) != null;
    }

    private SpiritJobRecipe findSpiritJobRecipe(ItemStack stack) {
        if (this.getJobType() == SpiritJobType.SMELTER || this.isTrader()) {
            return null;
        }
        return SpiritJobRecipe.findFirst(this.getJobType(), stack, this.getTier());
    }

    private ItemStack findSmeltingResult(ItemStack stack) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return FurnaceRecipes.instance().getSmeltingResult(stack);
    }

    private ItemStack findTradeResult(ItemStack stack) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        SpiritTradeRecipeRegistry.Recipe recipe = this.getTradeRecipe();
        if (recipe != null && recipe.matches(stack)) {
            return recipe.getOutput();
        }
        if (this.getJobType() == SpiritJobType.GAMBLER && this.isGamblerInput(stack)) {
            return this.getGamblerResult();
        }
        return ItemStack.EMPTY;
    }

    private boolean isGamblerInput(ItemStack stack) {
        int[] oreIds = OreDictionary.getOreIDs(stack);
        for (int oreId : oreIds) {
            if ("gems".equals(OreDictionary.getOreName(oreId))) {
                return true;
            }
        }
        return false;
    }

    private ItemStack getGamblerResult() {
        int roll = this.rand.nextInt(GAMBLER_TOTAL_WEIGHT);
        if ((roll -= 8) < 0) {
            return new ItemStack(ModItems.SPIRIT_ATTUNED_GEM);
        }
        if ((roll -= 1) < 0) {
            return new ItemStack(Items.DIAMOND);
        }
        if ((roll -= 1) < 0) {
            return new ItemStack(Items.EMERALD);
        }
        if ((roll -= 2) < 0) {
            return new ItemStack(Items.QUARTZ);
        }
        if ((roll -= 2) < 0) {
            return new ItemStack(Items.DYE, 1, 4);
        }
        if ((roll -= 4) < 0) {
            return new ItemStack(Items.PRISMARINE_CRYSTALS);
        }
        if ((roll -= 4) < 0) {
            return new ItemStack(ModItems.SPIRIT_ATTUNED_GEM);
        }
        if ((roll -= 16) < 0) {
            return new ItemStack(ModItems.IRON_DUST);
        }
        if ((roll -= 8) < 0) {
            return new ItemStack(Items.GOLD_NUGGET);
        }
        if ((roll -= 8) < 0) {
            return new ItemStack(ModItems.SILVER_NUGGET);
        }
        return new ItemStack(ModItems.IESNIUM_NUGGET);
    }

    private BlockPos getWorkOrigin() {
        return this.workAreaPos == null ? this.getPosition() : this.workAreaPos;
    }

    @Nullable
    private TileEntity getDepositInventoryWithSpace(ItemStack stack) {
        TileEntity configured = this.getConfiguredDepositInventory();
        if (InventoryAccess.isInventory(configured)) {
            return InventoryAccess.hasSpace(configured, stack) ? configured : null;
        }
        return this.findNearestInventoryWithSpace(stack);
    }

    @Nullable
    private TileEntity getConfiguredDepositInventory() {
        if (this.depositPos == null) {
            return null;
        }
        TileEntity tile = this.world.getTileEntity(this.depositPos);
        return InventoryAccess.isInventory(tile) ? tile : null;
    }

    @Nullable
    private TileEntity getConfiguredExtractInventory() {
        if (this.extractPos == null) {
            return null;
        }
        TileEntity tile = this.world.getTileEntity(this.extractPos);
        return InventoryAccess.isInventory(tile) ? tile : null;
    }

    @Nullable
    private TileEntity findNearestInventoryWithSpace(ItemStack stack) {
        return this.findNearestInventoryWithSpace(stack, null);
    }

    @Nullable
    private TileEntity findNearestInventoryWithSpace(ItemStack stack, @Nullable BlockPos excludedPos) {
        TileEntity best = null;
        double bestDistance = Double.MAX_VALUE;
        int range = this.getWorkRangeBlocks();
        BlockPos origin = this.getWorkOrigin();
        for (int x = -range; x <= range; x++) {
            for (int y = -4; y <= 4; y++) {
                for (int z = -range; z <= range; z++) {
                    BlockPos pos = origin.add(x, y, z);
                    if (excludedPos != null && excludedPos.equals(pos)) {
                        continue;
                    }
                    TileEntity tile = this.world.getTileEntity(pos);
                    if (!InventoryAccess.isInventory(tile)) {
                        continue;
                    }
                    if (!InventoryAccess.hasSpace(tile, stack)) {
                        continue;
                    }
                    double distance = this.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
                    if (distance < bestDistance) {
                        best = tile;
                        bestDistance = distance;
                    }
                }
            }
        }
        return best;
    }

    private void outputHarvestDrops(BlockPos pos, IBlockState state) {
        NonNullList<ItemStack> drops = NonNullList.create();
        state.getBlock().getDrops(drops, this.world, pos, state, 0);
        if (drops.isEmpty()) {
            return;
        }

        TileEntity target = this.getConfiguredDepositInventory();
        for (ItemStack drop : drops) {
            if (drop.isEmpty()) {
                continue;
            }
            ItemStack remaining = drop.copy();
            if (InventoryAccess.isInventory(target)) {
                remaining = InventoryAccess.insert(target, remaining);
            }
            if (!remaining.isEmpty()) {
                this.spawnStacksAt(remaining, pos.getX() + 0.5D, pos.getY() + 0.25D, pos.getZ() + 0.5D);
            }
        }
    }

    private void spawnStacksAt(ItemStack stack, double x, double y, double z) {
        int remaining = stack.getCount();
        int maxStackSize = stack.getMaxStackSize();
        while (remaining > 0) {
            ItemStack split = stack.copy();
            split.setCount(Math.min(remaining, maxStackSize));
            EntityItem result = new EntityItem(this.world, x, y, z, split);
            result.motionY = 0.12D;
            result.motionX = (this.rand.nextDouble() - 0.5D) * 0.08D;
            result.motionZ = (this.rand.nextDouble() - 0.5D) * 0.08D;
            result.setPickupDelay(10);
            this.world.spawnEntity(result);
            remaining -= split.getCount();
        }
    }

    private boolean canMergeStacks(ItemStack existing, ItemStack incoming) {
        return ItemStack.areItemsEqual(existing, incoming) && ItemStack.areItemStackTagsEqual(existing, incoming);
    }

    private boolean isTrader() {
        SpiritJobType type = this.getJobType();
        return type == SpiritJobType.TRADER_OTHERSTONE || type == SpiritJobType.TRADER_OTHERROCK
                || type == SpiritJobType.TRADER_SAPLING || type == SpiritJobType.GAMBLER;
    }

    private int getPickupCountForJob(ItemStack stack) {
        if (this.getJobType() == SpiritJobType.CLEANER) {
            return stack.getCount();
        }
        SpiritTradeRecipeRegistry.Recipe trade = this.getTradeRecipe();
        if (trade != null) {
            ItemStack held = this.getHeldItemMainhand();
            if (!held.isEmpty() && !this.canMergeStacks(held, stack)) {
                return 0;
            }
            int capacity = Math.min(stack.getMaxStackSize(),
                    trade.getIngredients().size() * this.getTraderOperationCount());
            return Math.max(0, capacity - held.getCount());
        }
        return this.isTrader() ? this.getTraderOperationCount() : this.getProcessingOperationCount();
    }

    private int getTraderOperationCount() {
        SpiritTradeRecipeRegistry.Recipe recipe =
                this.getTradeRecipe();
        if (SpiritTradeRecipeRegistry.isTradeJob(this.getJobType()) && this.tradeLimitOverride > 0) {
            return this.tradeLimitOverride;
        }
        if (recipe != null) {
            return recipe.getMaxTradesPerRound();
        }
        return 1;
    }

    private int getProcessTime() {
        if (SpiritTradeRecipeRegistry.isTradeJob(this.getJobType())) {
            return this.getTimeToConvert() * 20;
        }
        if (this.getJobType() == SpiritJobType.GAMBLER) {
            return PROCESS_TIME;
        }
        ModConfig.SpiritJobSettings.ProcessingSpiritSettings settings = this.getProcessingSettings();
        if (settings == null) {
            return this.getJobType() == SpiritJobType.SMELTER ? PROCESS_TIME * 2 : PROCESS_TIME;
        }
        int baseTime = this.getJobType() == SpiritJobType.SMELTER ? PROCESS_TIME * 2 : PROCESS_TIME;
        return Math.max(1, (int) Math.round(baseTime * settings.timeMultiplier));
    }

    private int getProcessingOperationCount() {
        ModConfig.SpiritJobSettings.ProcessingSpiritSettings settings = this.getProcessingSettings();
        return settings == null ? 1 : Math.max(1, settings.operationCount);
    }

    private float getProcessingOutputMultiplier(SpiritJobRecipe recipe) {
        if (recipe == null || this.getJobType() == SpiritJobType.SMELTER) {
            return 1.0F;
        }
        ModConfig.SpiritJobSettings.ProcessingSpiritSettings settings = this.getProcessingSettings();
        return settings == null ? 1.0F : Math.max(0.0F, (float) settings.outputMultiplier);
    }

    @Nullable
    private ModConfig.SpiritJobSettings.ProcessingSpiritSettings getProcessingSettings() {
        if (this.getJobType() == SpiritJobType.CRUSHER) {
            switch (this.getTier()) {
                case DJINNI:
                    return ModConfig.SPIRIT_JOBS.crusherDjinni;
                case AFRIT:
                    return ModConfig.SPIRIT_JOBS.crusherAfrit;
                case MARID:
                case ANCIENT:
                    return ModConfig.SPIRIT_JOBS.crusherMarid;
                default:
                    return ModConfig.SPIRIT_JOBS.crusherFoliot;
            }
        }
        if (this.getJobType() == SpiritJobType.CRYSTALLIZER) {
            switch (this.getTier()) {
                case DJINNI:
                    return ModConfig.SPIRIT_JOBS.crystallizerDjinni;
                case AFRIT:
                    return ModConfig.SPIRIT_JOBS.crystallizerAfrit;
                case MARID:
                case ANCIENT:
                    return ModConfig.SPIRIT_JOBS.crystallizerMarid;
                default:
                    return ModConfig.SPIRIT_JOBS.crystallizerFoliot;
            }
        }
        if (this.getJobType() == SpiritJobType.SMELTER) {
            switch (this.getTier()) {
                case DJINNI:
                    return ModConfig.SPIRIT_JOBS.smelterDjinni;
                case AFRIT:
                    return ModConfig.SPIRIT_JOBS.smelterAfrit;
                case MARID:
                case ANCIENT:
                    return ModConfig.SPIRIT_JOBS.smelterMarid;
                default:
                    return ModConfig.SPIRIT_JOBS.smelterFoliot;
            }
        }
        return null;
    }

    private void dropHeldItem() {
        ItemStack held = this.getHeldItemMainhand();
        if (held.isEmpty()) {
            return;
        }
        this.setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);
        this.spawnStacks(held, true);
        this.workTimer = 0;
        this.currentRecipe = null;
    }

    private void dropSoulShard() {
        this.droppedSoulShard = true;
        ItemStack shard = new ItemStack(ModItems.SOUL_SHARD);
        if (!ItemSoulShard.storeEntity(shard, this)) {
            return;
        }
        EntityItem shardEntity = new EntityItem(this.world, this.posX, this.posY + 0.5D, this.posZ, shard);
        shardEntity.setPickupDelay(5);
        shardEntity.motionX = 0.0D;
        shardEntity.motionZ = 0.0D;
        this.spawnEntityItem(shardEntity);
    }

    private void outputProcessedStack(ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }

        ItemStack remaining = stack.copy();
        TileEntity configuredTarget = this.getConfiguredDepositInventory();
        if (InventoryAccess.isInventory(configuredTarget)) {
            remaining = InventoryAccess.insert(configuredTarget, remaining);
            if (remaining.isEmpty()) {
                this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.BLOCK_CHEST_OPEN,
                        SoundCategory.NEUTRAL, 0.25F, 1.5F);
                this.spawnServerWorkParticles();
                return;
            }
        }

        remaining = this.tryOutputToDimensionalExtractor(remaining);
        if (remaining.isEmpty()) {
            return;
        }
        this.spawnStacks(remaining, true);
    }

    private ItemStack tryOutputToDimensionalExtractor(ItemStack stack) {
        BlockPos extractorPos = this.getPosition().down();
        if (this.world.getBlockState(extractorPos).getBlock() != ModBlocks.DIMENSIONAL_EXTRACTOR) {
            return stack;
        }
        TileEntity target = this.world.getTileEntity(extractorPos.down());
        if (!InventoryAccess.isInventory(target, EnumFacing.UP)) {
            return stack;
        }

        ItemStack remaining = InventoryAccess.insert(target, stack, EnumFacing.UP);
        target.markDirty();
        if (remaining.getCount() < stack.getCount()) {
            this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT,
                    SoundCategory.NEUTRAL, 0.25F, 1.8F);
            this.spawnServerWorkParticles();
        }
        return remaining;
    }

    private void spawnStacks(ItemStack stack, boolean markAsJobDrop) {
        int remaining = stack.getCount();
        int maxStackSize = stack.getMaxStackSize();
        while (remaining > 0) {
            ItemStack split = stack.copy();
            split.setCount(Math.min(remaining, maxStackSize));
            EntityItem result = new EntityItem(this.world, this.posX, this.posY + 0.4D, this.posZ, split);
            result.motionY = 0.12D;
            result.motionX = (this.rand.nextDouble() - 0.5D) * 0.08D;
            result.motionZ = (this.rand.nextDouble() - 0.5D) * 0.08D;
            result.setPickupDelay(10);
            if (markAsJobDrop) {
                result.getEntityData().setString(DROP_JOB_KEY, this.getJobType().name());
                result.getEntityData().setLong(DROP_TICK_KEY, this.world.getTotalWorldTime());
            }
            this.spawnEntityItem(result);
            remaining -= split.getCount();
        }
    }

    private void spawnEntityItem(EntityItem item) {
        if (this.captureDrops) {
            this.capturedDrops.add(item);
        } else {
            this.world.spawnEntity(item);
        }
    }

    private void playWorkSound() {
        if (this.getJobType() == SpiritJobType.CRUSHER) {
            this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.BLOCK_GRAVEL_BREAK,
                    SoundCategory.NEUTRAL, 0.5F, 0.9F + this.rand.nextFloat() * 0.3F);
        } else if (this.getJobType() == SpiritJobType.CRYSTALLIZER) {
            this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.BLOCK_GLASS_HIT,
                    SoundCategory.NEUTRAL, 0.5F, 1.2F + this.rand.nextFloat() * 0.3F);
        } else if (this.isTrader()) {
            this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE,
                    SoundCategory.NEUTRAL, 0.45F, 1.1F + this.rand.nextFloat() * 0.3F);
        } else {
            this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.BLOCK_FIRE_AMBIENT,
                    SoundCategory.NEUTRAL, 0.5F, 1.0F + this.rand.nextFloat() * 0.3F);
        }
    }

    private void spawnServerWorkParticles() {
        if (!(this.world instanceof WorldServer)) {
            return;
        }
        EnumParticleTypes particle = this.getWorkParticle();
        ((WorldServer) this.world).spawnParticle(particle, this.posX, this.posY + 0.7D, this.posZ, 2, 0.25D, 0.2D,
                0.25D, 0.01D);
    }

    private void debugAI(String message, @Nullable BlockPos pos, EnumParticleTypes particle) {
        if (!Occultism.DEBUG.debugAI || this.world == null || this.world.isRemote) {
            return;
        }
        if (message.equals(this.lastDebugMessage) && this.ticksExisted - this.lastDebugMessageTick < 40) {
            return;
        }
        this.lastDebugMessage = message;
        this.lastDebugMessageTick = this.ticksExisted;
        Occultism.LOGGER.info("[AI Debug] {} {} at {}: {}", this.getTier().getId(), this.getJobType().getId(),
                this.getPosition(), message);
        if (pos != null && this.world instanceof WorldServer) {
            ((WorldServer) this.world).spawnParticle(particle, pos.getX() + 0.5D, pos.getY() + 0.5D,
                    pos.getZ() + 0.5D, 8, 0.2D, 0.2D, 0.2D, 0.02D);
        }
    }

    private static String describeStack(ItemStack stack) {
        if (stack.isEmpty()) {
            return "empty";
        }
        return stack.getCount() + "x " + stack.getDisplayName();
    }

    private void spawnClientWorkParticles() {
        if (this.getHeldItemMainhand().isEmpty() || this.ticksExisted % 10 != 0) {
            return;
        }
        EnumParticleTypes particle = this.getWorkParticle();
        this.world.spawnParticle(particle, this.posX + (this.rand.nextDouble() - 0.5D) * 0.4D,
                this.posY + 0.7D, this.posZ + (this.rand.nextDouble() - 0.5D) * 0.4D, 0.0D, 0.01D, 0.0D);
    }

    private EnumParticleTypes getWorkParticle() {
        if (this.getJobType() == SpiritJobType.CRUSHER) {
            return EnumParticleTypes.CRIT;
        }
        if (this.getJobType() == SpiritJobType.CRYSTALLIZER) {
            return EnumParticleTypes.SPELL_WITCH;
        }
        if (this.isTrader()) {
            return EnumParticleTypes.PORTAL;
        }
        return EnumParticleTypes.FLAME;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("JobType", this.getJobType().ordinal());
        compound.setInteger("SpiritTier", this.getTier().ordinal());
        compound.setInteger("WorkTimer", this.workTimer);
        if (this.getWorldEffectJob() != null) {
            compound.setTag("spiritJob", this.worldEffectJob.writeToNBT());
            if (this.worldEffectCaster != null) {
                compound.setUniqueId("WorldEffectCaster", this.worldEffectCaster);
            } else {
                compound.removeTag("WorldEffectCaster");
            }
        } else {
            compound.removeTag("spiritJob");
            compound.removeTag("WorldEffectCaster");
        }
        if (SpiritTradeRecipeRegistry.isTradeJob(this.getJobType())) {
            compound.setString("spiritTradeId", this.getSpiritTradeId());
            compound.setInteger("timeToConvert", this.getTimeToConvert());
            compound.setInteger("conversionTimer", this.workTimer / 20);
            compound.setInteger("maxTradesPerRound", this.getTraderOperationCount());
        } else {
            compound.removeTag("spiritTradeId");
            compound.removeTag("timeToConvert");
            compound.removeTag("conversionTimer");
            compound.removeTag("maxTradesPerRound");
        }
        compound.setInteger("SpiritAge", this.getSpiritAge());
        compound.setInteger("SpiritMaxAge", this.getSpiritMaxAge());
        compound.setBoolean("isFilterBlacklist", this.isFilterBlacklist());
        compound.setTag("filterItems", this.filterItems.serializeNBT());
        compound.setString("tagFilter", this.getTagFilter());
        compound.setInteger("WorkAreaSize", this.workAreaSize.getValue());
        writeBlockPos(compound, "WorkArea", this.workAreaPos);
        writeBlockPos(compound, "Deposit", this.depositPos);
        writeBlockPos(compound, "Extract", this.extractPos);
        writeBlockPos(compound, "TransporterSource", this.transporterSourcePos);
        writeBlockPos(compound, "TransporterTarget", this.transporterTargetPos);
        writeBlockPos(compound, "StorageController", this.storageControllerPos);
        compound.setInteger("StorageControllerDimension", this.storageControllerDimension);
        compound.setString("ManageMachineState", this.manageMachineState.name());
        if (this.managedMachine != null) {
            compound.setTag("ManagedMachine", this.managedMachine.writeToNBT(new NBTTagCompound()));
        }
        if (this.currentDepositOrder != null && !this.currentDepositOrder.isComplete()) {
            compound.setTag("CurrentDepositOrder", this.currentDepositOrder.writeToNBT(new NBTTagCompound()));
        }
        NBTTagList orders = new NBTTagList();
        for (DepositOrder order : this.depositOrderQueue) {
            if (order != null && !order.isComplete()) {
                orders.appendTag(order.writeToNBT(new NBTTagCompound()));
            }
        }
        compound.setTag("DepositOrders", orders);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        int jobType = compound.getInteger("JobType");
        SpiritJobType[] values = SpiritJobType.values();
        this.setJobType(jobType >= 0 && jobType < values.length ? values[jobType] : SpiritJobType.CRUSHER);
        int tier = compound.hasKey("SpiritTier") ? compound.getInteger("SpiritTier") : SpiritTier.FOLIOT.ordinal();
        SpiritTier[] tiers = SpiritTier.values();
        this.setTier(tier >= 0 && tier < tiers.length ? tiers[tier] : SpiritTier.FOLIOT);
        this.workTimer = compound.getInteger("WorkTimer");
        this.worldEffectJob = this.getJobType().isWorldEffectJob() ? new WorldEffectJob(this.getJobType()) : null;
        if (this.worldEffectJob != null && compound.hasKey("spiritJob", 10)) {
            this.worldEffectJob.readFromNBT(compound.getCompoundTag("spiritJob"));
        }
        this.worldEffectCaster = compound.hasUniqueId("WorldEffectCaster")
                ? compound.getUniqueId("WorldEffectCaster") : null;
        this.spiritTradeId = null;
        this.tradeTimeOverride = -1;
        this.tradeLimitOverride = -1;
        this.currentTrade = null;
        if (SpiritTradeRecipeRegistry.isTradeJob(this.getJobType())) {
            if (compound.hasKey("spiritTradeId", 8)) {
                String canonical = SpiritTradeRecipeRegistry.canonicalId(compound.getString("spiritTradeId"));
                // Keep malformed explicit selections unresolved instead of silently performing a default trade.
                this.spiritTradeId = canonical == null ? compound.getString("spiritTradeId") : canonical;
            }
            if (compound.hasKey("timeToConvert", 3)) {
                this.tradeTimeOverride = Math.max(1, Math.min(SpiritTradeRecipeRegistry.MAX_CONVERSION_STEPS,
                        compound.getInteger("timeToConvert")));
            }
            if (compound.hasKey("maxTradesPerRound", 3)) {
                this.tradeLimitOverride = Math.max(1, Math.min(SpiritTradeRecipeRegistry.MAX_TRADES_PER_ROUND,
                        compound.getInteger("maxTradesPerRound")));
            }
            int seconds = compound.hasKey("conversionTimer", 3)
                    ? compound.getInteger("conversionTimer") : this.workTimer / 20;
            this.workTimer = Math.max(0, Math.min(seconds, this.getTimeToConvert() - 1)) * 20;
            this.currentTrade = this.getTradeRecipe();
        }
        this.setSpiritAge(compound.getInteger("SpiritAge"));
        this.setSpiritMaxAge(compound.hasKey("SpiritMaxAge") ? compound.getInteger("SpiritMaxAge") : -1);
        boolean hasFilterData = compound.hasKey("isFilterBlacklist", 1)
                || compound.hasKey("filterItems", 10) || compound.hasKey("tagFilter", 8);
        this.setFilterBlacklist(compound.hasKey("isFilterBlacklist", 1)
                ? compound.getBoolean("isFilterBlacklist") : !hasFilterData);
        this.loadFilterItems(compound.hasKey("filterItems", 10)
                ? compound.getCompoundTag("filterItems") : new NBTTagCompound());
        this.setTagFilter(compound.hasKey("tagFilter", 8) ? compound.getString("tagFilter") : "");
        this.workAreaSize = compound.hasKey("WorkAreaSize")
                ? WorkAreaSize.fromPersistedValue(compound.getInteger("WorkAreaSize")) : WorkAreaSize.SMALL;
        this.workAreaPos = readBlockPos(compound, "WorkArea");
        this.depositPos = readBlockPos(compound, "Deposit");
        this.extractPos = readBlockPos(compound, "Extract");
        this.transporterSourcePos = readBlockPos(compound, "TransporterSource");
        this.transporterTargetPos = readBlockPos(compound, "TransporterTarget");
        this.storageControllerPos = readBlockPos(compound, "StorageController");
        this.storageControllerDimension = compound.getInteger("StorageControllerDimension");
        this.manageMachineState = readManageMachineState(compound.getString("ManageMachineState"));
        this.managedMachine = compound.hasKey("ManagedMachine", 10)
                ? MachineReference.from(compound.getCompoundTag("ManagedMachine")) : null;
        this.currentDepositOrder = compound.hasKey("CurrentDepositOrder", 10)
                ? DepositOrder.from(compound.getCompoundTag("CurrentDepositOrder")) : null;
        this.depositOrderQueue = new ArrayDeque<>();
        if (compound.hasKey("DepositOrders", 9)) {
            NBTTagList orders = compound.getTagList("DepositOrders", 10);
            for (int index = 0; index < orders.tagCount() && this.depositOrderQueue.size() < MANAGE_MACHINE_MAX_ORDERS;
                    index++) {
                DepositOrder order = DepositOrder.from(orders.getCompoundTagAt(index));
                if (order != null && !order.isComplete()) {
                    this.depositOrderQueue.add(order);
                }
            }
        }
        this.currentRecipe = this.findSpiritJobRecipe(this.getHeldItemMainhand());
    }

    @Override
    public void notifyDataManagerChange(DataParameter<?> key) {
        super.notifyDataManagerChange(key);
        if (FILTER_ITEMS.equals(key) && this.world != null && this.world.isRemote) {
            this.loadFilterItems(this.dataManager.get(FILTER_ITEMS));
        }
    }

    private static ManageMachineState readManageMachineState(@Nullable String value) {
        if (value == null || value.isEmpty()) {
            return ManageMachineState.IDLE;
        }
        try {
            return ManageMachineState.valueOf(value);
        } catch (IllegalArgumentException ignored) {
            return ManageMachineState.IDLE;
        }
    }

    private static void writeBlockPos(NBTTagCompound compound, String key, @Nullable BlockPos pos) {
        if (pos == null) {
            return;
        }
        compound.setInteger(key + "X", pos.getX());
        compound.setInteger(key + "Y", pos.getY());
        compound.setInteger(key + "Z", pos.getZ());
    }

    @Nullable
    private static BlockPos readBlockPos(NBTTagCompound compound, String key) {
        String xKey = key + "X";
        String yKey = key + "Y";
        String zKey = key + "Z";
        if (!compound.hasKey(xKey) || !compound.hasKey(yKey) || !compound.hasKey(zKey)) {
            return null;
        }
        return new BlockPos(compound.getInteger(xKey), compound.getInteger(yKey), compound.getInteger(zKey));
    }
}
