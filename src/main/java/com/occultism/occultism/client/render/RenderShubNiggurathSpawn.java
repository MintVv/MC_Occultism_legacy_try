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
import com.occultism.occultism.client.model.ModelShubNiggurathSpawn;
import com.occultism.occultism.entity.EntityShubNiggurathSpawn;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderShubNiggurathSpawn extends RenderLiving<EntityShubNiggurathSpawn> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Tags.MOD_ID, "textures/entity/shub_niggurath_spawn.png");

    public RenderShubNiggurathSpawn(RenderManager renderManager) {
        super(renderManager, new ModelShubNiggurathSpawn(), 0.1F);
        this.addLayer(new BlinkingEyesLayer(this));
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityShubNiggurathSpawn entity) {
        return TEXTURE;
    }

    private static final class BlinkingEyesLayer implements LayerRenderer<EntityShubNiggurathSpawn> {

        private static final ResourceLocation BLINKING =
                new ResourceLocation(Tags.MOD_ID, "textures/entity/shub_niggurath_spawn_blinking.png");

        private final RenderShubNiggurathSpawn renderer;
        private final ModelShubNiggurathSpawn model = new ModelShubNiggurathSpawn();

        private BlinkingEyesLayer(RenderShubNiggurathSpawn renderer) {
            this.renderer = renderer;
        }

        @Override
        public void doRenderLayer(EntityShubNiggurathSpawn entity, float limbSwing, float limbSwingAmount,
                float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
            if (entity.isInvisible()) {
                return;
            }
            this.renderer.bindTexture(BLINKING);
            this.model.setModelAttributes(this.renderer.getMainModel());
            this.model.eye1.showModel = entity.isBlinking(0);
            this.model.eye2.showModel = entity.isBlinking(1);
            this.model.eye3.showModel = entity.isBlinking(2);
            this.model.eye4.showModel = entity.isBlinking(3);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
                    GlStateManager.DestFactor.ZERO);
            this.model.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            GlStateManager.disableBlend();
        }

        @Override
        public boolean shouldCombineTextures() {
            return true;
        }
    }
}
