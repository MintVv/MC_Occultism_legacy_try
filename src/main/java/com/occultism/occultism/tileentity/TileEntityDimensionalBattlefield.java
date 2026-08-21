package com.occultism.occultism.tileentity;

import com.google.common.collect.Multimap;
import com.occultism.occultism.item.ItemButcherKnife;
import com.occultism.occultism.item.ItemSoulGem;
import com.occultism.occultism.item.ItemSoulShard;
import com.occultism.occultism.registry.ModBlocks;
import com.occultism.occultism.registry.ModItems;
import com.occultism.occultism.util.InventoryAccess;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

public class TileEntityDimensionalBattlefield extends TileEntity implements ITickable, ISidedInventory {

    public static final int SOUL_SLOT = 0;
    public static final int WEAPON_SLOT = 1;
    public static final int FUEL_SLOT = 2;
    public static final int FIRST_OUTPUT_SLOT = 3;
    public static final int OUTPUT_SLOTS = 25;
    public static final int SLOT_COUNT = FIRST_OUTPUT_SLOT + OUTPUT_SLOTS;

    private static final int[] INPUT_SLOTS = {SOUL_SLOT, WEAPON_SLOT, FUEL_SLOT};
    private static final int[] OUTPUT_SLOT_IDS = createOutputSlots();
    private static final int[] ALL_SLOTS = createAllSlots();
    private static final String TAG_ENTITY = "OccultismEntity";
    private static final String TAG_ENTITY_ID = "EntityId";
    private static final int MAX_LUCK = 16;

    private NonNullList<ItemStack> stacks = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private int battleTime;
    private int maxBattleTime;
    private int fuelCost;
    private final IItemHandlerModifiable itemHandlerTop = new SidedInvWrapper(this, EnumFacing.UP);
    private final IItemHandlerModifiable itemHandlerBottom = new SidedInvWrapper(this, EnumFacing.DOWN);
    private final IItemHandlerModifiable itemHandlerSide = new SidedInvWrapper(this, EnumFacing.NORTH);
    private final IItemHandler itemHandlerAnySide = new CombinedInvWrapper(this.itemHandlerTop, this.itemHandlerBottom,
            this.itemHandlerSide);

    @Override
    public void update() {
        if (this.world == null || this.world.isRemote) {
            return;
        }

        ItemStack soul = this.getStackInSlot(SOUL_SLOT);
        ItemStack weapon = this.getStackInSlot(WEAPON_SLOT);
        if (!this.isValidSoul(soul) || !this.isValidWeapon(weapon)) {
            this.resetBattle();
            return;
        }

        EntityLivingBase target = this.createLivingEntity(soul);
        if (target == null) {
            this.resetBattle();
            return;
        }

        int requiredFuel = this.getRequiredFuel(soul, target);
        if (!this.hasFuelFor(requiredFuel)) {
            this.resetBattle();
            return;
        }

        if (this.world.isBlockPowered(this.pos)) {
            return;
        }

        if (this.maxBattleTime <= 0 || this.battleTime <= 0 || this.fuelCost != requiredFuel) {
            this.maxBattleTime = this.getMaxBattleTime(target, weapon);
            this.battleTime = this.maxBattleTime;
            this.fuelCost = requiredFuel;
            this.markDirty();
            return;
        }

        this.battleTime--;
        if (this.battleTime <= 0) {
            this.completeBattle(soul, weapon);
            this.maxBattleTime = this.getMaxBattleTime(target, weapon);
            this.battleTime = this.maxBattleTime;
        }

        if (this.world.getTotalWorldTime() % 20L == 0L) {
            this.markDirty();
        }
    }

    private void completeBattle(ItemStack soul, ItemStack weapon) {
        EntityLivingBase target = this.createLivingEntity(soul);
        if (target == null) {
            this.resetBattle();
            return;
        }

        int rolls = this.getRolls(soul);
        for (int i = 0; i < rolls; i++) {
            for (ItemStack drop : this.captureDrops(target, weapon)) {
                this.outputStack(drop);
            }
            target = this.createLivingEntity(soul);
            if (target == null) {
                break;
            }
        }

        this.consumeFuelOrSoul(soul);
        this.damageWeapon(weapon);
        this.markDirty();
    }

