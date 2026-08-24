package com.occultism.occultism.entity;

import com.occultism.occultism.spirit.SpiritTier;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.world.World;

/** The dedicated Djinni entity used by high-tier bound spirit rituals. */
public class EntityDjinni extends EntityBoundSpirit {

    public EntityDjinni(World worldIn) {
        super(worldIn, SpiritTier.DJINNI);
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
}
