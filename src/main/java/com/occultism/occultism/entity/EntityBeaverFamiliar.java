package com.occultism.occultism.entity;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import javax.annotation.Nullable;

import com.occultism.occultism.advancement.FamiliarTrigger;
import com.occultism.occultism.handler.FamiliarEventHandler;
import com.occultism.occultism.registry.ModItems;
import com.occultism.occultism.registry.ModAdvancements;
import com.occultism.occultism.registry.ModPotions;

import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIFollowOwner;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAISit;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemHandlerHelper;

public class EntityBeaverFamiliar extends EntityFamiliarBase implements IBlacksmithUpgradable {

    private static final DataParameter<Byte> VARIANTS =
            EntityDataManager.createKey(EntityBeaverFamiliar.class, DataSerializers.BYTE);
    private static final DataParameter<Boolean> BLACKSMITH_UPGRADE =
            EntityDataManager.createKey(EntityBeaverFamiliar.class, DataSerializers.BOOLEAN);
    private static final double OWNER_EFFECT_RANGE_SQ = 64.0D;
    private static final int MAX_LOGS_PER_TREE = 15;
    private static final int SNACK_INTERVAL = 20;

    private BlockPos treeTarget;
    private long lastSnackTime;

    public EntityBeaverFamiliar(World worldIn) {
        super(worldIn);
        this.setSize(0.8F, 0.55F);
        this.enablePersistence();
        this.experienceValue = 2;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(VARIANTS, Byte.valueOf((byte) 0));
        this.dataManager.register(BLACKSMITH_UPGRADE, Boolean.FALSE);
    }