    private List<ItemStack> captureDrops(EntityLivingBase target, ItemStack weapon) {
        List<ItemStack> drops = new ArrayList<>();
        if (!(this.world instanceof WorldServer)) {
            return drops;
        }
        FakePlayer fakePlayer = FakePlayerFactory.getMinecraft((WorldServer) this.world);
        ItemStack weaponCopy = weapon.copy();
        weaponCopy.setCount(1);
        fakePlayer.setHeldItem(EnumHand.MAIN_HAND, weaponCopy);
        fakePlayer.setPosition(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D);

        target.dimension = this.world.provider.getDimension();
        target.setPosition(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D);
        target.setHealth(Math.max(1.0F, target.getMaxHealth()));

        DropCapture capture = new DropCapture(target);
        MinecraftForge.EVENT_BUS.register(capture);
        try {
            target.attackEntityFrom(DamageSource.causePlayerDamage(fakePlayer), Float.MAX_VALUE);
        } finally {
            MinecraftForge.EVENT_BUS.unregister(capture);
        }
        for (EntityItem item : capture.getDrops()) {
            if (!item.getItem().isEmpty()) {
                drops.add(item.getItem().copy());
            }
        }
        return drops;
    }

    private void outputStack(ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        ItemStack remaining = this.tryOutputToExtractor(stack);
        if (!remaining.isEmpty()) {
            remaining = this.insertIntoOutputSlots(remaining);
        }
        // Upstream battlefield semantics discard overflow. The comparator helps automate pausing before this happens.
    }

    private ItemStack tryOutputToExtractor(ItemStack stack) {
        TileEntity target = this.getExtractorOutputTarget();
        return target == null ? stack.copy() : InventoryAccess.insert(target, stack, EnumFacing.UP);
    }

    @Nullable
    private TileEntity getExtractorOutputTarget() {
        if (this.world.getBlockState(this.pos.down()).getBlock() != ModBlocks.DIMENSIONAL_EXTRACTOR) {
            return null;
        }
        TileEntity tile = this.world.getTileEntity(this.pos.down(2));
        return InventoryAccess.isInventory(tile, EnumFacing.UP) ? tile : null;
    }

    private ItemStack insertIntoOutputSlots(ItemStack stack) {
        ItemStack remaining = stack.copy();
        for (int slot = FIRST_OUTPUT_SLOT; slot < SLOT_COUNT && !remaining.isEmpty(); slot++) {
            ItemStack existing = this.getStackInSlot(slot);
            if (existing.isEmpty()) {
                int insertCount = Math.min(remaining.getCount(), remaining.getMaxStackSize());
                ItemStack inserted = remaining.copy();
                inserted.setCount(insertCount);
                this.setInventorySlotContents(slot, inserted);
                remaining.shrink(insertCount);
                continue;
            }
            if (!this.canMergeStacks(existing, remaining)) {
                continue;
            }
            int insertCount = Math.min(remaining.getCount(), existing.getMaxStackSize() - existing.getCount());
            if (insertCount <= 0) {
                continue;
            }
            existing.grow(insertCount);
            this.setInventorySlotContents(slot, existing);
            remaining.shrink(insertCount);
        }
        return remaining;
    }

    private boolean canMergeStacks(ItemStack existing, ItemStack incoming) {
        return ItemStack.areItemsEqual(existing, incoming) && ItemStack.areItemStackTagsEqual(existing, incoming);
    }

