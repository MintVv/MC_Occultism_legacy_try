package com.occultism.occultism.client.render;

import com.occultism.occultism.Tags;
import com.occultism.occultism.client.model.ModelDemonicHusband;
import com.occultism.occultism.entity.EntityDemonicHusband;

import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderDemonicHusband extends RenderLiving<EntityDemonicHusband> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Tags.MOD_ID, "textures/entity/demonic_husband.png");

    public RenderDemonicHusband(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelDemonicHusband(), 0.45F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityDemonicHusband entity) {
        return TEXTURE;
    }
}
