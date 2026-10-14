package com.occultism.occultism.entity;

import com.occultism.occultism.spirit.SpiritJobType;
import com.occultism.occultism.spirit.SpiritTier;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.world.World;

/** Dedicated Marid job spirit. */
public class EntityMaridJob extends EntityFoliotJob {

    /** Forge entity registration requires a public constructor accepting only the world. */
    public EntityMaridJob(World worldIn) {
        this(worldIn, SpiritJobType.CRUSHER);
    }

    public EntityMaridJob(World worldIn, SpiritJobType jobType) {
        super(worldIn, jobType, SpiritTier.MARID);
    }

    @Override
    public void setTier(SpiritTier tier) {
        super.setTier(SpiritTier.MARID);
    }

    @Override
    protected void applyTierAttributes() {
        if (this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH) == null) {
            return;
        }
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(300.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.40D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(16.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(16.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).setBaseValue(100.0D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(50.0D);
        if (this.getHealth() > this.getMaxHealth()) {
            this.setHealth(this.getMaxHealth());
        }
    }
}
