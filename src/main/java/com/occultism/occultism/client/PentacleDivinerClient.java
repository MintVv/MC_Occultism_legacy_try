package com.occultism.occultism.client;

import java.util.List;

import com.occultism.occultism.compat.patchouli.PentaclePreview;
import com.occultism.occultism.ritual.PentacleDefinition;
import com.occultism.occultism.ritual.PentacleDetector;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Client half of the pentacle diviner.
 *
 * <p>Kept out of the item class so the item stays common code: the projection touches Patchouli's
 * client handler, and a common class reaching into client types is how this port once crashed a
 * dedicated server.</p>
 */
@SideOnly(Side.CLIENT)
public final class PentacleDivinerClient {

    private static int index;

    private PentacleDivinerClient() {
    }

    /**
     * @return true when the click was consumed.
     */
    public static boolean use(World world, BlockPos pos, EntityPlayer player, ItemStack stack) {
        if (player.isSneaking()) {
            return cycleAndShow(player);
        }
        if (PentaclePreview.isShowing()) {
            // A second activation clears rather than stacking a second projection.
            return PentaclePreview.clear(player);
        }
        return PentaclePreview.start(world, pos, player);
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

    /** Index is client-side only; the tooltip reads it to show what the next sneak-click selects. */
    public static PentacleDefinition current() {
        List<PentacleDefinition> definitions = PentacleDetector.getDefinitions();
        return definitions.isEmpty() ? null : definitions.get(Math.floorMod(index, definitions.size()));
    }
}
