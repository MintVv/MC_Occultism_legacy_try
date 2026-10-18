package com.occultism.occultism.client;

import com.occultism.occultism.Tags;
import com.occultism.occultism.block.BlockGoldenSacrificialBowl;
import com.occultism.occultism.block.BlockSacrificialBowl;
import com.occultism.occultism.compat.patchouli.PentaclePreview;
import com.occultism.occultism.item.ItemDictionaryOfSpirits;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Client hook for the pentacle projection.
 *
 * <p>Kept in its own client-only subscriber rather than folded into the shared player handler: the
 * projection touches client classes, and a common class reaching into those is exactly how this port
 * once crashed a dedicated server on a client-only type.</p>
 */
@Mod.EventBusSubscriber(modid = Tags.MOD_ID, value = Side.CLIENT)
public final class PentaclePreviewHandler {

    private PentaclePreviewHandler() {
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        World world = event.getWorld();
        if (!world.isRemote) {
            return;
        }
        EntityPlayer player = event.getEntityPlayer();
        if (player == null || !player.isSneaking()) {
            return;
        }
        ItemStack held = event.getItemStack();
        if (held.isEmpty() || !(held.getItem() instanceof ItemDictionaryOfSpirits)) {
            return;
        }

        BlockPos pos = event.getPos();
        // The ritual bowls are BlockGoldenSacrificialBowl, which does not extend the plain bowl.
        net.minecraft.block.Block block = world.getBlockState(pos).getBlock();
        if (!(block instanceof BlockSacrificialBowl) && !(block instanceof BlockGoldenSacrificialBowl)) {
            return;
        }

        if (PentaclePreview.start(world, pos, player)) {
            event.setCanceled(true);
            event.setCancellationResult(EnumActionResult.SUCCESS);
        }
    }
}
