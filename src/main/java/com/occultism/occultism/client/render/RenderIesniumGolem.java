package com.occultism.occultism.client.render;

import com.occultism.occultism.entity.EntityIesniumGolem;

import net.minecraft.client.renderer.entity.RenderIronGolem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Uses the stable 1.12 iron-golem model while keeping a dedicated renderer hook. */
@SideOnly(Side.CLIENT)
public class RenderIesniumGolem extends RenderIronGolem {

    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/entity/iron_golem.png");

    public RenderIesniumGolem(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    protected ResourceLocation getEntityTexture(net.minecraft.entity.monster.EntityIronGolem entity) {
        return TEXTURE;
    }
}
