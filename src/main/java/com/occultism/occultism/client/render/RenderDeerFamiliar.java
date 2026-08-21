package com.occultism.occultism.client.render;

import com.occultism.occultism.Tags;
import com.occultism.occultism.client.model.ModelDeerFamiliar;
import com.occultism.occultism.entity.EntityDeerFamiliar;

import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderDeerFamiliar extends RenderLiving<EntityDeerFamiliar> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Tags.MOD_ID, "textures/entity/deer_familiar.png");

    public RenderDeerFamiliar(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelDeerFamiliar(), 0.3F);
        this.addLayer(new RedNoseLayer(this));
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityDeerFamiliar entity) {
        return TEXTURE;
    }

    private static class RedNoseLayer implements LayerRenderer<EntityDeerFamiliar> {

        private static final ResourceLocation RED_NOSE =
                new ResourceLocation(Tags.MOD_ID, "textures/entity/deer_familiar_red_nose.png");
        private final RenderDeerFamiliar renderer;
        private final ModelDeerFamiliar model = new ModelDeerFamiliar();

        private RedNoseLayer(RenderDeerFamiliar renderer) {
            this.renderer = renderer;
        }

        @Override
        public void doRenderLayer(EntityDeerFamiliar entity, float limbSwing, float limbSwingAmount,
                float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
            if (!entity.hasRedNose() || entity.isInvisible()) {
                return;
            }
            this.renderer.bindTexture(RED_NOSE);
            this.model.setModelAttributes(this.renderer.getMainModel());
            this.model.setLivingAnimations(entity, limbSwing, limbSwingAmount, partialTicks);
            this.model.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        }

        @Override
        public boolean shouldCombineTextures() {
            return true;
        }
    }
}
