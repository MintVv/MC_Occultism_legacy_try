package com.occultism.occultism.client;

import com.occultism.occultism.Tags;
import com.occultism.occultism.block.BlockGoldenSacrificialBowl;
import com.occultism.occultism.block.BlockSacrificialBowl;
import com.occultism.occultism.compat.patchouli.PentaclePreview;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.input.Keyboard;

/**
 * Keybind that projects the pentacle around the nearest sacrificial bowl.
 *
 * <p>JEI has no way to host this: {@code IRecipeCategory} exposes only drawing and tooltip hooks,
 * with no click callback, which is why upstream's eye control lives in the Patchouli book page and
 * not in the recipe viewer. A keybind is the closest equivalent - it needs neither the book nor a
 * particular screen, just the player standing at a drawn pentacle.</p>
 */
@Mod.EventBusSubscriber(modid = Tags.MOD_ID, value = Side.CLIENT)
public final class ClientPentaclePreviewHandler {

    private static final String CATEGORY = "key.occultism.category";
    private static final int SEARCH_RADIUS = 8;

    private static KeyBinding previewKey;
    private static boolean registered;

    private ClientPentaclePreviewHandler() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        previewKey = new KeyBinding("key.occultism.pentacle_preview", Keyboard.KEY_P, CATEGORY);
        ClientRegistry.registerKeyBinding(previewKey);
        registered = true;
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.KeyInputEvent event) {
        if (!registered || Minecraft.getMinecraft().currentScreen != null) {
            return;
        }
        if (previewKey == null || !previewKey.isPressed()) {
            return;
        }
        EntityPlayer player = Minecraft.getMinecraft().player;
        if (player == null) {
            return;
        }
        World world = player.world;
        BlockPos bowl = findNearestBowl(world, player.getPosition());
        if (bowl == null) {
            player.sendStatusMessage(new net.minecraft.util.text.TextComponentTranslation(
                    "occultism.pentacle_preview.no_bowl"), true);
            return;
        }
        PentaclePreview.start(world, bowl, player);
    }

    private static BlockPos findNearestBowl(World world, BlockPos around) {
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -SEARCH_RADIUS; dx <= SEARCH_RADIUS; dx++) {
            for (int dy = -SEARCH_RADIUS; dy <= SEARCH_RADIUS; dy++) {
                for (int dz = -SEARCH_RADIUS; dz <= SEARCH_RADIUS; dz++) {
                    cursor.setPos(around.getX() + dx, around.getY() + dy, around.getZ() + dz);
                    Block block = world.getBlockState(cursor).getBlock();
                    if (!(block instanceof BlockSacrificialBowl) && !(block instanceof BlockGoldenSacrificialBowl)) {
                        continue;
                    }
                    double distance = cursor.distanceSq(around);
                    if (distance < bestDistance) {
                        bestDistance = distance;
                        best = cursor.toImmutable();
                    }
                }
            }
        }
        return best;
    }
}
