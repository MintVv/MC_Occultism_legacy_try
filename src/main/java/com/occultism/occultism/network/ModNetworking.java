package com.occultism.occultism.network;

import com.occultism.occultism.Tags;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import com.occultism.occultism.capability.FamiliarSettings;
import com.occultism.occultism.capability.FamiliarRingCapability;
import com.occultism.occultism.registry.ModCapabilities;
import com.occultism.occultism.tileentity.StorageSortDirection;
import com.occultism.occultism.tileentity.StorageSortType;
import com.occultism.occultism.tileentity.TileEntityStorageController;

public final class ModNetworking {

    public static final SimpleNetworkWrapper CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(Tags.MOD_ID);

    private ModNetworking() {
    }

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(MessageSetBookOfCallingConfig.Handler.class, MessageSetBookOfCallingConfig.class,
                id++, Side.SERVER);
        CHANNEL.registerMessage(MessageToggleFamiliarSettings.Handler.class, MessageToggleFamiliarSettings.class,
                id++, Side.SERVER);
        CHANNEL.registerMessage(MessageSyncFamiliarSettings.Handler.class, MessageSyncFamiliarSettings.class,
                id++, Side.CLIENT);
        CHANNEL.registerMessage(MessageUnequipFamiliarRing.Handler.class, MessageUnequipFamiliarRing.class,
                id++, Side.SERVER);
        CHANNEL.registerMessage(MessageSyncFamiliarRing.Handler.class, MessageSyncFamiliarRing.class,
                id++, Side.CLIENT);
        CHANNEL.registerMessage(MessageUpdateStorageSettings.Handler.class, MessageUpdateStorageSettings.class,
                id++, Side.SERVER);
        CHANNEL.registerMessage(MessageRequestStacks.Handler.class, MessageRequestStacks.class,
                id++, Side.SERVER);
        CHANNEL.registerMessage(MessageUpdateStacks.Handler.class, MessageUpdateStacks.class,
                id++, Side.CLIENT);
        CHANNEL.registerMessage(MessageSetRecipe.Handler.class, MessageSetRecipe.class,
                id++, Side.SERVER);
        CHANNEL.registerMessage(MessageSetManagedMachine.Handler.class, MessageSetManagedMachine.class,
                id++, Side.SERVER);
        CHANNEL.registerMessage(MessageRequestOrder.Handler.class, MessageRequestOrder.class,
                id++, Side.SERVER);
    }

    /** Sends a storage-terminal setting update from a client GUI. */
    public static void sendStorageSettings(String searchQuery, StorageSortType sortType,
            StorageSortDirection sortDirection) {
        CHANNEL.sendToServer(new MessageUpdateStorageSettings(searchQuery, sortType, sortDirection));
    }

    /** Requests the virtual stack snapshot for the currently opened container. */
    public static void requestStorageStacks() {
        CHANNEL.sendToServer(new MessageRequestStacks());
    }

    /** Sends an upstream-format 3x3 recipe template to the open terminal. */
    public static void sendStorageRecipe(NBTTagCompound recipe) {
        CHANNEL.sendToServer(new MessageSetRecipe(recipe));
    }

    /** Sends client-trusted machine faces/name; endpoint coordinates stay server-side. */
    public static void sendManagedMachine(com.occultism.occultism.spirit.MachineReference reference) {
        MessageSetManagedMachine message = new MessageSetManagedMachine(reference);
        if (message.isValid()) {
            CHANNEL.sendToServer(message);
        }
    }

    /** Requests a validated input order for a linked managed-machine spirit. */
    public static void requestMachineOrder(net.minecraft.util.math.BlockPos storageControllerPosition,
            int storageControllerDimension, com.occultism.occultism.spirit.MachineReference targetMachine,
            net.minecraft.item.ItemStack stack) {
        MessageRequestOrder message = new MessageRequestOrder(storageControllerPosition, storageControllerDimension,
                targetMachine, stack);
        if (message.isValid()) {
            CHANNEL.sendToServer(message);
        }
    }

    /** Sends a fresh virtual stack snapshot after a server-side inventory mutation. */
    public static void syncStorageStacks(EntityPlayerMP player, TileEntityStorageController controller) {
        if (player == null || controller == null || controller.isInvalid()) {
            return;
        }
        // The controller owns a bounded, invalidated-on-mutation message cache.
        // Reusing it keeps direct and proxy/container sends on one snapshot
        // boundary and avoids recompressing the full virtual inventory.
        CHANNEL.sendTo(controller.getMessageUpdateStacks(), player);
    }

    public static void syncFamiliarSettings(EntityPlayerMP player) {
        if (player == null || ModCapabilities.FAMILIAR_SETTINGS == null) {
            return;
        }
        FamiliarSettings settings = player.getCapability(ModCapabilities.FAMILIAR_SETTINGS, (EnumFacing) null);
        if (settings != null) {
            CHANNEL.sendTo(new MessageSyncFamiliarSettings(settings), player);
        }
    }

    public static void syncFamiliarRing(EntityPlayerMP player) {
        if (player == null || ModCapabilities.FAMILIAR_RING == null) {
            return;
        }
        FamiliarRingCapability ring = player.getCapability(ModCapabilities.FAMILIAR_RING, (EnumFacing) null);
        if (ring != null) {
            CHANNEL.sendTo(new MessageSyncFamiliarRing(ring), player);
        }
    }
}
