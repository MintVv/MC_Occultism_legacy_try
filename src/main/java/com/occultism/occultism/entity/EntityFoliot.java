package com.occultism.occultism.entity;

import com.occultism.occultism.spirit.SpiritJobType;
import com.occultism.occultism.spirit.SpiritTier;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

/**
 * Resource-backed Foliot entity used by the upstream ritual entity id.
 *
 * <p>Job rituals still use {@link EntityFoliotJob#createForTier}; this class
 * keeps the standalone {@code occultism:foliot} id available for commands and
 * integrations without changing that existing summon path.</p>
 */
public class EntityFoliot extends EntityFoliotJob {

    private static final int DEFAULT_SPIRIT_MAX_AGE = 60 * 60 * 24;

    public EntityFoliot(World worldIn) {
        super(worldIn, SpiritJobType.CRUSHER, SpiritTier.FOLIOT);
        this.setSize(0.6F, 0.9F);
        this.setSpiritMaxAge(DEFAULT_SPIRIT_MAX_AGE);
    }

    @Override
    public void setTier(SpiritTier tier) {
        super.setTier(SpiritTier.FOLIOT);
    }

    @Override
    protected void applyTierAttributes() {
        if (this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH) == null) {
            return;
        }
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(8.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.30D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(1.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(2.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).setBaseValue(1.0D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(50.0D);
        if (this.getHealth() > this.getMaxHealth()) {
            this.setHealth(this.getMaxHealth());
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        if (compound != null && !compound.hasKey("SpiritMaxAge")) {
            this.setSpiritMaxAge(DEFAULT_SPIRIT_MAX_AGE);
        }
    }
}
