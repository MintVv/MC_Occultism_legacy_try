package com.occultism.occultism.client.render;

import com.occultism.occultism.registry.ModBlocks;
import com.occultism.occultism.block.BlockGoldenSacrificialBowl;
import com.occultism.occultism.tileentity.TileEntitySacrificialBowl;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class TileEntitySacrificialBowlRenderer extends TileEntitySpecialRenderer<TileEntitySacrificialBowl> {

    @Override
    public void render(TileEntitySacrificialBowl te, double x, double y, double z, float partialTicks,
            int destroyStage, float alpha) {
        ItemStack stack = te.getStack();
        if (stack.isEmpty()) {
            this.spawnRitualParticles(te);
            return;
        }

        this.spawnRitualParticles(te);
        GlStateManager.pushMatrix();
        long gameTime = te.getWorld() == null ? 0L : te.getWorld().getTotalWorldTime();
        double bob = (Math.sin((gameTime - te.getLastChangeTime() + partialTicks) / 16.0D) * 0.5D + 0.5D)
                / 4.0D;
        float angle = (System.currentTimeMillis() / 16L) % 360L;

        GlStateManager.translate(x + 0.5D, y + 0.5D + bob, z + 0.5D);
        GlStateManager.rotate(angle, 0.0F, 1.0F, 0.0F);
        float scale = stack.getItem() == Item.getItemFromBlock(ModBlocks.SPIRIT_ATTUNED_CRYSTAL) ? 1.5F : 0.5F;
        GlStateManager.scale(scale, scale, scale);
        RenderHelper.enableStandardItemLighting();
        Minecraft.getMinecraft().getRenderItem().renderItem(stack, net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType.FIXED);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.popMatrix();
    }

    private void spawnRitualParticles(TileEntitySacrificialBowl te) {
        if (te.getWorld() == null || !te.getWorld().isRemote
                || !(te.getBlockType() instanceof BlockGoldenSacrificialBowl)
                || !te.isRitualActive()) {
            return;
        }
        long gameTime = te.getWorld().getTotalWorldTime();
        if (gameTime == te.getLastClientParticleTick()) {
            return;
        }
        te.setLastClientParticleTick(gameTime);
        if (te.getWorld().rand.nextInt(16) != 0) {
            return;
        }

        double x = te.getPos().getX() + 0.5D;
        double y = te.getPos().getY() + 0.5D;
        double z = te.getPos().getZ() + 0.5D;
        if (te.isWaitingForSacrifice() || te.isWaitingForItemUse()) {
            for (int i = 0; i < 6; i++) {
                ParticleRitualWaiting.spawn(te.getWorld(), x + te.getWorld().rand.nextGaussian(),
                        y, z + te.getWorld().rand.nextGaussian());
            }
        }
    }
}
