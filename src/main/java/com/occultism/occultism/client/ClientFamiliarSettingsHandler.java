package com.occultism.occultism.client;

import com.occultism.occultism.Tags;
import com.occultism.occultism.capability.FamiliarSettings;
import com.occultism.occultism.network.MessageToggleFamiliarSettings;
import com.occultism.occultism.network.ModNetworking;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

/** Client-only key bindings for the per-player Familiar effect switches. */
@Mod.EventBusSubscriber(modid = Tags.MOD_ID, value = Side.CLIENT)
public final class ClientFamiliarSettingsHandler {

    private static final String CATEGORY = "key.occultism.category";
    private static KeyBinding[] familiarKeys = new KeyBinding[0];
    private static boolean registered;

    private ClientFamiliarSettingsHandler() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        Class<?>[] familiars = FamiliarSettings.getFamiliarClasses();
        familiarKeys = new KeyBinding[familiars.length];
        for (int i = 0; i < familiars.length; i++) {
            familiarKeys[i] = new KeyBinding("key.occultism.familiar." + familiarPath(familiars[i]),
                    Keyboard.KEY_NONE, CATEGORY);
            ClientRegistry.registerKeyBinding(familiarKeys[i]);
        }
        registered = true;
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.KeyInputEvent event) {
        handleInput();
    }

    @SubscribeEvent
    public static void onMouseInput(InputEvent.MouseInputEvent event) {
        handleInput();
    }

    private static void handleInput() {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.player == null || minecraft.currentScreen != null || familiarKeys.length == 0) {
            return;
        }

        boolean[] pressed = new boolean[familiarKeys.length];
        boolean anyPressed = false;
        for (int i = 0; i < familiarKeys.length; i++) {
            pressed[i] = familiarKeys[i].isPressed();
            anyPressed |= pressed[i];
        }
        if (anyPressed) {
            ModNetworking.CHANNEL.sendToServer(new MessageToggleFamiliarSettings(pressed));
        }
    }

    private static String familiarPath(Class<?> familiarClass) {
        if (familiarClass != null && Entity.class.isAssignableFrom(familiarClass)) {
            @SuppressWarnings("unchecked")
            Class<? extends Entity> entityClass = (Class<? extends Entity>) familiarClass;
            ResourceLocation id = EntityList.getKey(entityClass);
            if (id != null) {
                return id.getPath();
            }
        }
        String name = familiarClass == null ? "unknown" : familiarClass.getSimpleName();
        if (name.startsWith("Entity") && name.endsWith("Familiar")) {
            name = name.substring("Entity".length(), name.length() - "Familiar".length());
        }
        return name.toLowerCase(java.util.Locale.ROOT);
    }
}
