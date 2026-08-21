package com.occultism.occultism.capability;

import javax.annotation.Nullable;

import com.occultism.occultism.entity.IFamiliar;
import com.occultism.occultism.item.ItemFamiliarRing;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;

/**
 * Native 1.12 accessory-slot equivalent for one Familiar Ring.
 *
 * <p>The ring remains an ItemStack so its contents survive normal player NBT
 * saves. The entity is only materialized while a player is present, and is
 * written back before serialization or removal from the slot.</p>
 */
public final class FamiliarRingCapability {

    private static final String TAG_RING = "Ring";

    private ItemStack ring = ItemStack.EMPTY;
    @Nullable
    private IFamiliar familiar;
    @Nullable
    private EntityPlayer familiarWorldOwner;

    public boolean isEquipped() {
        return !this.ring.isEmpty();
    }

    public ItemStack getRingCopy() {
        return this.ring.copy();
    }

    /**
     * Client-side renderers use this to associate an ItemStack with the
     * persistent native slot. Ordinary inventory rings must not be treated as
     * the equipped ring merely because they contain the same entity NBT.
     */
    public boolean isEquippedStack(@Nullable ItemStack stack) {
        return this.isEquipped() && stack != null && !stack.isEmpty()
                && ItemStack.areItemStacksEqual(this.ring, stack);
    }

    /** Equips a single filled Familiar Ring. The caller removes it from hand. */
    public boolean equip(EntityPlayer player, ItemStack stack) {
        if (player == null || stack == null || stack.isEmpty()
                || !(stack.getItem() instanceof ItemFamiliarRing)
                || !ItemFamiliarRing.hasStoredFamiliar(stack) || this.isEquipped()) {
            return false;
        }
        EntityLivingBase preview = ItemFamiliarRing.createStoredFamiliarForEquipment(stack, player.world);
        if (!ItemFamiliarRing.canEquipFamiliar(preview)) {
            return false;
        }
        this.ring = stack.copy();
        this.ring.setCount(1);
        this.familiar = null;
        this.familiarWorldOwner = null;
        return true;
    }

    /** Removes and returns the equipped ring, including its latest entity NBT. */
    public ItemStack unequip(@Nullable EntityPlayer player) {
        if (this.ring.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (player != null) {
            this.persistFamiliar();
        }
        ItemStack result = this.ring.copy();
        this.ring = ItemStack.EMPTY;
        this.familiar = null;
        this.familiarWorldOwner = null;
        return result;
    }

    /**
     * Resolves the stored entity in the current world without adding it to the
     * world. A dimension change recreates it from the latest serialized state.
     */
    @Nullable
    public IFamiliar getFamiliar(EntityPlayer player) {
        if (player == null || this.ring.isEmpty()) {
            return null;
        }
        if (this.familiar != null && this.familiarWorldOwner == player
                && this.familiar.getFamiliarEntity().world == player.world) {
            return this.familiar;
        }
        if (this.familiar != null) {
            this.persistFamiliar();
            this.familiar = null;
        }
        EntityLivingBase entity = ItemFamiliarRing.createStoredFamiliarForEquipment(this.ring, player.world);
        if (!(entity instanceof IFamiliar)) {
            return null;
        }
        this.familiar = (IFamiliar) entity;
        this.familiarWorldOwner = player;
        return this.familiar;
    }

    /** Returns the stored familiar only when its serialized owner is this player. */
    @Nullable
    public IFamiliar getActiveFamiliar(EntityPlayer player) {
        IFamiliar candidate = this.getFamiliar(player);
        if (candidate == null) {
            return null;
        }
        EntityLivingBase storedOwner = candidate.getFamiliarOwner();
        if (storedOwner == player || storedOwner != null && player.getUniqueID().equals(storedOwner.getUniqueID())) {
            return candidate;
        }
        return null;
    }

    /** Runs the upstream Curios familiar hook for the equipped ring. */
    public void tick(EntityPlayer player) {
        IFamiliar active = this.getActiveFamiliar(player);
        if (active == null) {
            return;
        }

        if (!player.world.isRemote && player.ticksExisted % 20 == 0 && active.isEffectEnabled(player)) {
            for (PotionEffect effect : active.getFamiliarEffects()) {
                if (effect != null) {
                    player.addPotionEffect(new PotionEffect(effect));
                }
            }
        }
        active.curioTick(player);
        if (!player.world.isRemote && player.ticksExisted % 20 == 0) {
            this.persistFamiliar();
        }
    }

    public void copyFrom(FamiliarRingCapability other) {
        if (other == null) {
            this.clear();
            return;
        }
        this.deserializeNBT(other.serializeNBT());
    }

    public void clear() {
        this.ring = ItemStack.EMPTY;
        this.familiar = null;
        this.familiarWorldOwner = null;
    }

    public NBTTagCompound serializeNBT() {
        this.persistFamiliar();
        NBTTagCompound nbt = new NBTTagCompound();
        if (!this.ring.isEmpty()) {
            nbt.setTag(TAG_RING, this.ring.writeToNBT(new NBTTagCompound()));
        }
        return nbt;
    }

    public void deserializeNBT(@Nullable NBTTagCompound nbt) {
        this.clear();
        if (nbt == null || !nbt.hasKey(TAG_RING, 10)) {
            return;
        }
        ItemStack restored = new ItemStack(nbt.getCompoundTag(TAG_RING));
        if (!restored.isEmpty() && restored.getItem() instanceof ItemFamiliarRing
                && ItemFamiliarRing.hasStoredFamiliar(restored)) {
            restored.setCount(1);
            this.ring = restored;
        }
    }

    private void persistFamiliar() {
        if (this.ring.isEmpty() || this.familiar == null) {
            return;
        }
        ItemFamiliarRing.updateStoredFamiliarState(this.ring, this.familiar.getFamiliarEntity());
    }
}
