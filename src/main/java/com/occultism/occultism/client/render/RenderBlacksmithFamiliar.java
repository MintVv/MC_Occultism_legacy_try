package com.occultism.occultism.client.render;

import com.occultism.occultism.Tags;
import com.occultism.occultism.client.model.ModelBlacksmithFamiliar;
import com.occultism.occultism.entity.EntityBlacksmithFamiliar;

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
public class RenderBlacksmithFamiliar extends RenderLiving<EntityBlacksmithFamiliar> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Tags.MOD_ID, "textures/entity/blacksmith_familiar.png");

    public RenderBlacksmithFamiliar(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelBlacksmithFamiliar(), 0.3F);
        this.addLayer(new IngotsLayer());
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityBlacksmithFamiliar entity) {
        return TEXTURE;
    }

    @Override
    protected void applyRotations(EntityBlacksmithFamiliar entity, float ageInTicks, float rotationYaw,
            float partialTicks) {
        if (!entity.isSitting()) {
            super.applyRotations(entity, ageInTicks, rotationYaw, partialTicks);
        }
    }

    private static class IngotsLayer implements LayerRenderer<EntityBlacksmithFamiliar> {

        private static final ItemStack IRON_INGOT = new ItemStack(Items.IRON_INGOT);

        @Override
        public void doRenderLayer(EntityBlacksmithFamiliar entity, float limbSwing, float limbSwingAmount,
                float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
            GlStateManager.pushMatrix();
            GlStateManager.scale(0.5F, 0.5F, 0.5F);
            for (int i = 0; i < entity.getBars(); i++) {
                GlStateManager.pushMatrix();
                GlStateManager.translate(i % 2 == 0 ? -0.3F : 0.3F,
                        2.03F - i / 2 * 0.03F, -0.15F);
                GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
                GlStateManager.rotate((float) i, 0.0F, 0.0F, 1.0F);
                Minecraft.getMinecraft().getRenderItem().renderItem(IRON_INGOT,
                        ItemCameraTransforms.TransformType.GROUND);
                GlStateManager.popMatrix();
            }
            GlStateManager.popMatrix();
        }

        @Override
        public boolean shouldCombineTextures() {
            return false;
        }
    }
}
