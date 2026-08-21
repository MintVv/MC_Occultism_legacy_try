package com.occultism.occultism.client.render;

import com.occultism.occultism.tileentity.TileEntitySacrificialBowl;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;

public class TileEntitySacrificialBowlRenderer extends TileEntitySpecialRenderer<TileEntitySacrificialBowl> {

    @Override
    public void render(TileEntitySacrificialBowl te, double x, double y, double z, float partialTicks,
            int destroyStage, float alpha) {
        ItemStack stack = te.getStack();
        if (stack.isEmpty()) {
            return;
        }

        GlStateManager.pushMatrix();
        long gameTime = te.getWorld() == null ? 0L : te.getWorld().getTotalWorldTime();
        double bob = Math.sin((gameTime + partialTicks) / 16.0D) * 0.06D + 0.14D;
        float angle = ((gameTime + partialTicks) * 4.0F) % 360.0F;

        GlStateManager.translate(x + 0.5D, y + 0.72D + bob, z + 0.5D);
        GlStateManager.rotate(angle, 0.0F, 1.0F, 0.0F);
        GlStateManager.scale(0.5F, 0.5F, 0.5F);
        RenderHelper.enableStandardItemLighting();
        Minecraft.getMinecraft().getRenderItem().renderItem(stack, net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType.FIXED);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.popMatrix();
    }
}
