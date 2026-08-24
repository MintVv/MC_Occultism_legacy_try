package com.occultism.occultism.registry;

import javax.annotation.Nullable;

import com.occultism.occultism.capability.FamiliarRingCapability;
import com.occultism.occultism.capability.FamiliarSettings;
import com.occultism.occultism.capability.DoubleJumpCapability;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraft.util.EnumFacing;

/** Forge capability registration for persistent player Familiar settings. */
public final class ModCapabilities {

    @CapabilityInject(FamiliarSettings.class)
    public static Capability<FamiliarSettings> FAMILIAR_SETTINGS = null;

    @CapabilityInject(FamiliarRingCapability.class)
    public static Capability<FamiliarRingCapability> FAMILIAR_RING = null;

    @CapabilityInject(DoubleJumpCapability.class)
    public static Capability<DoubleJumpCapability> DOUBLE_JUMP = null;

    private static boolean registered;

    private ModCapabilities() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        CapabilityManager.INSTANCE.register(FamiliarSettings.class, new IStorage<FamiliarSettings>() {
            @Nullable
            @Override
            public NBTBase writeNBT(Capability<FamiliarSettings> capability, FamiliarSettings instance,
                    EnumFacing side) {
                return instance.serializeNBT();
            }

            @Override
            public void readNBT(Capability<FamiliarSettings> capability, FamiliarSettings instance,
                    EnumFacing side, NBTBase nbt) {
                if (nbt instanceof NBTTagCompound) {
                    instance.deserializeNBT((NBTTagCompound) nbt);
                }
            }
        }, FamiliarSettings::new);
        CapabilityManager.INSTANCE.register(FamiliarRingCapability.class, new IStorage<FamiliarRingCapability>() {
            @Nullable
            @Override
            public NBTBase writeNBT(Capability<FamiliarRingCapability> capability, FamiliarRingCapability instance,
                    EnumFacing side) {
                return instance.serializeNBT();
            }

            @Override
            public void readNBT(Capability<FamiliarRingCapability> capability, FamiliarRingCapability instance,
                    EnumFacing side, NBTBase nbt) {
                if (nbt instanceof NBTTagCompound) {
                    instance.deserializeNBT((NBTTagCompound) nbt);
                }
            }
        }, FamiliarRingCapability::new);
        CapabilityManager.INSTANCE.register(DoubleJumpCapability.class, new IStorage<DoubleJumpCapability>() {
            @Nullable
            @Override
            public NBTBase writeNBT(Capability<DoubleJumpCapability> capability, DoubleJumpCapability instance,
                    EnumFacing side) {
                return instance.serializeNBT();
            }

            @Override
            public void readNBT(Capability<DoubleJumpCapability> capability, DoubleJumpCapability instance,
                    EnumFacing side, NBTBase nbt) {
                if (nbt instanceof NBTTagCompound) {
                    instance.deserializeNBT((NBTTagCompound) nbt);
                }
            }
        }, DoubleJumpCapability::new);
        registered = true;
    }

    /** Capability provider used by player attachment events. */
    public static final class Provider implements ICapabilitySerializable<NBTTagCompound> {

        private final FamiliarSettings settings = new FamiliarSettings();

        @SuppressWarnings("unchecked")
        @Nullable
        public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
            return capability == FAMILIAR_SETTINGS ? (T) this.settings : null;
        }

        @Override
        public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
            return capability == FAMILIAR_SETTINGS;
        }

        public FamiliarSettings getSettings() {
            return this.settings;
        }

        @Override
        public NBTTagCompound serializeNBT() {
            return this.settings.serializeNBT();
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt) {
            this.settings.deserializeNBT(nbt);
        }
    }

    /** Player provider for the native one-slot Familiar Ring accessory. */
    public static final class FamiliarRingProvider implements ICapabilitySerializable<NBTTagCompound> {

        private final FamiliarRingCapability ring = new FamiliarRingCapability();

        @SuppressWarnings("unchecked")
        @Nullable
        public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
            return capability == FAMILIAR_RING ? (T) this.ring : null;
        }

        @Override
        public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
            return capability == FAMILIAR_RING;
        }

        public FamiliarRingCapability getRing() {
            return this.ring;
        }

        @Override
        public NBTTagCompound serializeNBT() {
            return this.ring.serializeNBT();
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt) {
            this.ring.deserializeNBT(nbt);
        }
    }

    /** Player provider for the transient aerial-jump counter. */
    public static final class DoubleJumpProvider implements ICapabilitySerializable<NBTTagCompound> {

        private final DoubleJumpCapability doubleJump = new DoubleJumpCapability();

        @SuppressWarnings("unchecked")
        @Nullable
        public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
            return capability == DOUBLE_JUMP ? (T) this.doubleJump : null;
        }

        @Override
        public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
            return capability == DOUBLE_JUMP;
        }

        @Override
        public NBTTagCompound serializeNBT() {
            return this.doubleJump.serializeNBT();
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt) {
            this.doubleJump.deserializeNBT(nbt);
        }
    }
}