    private void consumeFuelOrSoul(ItemStack soul) {
        if (soul.getItem() == ModItems.SOUL_SHATTERED) {
            if (this.world.rand.nextFloat() < 0.33F) {
                soul.shrink(1);
                if (soul.isEmpty()) {
                    this.setInventorySlotContents(SOUL_SLOT, ItemStack.EMPTY);
                }
            }
            return;
        }

        ItemStack fuel = this.getStackInSlot(FUEL_SLOT);
        int fuelValue = this.getFuelValue(fuel);
        if (fuelValue <= 0) {
            return;
        }
        int amount = Math.max(1, (this.fuelCost + fuelValue - 1) / fuelValue);
        fuel.shrink(amount);
        if (fuel.isEmpty()) {
            this.setInventorySlotContents(FUEL_SLOT, ItemStack.EMPTY);
        } else {
            this.setInventorySlotContents(FUEL_SLOT, fuel);
        }
    }

    private void damageWeapon(ItemStack weapon) {
        if (weapon.isEmpty() || !weapon.isItemStackDamageable()) {
            return;
        }
        weapon.attemptDamageItem(1, this.world.rand, null);
        if (weapon.getItemDamage() >= weapon.getMaxDamage()) {
            this.setInventorySlotContents(WEAPON_SLOT, ItemStack.EMPTY);
        } else {
            this.setInventorySlotContents(WEAPON_SLOT, weapon);
        }
    }

    private int getRequiredFuel(ItemStack soul, EntityLivingBase target) {
        if (soul.getItem() == ModItems.SOUL_SHATTERED) {
            return 0;
        }
        return Math.max(1, (int) Math.ceil(target.getMaxHealth()));
    }

    private boolean hasFuelFor(int requiredFuel) {
        if (requiredFuel <= 0) {
            return true;
        }
        ItemStack fuel = this.getStackInSlot(FUEL_SLOT);
        int value = this.getFuelValue(fuel);
        return value > 0 && value * fuel.getCount() >= requiredFuel;
    }

    private int getFuelValue(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        if (stack.getItem() == ModItems.DATURA_SEEDS) {
            return 1;
        }
        if (stack.getItem() == ModItems.DATURA) {
            return 2;
        }
        if (stack.getItem() == ModItems.DEMONS_DREAM_ESSENCE) {
            return 20;
        }
        if (stack.getItem() == ModItems.OTHERWORLD_ESSENCE) {
            return 32;
        }
        return 0;
    }

    private int getRolls(ItemStack soul) {
        if (soul.getItem() == ModItems.TRINITY_GEM) {
            return 3;
        }
        int luck = Math.min(MAX_LUCK, Math.max(0, EnchantmentHelper.getEnchantmentLevel(Enchantments.LOOTING,
                this.getStackInSlot(WEAPON_SLOT))));
        return 1 + (luck >= 3 && this.world.rand.nextInt(4) == 0 ? 1 : 0);
    }

    private int getMaxBattleTime(EntityLivingBase target, ItemStack weapon) {
        double attackDamage = Math.max(1.0D, this.getAttackDamage(weapon));
        double attackSpeed = Math.max(0.2D, this.getAttackSpeed(weapon));
        int hits = Math.max(1, (int) Math.ceil(target.getMaxHealth() / attackDamage));
        int hitTicks = Math.max(4, (int) Math.ceil(40.0D / attackSpeed));
        return Math.max(20, hits * hitTicks);
    }

    private double getAttackDamage(ItemStack stack) {
        double attackDamage = 1.0D;
        for (AttributeModifier modifier : stack.getAttributeModifiers(net.minecraft.inventory.EntityEquipmentSlot.MAINHAND)
                .get(SharedMonsterAttributes.ATTACK_DAMAGE.getName())) {
            attackDamage += this.getModifierAmount(attackDamage, modifier);
        }
        int sharpness = EnchantmentHelper.getEnchantmentLevel(Enchantments.SHARPNESS, stack);
        return attackDamage + sharpness;
    }

    private double getAttackSpeed(ItemStack stack) {
        double attackSpeed = 4.0D;
        for (AttributeModifier modifier : stack.getAttributeModifiers(net.minecraft.inventory.EntityEquipmentSlot.MAINHAND)
                .get(SharedMonsterAttributes.ATTACK_SPEED.getName())) {
            attackSpeed += this.getModifierAmount(attackSpeed, modifier);
        }
        return attackSpeed;
    }

