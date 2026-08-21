package com.occultism.occultism.client.render;

import com.occultism.occultism.Tags;
import com.occultism.occultism.client.model.ModelCthulhuFamiliar;
import com.occultism.occultism.entity.EntityCthulhuFamiliar;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderCthulhuFamiliar extends RenderLiving<EntityCthulhuFamiliar> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Tags.MOD_ID, "textures/entity/cthulhu_familiar.png");

    public RenderCthulhuFamiliar(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelCthulhuFamiliar(), 0.3F);
        this.addLayer(new FlowerLayer());
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityCthulhuFamiliar entity) {
        return TEXTURE;
    }

    @Override
    protected void preRenderCallback(EntityCthulhuFamiliar entitylivingbaseIn, float partialTickTime) {
        GlStateManager.translate(0.0F, entitylivingbaseIn.isSitting()
                ? -0.35F : entitylivingbaseIn.getAnimationHeight(partialTickTime) * 0.08F, 0.0F);
    }

    private static class FlowerLayer implements LayerRenderer<EntityCthulhuFamiliar> {

        private static final ItemStack FLOWER = new ItemStack(Blocks.RED_FLOWER, 1, 0);

        @Override
        public void doRenderLayer(EntityCthulhuFamiliar entity, float limbSwing, float limbSwingAmount,
                float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
            if (entity.isInvisible() || !entity.isGiving()) {
                return;
            }
            GlStateManager.pushMatrix();
            GlStateManager.scale(1.25F, -1.25F, 1.25F);
            GlStateManager.translate(0.0F, -0.75F, -0.35F);
            GlStateManager.rotate(-65.0F, 1.0F, 0.0F, 0.0F);
            Minecraft.getMinecraft().getRenderItem().renderItem(FLOWER,
                    ItemCameraTransforms.TransformType.GROUND);
            GlStateManager.popMatrix();
        }

        @Override
        public boolean shouldCombineTextures() {
            return false;
        }
    }
}
