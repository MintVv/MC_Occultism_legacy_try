package com.occultism.occultism.client;

import com.occultism.occultism.Tags;
import com.occultism.occultism.network.MessageDoubleJump;
import com.occultism.occultism.network.ModNetworking;
import com.occultism.occultism.util.MovementUtil;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;

/** Predicts the jump locally and asks the server to authorize the same jump. */
@Mod.EventBusSubscriber(modid = Tags.MOD_ID, value = Side.CLIENT)
public final class ClientDoubleJumpHandler {

    private static boolean jumpKeyDown;

    private ClientDoubleJumpHandler() {
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.KeyInputEvent event) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.player == null || minecraft.currentScreen != null) {
            jumpKeyDown = false;
            return;
        }
        boolean pressed = minecraft.gameSettings.keyBindJump.isKeyDown();
        if (pressed && !jumpKeyDown && MovementUtil.doubleJump(minecraft.player)) {
            ModNetworking.CHANNEL.sendToServer(new MessageDoubleJump());
        }
        jumpKeyDown = pressed;
    }
}
