package com.occultism.occultism.network;

import javax.annotation.Nullable;

import com.occultism.occultism.container.ContainerStorageController;
import com.occultism.occultism.spirit.MachineReference;
import com.occultism.occultism.tileentity.TileEntityStorageController;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Requests an input order for a linked machine from an open storage terminal.
 *
 * <p>The 1.12 packet carries a compact machine reference because this branch
 * has no GlobalBlockPos/FriendlyByteBuf implementation.  The server treats all
 * endpoint data as an assertion only: {@link TileEntityStorageController}
 * resolves the registered route and verifies the bound spirit before queuing
 * anything.</p>
 */
public class MessageRequestOrder implements IMessage {

    public static final int MAX_NBT_BYTES = 16 * 1024;
    public static final int MAX_CUSTOM_NAME_LENGTH = 64;
    public static final int MAX_REGISTRY_NAME_LENGTH = 256;

    @Nullable
    private BlockPos storageControllerPosition;
    private int storageControllerDimension;
    @Nullable
    private MachineReference targetMachine;
    private ItemStack stack = ItemStack.EMPTY;
    private boolean valid;

    public MessageRequestOrder() {
        this.valid = false;
    }

    public MessageRequestOrder(@Nullable BlockPos storageControllerPosition, int storageControllerDimension,
            @Nullable MachineReference targetMachine, @Nullable ItemStack stack) {
        this.storageControllerPosition = storageControllerPosition;
        this.storageControllerDimension = storageControllerDimension;
        this.targetMachine = targetMachine == null ? null : copy(targetMachine);
        this.stack = stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
        this.valid = isBoundedPayload(this.storageControllerPosition, this.targetMachine, this.stack);
    }

    @Nullable
    public BlockPos getStorageControllerPosition() {
        return this.storageControllerPosition;
    }

    public int getStorageControllerDimension() {
        return this.storageControllerDimension;
    }

    @Nullable
    public MachineReference getTargetMachine() {
        return this.targetMachine == null ? null : copy(this.targetMachine);
    }

    public ItemStack getStack() {
        return this.stack.copy();
    }

    public boolean isValid() {
        return this.valid && isBoundedPayload(this.storageControllerPosition, this.targetMachine, this.stack);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.valid = false;
        this.storageControllerPosition = null;
        this.targetMachine = null;
        this.stack = ItemStack.EMPTY;
        try {
            if (buf == null || buf.readableBytes() <= 0 || buf.readableBytes() > MAX_NBT_BYTES) {
                return;
            }
            NBTTagCompound payload = ByteBufUtils.readTag(buf);
            if (payload == null || payload.getKeySet().size() > 12
                    || !payload.hasKey("Storage", 10) || !payload.hasKey("Machine", 10)
                    || !payload.hasKey("Stack", 10)) {
                return;
            }
            NBTTagCompound storage = payload.getCompoundTag("Storage");
            if (!storage.hasKey("X", 3) || !storage.hasKey("Y", 3) || !storage.hasKey("Z", 3)) {
                return;
            }
            this.storageControllerPosition = new BlockPos(storage.getInteger("X"), storage.getInteger("Y"),
                    storage.getInteger("Z"));
            this.storageControllerDimension = storage.getInteger("Dimension");
            this.targetMachine = MachineReference.from(payload.getCompoundTag("Machine"));
            ItemStack decoded = new ItemStack(payload.getCompoundTag("Stack"));
            this.stack = decoded.isEmpty() ? ItemStack.EMPTY : decoded;
            this.valid = isBoundedPayload(this.storageControllerPosition, this.targetMachine, this.stack);
        } catch (RuntimeException ignored) {
            this.storageControllerPosition = null;
            this.targetMachine = null;
            this.stack = ItemStack.EMPTY;
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        if (!this.isValid()) {
            throw new IllegalStateException("Invalid machine-order payload");
        }
        NBTTagCompound payload = new NBTTagCompound();
        NBTTagCompound storage = new NBTTagCompound();
        storage.setInteger("X", this.storageControllerPosition.getX());
        storage.setInteger("Y", this.storageControllerPosition.getY());
        storage.setInteger("Z", this.storageControllerPosition.getZ());
        storage.setInteger("Dimension", this.storageControllerDimension);
        payload.setTag("Storage", storage);
        payload.setTag("Machine", this.targetMachine.writeToNBT(new NBTTagCompound()));
        payload.setTag("Stack", this.stack.writeToNBT(new NBTTagCompound()));
        ByteBufUtils.writeTag(buf, payload);
    }

    private static boolean isBoundedPayload(@Nullable BlockPos storage, @Nullable MachineReference machine,
            @Nullable ItemStack stack) {
        if (storage == null || machine == null || !machine.isStrictlyConfigured() || stack == null || stack.isEmpty()
                || stack.getCount() <= 0 || stack.getCount() > stack.getMaxStackSize()) {
            return false;
        }
        return length(machine.getCustomName()) <= MAX_CUSTOM_NAME_LENGTH
                && length(machine.getInsertRegistryName()) <= MAX_REGISTRY_NAME_LENGTH
                && length(machine.getExtractRegistryName()) <= MAX_REGISTRY_NAME_LENGTH;
    }

    private static int length(@Nullable String value) {
        return value == null ? 0 : value.length();
    }

    private static MachineReference copy(MachineReference source) {
        return MachineReference.from(source.writeToNBT(new NBTTagCompound()));
    }

    public static class Handler implements IMessageHandler<MessageRequestOrder, IMessage> {

        @Override
        public IMessage onMessage(final MessageRequestOrder message, MessageContext context) {
            if (context.side != Side.SERVER || context.getServerHandler() == null || message == null
                    || !message.isValid()) {
                return null;
            }
            final EntityPlayerMP player = context.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> applyOnServer(message, player));
            return null;
        }

        private static void applyOnServer(MessageRequestOrder message, EntityPlayerMP player) {
            if (player == null || !(player.openContainer instanceof ContainerStorageController)) {
                return;
            }
            ContainerStorageController container = (ContainerStorageController) player.openContainer;
            if (!container.canInteractWith(player)) {
                return;
            }
            TileEntityStorageController controller = container.getStorageController();
            if (controller == null || controller.isInvalid() || controller.getWorld() == null
                    || controller.getWorld().isRemote || controller.getPos() == null
                    || !controller.getWorld().isBlockLoaded(controller.getPos())
                    || controller.getWorld().provider.getDimension() != message.storageControllerDimension
                    || !controller.getPos().equals(message.storageControllerPosition)
                    || controller.getWorld().getTileEntity(controller.getPos()) != controller) {
                return;
            }
            // addDepositOrder performs the linked-route and bound-spirit checks;
            // this call never resolves or loads an endpoint supplied by a client.
            if (controller.addDepositOrder(message.targetMachine, message.stack, message.stack.getCount())) {
                player.sendStatusMessage(new TextComponentTranslation(
                        "network.messages.occultism.request_order.order_received"), true);
            }
        }
    }
}
