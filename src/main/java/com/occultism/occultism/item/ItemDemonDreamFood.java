package com.occultism.occultism.item;

import java.util.ArrayList;
import java.util.List;

import com.occultism.occultism.entity.EntityBoundSpirit;
import com.occultism.occultism.entity.EntityDemonicPartner;
import com.occultism.occultism.entity.EntityAfritWild;
import com.occultism.occultism.entity.EntityFoliotJob;
import com.occultism.occultism.entity.EntityPossessedEnderman;
import com.occultism.occultism.entity.EntityPossessedEndermite;
import com.occultism.occultism.entity.EntityPossessedGhast;
import com.occultism.occultism.entity.EntityPossessedBlaze;
import com.occultism.occultism.entity.EntityPossessedGuardian;
import com.occultism.occultism.entity.EntityPossessedWitch;
import com.occultism.occultism.entity.EntityPossessedZombifiedPiglin;
import com.occultism.occultism.entity.EntityShubNiggurathSpawn;
import com.occultism.occultism.entity.IFamiliar;
import com.occultism.occultism.entity.IWildHuntEntity;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityParrot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class ItemDemonDreamFood extends ItemFood {

    private final int spiritHealing;
    private final List<EffectEntry> effects = new ArrayList<>();

    public ItemDemonDreamFood(int spiritHealing) {
        super(0, 0.0F, false);
        this.spiritHealing = spiritHealing;
        this.setAlwaysEdible();
    }

    public ItemDemonDreamFood addEffect(Potion potion, int duration, int amplifier, float probability) {
        this.effects.add(new EffectEntry(potion, duration, amplifier, probability));
        return this;
    }

    public boolean tryHealSpirit(ItemStack stack, EntityPlayer player, net.minecraft.entity.Entity target,
            net.minecraft.util.EnumHand hand) {
        if (!isHealableSpirit(target) || this.spiritHealing <= 0) {
            return false;
        }
        EntityLivingBase spirit = (EntityLivingBase) target;
        if (spirit.getHealth() >= spirit.getMaxHealth()) {
            return false;
        }
        if (!player.world.isRemote) {
            spirit.heal(this.spiritHealing);
            stack.shrink(1);
        }
        player.swingArm(hand == null ? net.minecraft.util.EnumHand.MAIN_HAND : hand);
        return true;
    }

    private static boolean isHealableSpirit(net.minecraft.entity.Entity target) {
        return target instanceof EntityFoliotJob
                || target instanceof EntityBoundSpirit
                || target instanceof EntityAfritWild
                || target instanceof IWildHuntEntity
                || target instanceof EntityPossessedEndermite
                || target instanceof EntityPossessedEnderman
                || target instanceof EntityPossessedGhast
                || target instanceof EntityPossessedBlaze
                || target instanceof EntityPossessedGuardian
                || target instanceof EntityPossessedWitch
                || target instanceof EntityPossessedZombifiedPiglin
                || target instanceof IFamiliar
                || target instanceof EntityParrot
                || target instanceof EntityShubNiggurathSpawn
                || target instanceof EntityDemonicPartner;
    }

    @Override
    protected void onFoodEaten(ItemStack stack, World worldIn, EntityPlayer player) {
        if (worldIn.isRemote) {
            return;
        }
        for (EffectEntry entry : this.effects) {
            if (worldIn.rand.nextFloat() < entry.probability) {
                player.addPotionEffect(new PotionEffect(entry.potion, entry.duration, entry.amplifier));
            }
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        String text = I18n.format(this.getTranslationKey() + ".auto_tooltip");
        if (text.equals(this.getTranslationKey() + ".auto_tooltip")) {
            return;
        }
        for (String line : text.split("\\\\n|\\n")) {
            if (!line.isEmpty()) {
                tooltip.add(line);
            }
        }
    }

    private static final class EffectEntry {
        private final Potion potion;
        private final int duration;
        private final int amplifier;
        private final float probability;

        private EffectEntry(Potion potion, int duration, int amplifier, float probability) {
            this.potion = potion;
            this.duration = duration;
            this.amplifier = amplifier;
            this.probability = probability;
        }
    }
}