    @Override
    protected void initEntityAI() {
        this.aiSit = new EntityAISit(this);
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(0, new EntityAIPanic(this, 1.25D));
        this.tasks.addTask(1, this.aiSit);
        this.tasks.addTask(2, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(4, new ChopTreeGoal(this));
        this.tasks.addTask(5, new EntityAIFollowOwner(this, 1.0D, 3.0F, 1.0F));
        this.tasks.addTask(7, new EntityAIWanderAvoidWater(this, 1.0D));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(30.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(15.0D);
    }

    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
        IEntityLivingData data = super.onInitialSpawn(difficulty, livingdata);
        this.setWhiskers(this.rand.nextBoolean());
        this.setEars(this.rand.nextBoolean());
        this.setBigTail(this.rand.nextDouble() < 0.1D);
        return data;
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (!this.world.isRemote && this.ticksExisted % 10 == 0 && this.isOwnerNearForEffect()) {
            EntityLivingBase owner = this.getOwner();
            owner.addPotionEffect(new PotionEffect(ModPotions.BEAVER_HARVEST, 300, 0, false, true));
        }
    }

    @Override
    public Iterable<PotionEffect> getFamiliarEffects() {
        return java.util.Collections.singletonList(
                new PotionEffect(ModPotions.BEAVER_HARVEST, 300, 0, false, true));
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (this.isOwner(player) && !player.isSneaking() && stack.isEmpty()) {
            if (!this.world.isRemote) {
                if (!this.hasBlacksmithUpgrade()) {
                    player.sendStatusMessage(new TextComponentTranslation("dialog.occultism.beaver.no_upgrade"), true);
                } else if (this.world.getTotalWorldTime() > this.lastSnackTime + SNACK_INTERVAL) {
                    this.lastSnackTime = this.world.getTotalWorldTime();
                    ItemHandlerHelper.giveItemToPlayer(player, new ItemStack(ModItems.BEAVER_NUGGET));
                } else {
                    player.sendStatusMessage(
                            new TextComponentTranslation("dialog.occultism.beaver.snack_on_cooldown"), true);
                }
            }
            return true;
        }
        if (hand == EnumHand.MAIN_HAND && this.isOwner(player) && stack.isEmpty()) {
            this.setSitting(!this.isSitting());
            if (this.aiSit != null) {
                this.aiSit.setSitting(this.isSitting());
            }
            this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_RABBIT_AMBIENT,
                    this.getSoundCategory(), 0.35F, 0.8F);
            return true;
        }
        return super.processInteract(player, hand);
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (this.isEntityInvulnerable(source)) {
            return false;
        }
        this.setSitting(false);
        if (this.aiSit != null) {
            this.aiSit.setSitting(false);
        }
        return super.attackEntityFrom(source, amount);
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
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
        this.dataManager.set(VARIANTS, Byte.valueOf(compound.getByte("OccultismBeaverVariants")));
        this.setBlacksmithUpgrade(compound.getBoolean("hasBlacksmithUpgrade"));
        if (compound.hasKey("OccultismTreeTargetX")) {
            this.treeTarget = new BlockPos(compound.getInteger("OccultismTreeTargetX"),
                    compound.getInteger("OccultismTreeTargetY"), compound.getInteger("OccultismTreeTargetZ"));
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setByte("OccultismBeaverVariants", this.dataManager.get(VARIANTS).byteValue());
        compound.setBoolean("hasBlacksmithUpgrade", this.hasBlacksmithUpgrade());
        if (this.treeTarget != null) {
            compound.setInteger("OccultismTreeTargetX", this.treeTarget.getX());
            compound.setInteger("OccultismTreeTargetY", this.treeTarget.getY());
            compound.setInteger("OccultismTreeTargetZ", this.treeTarget.getZ());
        }
    }

    public void setTreeTarget(BlockPos pos) {
        this.treeTarget = pos == null ? null : pos.toImmutable();
    }

    public boolean isOwnerNearForEffect() {
        EntityLivingBase owner = this.getOwner();
        return owner != null && owner.isEntityAlive() && this.getDistanceSq(owner) < OWNER_EFFECT_RANGE_SQ;
    }

    public boolean hasWhiskers() {
        return this.hasVariant(0);
    }

    public boolean hasEars() {
        return this.hasVariant(1);
    }

    public boolean hasBigTail() {
        return this.hasVariant(2);
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

    private void setWhiskers(boolean whiskers) {
        this.setVariant(0, whiskers);
    }

    private void setEars(boolean ears) {
        this.setVariant(1, ears);
    }

    private void setBigTail(boolean bigTail) {
        this.setVariant(2, bigTail);
    }

    private void setBlacksmithUpgrade(boolean upgraded) {
        this.dataManager.set(BLACKSMITH_UPGRADE, Boolean.valueOf(upgraded));
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

    private static class ChopTreeGoal extends EntityAIBase {

        private final EntityBeaverFamiliar beaver;

        private ChopTreeGoal(EntityBeaverFamiliar beaver) {
            this.beaver = beaver;
            this.setMutexBits(5);
        }

        @Override
        public boolean shouldExecute() {
            return !this.beaver.isSitting() && this.beaver.isOwnerNearForEffect()
                    && this.beaver.treeTarget != null && this.isLog(this.beaver.treeTarget);
        }

        @Override
        public boolean shouldContinueExecuting() {
            return this.shouldExecute();
        }

        @Override
        public void startExecuting() {
            this.moveToTarget();
        }

        @Override
        public void updateTask() {
            BlockPos target = this.beaver.treeTarget;
            if (target == null) {
                return;
            }
            if (this.beaver.getDistanceSq(target) > 4.0D) {
                this.moveToTarget();
                return;
            }
            this.chopTree(target);
            this.beaver.treeTarget = null;
        }

        private void moveToTarget() {
            BlockPos target = this.beaver.treeTarget;
            if (target != null) {
                this.beaver.getNavigator().tryMoveToXYZ(target.getX(), target.getY(), target.getZ(), 1.0D);
            }
        }

        private boolean isLog(BlockPos pos) {
            return FamiliarEventHandler.isLog(this.beaver.world.getBlockState(pos), this.beaver.world, pos);
        }

        private void chopTree(BlockPos target) {
            LinkedList<BlockPos> open = new LinkedList<>();
            Set<BlockPos> visited = new HashSet<>();
            Set<BlockPos> logs = new HashSet<>();
            open.add(target);

            while (!open.isEmpty() && logs.size() <= MAX_LOGS_PER_TREE) {
                BlockPos pos = open.removeFirst();
                if (!visited.add(pos)) {
                    continue;
                }
                if (!this.isLog(pos)) {
                    continue;
                }
                logs.add(pos);
                for (BlockPos next : BlockPos.getAllInBox(pos.add(-1, -1, -1), pos.add(1, 1, 1))) {
                    BlockPos immutable = next.toImmutable();
                    if (!visited.contains(immutable)) {
                        open.add(immutable);
                    }
                }
            }

            if (logs.isEmpty() || logs.size() > MAX_LOGS_PER_TREE) {
                return;
            }
            boolean chopped = false;
            for (BlockPos pos : logs) {
                chopped |= this.beaver.world.destroyBlock(pos, true);
            }
            if (chopped) {
                ModAdvancements.FAMILIAR.trigger(this.beaver.getFamiliarOwner(),
                        FamiliarTrigger.Type.BEAVER_WOODCHOP);
            }
        }
    }
}
