package com.occultism.occultism.client.render;

import com.occultism.occultism.Tags;
import com.occultism.occultism.client.model.ModelBeaverFamiliar;
import com.occultism.occultism.entity.EntityBeaverFamiliar;

import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderBeaverFamiliar extends RenderLiving<EntityBeaverFamiliar> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Tags.MOD_ID, "textures/entity/beaver_familiar.png");

    public RenderBeaverFamiliar(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelBeaverFamiliar(), 0.3F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityBeaverFamiliar entity) {
        return TEXTURE;
    }
}
