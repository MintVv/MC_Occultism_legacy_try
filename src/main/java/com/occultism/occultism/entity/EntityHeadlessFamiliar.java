package com.occultism.occultism.entity;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.occultism.occultism.advancement.FamiliarTrigger;
import com.occultism.occultism.registry.ModAdvancements;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIFollowOwner;
import net.minecraft.entity.ai.EntityAIFollow;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAISit;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityWitherSkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.oredict.OreDictionary;

public class EntityHeadlessFamiliar extends EntityFamiliarBase implements IBlacksmithUpgradable {

    private static final int HEAD_TIME = 20 * 60;
    private static final byte NO_HEAD = 0;

    private static final DataParameter<Byte> VARIANTS =
            EntityDataManager.createKey(EntityHeadlessFamiliar.class, DataSerializers.BYTE);
    private static final DataParameter<Byte> HEAD =
            EntityDataManager.createKey(EntityHeadlessFamiliar.class, DataSerializers.BYTE);
    private static final DataParameter<Byte> WEAPON =
            EntityDataManager.createKey(EntityHeadlessFamiliar.class, DataSerializers.BYTE);
    private static final DataParameter<Byte> REBUILT =
            EntityDataManager.createKey(EntityHeadlessFamiliar.class, DataSerializers.BYTE);
    private static final DataParameter<Boolean> BLACKSMITH_UPGRADE =
            EntityDataManager.createKey(EntityHeadlessFamiliar.class, DataSerializers.BOOLEAN);

    private static final Map<Class<? extends EntityLivingBase>, Byte> HEAD_TYPES = new LinkedHashMap<>();

    private int headTimer;
    private int headlessDieTimer;

    static {
        registerHead((byte) 1, EntityPlayer.class);
        registerHead((byte) 2, EntityZombie.class);
        registerHead((byte) 3, EntitySkeleton.class);
        registerHead((byte) 4, EntityWitherSkeleton.class);
        registerHead((byte) 5, EntityCreeper.class);
        registerHead((byte) 6, EntitySpider.class);
        registerHead((byte) 7, EntityCthulhuFamiliar.class);
    }

    public EntityHeadlessFamiliar(World worldIn) {
        super(worldIn);
        this.setSize(0.6F, 0.7F);
        this.enablePersistence();
        this.experienceValue = 2;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(VARIANTS, Byte.valueOf((byte) 0));
        this.dataManager.register(HEAD, Byte.valueOf(NO_HEAD));
        this.dataManager.register(WEAPON, Byte.valueOf((byte) 0));
        this.dataManager.register(REBUILT, Byte.valueOf((byte) 0));
        this.dataManager.register(BLACKSMITH_UPGRADE, Boolean.FALSE);
    }

    @Override
    protected void initEntityAI() {
        this.aiSit = new EntityAISit(this);
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(0, new EntityAIPanic(this, 1.25D));
        this.tasks.addTask(1, this.aiSit);
        this.tasks.addTask(2, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(3, new EntityAIFollowOwner(this, 1.0D, 3.0F, 1.0F));
        this.tasks.addTask(6, new EntityDevilFamiliar.AttackGoal(this, 5.0F) {
            @Override
            public boolean shouldExecute() {
                return super.shouldExecute() && !EntityHeadlessFamiliar.this.isHeadlessDead();
            }
        });
        this.tasks.addTask(7, new EntityAIWanderAvoidWater(this, 1.0D));
        this.tasks.addTask(8, new EntityAIFollow(this, 1.0D, 3.0F, 7.0F));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(40.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(15.0D);
        if (this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE) == null) {
            this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(6.0D);
        } else {
            this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(6.0D);
        }
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(30.0D);
    }

    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
        IEntityLivingData data = super.onInitialSpawn(difficulty, livingdata);
        this.setWeapon((byte) this.rand.nextInt(3));
        this.setHairy(this.rand.nextBoolean());
        this.setGlasses(this.rand.nextDouble() < 0.1D);
        return data;
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (!this.world.isRemote) {
            if (this.headTimer-- == 0) {
                this.setHead(NO_HEAD);
            }
            this.applyBlacksmithWeaknessAura();
        } else {
            if (this.hasBlacksmithUpgrade() && !this.isHeadlessDead() && this.ticksExisted % 10 == 0) {
                this.spawnBlacksmithUpgradeParticle();
            }
            if (this.headlessDieTimer-- <= 0) {
                return;
            }
            for (int i = 0; i < 2; i++) {
                this.world.spawnParticle(EnumParticleTypes.REDSTONE,
                        this.posX + (this.rand.nextDouble() - 0.5D) * 0.6D,
                        this.posY + 0.8D + this.rand.nextDouble() * 0.4D,
                        this.posZ + (this.rand.nextDouble() - 0.5D) * 0.6D,
                        0.5D, 0.0D, 0.0D);
            }
        }
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        if (hand != EnumHand.MAIN_HAND || !this.isOwner(player)) {
            return super.processInteract(player, hand);
        }
        ItemStack stack = player.getHeldItem(hand);
        if (this.isHeadlessDead() && this.tryRebuild(player, stack)) {
            return true;
        }
        if (stack.isEmpty()) {
            this.setSitting(!this.isSitting());
            if (this.aiSit != null) {
                this.aiSit.setSitting(this.isSitting());
            }
            return true;
        }
        return super.processInteract(player, hand);
    }

