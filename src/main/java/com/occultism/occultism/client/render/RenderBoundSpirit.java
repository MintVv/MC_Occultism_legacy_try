package com.occultism.occultism.client.render;

import com.occultism.occultism.Tags;
import com.occultism.occultism.client.model.ModelFoliotJob;
import com.occultism.occultism.entity.EntityBoundSpirit;
import com.occultism.occultism.spirit.SpiritTier;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderBoundSpirit extends RenderBiped<EntityBoundSpirit> {

    private static final ResourceLocation TEXTURE_DJINNI =
            new ResourceLocation(Tags.MOD_ID, "textures/entity/foliot_worker.png");
    private static final ResourceLocation TEXTURE_AFRIT =
            new ResourceLocation(Tags.MOD_ID, "textures/entity/foliot_crusher.png");
    private static final ResourceLocation TEXTURE_MARID =
            new ResourceLocation(Tags.MOD_ID, "textures/entity/foliot_otherrock_trader.png");
    private static final ResourceLocation TEXTURE_ANCIENT =
            new ResourceLocation(Tags.MOD_ID, "textures/entity/foliot_janitor.png");

    public RenderBoundSpirit(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelFoliotJob(), 0.45F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityBoundSpirit entity) {
        if (entity.getTier() == SpiritTier.AFRIT) {
            return TEXTURE_AFRIT;
        }
        if (entity.getTier() == SpiritTier.MARID) {
            return TEXTURE_MARID;
        }
        if (entity.getTier() == SpiritTier.ANCIENT) {
            return TEXTURE_ANCIENT;
        }
        return TEXTURE_DJINNI;
    }

    @Override
    protected void preRenderCallback(EntityBoundSpirit entitylivingbaseIn, float partialTickTime) {
        float scale = entitylivingbaseIn.getTier().getRenderScale();
        GlStateManager.scale(scale, scale, scale);
    }
}
