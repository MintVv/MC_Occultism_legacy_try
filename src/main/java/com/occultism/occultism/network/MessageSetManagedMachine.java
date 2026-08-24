package com.occultism.occultism.network;

import java.util.UUID;

import javax.annotation.Nullable;

import com.occultism.occultism.entity.EntityFoliotJob;
import com.occultism.occultism.item.ItemBookOfCallingFoliotJob;
import com.occultism.occultism.spirit.MachineReference;
import com.occultism.occultism.spirit.SpiritJobType;
import com.occultism.occultism.util.ItemNBTUtil;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Updates the client-trusted portion of a managed-machine reference.
 *
 * <p>As in the upstream packet, the server never accepts endpoint positions
 * from this message. It resolves the spirit from the currently held Book of
 * Calling, verifies that the book is bound to a MANAGE_MACHINE spirit, and
 * applies only the two faces and custom display name to the server-owned
 * reference.</p>
 */
public class MessageSetManagedMachine implements IMessage {

    public static final int MAX_NBT_BYTES = 8 * 1024;
    public static final int MAX_CUSTOM_NAME_LENGTH = 64;

    @Nullable
    private MachineReference managedMachine;
    private boolean valid;

    public MessageSetManagedMachine() {
        this.valid = false;
    }

    public MessageSetManagedMachine(@Nullable MachineReference managedMachine) {
        this.managedMachine = managedMachine == null ? null : copy(managedMachine);
        this.valid = isBoundedReference(this.managedMachine);
    }

    @Nullable
    public MachineReference getManagedMachine() {
        return this.managedMachine == null ? null : copy(this.managedMachine);
    }

    public boolean isValid() {
        return this.valid && isBoundedReference(this.managedMachine);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.valid = false;
        this.managedMachine = null;
        try {
            if (buf == null || buf.readableBytes() > MAX_NBT_BYTES) {
                return;
            }
            NBTTagCompound payload = ByteBufUtils.readTag(buf);
            if (payload == null || payload.getKeySet().size() > 16) {
                return;
            }
            MachineReference decoded = MachineReference.from(payload);
            if (isBoundedReference(decoded)) {
                this.managedMachine = decoded;
                this.valid = true;
            }
        } catch (RuntimeException ignored) {
            this.managedMachine = null;
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        if (!this.isValid()) {
            throw new IllegalStateException("Invalid managed-machine payload");
        }
        ByteBufUtils.writeTag(buf, this.managedMachine.writeToNBT(new NBTTagCompound()));
    }

    private static boolean isBoundedReference(@Nullable MachineReference reference) {
        if (reference == null || !reference.isStrictlyConfigured()) {
            return false;
        }
        return safeLength(reference.getCustomName()) <= MAX_CUSTOM_NAME_LENGTH
                && safeLength(reference.getInsertRegistryName()) <= 256
                && safeLength(reference.getExtractRegistryName()) <= 256;
    }

    private static int safeLength(@Nullable String value) {
        return value == null ? 0 : value.length();
    }

    private static MachineReference copy(MachineReference source) {
        return MachineReference.from(source.writeToNBT(new NBTTagCompound()));
    }

    public static class Handler implements IMessageHandler<MessageSetManagedMachine, IMessage> {

        @Override
        public IMessage onMessage(final MessageSetManagedMachine message, MessageContext context) {
            if (context.side != Side.SERVER || context.getServerHandler() == null || message == null
                    || !message.isValid()) {
                return null;
            }
            final EntityPlayerMP player = context.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> applyOnServer(message, player));
            return null;
        }

        private static void applyOnServer(MessageSetManagedMachine message, EntityPlayerMP player) {
            if (player == null) {
                return;
            }
            ItemStack stack = player.getHeldItem(EnumHand.MAIN_HAND);
            if (stack.isEmpty() || !(stack.getItem() instanceof ItemBookOfCallingFoliotJob)) {
                return;
            }
            UUID spiritUuid = ItemNBTUtil.getSpiritEntityUUID(stack);
            if (spiritUuid == null) {
                return;
            }
            Entity spiritEntity = player.getServerWorld().getEntityFromUuid(spiritUuid);
            if (!(spiritEntity instanceof EntityFoliotJob)) {
                return;
            }
            EntityFoliotJob spirit = (EntityFoliotJob) spiritEntity;
            if (spirit.getJobType() != SpiritJobType.MANAGE_MACHINE || spirit.getManagedMachine() == null) {
                return;
            }
            MachineReference trusted = spirit.getManagedMachine();
            MachineReference requested = message.managedMachine;
            if (requested == null) {
                return;
            }
            // Positions, dimensions and registry names remain server-owned.
            trusted.setInsertPosition(trusted.getInsertPosition(), trusted.getInsertDimension(),
                    requested.getInsertFacing(), trusted.getInsertRegistryName());
            trusted.setExtractPosition(trusted.getExtractPosition(), trusted.getExtractDimension(),
                    requested.getExtractFacing(), trusted.getExtractRegistryName());
            trusted.setCustomName(requested.getCustomName());
            spirit.setManagedMachine(trusted);
            ItemNBTUtil.setManagedMachine(stack, trusted);
            player.setHeldItem(EnumHand.MAIN_HAND, stack);
            player.inventoryContainer.detectAndSendChanges();
        }
    }
}
