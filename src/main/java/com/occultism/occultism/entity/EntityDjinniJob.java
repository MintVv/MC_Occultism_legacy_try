package com.occultism.occultism.entity;

import com.occultism.occultism.spirit.SpiritJobType;
import com.occultism.occultism.spirit.SpiritTier;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.world.World;

/** Dedicated Djinni job spirit, including the enlarged tier-2 crusher body. */
public class EntityDjinniJob extends EntityFoliotJob {

    /** Forge entity registration requires a public constructor accepting only the world. */
    public EntityDjinniJob(World worldIn) {
        this(worldIn, SpiritJobType.CRUSHER);
    }

    public EntityDjinniJob(World worldIn, SpiritJobType jobType) {
        super(worldIn, jobType, SpiritTier.DJINNI);
        this.updateEntitySizeForJob();
    }

    @Override
    public void setTier(SpiritTier tier) {
        super.setTier(SpiritTier.DJINNI);
    }

    @Override
    protected void applyTierAttributes() {
        if (this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH) == null) {
            return;
        }
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(100.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.30D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(3.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(4.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).setBaseValue(5.0D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(50.0D);
        if (this.getHealth() > this.getMaxHealth()) {
            this.setHealth(this.getMaxHealth());
        }
    }

    @Override
    protected void updateEntitySizeForJob() {
        if (this.getJobType() == SpiritJobType.CRUSHER) {
            this.setSize(1.2F, 2.4F);
        } else {
            this.setSize(0.7F, 1.6F);
        }
    }
}