    private double getModifierAmount(double base, AttributeModifier modifier) {
        if (modifier.getOperation() == 0) {
            return modifier.getAmount();
        }
        if (modifier.getOperation() == 1) {
            return base * modifier.getAmount();
        }
        if (modifier.getOperation() == 2) {
            return base * modifier.getAmount();
        }
        return 0.0D;
    }

    private boolean isValidSoul(ItemStack stack) {
        return ItemSoulGem.hasEntity(stack) || ItemSoulShard.hasStoredEntity(stack);
    }

    private boolean isValidWeapon(ItemStack stack) {
        if (stack.isEmpty() || !stack.isItemStackDamageable() || stack.getMaxDamage() <= 0) {
            return false;
        }
        if (stack.getItem() instanceof ItemButcherKnife || stack.getItem() instanceof ItemSword
                || stack.getItem() instanceof ItemAxe) {
            return true;
        }
        Multimap<String, AttributeModifier> modifiers = stack.getAttributeModifiers(
                net.minecraft.inventory.EntityEquipmentSlot.MAINHAND);
        return modifiers.containsKey(SharedMonsterAttributes.ATTACK_DAMAGE.getName());
    }

    private boolean isValidFuel(ItemStack stack) {
        return this.getFuelValue(stack) > 0;
    }

    @Nullable
    private EntityLivingBase createLivingEntity(ItemStack stack) {
        Entity entity = null;
        if (ItemSoulShard.hasStoredEntity(stack)) {
            entity = ItemSoulShard.createStoredEntity(stack, this.world);
        } else if (ItemSoulGem.hasEntity(stack) && stack.hasTagCompound()) {
            NBTTagCompound entityData = stack.getTagCompound().getCompoundTag(TAG_ENTITY).copy();
            String id = stack.getTagCompound().getString(TAG_ENTITY_ID);
            if (!id.isEmpty()) {
                entityData.setString("id", id);
            }
            entityData.removeTag("Pos");
            entityData.removeTag("Motion");
            entityData.removeTag("Rotation");
            entity = EntityList.createEntityFromNBT(entityData, this.world);
        }
        return entity instanceof EntityLivingBase ? (EntityLivingBase) entity : null;
    }

    private void resetBattle() {
        this.battleTime = 0;
        this.maxBattleTime = 0;
        this.fuelCost = 0;
    }

    public int getRedstoneSignal() {
        int signal = 0;
        ItemStack weapon = this.getStackInSlot(WEAPON_SLOT);
        if (!weapon.isEmpty() && weapon.isItemStackDamageable()) {
            signal = Math.max(signal, (int) (15.0F * weapon.getItemDamage()
                    / Math.max(1, weapon.getMaxDamage() - 1)));
        }
        int occupied = 0;
        for (ItemStack stack : this.stacks) {
            if (!stack.isEmpty()) {
                occupied++;
            }
        }
        return Math.max(signal, Math.min(15, occupied * 15 / SLOT_COUNT));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        ItemStackHelper.saveAllItems(compound, this.stacks);
        compound.setInteger("BattleTime", this.battleTime);
        compound.setInteger("MaxBattleTime", this.maxBattleTime);
        compound.setInteger("FuelCost", this.fuelCost);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.stacks = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
        ItemStackHelper.loadAllItems(compound, this.stacks);
        this.battleTime = compound.getInteger("BattleTime");
        this.maxBattleTime = compound.getInteger("MaxBattleTime");
        this.fuelCost = compound.getInteger("FuelCost");
    }

    @Override
    public String getName() {
        return "container.occultism.dimensional_battlefield";
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TextComponentTranslation(this.getName());
    }

