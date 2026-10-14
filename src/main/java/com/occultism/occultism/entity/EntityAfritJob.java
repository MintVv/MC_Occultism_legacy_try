package com.occultism.occultism.entity;

import com.occultism.occultism.spirit.SpiritJobType;
import com.occultism.occultism.spirit.SpiritTier;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.world.World;

/** Dedicated Afrit job spirit. */
public class EntityAfritJob extends EntityFoliotJob {

    /** Forge entity registration requires a public constructor accepting only the world. */
    public EntityAfritJob(World worldIn) {
        this(worldIn, SpiritJobType.CRUSHER);
    }

    public EntityAfritJob(World worldIn, SpiritJobType jobType) {
        super(worldIn, jobType, SpiritTier.AFRIT);
    }

    @Override
    public void setTier(SpiritTier tier) {
        super.setTier(SpiritTier.AFRIT);
    }

    @Override
    protected void applyTierAttributes() {
        if (this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH) == null) {
            return;
        }
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(200.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.40D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(8.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(8.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).setBaseValue(50.0D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(50.0D);
        if (this.getHealth() > this.getMaxHealth()) {
            this.setHealth(this.getMaxHealth());
        }
    }
}
