package com.occultism.occultism.client.render;

import com.occultism.occultism.Tags;
import com.occultism.occultism.item.ItemOtherworldGoggles;
import com.occultism.occultism.registry.ModPotions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID, value = Side.CLIENT)
public final class ThirdEyeOverlayRenderer extends Gui {

    private static final ResourceLocation THIRD_EYE_OVERLAY =
            new ResourceLocation(Tags.MOD_ID, "textures/overlay/third_eye.png");

    private ThirdEyeOverlayRenderer() {
    }

    @SubscribeEvent
    public static void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.HELMET) {
            return;
        }
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.player == null || minecraft.gameSettings.hideGUI) {
            return;
        }
        boolean hasThirdEye = minecraft.player.isPotionActive(ModPotions.THIRD_EYE);
        boolean hasGoggles = ItemOtherworldGoggles.isGoggles(
                minecraft.player.getItemStackFromSlot(EntityEquipmentSlot.HEAD));
        if (!hasThirdEye && !hasGoggles) {
            return;
        }
        renderOverlay(minecraft, event.getResolution().getScaledWidth(), event.getResolution().getScaledHeight());
    }

    private static void renderOverlay(Minecraft minecraft, int width, int height) {
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableAlpha();
        minecraft.getTextureManager().bindTexture(THIRD_EYE_OVERLAY);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(0.0D, height, -90.0D).tex(0.0D, 1.0D).endVertex();
        buffer.pos(width, height, -90.0D).tex(1.0D, 1.0D).endVertex();
        buffer.pos(width, 0.0D, -90.0D).tex(1.0D, 0.0D).endVertex();
        buffer.pos(0.0D, 0.0D, -90.0D).tex(0.0D, 0.0D).endVertex();
        tessellator.draw();

        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableAlpha();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
