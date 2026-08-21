package com.occultism.occultism.client.render;

import com.occultism.occultism.Tags;
import com.occultism.occultism.entity.EntityOtherworldBird;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderParrot;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Uses the native 1.12 ParrotModel with the upstream Otherworld Bird texture. */
@SideOnly(Side.CLIENT)
public class RenderOtherworldBird extends RenderParrot {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Tags.MOD_ID, "textures/entity/otherworld_bird.png");

    public RenderOtherworldBird(RenderManager renderManagerIn) {
        super(renderManagerIn);
    }

    @Override
    protected ResourceLocation getEntityTexture(net.minecraft.entity.passive.EntityParrot entity) {
        return TEXTURE;
    }
}
