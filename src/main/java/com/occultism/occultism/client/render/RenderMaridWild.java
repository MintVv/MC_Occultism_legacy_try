package com.occultism.occultism.client.render;

import com.occultism.occultism.Tags;
import com.occultism.occultism.client.model.ModelAfritWild;
import com.occultism.occultism.entity.EntityMaridWild;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** 1.12 renderer for the unbound Marid, reusing the local wild-spirit model. */
@SideOnly(Side.CLIENT)
public class RenderMaridWild extends RenderLiving<EntityMaridWild> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Tags.MOD_ID, "textures/entity/afrit.png");

    public RenderMaridWild(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelAfritWild(), 0.65F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityMaridWild entity) {
        return TEXTURE;
    }

    @Override
    protected void preRenderCallback(EntityMaridWild entity, float partialTickTime) {
        GlStateManager.scale(1.08F, 1.08F, 1.08F);
    }
}
