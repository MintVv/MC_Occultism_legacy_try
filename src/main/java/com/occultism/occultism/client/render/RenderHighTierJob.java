package com.occultism.occultism.client.render;

import com.occultism.occultism.Tags;
import com.occultism.occultism.client.model.ModelHighTierSpirit;
import com.occultism.occultism.entity.EntityFoliotJob;
import com.occultism.occultism.spirit.SpiritTier;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderHighTierJob<T extends EntityFoliotJob> extends RenderBiped<T> {

    private final SpiritTier tier;
    private final ResourceLocation texture;

    public RenderHighTierJob(RenderManager renderManagerIn, SpiritTier tier) {
        super(renderManagerIn, new ModelHighTierSpirit(tier), 0.35F);
        this.tier = tier;
        this.texture = new ResourceLocation(Tags.MOD_ID,
                "textures/entity/" + tier.getId().getPath() + "_worker.png");
    }

    @Override
    protected ResourceLocation getEntityTexture(T entity) {
        return this.texture;
    }

    @Override
    protected void preRenderCallback(T entity, float partialTickTime) {
        float scale = 0.75F * (entity.getTier() == this.tier ? this.tier.getRenderScale() : 1.0F);
        GlStateManager.scale(scale, scale, scale);
    }
}
