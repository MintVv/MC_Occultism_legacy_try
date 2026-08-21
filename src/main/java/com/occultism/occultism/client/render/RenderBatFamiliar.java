package com.occultism.occultism.client.render;

import com.occultism.occultism.Tags;
import com.occultism.occultism.client.model.ModelBatFamiliar;
import com.occultism.occultism.entity.EntityBatFamiliar;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderBatFamiliar extends RenderLiving<EntityBatFamiliar> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Tags.MOD_ID, "textures/entity/bat_familiar.png");

    public RenderBatFamiliar(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelBatFamiliar(), 0.3F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityBatFamiliar entity) {
        return TEXTURE;
    }

    @Override
    protected void preRenderCallback(EntityBatFamiliar entitylivingbaseIn, float partialTickTime) {
        if (!entitylivingbaseIn.isSitting()) {
            GlStateManager.translate(0.0F, entitylivingbaseIn.getAnimationHeight(partialTickTime) * 0.1F + 0.1F,
                    0.0F);
        }
    }
}
