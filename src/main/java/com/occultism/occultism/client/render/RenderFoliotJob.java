package com.occultism.occultism.client.render;

import com.occultism.occultism.Tags;
import com.occultism.occultism.client.model.ModelFoliotJob;
import com.occultism.occultism.entity.EntityFoliotJob;
import com.occultism.occultism.spirit.SpiritJobType;
import com.occultism.occultism.spirit.SpiritTier;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderFoliotJob extends RenderBiped<EntityFoliotJob> {

    private static final ResourceLocation TEXTURE_WORKER =
            new ResourceLocation(Tags.MOD_ID, "textures/entity/foliot_worker.png");
    private static final ResourceLocation TEXTURE_CRUSHER =
            new ResourceLocation(Tags.MOD_ID, "textures/entity/foliot_crusher.png");
    private static final ResourceLocation TEXTURE_JANITOR =
            new ResourceLocation(Tags.MOD_ID, "textures/entity/foliot_janitor.png");
    private static final ResourceLocation TEXTURE_FARMER =
            new ResourceLocation(Tags.MOD_ID, "textures/entity/foliot_farmer.png");
    private static final ResourceLocation TEXTURE_LUMBERJACK =
            new ResourceLocation(Tags.MOD_ID, "textures/entity/foliot_lumberjack.png");
    private static final ResourceLocation TEXTURE_TRANSPORTER =
            new ResourceLocation(Tags.MOD_ID, "textures/entity/foliot_transporter.png");
    private static final ResourceLocation TEXTURE_OTHERSTONE_TRADER =
            new ResourceLocation(Tags.MOD_ID, "textures/entity/foliot_otherstone_trader.png");
    private static final ResourceLocation TEXTURE_OTHERROCK_TRADER =
            new ResourceLocation(Tags.MOD_ID, "textures/entity/foliot_otherrock_trader.png");
    private static final ResourceLocation TEXTURE_SAPLING_TRADER =
            new ResourceLocation(Tags.MOD_ID, "textures/entity/foliot_sapling_trader.png");

    public RenderFoliotJob(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelFoliotJob(), 0.35F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityFoliotJob entity) {
        SpiritJobType jobType = entity.getJobType();
        if (jobType == SpiritJobType.CRUSHER || jobType == SpiritJobType.CRYSTALLIZER) {
            return TEXTURE_CRUSHER;
        }
        if (jobType == SpiritJobType.CLEANER) {
            return TEXTURE_JANITOR;
        }
        if (jobType == SpiritJobType.FARMER) {
            return TEXTURE_FARMER;
        }
        if (jobType == SpiritJobType.LUMBERJACK) {
            return TEXTURE_LUMBERJACK;
        }
        if (jobType == SpiritJobType.TRANSPORT_ITEMS) {
            return TEXTURE_TRANSPORTER;
        }
        if (jobType == SpiritJobType.TRADER_OTHERSTONE) {
            return TEXTURE_OTHERSTONE_TRADER;
        }
        if (jobType == SpiritJobType.TRADER_OTHERROCK) {
            return TEXTURE_OTHERROCK_TRADER;
        }
        if (jobType == SpiritJobType.TRADER_SAPLING) {
            return TEXTURE_SAPLING_TRADER;
        }
        return TEXTURE_WORKER;
    }

    @Override
    protected void preRenderCallback(EntityFoliotJob entitylivingbaseIn, float partialTickTime) {
        float scale = 0.75F;
        if (entitylivingbaseIn.getTier() != SpiritTier.FOLIOT) {
            scale *= entitylivingbaseIn.getTier().getRenderScale();
        }
        GlStateManager.scale(scale, scale, scale);
    }
}
