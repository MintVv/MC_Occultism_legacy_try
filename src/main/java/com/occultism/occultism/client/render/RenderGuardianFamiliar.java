package com.occultism.occultism.client.render;

import com.occultism.occultism.Tags;
import com.occultism.occultism.client.model.ModelGuardianFamiliar;
import com.occultism.occultism.entity.EntityGuardianFamiliar;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderGuardianFamiliar extends RenderLiving<EntityGuardianFamiliar> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Tags.MOD_ID, "textures/entity/guardian_familiar.png");

    public RenderGuardianFamiliar(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelGuardianFamiliar(), 0.3F);
        this.addLayer(new OverlayLayer(this));
        this.addLayer(new TreeLayer(this));
        this.addLayer(new ToolsLayer());
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityGuardianFamiliar entity) {
        return TEXTURE;
    }

    @Override
    protected void preRenderCallback(EntityGuardianFamiliar entitylivingbaseIn, float partialTickTime) {
        if (entitylivingbaseIn.isSitting()) {
            GlStateManager.translate(0.0F, entitylivingbaseIn.hasLegs() ? -0.36F : -0.5F, 0.0F);
        } else if (!entitylivingbaseIn.hasLegs()) {
            GlStateManager.translate(0.0F, entitylivingbaseIn.getAnimationHeight(partialTickTime) * 0.08F, 0.0F);
        }
    }

    private static class OverlayLayer implements LayerRenderer<EntityGuardianFamiliar> {

        private static final ResourceLocation OVERLAY =
                new ResourceLocation(Tags.MOD_ID, "textures/entity/guardian_familiar_overlay.png");
        private final RenderGuardianFamiliar renderer;
        private final ModelGuardianFamiliar model = new ModelGuardianFamiliar();

        private OverlayLayer(RenderGuardianFamiliar renderer) {
            this.renderer = renderer;
        }

        @Override
        public void doRenderLayer(EntityGuardianFamiliar entity, float limbSwing, float limbSwingAmount,
                float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
            if (entity.isInvisible()) {
                return;
            }
            this.renderer.bindTexture(OVERLAY);
            this.model.setModelAttributes(this.renderer.getMainModel());
            this.model.setLivingAnimations(entity, limbSwing, limbSwingAmount, partialTicks);
            GlStateManager.enableBlend();
            GlStateManager.disableLighting();
            float alpha = (float) ((Math.cos(ageInTicks / 20.0F) + 1.0F) * 0.3F + 0.4F);
            GlStateManager.color(entity.getRed(), entity.getGreen(), entity.getBlue(), alpha);
            this.model.setTreeLayerOnly(false);
            this.model.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableLighting();
            GlStateManager.disableBlend();
        }

        @Override
        public boolean shouldCombineTextures() {
            return true;
        }
    }

    private static class TreeLayer implements LayerRenderer<EntityGuardianFamiliar> {

        private static final ResourceLocation TREE =
                new ResourceLocation(Tags.MOD_ID, "textures/entity/guardian_familiar_tree.png");
        private final RenderGuardianFamiliar renderer;
        private final ModelGuardianFamiliar model = new ModelGuardianFamiliar();

        private TreeLayer(RenderGuardianFamiliar renderer) {
            this.renderer = renderer;
        }

        @Override
        public void doRenderLayer(EntityGuardianFamiliar entity, float limbSwing, float limbSwingAmount,
                float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
            if (entity.isInvisible() || !entity.hasTree()) {
                return;
            }
            this.renderer.bindTexture(TREE);
            this.model.setModelAttributes(this.renderer.getMainModel());
            this.model.setLivingAnimations(entity, limbSwing, limbSwingAmount, partialTicks);
            this.model.setTreeLayerOnly(true);
            this.model.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            this.model.setTreeLayerOnly(false);
        }

        @Override
        public boolean shouldCombineTextures() {
            return true;
        }
    }

    private static class ToolsLayer implements LayerRenderer<EntityGuardianFamiliar> {

        @Override
        public void doRenderLayer(EntityGuardianFamiliar entity, float limbSwing, float limbSwingAmount,
                float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
            if (entity.isInvisible() || !entity.hasTools()) {
                return;
            }
            renderTool(new ItemStack(Items.STONE_SWORD), -0.18F, 0.55F, -0.24F, -60.0F, 0.0F, 20.0F);
            renderTool(new ItemStack(Items.STONE_AXE), 0.18F, 0.85F, 0.28F, 60.0F, 0.0F, -110.0F);
            if (entity.getLives() > EntityGuardianFamiliar.ONE_ARMED) {
                renderTool(new ItemStack(Items.STONE_PICKAXE), 0.45F, 0.75F, 0.0F, 0.0F, 0.0F, 210.0F);
            }
        }

        @Override
        public boolean shouldCombineTextures() {
            return false;
        }

        private static void renderTool(ItemStack stack, float x, float y, float z, float yaw, float pitch,
                float roll) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            GlStateManager.rotate(yaw, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(pitch, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(roll, 0.0F, 0.0F, 1.0F);
            GlStateManager.scale(0.45F, 0.45F, 0.45F);
            Minecraft.getMinecraft().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.GROUND);
            GlStateManager.popMatrix();
        }
    }
}