    @Override
    public void setFamiliarOwner(EntityLivingBase owner) {
        super.setFamiliarOwner(owner);
        if (owner != null && this.world != null && !this.world.isRemote && this.hasGlasses()) {
            ModAdvancements.FAMILIAR.trigger(owner, FamiliarTrigger.Type.RARE_VARIANT);
        }
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (this.isEntityInvulnerable(source)) {
            return false;
        }
        if (source == DamageSource.IN_WALL || "flyIntoWall".equals(source.damageType)) {
            return false;
        }
        this.setSitting(false);
        if (this.aiSit != null) {
            this.aiSit.setSitting(false);
        }
        return super.attackEntityFrom(source, amount);
    }

    @Override
    protected void damageEntity(DamageSource source, float amount) {
        super.damageEntity(source, amount);
        if (!this.world.isRemote && this.getHealth() / this.getMaxHealth() < 0.5F && !this.isHeadlessDead()) {
            this.setHeadlessDead(true);
            this.setAttackTarget(null);
            this.killHeadless();
            this.playSound(SoundEvents.ENTITY_ZOMBIE_BREAK_DOOR_WOOD, this.getSoundVolume(), 1.2F);
            if (this.world instanceof WorldServer) {
                ((WorldServer) this.world).spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.posX,
                        this.posY + 0.6D, this.posZ, 12, 0.25D, 0.3D, 0.25D, 0.02D);
            }
        }
    }

    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return null;
    }

    @Override
    @Nullable
    public EntityAgeable createChild(EntityAgeable ageable) {
        return null;
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.dataManager.set(VARIANTS, Byte.valueOf(compound.getByte("OccultismHeadlessVariants")));
        this.setHead(compound.getByte("OccultismHeadlessHead"));
        this.headTimer = compound.getInteger("OccultismHeadlessHeadTimer");
        this.setWeapon(compound.getByte("OccultismHeadlessWeapon"));
        this.setRebuilt(compound.getByte("OccultismHeadlessRebuilt"));
        this.setBlacksmithUpgrade(compound.getBoolean("hasBlacksmithUpgrade"));
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setByte("OccultismHeadlessVariants", this.dataManager.get(VARIANTS).byteValue());
        compound.setByte("OccultismHeadlessHead", this.getHead());
        compound.setInteger("OccultismHeadlessHeadTimer", this.headTimer);
        compound.setByte("OccultismHeadlessWeapon", this.getWeapon());
        compound.setByte("OccultismHeadlessRebuilt", this.getRebuilt());
        compound.setBoolean("hasBlacksmithUpgrade", this.hasBlacksmithUpgrade());
    }

    public boolean stealHeadFrom(EntityLivingBase target) {
        if (target == null || this.isHeadlessDead()) {
            return false;
        }
        Byte head = getHeadFor(target);
        if (head == null) {
            return false;
        }
        this.setHead(head.byteValue());
        return true;
    }

    public boolean hasMatchingHead(EntityLivingBase target) {
        Byte head = getHeadFor(target);
        return !this.isHeadlessDead() && this.hasHead() && head != null && this.getHead() == head.byteValue();
    }

    public boolean hasBlacksmithUpgrade() {
        return this.dataManager.get(BLACKSMITH_UPGRADE).booleanValue();
    }

    @Override
    @Nullable
    public EntityLivingBase getFamiliarOwner() {
        return this.getOwner();
    }

    @Override
    public EntityLivingBase getFamiliarEntity() {
        return this;
    }

    @Override
    public boolean canBlacksmithUpgrade() {
        return !this.hasBlacksmithUpgrade();
    }

    @Override
    public void blacksmithUpgrade() {
        if (this.canBlacksmithUpgrade()) {
            this.setBlacksmithUpgrade(true);
        }
    }

    public boolean hasHead() {
        return this.getHead() != NO_HEAD;
    }

    public byte getHead() {
        return this.dataManager.get(HEAD).byteValue();
    }

    public byte getWeapon() {
        return this.dataManager.get(WEAPON).byteValue();
    }

    public ItemStack getWeaponItem() {
        switch (this.getWeapon()) {
            case 1:
                return new ItemStack(Items.IRON_AXE);
            case 2:
                return new ItemStack(Items.IRON_HOE);
            default:
                return new ItemStack(Items.IRON_SWORD);
        }
    }

    public boolean isHairy() {
        return this.hasVariant(0);
    }

    public boolean hasGlasses() {
        return this.hasVariant(1);
    }

    public boolean isHeadlessDead() {
        return this.hasVariant(2);
    }

    public boolean isRebuilt(Rebuilt rebuilt) {
        return ((this.getRebuilt() >> rebuilt.getValue()) & 1) == 1;
    }

    public boolean isFullyRebuilt() {
        return this.getRebuilt() == 63;
    }

    public ItemStack getHeadDisplayStack() {
        switch (this.getHead()) {
            case 1:
                return new ItemStack(Items.SKULL, 1, 3);
            case 2:
                return new ItemStack(Items.SKULL, 1, 2);
            case 3:
                return new ItemStack(Items.SKULL, 1, 0);
            case 4:
                return new ItemStack(Items.SKULL, 1, 1);
            case 5:
                return new ItemStack(Items.SKULL, 1, 4);
            default:
                return new ItemStack(Blocks.PUMPKIN);
        }
    }

    public void killHeadless() {
        this.headlessDieTimer = 20;
    }

    private boolean tryRebuild(EntityPlayer player, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        boolean success = false;
        if (isWheat(stack) && !this.isRebuilt(Rebuilt.LEFT_LEG)) {
            this.setRebuilt(Rebuilt.LEFT_LEG);
            success = true;
        } else if (isWheat(stack) && !this.isRebuilt(Rebuilt.RIGHT_LEG)) {
            this.setRebuilt(Rebuilt.RIGHT_LEG);
            success = true;
        } else if (Block.getBlockFromItem(stack.getItem()) == Blocks.HAY_BLOCK
                && !this.isRebuilt(Rebuilt.BODY)) {
            this.setRebuilt(Rebuilt.BODY);
            success = true;
        } else if (isWoodenRod(stack) && this.isRebuilt(Rebuilt.BODY) && !this.isRebuilt(Rebuilt.LEFT_ARM)) {
            this.setRebuilt(Rebuilt.LEFT_ARM);
            success = true;
        } else if (isWoodenRod(stack) && this.isRebuilt(Rebuilt.BODY) && !this.isRebuilt(Rebuilt.RIGHT_ARM)) {
            this.setRebuilt(Rebuilt.RIGHT_ARM);
            success = true;
        } else if (Block.getBlockFromItem(stack.getItem()) == Blocks.PUMPKIN && this.isRebuilt(Rebuilt.BODY)
                && !this.isRebuilt(Rebuilt.HEAD)) {
            this.setRebuilt(Rebuilt.HEAD);
            success = true;
        }

        if (!success) {
            return false;
        }
        if (!player.capabilities.isCreativeMode) {
            stack.shrink(1);
        }
        this.playSound(SoundEvents.BLOCK_GRASS_PLACE, this.getSoundVolume(), 1.0F);
        if (this.isFullyRebuilt()) {
            ModAdvancements.FAMILIAR.trigger(player, FamiliarTrigger.Type.HEADLESS_REBUILT);
        }
        return true;
    }

    private void setHead(byte head) {
        this.dataManager.set(HEAD, Byte.valueOf(head));
        if (head != NO_HEAD) {
            this.headTimer = HEAD_TIME;
        }
    }

    private void setWeapon(byte weapon) {
        if (weapon < 0 || weapon > 2) {
            weapon = 0;
        }
        this.dataManager.set(WEAPON, Byte.valueOf(weapon));
    }

    private byte getRebuilt() {
        return this.dataManager.get(REBUILT).byteValue();
    }

    private void setRebuilt(byte rebuilt) {
        this.dataManager.set(REBUILT, Byte.valueOf(rebuilt));
    }

    private void setRebuilt(Rebuilt rebuilt) {
        this.setRebuilt((byte) (this.getRebuilt() | (1 << rebuilt.getValue())));
    }

    private void setHairy(boolean hairy) {
        this.setVariant(0, hairy);
    }

    private void setGlasses(boolean glasses) {
        this.setVariant(1, glasses);
    }

    private void setHeadlessDead(boolean dead) {
        this.setVariant(2, dead);
    }

    private void setBlacksmithUpgrade(boolean upgraded) {
        this.dataManager.set(BLACKSMITH_UPGRADE, Boolean.valueOf(upgraded));
    }

    private void applyBlacksmithWeaknessAura() {
        if (!this.hasBlacksmithUpgrade() || this.isHeadlessDead() || this.ticksExisted % 10 != 0
                || !HEAD_TYPES.containsValue(Byte.valueOf(this.getHead()))) {
            return;
        }
        EntityLivingBase owner = this.getOwner();
        List<EntityLivingBase> nearby = this.world.getEntitiesWithinAABB(EntityLivingBase.class,
                this.getEntityBoundingBox().grow(5.0D));
        for (EntityLivingBase candidate : nearby) {
            if (candidate != owner && this.hasMatchingHead(candidate)) {
                candidate.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 60, 0));
            }
        }
    }

    private void spawnBlacksmithUpgradeParticle() {
        float yaw = this.rotationYaw * 0.017453292F;
        double x = this.posX + MathHelper.sin(yaw) * 0.15D + (this.rand.nextDouble() - 0.5D) * 0.08D;
        double y = this.posY + 1.1D + (this.rand.nextDouble() - 0.5D) * 0.08D;
        double z = this.posZ - MathHelper.cos(yaw) * 0.15D + (this.rand.nextDouble() - 0.5D) * 0.08D;
        this.world.spawnParticle(EnumParticleTypes.SPELL_MOB, x, y, z, 0.1D, 0.8D, 1.0D);
    }

    private void setVariant(int offset, boolean value) {
        byte variants = this.dataManager.get(VARIANTS).byteValue();
        if (value) {
            variants = (byte) (variants | (1 << offset));
        } else {
            variants = (byte) (variants & ~(1 << offset));
        }
        this.dataManager.set(VARIANTS, Byte.valueOf(variants));
    }

    private boolean hasVariant(int offset) {
        return ((this.dataManager.get(VARIANTS).byteValue() >> offset) & 1) == 1;
    }

    private static boolean isWheat(ItemStack stack) {
        return stack.getItem() == Items.WHEAT || matchesOre(stack, "cropWheat");
    }

    private static boolean isWoodenRod(ItemStack stack) {
        return stack.getItem() == Items.STICK || matchesOre(stack, "rodWooden");
    }

    private static boolean matchesOre(ItemStack stack, String oreName) {
        for (ItemStack oreStack : OreDictionary.getOres(oreName)) {
            if (OreDictionary.itemMatches(oreStack, stack, false)) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    private static Byte getHeadFor(EntityLivingBase target) {
        if (target instanceof EntityPlayer) {
            return HEAD_TYPES.get(EntityPlayer.class);
        }
        return HEAD_TYPES.get(target.getClass());
    }

    private static void registerHead(byte id, Class<? extends EntityLivingBase> type) {
        HEAD_TYPES.put(type, Byte.valueOf(id));
    }

    public enum Rebuilt {
        LEFT_LEG(0),
        RIGHT_LEG(1),
        BODY(2),
        LEFT_ARM(3),
        RIGHT_ARM(4),
        HEAD(5);

        private final int value;

        Rebuilt(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    }
}
