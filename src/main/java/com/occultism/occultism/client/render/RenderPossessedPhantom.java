package com.occultism.occultism.client.render;

import com.occultism.occultism.Tags;
import com.occultism.occultism.client.model.ModelPossessedPhantom;
import com.occultism.occultism.entity.EntityPossessedPhantom;

import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderPossessedPhantom extends RenderLiving<EntityPossessedPhantom> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Tags.MOD_ID, "textures/entity/possessed_phantom.png");

    public RenderPossessedPhantom(RenderManager renderManager) {
        super(renderManager, new ModelPossessedPhantom(), 0.3F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityPossessedPhantom entity) {
        return TEXTURE;
    }
}
