package com.occultism.occultism.client.render;

import com.occultism.occultism.Tags;
import com.occultism.occultism.client.model.ModelMummyFamiliar;
import com.occultism.occultism.entity.EntityMummyFamiliar;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderMummyFamiliar extends RenderLiving<EntityMummyFamiliar> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Tags.MOD_ID, "textures/entity/mummy_familiar.png");

    public RenderMummyFamiliar(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelMummyFamiliar(), 0.3F);
        this.addLayer(new EyesLayer(this));
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityMummyFamiliar entity) {
        return TEXTURE;
    }

    private static class EyesLayer implements LayerRenderer<EntityMummyFamiliar> {

        private static final ResourceLocation EYES =
                new ResourceLocation(Tags.MOD_ID, "textures/entity/mummy_familiar_eyes.png");
        private final RenderMummyFamiliar renderer;
        private final ModelMummyFamiliar model = new ModelMummyFamiliar();

        private EyesLayer(RenderMummyFamiliar renderer) {
            this.renderer = renderer;
        }

        @Override
        public void doRenderLayer(EntityMummyFamiliar entity, float limbSwing, float limbSwingAmount,
                float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
            if (entity.isInvisible()) {
                return;
            }
            this.renderer.bindTexture(EYES);
            this.model.setModelAttributes(this.renderer.getMainModel());
            this.model.setLivingAnimations(entity, limbSwing, limbSwingAmount, partialTicks);
            GlStateManager.enableBlend();
            GlStateManager.disableLighting();
            this.model.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            GlStateManager.enableLighting();
            GlStateManager.disableBlend();
        }

        @Override
        public boolean shouldCombineTextures() {
            return true;
        }
    }
}
