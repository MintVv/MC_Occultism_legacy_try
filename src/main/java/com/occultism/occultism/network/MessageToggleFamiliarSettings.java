package com.occultism.occultism.network;

import com.occultism.occultism.capability.FamiliarSettings;
import com.occultism.occultism.registry.ModCapabilities;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Client request to toggle settings. The payload is a fixed boolean array in
 * the server-owned FamiliarSettings class order; it never accepts arbitrary
 * registry names from the client.
 */
public class MessageToggleFamiliarSettings implements IMessage {

    private boolean[] pressed;

    public MessageToggleFamiliarSettings() {
        this.pressed = new boolean[FamiliarSettings.getFamiliarClasses().length];
    }

    public MessageToggleFamiliarSettings(boolean[] pressed) {
        int size = FamiliarSettings.getFamiliarClasses().length;
        this.pressed = new boolean[size];
        if (pressed != null) {
            System.arraycopy(pressed, 0, this.pressed, 0, Math.min(pressed.length, size));
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int size = FamiliarSettings.getFamiliarClasses().length;
        this.pressed = new boolean[size];
        for (int i = 0; i < size; i++) {
            this.pressed[i] = buf.readBoolean();
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        for (boolean value : this.pressed) {
            buf.writeBoolean(value);
        }
    }

    public static class Handler implements IMessageHandler<MessageToggleFamiliarSettings, IMessage> {

        @Override
        public IMessage onMessage(MessageToggleFamiliarSettings message, MessageContext context) {
            EntityPlayerMP player = context.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                if (ModCapabilities.FAMILIAR_SETTINGS == null) {
                    return;
                }
                FamiliarSettings settings = player.getCapability(ModCapabilities.FAMILIAR_SETTINGS,
                        (EnumFacing) null);
                if (settings == null) {
                    return;
                }
                Class<?>[] familiars = FamiliarSettings.getFamiliarClasses();
                for (int i = 0; i < familiars.length && i < message.pressed.length; i++) {
                    if (message.pressed[i]) {
                        boolean enabled = settings.toggleFamiliar(familiars[i]);
                        String id = familiarId(familiars[i]);
                        player.sendStatusMessage(new TextComponentTranslation(
                                "message.occultism.familiar." + id + (enabled ? ".enabled" : ".disabled")), true);
                    }
                }
                ModNetworking.syncFamiliarSettings(player);
            });
            return null;
        }

        private static String familiarId(Class<?> familiarClass) {
            if (Entity.class.isAssignableFrom(familiarClass)) {
                @SuppressWarnings("unchecked")
                Class<? extends Entity> entityClass = (Class<? extends Entity>) familiarClass;
                ResourceLocation id = EntityList.getKey(entityClass);
                if (id != null) {
                    return id.getPath();
                }
            }
            String name = familiarClass.getSimpleName();
            if (name.startsWith("Entity") && name.endsWith("Familiar")) {
                name = name.substring("Entity".length(), name.length() - "Familiar".length());
            }
            return name.toLowerCase(java.util.Locale.ROOT);
        }
    }
}
