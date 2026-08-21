package com.occultism.occultism.client.render;

import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderVex;
import net.minecraft.entity.monster.EntityVex;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderPurifiedAllay extends RenderVex {

    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/entity/illager/vex.png");

    public RenderPurifiedAllay(RenderManager renderManagerIn) {
        super(renderManagerIn);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityVex entity) {
        return TEXTURE;
    }
}
