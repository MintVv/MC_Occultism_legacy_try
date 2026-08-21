package com.occultism.occultism.client.render;

import com.occultism.occultism.Tags;
import com.occultism.occultism.client.model.ModelAfritWild;
import com.occultism.occultism.entity.EntityAfritWild;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** 1.12 renderer counterpart of the upstream AfritWildRenderer. */
@SideOnly(Side.CLIENT)
public class RenderAfritWild extends RenderLiving<EntityAfritWild> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Tags.MOD_ID, "textures/entity/afrit.png");

    public RenderAfritWild(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelAfritWild(), 0.55F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityAfritWild entity) {
        return TEXTURE;
    }

    @Override
    protected void preRenderCallback(EntityAfritWild entity, float partialTickTime) {
        GlStateManager.scale(1.0F, 1.0F, 1.0F);
    }
}
