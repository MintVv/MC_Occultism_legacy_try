package com.occultism.occultism.client.render;

import com.occultism.occultism.Tags;
import com.occultism.occultism.client.model.ModelPossessedWarden;
import com.occultism.occultism.entity.EntityPossessedWarden;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderPossessedWarden extends RenderLiving<EntityPossessedWarden> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Tags.MOD_ID, "textures/entity/possessed_warden.png");

    public RenderPossessedWarden(RenderManager renderManager) {
        super(renderManager, new ModelPossessedWarden(), 0.85F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityPossessedWarden entity) {
        return TEXTURE;
    }

    @Override
    protected void preRenderCallback(EntityPossessedWarden entity, float partialTickTime) {
        // The 1.12 model is authored in pixel units; this matches the 1.8 x 5.8 hitbox.
        GlStateManager.scale(1.22F, 1.22F, 1.22F);
    }
}
