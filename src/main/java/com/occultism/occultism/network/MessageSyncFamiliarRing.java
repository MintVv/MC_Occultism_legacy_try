package com.occultism.occultism.network;

import com.occultism.occultism.capability.FamiliarRingCapability;
import com.occultism.occultism.registry.ModCapabilities;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/** Server-to-client synchronization for the native Familiar Ring slot. */
public class MessageSyncFamiliarRing implements IMessage {

    private NBTTagCompound ring;

    public MessageSyncFamiliarRing() {
    }

    public MessageSyncFamiliarRing(FamiliarRingCapability capability) {
        this.ring = capability == null ? new NBTTagCompound() : capability.serializeNBT();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.ring = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, this.ring == null ? new NBTTagCompound() : this.ring);
    }

    public static class Handler implements IMessageHandler<MessageSyncFamiliarRing, IMessage> {

        @Override
        public IMessage onMessage(MessageSyncFamiliarRing message, MessageContext context) {
            if (context.side != Side.CLIENT) {
                return null;
            }
            Minecraft.getMinecraft().addScheduledTask(() -> {
                if (Minecraft.getMinecraft().player == null || ModCapabilities.FAMILIAR_RING == null) {
                    return;
                }
                FamiliarRingCapability capability = Minecraft.getMinecraft().player.getCapability(
                        ModCapabilities.FAMILIAR_RING, (EnumFacing) null);
                if (capability != null) {
                    capability.deserializeNBT(message.ring);
                }
            });
            return null;
        }
    }
}
