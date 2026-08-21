package com.occultism.occultism.entity;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Optional;
import com.occultism.occultism.advancement.FamiliarTrigger;
import com.occultism.occultism.registry.ModAdvancements;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIFollow;
import net.minecraft.entity.ai.EntityAIFollowOwner;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAISit;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityGreedyFamiliar extends EntityFamiliarBase implements IBlacksmithUpgradable {

    private static final DataParameter<Boolean> BLACKSMITH_UPGRADE =
            EntityDataManager.createKey(EntityGreedyFamiliar.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Optional<BlockPos>> TARGET_BLOCK =
            EntityDataManager.createKey(EntityGreedyFamiliar.class, DataSerializers.OPTIONAL_BLOCK_POS);

    private float earRotZ;
    private float earRotZ0;
    private float earRotX;
    private float earRotX0;
    private float peekRot;
    private float peekRot0;
    private float monsterRot;
    private float monsterRot0;
    private int monsterAnimTimer;
    private boolean partying;
    private BlockPos jukeboxPosition;

    public EntityGreedyFamiliar(World worldIn) {
        super(worldIn);
        this.setSize(0.5F, 0.9F);
        this.enablePersistence();
        this.experienceValue = 2;
        this.monsterAnimTimer = this.rand.nextInt(100);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(BLACKSMITH_UPGRADE, Boolean.FALSE);
        this.dataManager.register(TARGET_BLOCK, Optional.absent());
    }

    @Override
    protected void initEntityAI() {
        this.aiSit = new EntityAISit(this);
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(0, new EntityAIPanic(this, 1.25D));
        this.tasks.addTask(1, this.aiSit);
        this.tasks.addTask(2, new FindBlockGoal(this));
        this.tasks.addTask(3, new RideDragonGoal(this));
        this.tasks.addTask(4, new FindItemGoal(this));
        this.tasks.addTask(4, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(5, new EntityAIFollowOwner(this, 1.0D, 3.0F, 1.0F));
        this.tasks.addTask(6, new EntityAIWanderAvoidWater(this, 1.0D));
        this.tasks.addTask(7, new EntityAIFollow(this, 1.0D, 3.0F, 7.0F));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(30.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(15.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).setBaseValue(10.0D);
        if (this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE) == null) {
            this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(6.0D);
        } else {
            this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(6.0D);
        }
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (this.world.isRemote) {
            if (this.jukeboxPosition == null
                    || this.jukeboxPosition.distanceSq(this.posX, this.posY, this.posZ) > 12.0D
                    || this.world.getBlockState(this.jukeboxPosition).getBlock() != Blocks.JUKEBOX) {
                this.partying = false;
                this.jukeboxPosition = null;
            }
            this.updateClientAnimations();
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void setPartying(BlockPos pos, boolean isPartying) {
        this.jukeboxPosition = pos;
        this.partying = isPartying;
    }

    public boolean isPartying() {
        return this.partying;
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (this.hasBlacksmithUpgrade() && !this.getHeldItemOffhand().isEmpty()) {
            if (!this.world.isRemote) {
                ItemHandlerHelper.giveItemToPlayer(player, this.getHeldItemOffhand());
                this.setHeldItem(EnumHand.OFF_HAND, ItemStack.EMPTY);
                this.clearTargetBlock();
            }
            return true;
        } else if (this.hasBlacksmithUpgrade() && stack.getItem() instanceof ItemBlock) {
            if (!this.world.isRemote) {
                this.setHeldItem(EnumHand.OFF_HAND, new ItemStack(stack.getItem()));
                stack.shrink(1);
                this.clearTargetBlock();
            }
            return true;
        }
        if (hand == EnumHand.MAIN_HAND && this.isOwner(player) && stack.isEmpty()) {
            if (!this.world.isRemote) {
                this.setSitting(!this.isSitting());
                if (this.aiSit != null) {
                    this.aiSit.setSitting(this.isSitting());
                }
                if (this.isSitting()) {
                    this.clearTargetBlock();
                }
            }
            return true;
        }
        return super.processInteract(player, hand);
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
        this.setBlacksmithUpgrade(compound.getBoolean("hasBlacksmithUpgrade"));
        this.setTargetBlock(Optional.absent());
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setBoolean("hasBlacksmithUpgrade", this.hasBlacksmithUpgrade());
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

    /** Native 1.12 ring hook matching the upstream five-block item magnet. */
    @Override
    public void curioTick(@Nullable EntityLivingBase wearer) {
        if (!(wearer instanceof EntityPlayer) || !this.isEffectEnabled(wearer) || this.world.isRemote) {
            return;
        }
        EntityPlayer player = (EntityPlayer) wearer;
        List<EntityItem> items = player.world.getEntitiesWithinAABB(EntityItem.class,
                player.getEntityBoundingBox().grow(5.0D), item -> {
                    if (item == null || item.isDead || item.getItem().isEmpty()) {
                        return false;
                    }
                    ItemStack stack = item.getItem();
                    return !(stack.hasTagCompound() && stack.getTagCompound().getBoolean("PreventRemoteMovement"))
                            && !item.getEntityData().getBoolean("PreventRemoteMovement");
                });
        for (EntityItem item : items) {
            item.onCollideWithPlayer(player);
        }
    }

    @Override
    public void blacksmithUpgrade() {
        if (!this.canBlacksmithUpgrade()) {
            return;
        }
        this.setBlacksmithUpgrade(true);
    }

    public Optional<BlockPos> getTargetBlock() {
        return this.dataManager.get(TARGET_BLOCK);
    }

    public float getLidRot(float partialTicks) {
        return lerp(partialTicks, this.peekRot0, this.peekRot);
    }

    public float getMonsterRot(float partialTicks) {
        return lerp(partialTicks, this.monsterRot0, this.monsterRot);
    }

    public float getEarRotZ(float partialTicks) {
        return lerp(partialTicks, this.earRotZ0, this.earRotZ);
    }

    public float getEarRotX(float partialTicks) {
        return lerp(partialTicks, this.earRotX0, this.earRotX);
    }

    private void setBlacksmithUpgrade(boolean value) {
        this.dataManager.set(BLACKSMITH_UPGRADE, Boolean.valueOf(value));
    }

    private void setTargetBlock(Optional<BlockPos> pos) {
        this.dataManager.set(TARGET_BLOCK, pos);
    }

    private void clearTargetBlock() {
        this.getNavigator().clearPath();
        this.setTargetBlock(Optional.absent());
    }

    private void updateClientAnimations() {
        this.earRotX0 = this.earRotX;
        this.earRotZ0 = this.earRotZ;

        Optional<BlockPos> targetBlock = this.getTargetBlock();
        if (targetBlock.isPresent()) {
            BlockPos target = targetBlock.get();
            Vec3d targetCenter = new Vec3d(target.getX() + 0.5D, target.getY() + 0.5D,
                    target.getZ() + 0.5D);
            float targetEarRotZ = -0.5F;
            double eyeY = this.posY + this.getEyeHeight();
            if (Math.abs(targetCenter.y - eyeY) > 1.0D) {
                targetEarRotZ = targetCenter.y > eyeY ? 0.0F : -2.0F;
            }
            this.earRotZ = lerp(0.1F, this.earRotZ, targetEarRotZ);

            Vec3d look = this.getLook(1.0F);
            Vec3d toTarget = new Vec3d(this.posX - targetCenter.x, this.posY - targetCenter.y,
                    this.posZ - targetCenter.z).normalize();
            toTarget = new Vec3d(toTarget.x, 0.0D, toTarget.z);
            float targetEarRotX = toTarget.dotProduct(look) < 0.0D ? 1.0F : -1.0F;
            this.earRotX = lerp(0.1F, this.earRotX, targetEarRotX);
        } else {
            this.earRotX = lerp(0.1F, this.earRotX, 0.0F);
            this.earRotZ = lerp(0.1F, this.earRotZ, -0.5F);
        }

        this.monsterAnimTimer++;
        this.peekRot0 = this.peekRot;
        this.monsterRot0 = this.monsterRot;
        if (this.monsterAnimTimer % 300 < 200) {
            int peekTimer = this.monsterAnimTimer % 300 % 200;
            if (peekTimer > 30 && peekTimer < 50) {
                this.monsterRot = lerp(0.3F, this.monsterRot, toRad(37.0F));
            } else if (peekTimer > 50 && peekTimer < 70) {
                this.monsterRot = lerp(0.3F, this.monsterRot, toRad(-37.0F));
            } else if (peekTimer > 70) {
                this.monsterRot = lerp(0.3F, this.monsterRot, 0.0F);
            }

            if (peekTimer < 100) {
                this.peekRot = lerp(0.1F, this.peekRot, toRad(46.0F));
            } else {
                this.peekRot = lerp(0.1F, this.peekRot, 0.0F);
            }
        } else {
            this.peekRot = 0.0F;
            this.monsterRot = 0.0F;
        }
    }

    private static float lerp(float amount, float start, float end) {
        return start + amount * (end - start);
    }

    private static float toRad(float degrees) {
        return (float) Math.toRadians(degrees);
    }

    public static class FindItemGoal extends EntityAIBase {

        private static final double RANGE = 12.0D;

        private final EntityTameable familiar;

        public FindItemGoal(EntityTameable familiar) {
            this.familiar = familiar;
            this.setMutexBits(1);
        }

        @Override
        public boolean shouldExecute() {
            return this.getNearbyItem() != null && this.familiar.getOwner() instanceof EntityPlayer;
        }

        @Override
        public void startExecuting() {
            EntityItem item = this.getNearbyItem();
            if (item != null) {
                this.familiar.getNavigator().tryMoveToEntityLiving(item, 1.2D);
            }
        }

        @Override
        public void updateTask() {
            EntityItem item = this.getNearbyItem();
            if (item == null) {
                return;
            }

            this.familiar.getNavigator().tryMoveToEntityLiving(item, 1.2D);
            EntityLivingBase owner = this.familiar.getOwner();
            if (this.familiar.getDistanceSq(item) < 4.0D && owner instanceof EntityPlayer) {
                int beforeCount = item.getItem().getCount();
                item.onCollideWithPlayer((EntityPlayer) owner);
                if (item.isDead || item.getItem().isEmpty() || item.getItem().getCount() < beforeCount) {
                    ModAdvancements.FAMILIAR.trigger(owner, FamiliarTrigger.Type.GREEDY_ITEM);
                }
            }
        }

        @Nullable
        private EntityItem getNearbyItem() {
            EntityLivingBase owner = this.familiar.getOwner();
            if (!(owner instanceof EntityPlayer)) {
                return null;
            }

            EntityPlayer player = (EntityPlayer) owner;
            IItemHandler inventory = new PlayerMainInvWrapper(player.inventory);
            List<EntityItem> items = this.familiar.world.getEntitiesWithinAABB(EntityItem.class,
                    this.familiar.getEntityBoundingBox().grow(RANGE));
            for (EntityItem item : items) {
                ItemStack stack = item.getItem();
                if (item.isDead || stack.isEmpty()) {
                    continue;
                }

                boolean stackDemagnetized = stack.hasTagCompound()
                        && stack.getTagCompound().getBoolean("PreventRemoteMovement");
                boolean entityDemagnetized = item.getEntityData().getBoolean("PreventRemoteMovement");
                if (!stackDemagnetized && !entityDemagnetized
                        && ItemHandlerHelper.insertItemStacked(inventory, stack, true).getCount() != stack.getCount()) {
                    return item;
                }
            }
            return null;
        }
    }

    private static class RideDragonGoal extends EntityAIBase {

        private final EntityGreedyFamiliar rider;
        private EntityDragonFamiliar mount;

        private RideDragonGoal(EntityGreedyFamiliar rider) {
            this.rider = rider;
            this.setMutexBits(5);
        }

        @Override
        public boolean shouldExecute() {
            if (this.rider.getRidingEntity() instanceof EntityDragonFamiliar) {
                return true;
            }
            this.mount = this.findMount();
            if (this.mount == null) {
                return false;
            }
            this.rider.getNavigator().tryMoveToEntityLiving(this.mount, 1.0D);
            return true;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return this.rider.getRidingEntity() instanceof EntityDragonFamiliar
                    || this.mount != null && !this.rider.getNavigator().noPath();
        }

        @Override
        public void resetTask() {
            this.rider.dismountRidingEntity();
            this.mount = null;
        }

        @Override
        public void updateTask() {
            if (this.mount != null && this.rider.getDistanceSq(this.mount) < 5.0D
                    && !this.mount.isBeingRidden()) {
                this.rider.startRiding(this.mount);
                if (this.rider.getRidingEntity() == this.mount) {
                    this.mount.getNavigator().clearPath();
                    ModAdvancements.FAMILIAR.trigger(this.rider.getFamiliarOwner(),
                            FamiliarTrigger.Type.DRAGON_RIDE);
                }
            }
        }

        @Nullable
        private EntityDragonFamiliar findMount() {
            EntityLivingBase owner = this.rider.getFamiliarOwner();
            if (owner == null) {
                return null;
            }
            List<EntityDragonFamiliar> mounts = this.rider.world.getEntitiesWithinAABB(EntityDragonFamiliar.class,
                    this.rider.getEntityBoundingBox().grow(5.0D),
                    dragon -> dragon != null && dragon.isEntityAlive() && dragon.getOwner() == owner
                            && !dragon.isBeingRidden() && !dragon.isSitting());
            return mounts.isEmpty() ? null : mounts.get(0);
        }
    }

    private static class FindBlockGoal extends EntityAIBase {

        private static final int HORIZONTAL_RANGE = 10;
        private static final int VERTICAL_RANGE = 5;
        private static final int MIN_SEARCH_COOLDOWN = 200;
        private static final int SEARCH_COOLDOWN_VARIANCE = 200;
        private static final int MAX_TARGET_TICKS = 1200;
        private static final int MIN_STAY_TICKS = 1200;
        private static final int STAY_TICK_VARIANCE = 1200;
        private static final int PATH_RECALCULATION_INTERVAL = 40;
        private static final double MOVEMENT_SPEED = 1.0D;
        private static final double ARRIVAL_DISTANCE_SQ = 1.0D;

        private final EntityGreedyFamiliar greedy;
        private BlockPos targetBlock;
        private Block lastSampleBlock;
        private Block targetSampleBlock;
        private int nextSearchTick;
        private int targetTicks;
        private int maxStayTicks;
        private int pathRecalculationCooldown;

        private FindBlockGoal(EntityGreedyFamiliar greedy) {
            this.greedy = greedy;
            this.setMutexBits(5);
        }

        @Override
        public boolean shouldExecute() {
            Block sampleBlock = this.getSampleBlock();
            if (this.greedy.isSitting() || sampleBlock == null) {
                this.lastSampleBlock = null;
                return false;
            }

            boolean sampleChanged = sampleBlock != this.lastSampleBlock;
            this.lastSampleBlock = sampleBlock;
            if (!sampleChanged && this.greedy.ticksExisted < this.nextSearchTick) {
                return false;
            }

            this.targetBlock = this.findNearestTarget(sampleBlock);
            if (this.targetBlock == null) {
                this.scheduleNextSearch();
                return false;
            }
            this.targetSampleBlock = sampleBlock;
            return this.targetBlock != null;
        }

        @Override
        public boolean shouldContinueExecuting() {
            Block sampleBlock = this.getSampleBlock();
            return !this.greedy.isSitting() && this.targetBlock != null && sampleBlock != null
                    && sampleBlock == this.targetSampleBlock && this.targetTicks >= -this.maxStayTicks
                    && this.targetTicks <= MAX_TARGET_TICKS
                    && this.greedy.world.isBlockLoaded(this.targetBlock)
                    && this.isTarget(this.targetBlock, sampleBlock);
        }

        @Override
        public void startExecuting() {
            this.targetTicks = 0;
            this.maxStayTicks = this.greedy.getRNG().nextInt(
                    this.greedy.getRNG().nextInt(STAY_TICK_VARIANCE) + MIN_STAY_TICKS) + MIN_STAY_TICKS;
            this.greedy.setTargetBlock(Optional.of(this.targetBlock));
            this.moveToTarget();
            this.pathRecalculationCooldown = PATH_RECALCULATION_INTERVAL;
        }

        @Override
        public void resetTask() {
            this.greedy.getNavigator().clearPath();
            this.greedy.setTargetBlock(Optional.absent());
            this.targetBlock = null;
            this.targetSampleBlock = null;
            this.targetTicks = 0;
            this.maxStayTicks = 0;
            this.pathRecalculationCooldown = 0;
            this.scheduleNextSearch();
        }

        @Override
        public void updateTask() {
            if (this.targetBlock == null) {
                return;
            }
            this.greedy.getLookHelper().setLookPosition(this.targetBlock.getX() + 0.5D,
                    this.targetBlock.getY() + 0.5D, this.targetBlock.getZ() + 0.5D, 10.0F, 20.0F);

            if (this.getDistanceSqToTarget() <= ARRIVAL_DISTANCE_SQ) {
                this.targetTicks--;
                this.greedy.getNavigator().clearPath();
                this.pathRecalculationCooldown = PATH_RECALCULATION_INTERVAL;
                return;
            }

            this.targetTicks++;
            if (--this.pathRecalculationCooldown <= 0) {
                this.moveToTarget();
                this.pathRecalculationCooldown = PATH_RECALCULATION_INTERVAL;
            }
        }

        @Nullable
        private BlockPos findNearestTarget(Block sampleBlock) {
            BlockPos center = new BlockPos(this.greedy);
            BlockPos nearest = null;
            double nearestDistance = Double.MAX_VALUE;
            for (int y = -VERTICAL_RANGE; y <= VERTICAL_RANGE; y++) {
                for (int x = -HORIZONTAL_RANGE; x <= HORIZONTAL_RANGE; x++) {
                    for (int z = -HORIZONTAL_RANGE; z <= HORIZONTAL_RANGE; z++) {
                        BlockPos pos = center.add(x, y, z);
                        if (!this.greedy.world.isBlockLoaded(pos) || !this.isTarget(pos, sampleBlock)) {
                            continue;
                        }
                        double distance = this.greedy.getDistanceSq(pos);
                        if (distance < nearestDistance) {
                            nearest = pos.toImmutable();
                            nearestDistance = distance;
                        }
                    }
                }
            }
            return nearest;
        }

        @Nullable
        private Block getSampleBlock() {
            ItemStack sample = this.greedy.getHeldItemOffhand();
            return sample.isEmpty() ? null : Block.getBlockFromItem(sample.getItem());
        }

        private boolean isTarget(BlockPos pos, Block sampleBlock) {
            return this.greedy.world.getBlockState(pos).getBlock() == sampleBlock;
        }

        private void scheduleNextSearch() {
            this.nextSearchTick = this.greedy.ticksExisted + MIN_SEARCH_COOLDOWN
                    + this.greedy.getRNG().nextInt(SEARCH_COOLDOWN_VARIANCE);
        }

        private double getDistanceSqToTarget() {
            double x = this.greedy.posX - (this.targetBlock.getX() + 0.5D);
            double y = this.greedy.posY - this.targetBlock.getY();
            double z = this.greedy.posZ - (this.targetBlock.getZ() + 0.5D);
            return x * x + y * y + z * z;
        }

        private void moveToTarget() {
            if (this.targetBlock != null) {
                this.greedy.getNavigator().tryMoveToXYZ(this.targetBlock.getX() + 0.5D,
                        this.targetBlock.getY(), this.targetBlock.getZ() + 0.5D, MOVEMENT_SPEED);
            }
        }
    }
}
