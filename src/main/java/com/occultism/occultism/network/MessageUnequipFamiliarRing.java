package com.occultism.occultism.network;

import com.occultism.occultism.capability.FamiliarRingCapability;
import com.occultism.occultism.handler.FamiliarRingHandler;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/** Client request to remove the equipped ring; all state is validated server-side. */
public class MessageUnequipFamiliarRing implements IMessage {

    @Override
    public void fromBytes(ByteBuf buf) {
    }

    @Override
    public void toBytes(ByteBuf buf) {
    }

    public static class Handler implements IMessageHandler<MessageUnequipFamiliarRing, IMessage> {

        @Override
        public IMessage onMessage(MessageUnequipFamiliarRing message, MessageContext context) {
            EntityPlayerMP player = context.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                if (!player.isSneaking() || !player.getHeldItem(EnumHand.MAIN_HAND).isEmpty()) {
                    return;
                }
                FamiliarRingCapability capability = FamiliarRingHandler.get(player);
                if (capability == null || !capability.isEquipped()) {
                    return;
                }
                ItemStack removed = capability.unequip(player);
                if (removed.isEmpty()) {
                    return;
                }
                if (!player.addItemStackToInventory(removed)) {
                    player.dropItem(removed, false);
                }
                player.sendStatusMessage(
                        new TextComponentTranslation("item.occultism.familiar_ring.message.unequipped"), true);
                ModNetworking.syncFamiliarRing(player);
            });
            return null;
        }
    }
}