    @Override
    public int getSizeInventory() {
        return SLOT_COUNT;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : this.stacks) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return index >= 0 && index < this.stacks.size() ? this.stacks.get(index) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        ItemStack stack = ItemStackHelper.getAndSplit(this.stacks, index, count);
        if (!stack.isEmpty()) {
            if (index == SOUL_SLOT || index == WEAPON_SLOT || index == FUEL_SLOT) {
                this.resetBattle();
            }
            this.markDirty();
        }
        return stack;
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        ItemStack stack = ItemStackHelper.getAndRemove(this.stacks, index);
        if (!stack.isEmpty()) {
            if (index == SOUL_SLOT || index == WEAPON_SLOT || index == FUEL_SLOT) {
                this.resetBattle();
            }
            this.markDirty();
        }
        return stack;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        this.stacks.set(index, stack);
        if (!stack.isEmpty() && stack.getCount() > this.getInventoryStackLimit()) {
            stack.setCount(this.getInventoryStackLimit());
        }
        if (index == SOUL_SLOT || index == WEAPON_SLOT || index == FUEL_SLOT) {
            this.resetBattle();
        }
        this.markDirty();
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return this.world.getTileEntity(this.pos) == this
                && player.getDistanceSq(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D,
                        this.pos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void openInventory(EntityPlayer player) {
    }

    @Override
    public void closeInventory(EntityPlayer player) {
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        if (index == SOUL_SLOT) {
            return this.isValidSoul(stack);
        }
        if (index == WEAPON_SLOT) {
            return this.isValidWeapon(stack);
        }
        if (index == FUEL_SLOT) {
            return this.isValidFuel(stack);
        }
        return false;
    }

    @Override
    public int getField(int id) {
        if (id == 0) {
            return this.battleTime;
        }
        if (id == 1) {
            return this.maxBattleTime;
        }
        if (id == 2) {
            return this.fuelCost;
        }
        return 0;
    }

    @Override
    public void setField(int id, int value) {
        if (id == 0) {
            this.battleTime = value;
        } else if (id == 1) {
            this.maxBattleTime = value;
        } else if (id == 2) {
            this.fuelCost = value;
        }
    }

    @Override
    public int getFieldCount() {
        return 3;
    }

    @Override
    public void clear() {
        this.stacks = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
        this.resetBattle();
    }

    @Override
    public int[] getSlotsForFace(EnumFacing side) {
        if (side == EnumFacing.UP) {
            return INPUT_SLOTS;
        }
        if (side == EnumFacing.DOWN) {
            return OUTPUT_SLOT_IDS;
        }
        return ALL_SLOTS;
    }

    @Override
    public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
        return index >= SOUL_SLOT && index <= FUEL_SLOT && direction != EnumFacing.DOWN
                && this.isItemValidForSlot(index, itemStackIn);
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
        return direction != EnumFacing.UP && (index >= FIRST_OUTPUT_SLOT || direction != EnumFacing.DOWN);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override
    @Nullable
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (facing == null) {
                return (T) this.itemHandlerAnySide;
            }
            if (facing == EnumFacing.UP) {
                return (T) this.itemHandlerTop;
            }
            if (facing == EnumFacing.DOWN) {
                return (T) this.itemHandlerBottom;
            }
            return (T) this.itemHandlerSide;
        }
        return super.getCapability(capability, facing);
    }

    private static int[] createOutputSlots() {
        int[] slots = new int[OUTPUT_SLOTS];
        for (int i = 0; i < slots.length; i++) {
            slots[i] = FIRST_OUTPUT_SLOT + i;
        }
        return slots;
    }

    private static int[] createAllSlots() {
        int[] slots = new int[SLOT_COUNT];
        for (int i = 0; i < slots.length; i++) {
            slots[i] = i;
        }
        return slots;
    }

    private static final class DropCapture {

        private final UUID targetId;
        private final List<EntityItem> drops = new ArrayList<>();

        private DropCapture(EntityLivingBase target) {
            this.targetId = target.getUniqueID();
        }

        @SubscribeEvent
        public void onLivingDrops(LivingDropsEvent event) {
            if (!this.targetId.equals(event.getEntityLiving().getUniqueID())) {
                return;
            }
            this.drops.addAll(event.getDrops());
            event.setCanceled(true);
        }

        private List<EntityItem> getDrops() {
            return this.drops;
        }
    }
}
