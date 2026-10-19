package com.occultism.occultism.client;

import java.util.List;

import com.occultism.occultism.block.BlockGoldenSacrificialBowl;
import com.occultism.occultism.block.BlockSacrificialBowl;
import com.occultism.occultism.compat.patchouli.PentaclePreview;
import com.occultism.occultism.ritual.PentacleDefinition;
import com.occultism.occultism.ritual.PentacleDetector;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Client half of the pentacle diviner.
 *
 * <p>The diviner exists to plan a build, not to inspect a finished one: the ritual satchel draws the
 * pentacle for you, so asking the player to place a bowl first would be backwards. Using the item
 * therefore projects the selected pentacle around the player, and only falls back to "what is drawn
 * here" when the player deliberately aims at a bowl.</p>
 */
@SideOnly(Side.CLIENT)
public final class PentacleDivinerClient {

    private static int index;

    private PentacleDivinerClient() {
    }

    /** @return true when the click was consumed. */
    public static boolean use(World world, EntityPlayer player, ItemStack stack) {
        if (player.isSneaking()) {
            return cycleAndShow(player);
        }
        if (PentaclePreview.isShowing()) {
            // A second activation clears rather than stacking a second projection.
            return PentaclePreview.clear(player);
        }

        // Aiming at a bowl means "show me what I have already drawn here".
        BlockPos bowl = bowlUnderCrosshair(world, player);
        if (bowl != null) {
            return PentaclePreview.start(world, bowl, player);
        }

        PentacleDefinition current = current();
        if (current == null) {
            return false;
        }
        return PentaclePreview.showFor(current, player);
    }

    private static BlockPos bowlUnderCrosshair(World world, EntityPlayer player) {
        RayTraceResult hit = player.rayTrace(8.0D, 1.0F);
        if (hit == null || hit.typeOfHit != RayTraceResult.Type.BLOCK) {
            return null;
        }
        Block block = world.getBlockState(hit.getBlockPos()).getBlock();
        // The ritual bowls extend Block directly and are not a subclass of the plain bowl.
        boolean bowl = block instanceof BlockSacrificialBowl || block instanceof BlockGoldenSacrificialBowl;
        return bowl ? hit.getBlockPos() : null;
    }

    private static boolean cycleAndShow(EntityPlayer player) {
        List<PentacleDefinition> definitions = PentacleDetector.getDefinitions();
        if (definitions.isEmpty()) {
            return false;
        }
        index = Math.floorMod(index + 1, definitions.size());
        PentacleDefinition definition = definitions.get(index);
        boolean shown = PentaclePreview.showFor(definition, player);
        if (shown) {
            player.sendStatusMessage(new TextComponentTranslation("occultism.pentacle_diviner.cycle",
                    definition.getDisplayName(), index + 1, definitions.size()), true);
        }
        return shown;
    }

    /** The pentacle the diviner would project next; exposed so the item can name it in a tooltip. */
    public static PentacleDefinition current() {
        List<PentacleDefinition> definitions = PentacleDetector.getDefinitions();
        return definitions.isEmpty() ? null : definitions.get(Math.floorMod(index, definitions.size()));
    }
}
