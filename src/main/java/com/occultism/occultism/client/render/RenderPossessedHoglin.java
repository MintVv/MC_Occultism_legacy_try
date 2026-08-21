package com.occultism.occultism.client.render;

import com.occultism.occultism.client.model.ModelPossessedHoglin;
import com.occultism.occultism.entity.EntityPossessedHoglin;

import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderPossessedHoglin extends RenderLiving<EntityPossessedHoglin> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("minecraft", "textures/entity/pig/pig.png");

    public RenderPossessedHoglin(RenderManager renderManager) {
        super(renderManager, new ModelPossessedHoglin(), 0.6F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityPossessedHoglin entity) {
        return TEXTURE;
    }
}
