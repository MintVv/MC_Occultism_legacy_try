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
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.occultism.occultism.client.render;

import com.occultism.occultism.Tags;
import com.occultism.occultism.client.model.ModelBeholderFamiliar;
import com.occultism.occultism.entity.EntityBeholderFamiliar;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderBeholderFamiliar extends RenderLiving<EntityBeholderFamiliar> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Tags.MOD_ID, "textures/entity/beholder_familiar.png");

    public RenderBeholderFamiliar(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelBeholderFamiliar(), 0.3F);
        this.addLayer(new SleepLayer(this));
    }

    @Override
    public void doRender(EntityBeholderFamiliar entity, double x, double y, double z,
            float entityYaw, float partialTicks) {
        super.doRender(entity, x, y + entity.getAnimationHeight(partialTicks), z, entityYaw, partialTicks);
    }

    @Override
    protected void preRenderCallback(EntityBeholderFamiliar entity, float partialTickTime) {
        if (!entity.isEating()) {
            return;
        }
        float eatTimer = entity.getEatTimer(partialTickTime);
        float scale = eatTimer < 5.0F / 6.0F
                ? 1.0F
                : MathHelper.sin((eatTimer - 5.0F / 6.0F) * 6.0F * (float) Math.PI) + 1.0F;
        GlStateManager.scale(scale, scale, scale);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityBeholderFamiliar entity) {
        return TEXTURE;
    }

    private static final class SleepLayer implements LayerRenderer<EntityBeholderFamiliar> {

        private static final ResourceLocation SLEEP =
                new ResourceLocation(Tags.MOD_ID, "textures/entity/beholder_familiar_sleep.png");
        private final RenderBeholderFamiliar renderer;

        private SleepLayer(RenderBeholderFamiliar renderer) {
            this.renderer = renderer;
        }

        @Override
        public void doRenderLayer(EntityBeholderFamiliar entity, float limbSwing, float limbSwingAmount,
                float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
            if (entity.isInvisible() || !entity.isSitting()) {
                return;
            }
            this.renderer.bindTexture(SLEEP);
            ModelBeholderFamiliar model = (ModelBeholderFamiliar) this.renderer.getMainModel();
            model.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        }

        @Override
        public boolean shouldCombineTextures() {
            return true;
        }
    }
}
