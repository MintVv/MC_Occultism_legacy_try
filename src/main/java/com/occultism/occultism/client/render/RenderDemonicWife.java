package com.occultism.occultism.client.render;

import com.occultism.occultism.Tags;
import com.occultism.occultism.client.model.ModelDemonicWife;
import com.occultism.occultism.entity.EntityDemonicWife;

import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderDemonicWife extends RenderLiving<EntityDemonicWife> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Tags.MOD_ID, "textures/entity/demonic_wife.png");

    public RenderDemonicWife(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelDemonicWife(), 0.45F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityDemonicWife entity) {
        return TEXTURE;
    }
}
