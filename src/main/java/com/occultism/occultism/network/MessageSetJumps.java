package com.occultism.occultism.network;

import com.occultism.occultism.capability.DoubleJumpCapability;
import com.occultism.occultism.registry.ModCapabilities;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/** Server-to-client synchronization for the aerial jump counter. */
public class MessageSetJumps implements IMessage {

    private int jumps;

    public MessageSetJumps() {
    }

    public MessageSetJumps(int jumps) {
        this.jumps = Math.max(0, jumps);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.jumps = Math.max(0, buf.readInt());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.jumps);
    }

    public static class Handler implements IMessageHandler<MessageSetJumps, IMessage> {

        @Override
        public IMessage onMessage(MessageSetJumps message, MessageContext context) {
            if (context.side != Side.CLIENT) {
                return null;
            }
            Minecraft.getMinecraft().addScheduledTask(() -> {
                if (Minecraft.getMinecraft().player == null || ModCapabilities.DOUBLE_JUMP == null
                        || Minecraft.getMinecraft().player.onGround) {
                    return;
                }
                DoubleJumpCapability capability = Minecraft.getMinecraft().player.getCapability(
                        ModCapabilities.DOUBLE_JUMP, (EnumFacing) null);
                if (capability != null) {
                    capability.setJumps(message.jumps);
                }
            });
            return null;
        }
    }
}
