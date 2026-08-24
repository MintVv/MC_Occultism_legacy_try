package com.occultism.occultism.network;

import com.occultism.occultism.util.MovementUtil;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/** Client request for one server-authoritative aerial jump. */
public class MessageDoubleJump implements IMessage {

    @Override
    public void fromBytes(ByteBuf buf) {
    }

    @Override
    public void toBytes(ByteBuf buf) {
    }

    public static class Handler implements IMessageHandler<MessageDoubleJump, IMessage> {

        @Override
        public IMessage onMessage(MessageDoubleJump message, MessageContext context) {
            if (context.side != Side.SERVER || context.getServerHandler() == null) {
                return null;
            }
            EntityPlayerMP player = context.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                if (!MovementUtil.doubleJump(player)) {
                    return;
                }
                player.world.spawnParticle(EnumParticleTypes.CLOUD, player.posX, player.posY, player.posZ,
                        0.0D, 0.0D, 0.0D, 5);
                ModNetworking.syncDoubleJump(player);
            });
            return null;
        }
    }
}
