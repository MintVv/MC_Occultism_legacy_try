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
import com.occultism.occultism.client.model.ModelGoatFamiliar;
import com.occultism.occultism.entity.EntityGoatFamiliar;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderGoatFamiliar extends RenderLiving<EntityGoatFamiliar> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Tags.MOD_ID, "textures/entity/goat_familiar.png");

    public RenderGoatFamiliar(RenderManager renderManager) {
        super(renderManager, new ModelGoatFamiliar(), 0.3F);
        this.addLayer(new BlackLayer(this));
    }

    @Override
    protected void preRenderCallback(EntityGoatFamiliar entity, float partialTickTime) {
        float scale = entity.getScale();
        GlStateManager.scale(scale, scale, scale);
        if (entity.isPartying()) {
            GlStateManager.translate(0.0F, -0.25F, 0.0F);
        } else if (entity.isSitting()) {
            GlStateManager.translate(0.0F, -0.3F, 0.0F);
        }
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityGoatFamiliar entity) {
        return TEXTURE;
    }

    private static final class BlackLayer implements LayerRenderer<EntityGoatFamiliar> {

        private final RenderGoatFamiliar renderer;
        private final ModelGoatFamiliar model = new ModelGoatFamiliar();

        private BlackLayer(RenderGoatFamiliar renderer) {
            this.renderer = renderer;
        }

        @Override
        public void doRenderLayer(EntityGoatFamiliar entity, float limbSwing, float limbSwingAmount,
                float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
            if (entity.isInvisible() || !entity.isBlack()) {
                return;
            }
            this.renderer.bindTexture(TEXTURE);
            this.model.setModelAttributes(this.renderer.getMainModel());
            this.model.setLivingAnimations(entity, limbSwing, limbSwingAmount, partialTicks);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
                    GlStateManager.DestFactor.ZERO);
            GlStateManager.color(0.0F, 0.0F, 0.0F, 0.5F);
            this.model.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.disableBlend();
        }

        @Override
        public boolean shouldCombineTextures() {
            return true;
        }
    }
}
