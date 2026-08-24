package com.occultism.occultism.entity;

import javax.annotation.Nullable;

import com.occultism.occultism.Tags;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityGuardian;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.event.ForgeEventFactory;

/** 1.12-compatible hostile Marid used by the unbound Marid ritual. */
public class EntityMaridWild extends EntityMob {

    private static final ResourceLocation LOOT_TABLE = LootTableList.register(
            new ResourceLocation(Tags.MOD_ID, "entities/marid_wild"));

    public EntityMaridWild(World worldIn) {
        super(worldIn);
        this.setSize(1.4F, 3.5F);
        this.experienceValue = 50;
        this.setCanPickUpLoot(false);
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(1, new EntityAISwimming(this));
        this.tasks.addTask(3, new EntityAIAttackMelee(this, 1.0D, true));
        this.tasks.addTask(7, new EntityAIWander(this, 1.0D));
        this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(8, new EntityAILookIdle(this));
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this, EntityPlayer.class, true));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(50.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(14.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).setBaseValue(8.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(300.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.41D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(16.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).setBaseValue(50.0D);
        this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1.0D);
    }

    /** The upstream Marid arrives with a guardian entourage. */
    @Override
    @Nullable
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
        if (!this.world.isRemote) {
            int guardianCount = 3 + this.rand.nextInt(6);
            for (int i = 0; i < guardianCount; i++) {
                EntityGuardian guardian = new EntityGuardian(this.world);
                if (!ForgeEventFactory.doSpecialSpawn(guardian, this.world, (float) this.posX, (float) this.posY,
                        (float) this.posZ)) {
                    guardian.onInitialSpawn(difficulty, livingdata);
                }
                double offsetX = this.rand.nextGaussian() * (1 + this.rand.nextInt(4));
                double offsetZ = this.rand.nextGaussian() * (1 + this.rand.nextInt(4));
                guardian.setLocationAndAngles(MathHelper.floor(this.posX) + offsetX, this.posY + 1.5D,
                        MathHelper.floor(this.posZ) + offsetZ, this.rand.nextInt(360), 0.0F);
                guardian.enablePersistence();
                this.world.spawnEntity(guardian);
            }
        }
        return super.onInitialSpawn(difficulty, livingdata);
    }

    @Override
    public boolean isEntityInvulnerable(DamageSource source) {
        if (source.isFireDamage() || source.isExplosion() || source == DamageSource.DROWN
                || source == DamageSource.MAGIC) {
            return true;
        }
        EntityLivingBase trueSource = source.getTrueSource() instanceof EntityLivingBase
                ? (EntityLivingBase) source.getTrueSource() : null;
        EntityLivingBase immediateSource = source.getImmediateSource() instanceof EntityLivingBase
                ? (EntityLivingBase) source.getImmediateSource() : null;
        if (trueSource instanceof EntityMaridWild || immediateSource instanceof EntityMaridWild) {
            return true;
        }
        return super.isEntityInvulnerable(source);
    }

    @Override
    protected ResourceLocation getLootTable() {
        return LOOT_TABLE;
    }

    @Override
    protected void playStepSound(net.minecraft.util.math.BlockPos pos, net.minecraft.block.Block blockIn) {
        // Marids hover in the upstream model and have no audible footsteps.
    }
}
