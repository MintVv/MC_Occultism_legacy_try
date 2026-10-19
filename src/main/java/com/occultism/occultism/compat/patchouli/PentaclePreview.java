package com.occultism.occultism.compat.patchouli;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import vazkii.patchouli.api.IMultiblock;
import vazkii.patchouli.api.PatchouliAPI;
import vazkii.patchouli.client.handler.MultiblockVisualizationHandler;
import vazkii.patchouli.common.multiblock.Multiblock;

import com.occultism.occultism.Tags;
import com.occultism.occultism.ritual.PentacleDefinition;
import com.occultism.occultism.ritual.PentacleDetector;
import com.occultism.occultism.ritual.PentacleMatch;

/**
 * Projects a drawn pentacle into the world using Patchouli's own visualization.
 *
 * <p>The book already offers this on a pentacle page, but only after finding the page and its
 * visualize control. This exposes the same thing from the world: sneak and right-click a
 * sacrificial bowl while holding the dictionary, and the pentacle currently drawn around that bowl
 * is projected as a ghost. Nothing new is rendered - {@code MultiblockVisualizationHandler} is the
 * public entry point Patchouli's own page uses, so the controls and the look are the ones players
 * already know from the book.</p>
 *
 * <p>The multiblock registered for a pentacle is the same one the page shows, so what appears here
 * is exactly what the book would project.</p>
 */
@SideOnly(Side.CLIENT)
public final class PentaclePreview {

    private PentaclePreview() {
    }

    /**
     * @return true when a projection was started, so the caller can swallow the interaction.
     */
    public static boolean start(World world, BlockPos bowlPos, EntityPlayer player) {
        if (!Loader.isModLoaded("patchouli") || world == null || bowlPos == null) {
            return false;
        }

        List<PentacleMatch> matches = PentacleDetector.findMatchesAt(world, bowlPos);
        if (matches.isEmpty()) {
            player.sendStatusMessage(new TextComponentTranslation("occultism.pentacle_preview.none"), true);
            return false;
        }

        return showFor(matches.get(0).getDefinition(), player);
    }

    /**
     * Projects the given pentacle, whether or not it is currently drawn in the world. Used by the
     * diviner to show a pentacle as a template to build.
     */
    public static boolean showFor(PentacleDefinition definition, EntityPlayer player) {
        ResourceLocation id = new ResourceLocation(Tags.MOD_ID, definition.getId());
        IMultiblock multiblock = PatchouliAPI.instance.getMultiblock(id);
        if (!(multiblock instanceof Multiblock)) {
            player.sendStatusMessage(new TextComponentTranslation("occultism.pentacle_preview.unavailable"), true);
            return false;
        }

        // Same call Patchouli's own multiblock page makes; unanchored so the ghost can be walked
        // into place, which is how the book preview behaves. Guarded because an API change in a
        // future Patchouli release must not take the client down with it.
        try {
            MultiblockVisualizationHandler.setMultiblock((Multiblock) multiblock, definition.getDisplayName(),
                    null, false);
        } catch (RuntimeException | LinkageError failure) {
            player.sendStatusMessage(new TextComponentTranslation("occultism.pentacle_preview.unavailable"), true);
            return false;
        }
        player.sendStatusMessage(new TextComponentTranslation("occultism.pentacle_preview.shown",
                definition.getDisplayName()), true);
        return true;
    }

    /** True when the client currently has a projection running, so a second click does not stack. */
    public static boolean isShowing() {
        return Loader.isModLoaded("patchouli") && MultiblockVisualizationHandler.hasMultiblock;
    }

    /** Drops the current projection; the second activation of a diviner clears instead of stacking. */
    public static boolean clear(EntityPlayer player) {
        if (!isShowing()) {
            return false;
        }
        try {
            MultiblockVisualizationHandler.hasMultiblock = false;
        } catch (RuntimeException | LinkageError failure) {
            return false;
        }
        player.sendStatusMessage(new TextComponentTranslation("occultism.pentacle_preview.cleared"), true);
        return true;
    }

    /** Used only to keep the Minecraft import meaningful for future hud work; safe to drop. */
    static Minecraft client() {
        return Minecraft.getMinecraft();
    }
}
