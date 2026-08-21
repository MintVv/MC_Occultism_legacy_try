package com.occultism.occultism.network;

import com.occultism.occultism.Tags;
import com.occultism.occultism.capability.FamiliarSettings;
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

/** Server-to-client synchronization for the player's Familiar settings. */
public class MessageSyncFamiliarSettings implements IMessage {

    private NBTTagCompound settings;

    public MessageSyncFamiliarSettings() {
    }

    public MessageSyncFamiliarSettings(FamiliarSettings settings) {
        this.settings = settings == null ? new NBTTagCompound() : settings.serializeNBT();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.settings = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, this.settings == null ? new NBTTagCompound() : this.settings);
    }

    public static class Handler implements IMessageHandler<MessageSyncFamiliarSettings, IMessage> {

        @Override
        public IMessage onMessage(MessageSyncFamiliarSettings message, MessageContext context) {
            if (context.side != Side.CLIENT) {
                return null;
            }
            Minecraft.getMinecraft().addScheduledTask(() -> {
                if (Minecraft.getMinecraft().player == null || ModCapabilities.FAMILIAR_SETTINGS == null) {
                    return;
                }
                FamiliarSettings settings = Minecraft.getMinecraft().player.getCapability(
                        ModCapabilities.FAMILIAR_SETTINGS, (EnumFacing) null);
                if (settings != null) {
                    settings.deserializeNBT(message.settings);
                }
            });
            return null;
        }
    }
}
