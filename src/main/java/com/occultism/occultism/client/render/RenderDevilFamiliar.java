package com.occultism.occultism.client.render;

import com.occultism.occultism.Tags;
import com.occultism.occultism.client.model.ModelDevilFamiliar;
import com.occultism.occultism.entity.EntityDevilFamiliar;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderDevilFamiliar extends RenderLiving<EntityDevilFamiliar> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Tags.MOD_ID, "textures/entity/familiar_devil.png");

    public RenderDevilFamiliar(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelDevilFamiliar(), 0.3F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityDevilFamiliar entity) {
        return TEXTURE;
    }

    @Override
    protected void preRenderCallback(EntityDevilFamiliar entitylivingbaseIn, float partialTickTime) {
        if (!entitylivingbaseIn.isSitting()) {
            GlStateManager.translate(0.0F, entitylivingbaseIn.getAnimationHeight(partialTickTime) * 0.1F, 0.0F);
        }
    }
}
