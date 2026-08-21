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
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions
 * of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package com.occultism.occultism.client.render;

import com.occultism.occultism.Tags;
import com.occultism.occultism.client.model.ModelDragonFamiliar;
import com.occultism.occultism.entity.EntityDragonFamiliar;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderDragonFamiliar extends RenderLiving<EntityDragonFamiliar> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Tags.MOD_ID, "textures/entity/dragon_familiar.png");
    private static final ItemStack STICK = new ItemStack(Items.STICK);
    private static final ItemStack IRON_SWORD = new ItemStack(Items.IRON_SWORD);

    public RenderDragonFamiliar(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelDragonFamiliar(), 0.3F);
        this.addLayer(new StickLayer(this));
        this.addLayer(new SwordLayer(this));
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityDragonFamiliar entity) {
        return TEXTURE;
    }

    @Override
    public void doRender(EntityDragonFamiliar entity, double x, double y, double z, float entityYaw,
            float partialTicks) {
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
        this.renderPetText(entity, x, y, z, partialTicks);
    }

    private void renderPetText(EntityDragonFamiliar entity, double x, double y, double z, float partialTicks) {
        float textTimer = entity.getPetTimer() + partialTicks;
        if (textTimer >= EntityDragonFamiliar.MAX_PET_TIMER) {
            return;
        }

        String text = I18n.format("dialog.occultism.dragon.pet");
        float height = entity.height + 0.5F;
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x, (float) y + height + textTimer / 20.0F, (float) z);
        GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        float pitchDirection = this.renderManager.options.thirdPersonView == 2 ? -1.0F : 1.0F;
        GlStateManager.rotate(pitchDirection * this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
        GlStateManager.translate(MathHelper.sin(textTimer / 2.0F) * 0.5F, 0.0F, 0.0F);
        float size = (1.0F - textTimer / EntityDragonFamiliar.MAX_PET_TIMER) * 0.025F;
        GlStateManager.scale(-size, -size, size);
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO);
        FontRenderer font = this.getFontRendererFromRenderManager();
        font.drawString(text, -font.getStringWidth(text) / 2.0F, 0.0F, 0xFFFFFF, false);
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }

    private static class StickLayer implements LayerRenderer<EntityDragonFamiliar> {

        private final RenderDragonFamiliar renderer;

        private StickLayer(RenderDragonFamiliar renderer) {
            this.renderer = renderer;
        }

        @Override
        public void doRenderLayer(EntityDragonFamiliar entity, float limbSwing, float limbSwingAmount,
                float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
            if (!entity.hasStick()) {
                return;
            }

            GlStateManager.pushMatrix();
            ModelDragonFamiliar model = (ModelDragonFamiliar) this.renderer.getMainModel();
            model.body.postRender(scale);
            model.neck1.postRender(scale);
            model.neck2.postRender(scale);
            model.head.postRender(scale);
            model.jaw.postRender(scale);
            GlStateManager.translate(-0.08F, -0.07F, -0.15F);
            GlStateManager.rotate(-45.0F, 0.0F, 0.0F, 1.0F);
            Minecraft.getMinecraft().getRenderItem().renderItem(STICK,
                    ItemCameraTransforms.TransformType.GROUND);
            GlStateManager.popMatrix();
        }

        @Override
        public boolean shouldCombineTextures() {
            return false;
        }
    }

    private static class SwordLayer implements LayerRenderer<EntityDragonFamiliar> {

        private final RenderDragonFamiliar renderer;

        private SwordLayer(RenderDragonFamiliar renderer) {
            this.renderer = renderer;
        }

        @Override
        public void doRenderLayer(EntityDragonFamiliar entity, float limbSwing, float limbSwingAmount,
                float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
            if (!entity.hasSword()) {
                return;
            }

            GlStateManager.pushMatrix();
            ModelDragonFamiliar model = (ModelDragonFamiliar) this.renderer.getMainModel();
            model.body.postRender(scale);
            model.tail1.postRender(scale);
            model.tail2.postRender(scale);
            model.tail3.postRender(scale);
            GlStateManager.translate(0.0F, 0.24F, 0.32F);
            GlStateManager.translate(0.0F, -0.23F, -0.12F);
            GlStateManager.rotate(MathHelper.sin(ageInTicks / 20.0F) * 20.0F + 130.0F,
                    1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(90.0F + MathHelper.cos(ageInTicks / 20.0F) * 20.0F,
                    0.0F, 1.0F, 0.0F);
            GlStateManager.translate(0.23F, 0.12F, 0.0F);
            Minecraft.getMinecraft().getRenderItem().renderItem(IRON_SWORD,
                    ItemCameraTransforms.TransformType.GROUND);
            GlStateManager.popMatrix();
        }

        @Override
        public boolean shouldCombineTextures() {
            return false;
        }
    }
}
