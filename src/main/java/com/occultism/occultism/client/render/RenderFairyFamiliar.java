/*
 * MIT License
 *
 * Copyright 2021 vemerion
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT
 * OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package com.occultism.occultism.client.render;

import com.occultism.occultism.Tags;
import com.occultism.occultism.client.model.ModelFairyFamiliar;
import com.occultism.occultism.entity.EntityFairyFamiliar;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderFairyFamiliar extends RenderLiving<EntityFairyFamiliar> {

    private static final float RADIANS_TO_DEGREES = 180.0F / (float) Math.PI;
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Tags.MOD_ID, "textures/entity/fairy_familiar.png");
    private boolean applyMagicRotation;
    private float magicRotationDegrees;

    public RenderFairyFamiliar(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelFairyFamiliar(), 0.3F);
        this.addLayer(new SleepLayer(this));
    }

    @Override
    public void doRender(EntityFairyFamiliar entity, double x, double y, double z, float entityYaw,
            float partialTicks) {
        Entity target = entity.getMagicTarget();
        if (target != null) {
            Vec3d magicPosition = entity.getMagicPosition(partialTicks);
            if (magicPosition != null) {
                Vec3d position = interpolate(entity, partialTicks);
                Vec3d offset = magicPosition.subtract(position);
                x += offset.x;
                y += offset.y;
                z += offset.z;
            }
        }
        y += entity.getAnimationHeight(partialTicks);

        this.applyMagicRotation = target != null;
        this.magicRotationDegrees = this.applyMagicRotation
                ? -entity.getMagicRadiusAngle(partialTicks).y * RADIANS_TO_DEGREES
                : 0.0F;
        this.shadowOpaque = this.applyMagicRotation ? 0.0F : 1.0F;
        try {
            super.doRender(entity, x, y, z, entityYaw, partialTicks);
        } finally {
            this.applyMagicRotation = false;
        }
    }

    @Override
    protected void renderLivingAt(EntityFairyFamiliar entity, double x, double y, double z) {
        super.renderLivingAt(entity, x, y, z);
        if (this.applyMagicRotation) {
            GlStateManager.rotate(this.magicRotationDegrees, 0.0F, 1.0F, 0.0F);
        }
    }

    @Override
    protected void renderModel(EntityFairyFamiliar entity, float limbSwing, float limbSwingAmount,
            float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor) {
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO);
        try {
            super.renderModel(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
        } finally {
            GlStateManager.disableBlend();
        }
    }

    private static Vec3d interpolate(Entity entity, float partialTicks) {
        return new Vec3d(entity.prevPosX + (entity.posX - entity.prevPosX) * partialTicks,
                entity.prevPosY + (entity.posY - entity.prevPosY) * partialTicks,
                entity.prevPosZ + (entity.posZ - entity.prevPosZ) * partialTicks);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityFairyFamiliar entity) {
        return TEXTURE;
    }

    private static class SleepLayer implements LayerRenderer<EntityFairyFamiliar> {

        private static final ResourceLocation SLEEP =
                new ResourceLocation(Tags.MOD_ID, "textures/entity/fairy_familiar_sleep.png");
        private final RenderFairyFamiliar renderer;

        private SleepLayer(RenderFairyFamiliar renderer) {
            this.renderer = renderer;
        }

        @Override
        public void doRenderLayer(EntityFairyFamiliar entity, float limbSwing, float limbSwingAmount,
                float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
            if (entity.isInvisible() || !entity.isSitting() || entity.isPartying()) {
                return;
            }

            this.renderer.bindTexture(SLEEP);
            ModelFairyFamiliar model = (ModelFairyFamiliar) this.renderer.getMainModel();
            model.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        }

        @Override
        public boolean shouldCombineTextures() {
            return true;
        }
    }
}
