package com.occultism.occultism.client.render;

import com.occultism.occultism.Tags;
import com.occultism.occultism.entity.EntityPossessedBee;

import net.minecraft.client.renderer.entity.RenderBat;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderPossessedBee extends RenderBat {

    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/entity/bat.png");

    public RenderPossessedBee(RenderManager renderManagerIn) {
        super(renderManagerIn);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityBat entity) {
        return TEXTURE;
    }
}
