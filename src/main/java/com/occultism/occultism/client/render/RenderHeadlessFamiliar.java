package com.occultism.occultism.client.render;

import java.time.LocalDate;

import com.occultism.occultism.Tags;
import com.occultism.occultism.client.model.ModelHeadlessFamiliar;
import com.occultism.occultism.entity.EntityHeadlessFamiliar;
import com.occultism.occultism.entity.EntityHeadlessFamiliar.Rebuilt;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderHeadlessFamiliar extends RenderLiving<EntityHeadlessFamiliar> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Tags.MOD_ID, "textures/entity/headless_familiar.png");

    public RenderHeadlessFamiliar(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelHeadlessFamiliar(), 0.3F);
        this.addLayer(new PumpkinLayer(this));
        this.addLayer(new HeadLayer());
        this.addLayer(new WeaponLayer());
        this.addLayer(new RebuiltLayer());
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityHeadlessFamiliar entity) {
        return TEXTURE;
    }

    @Override
    protected void preRenderCallback(EntityHeadlessFamiliar entitylivingbaseIn, float partialTickTime) {
        if (entitylivingbaseIn.isSitting()) {
            GlStateManager.translate(0.0F, -0.12F, 0.0F);
        }
    }

    private static class PumpkinLayer implements LayerRenderer<EntityHeadlessFamiliar> {

        private static final ResourceLocation PUMPKIN =
                new ResourceLocation(Tags.MOD_ID, "textures/entity/headless_familiar_pumpkin.png");
        private static final ResourceLocation CHRISTMAS =
                new ResourceLocation(Tags.MOD_ID, "textures/entity/headless_familiar_christmas.png");

        private final RenderHeadlessFamiliar renderer;
        private final ModelHeadlessFamiliar model = new ModelHeadlessFamiliar();

        private PumpkinLayer(RenderHeadlessFamiliar renderer) {
            this.renderer = renderer;
        }

        @Override
        public void doRenderLayer(EntityHeadlessFamiliar entity, float limbSwing, float limbSwingAmount,
                float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
            if (entity.isInvisible() || entity.hasHead() || entity.isHeadlessDead()) {
                return;
            }
            this.renderer.bindTexture(isChristmas() ? CHRISTMAS : PUMPKIN);
            this.model.setModelAttributes(this.renderer.getMainModel());
            this.model.setLivingAnimations(entity, limbSwing, limbSwingAmount, partialTicks);
            GlStateManager.enableBlend();
            this.model.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            GlStateManager.disableBlend();
        }

        @Override
        public boolean shouldCombineTextures() {
            return true;
        }

        private static boolean isChristmas() {
            LocalDate date = LocalDate.now();
            int month = date.getMonthValue();
            int day = date.getDayOfMonth();
            return month == 12 && day >= 15 || month == 1 && day <= 15;
        }
    }

    private static class HeadLayer implements LayerRenderer<EntityHeadlessFamiliar> {

        @Override
        public void doRenderLayer(EntityHeadlessFamiliar entity, float limbSwing, float limbSwingAmount,
                float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
            if (entity.isInvisible() || entity.isHeadlessDead() || !entity.hasHead()) {
                return;
            }
            GlStateManager.pushMatrix();
            GlStateManager.translate(0.25F, 1.05F, -0.05F);
            GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(-netHeadYaw, 0.0F, 0.0F, 1.0F);
            GlStateManager.scale(0.35F, 0.35F, 0.35F);
            Minecraft.getMinecraft().getRenderItem().renderItem(entity.getHeadDisplayStack(),
                    ItemCameraTransforms.TransformType.GROUND);
            GlStateManager.popMatrix();
        }

        @Override
        public boolean shouldCombineTextures() {
            return false;
        }
    }

    private static class WeaponLayer implements LayerRenderer<EntityHeadlessFamiliar> {

        @Override
        public void doRenderLayer(EntityHeadlessFamiliar entity, float limbSwing, float limbSwingAmount,
                float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
            if (entity.isInvisible() || entity.isHeadlessDead()) {
                return;
            }
            renderItem(entity.getWeaponItem(), -0.17F, 0.75F, -0.16F, 90.0F, 0.0F, -45.0F, 0.45F);
        }

        @Override
        public boolean shouldCombineTextures() {
            return false;
        }
    }

    private static class RebuiltLayer implements LayerRenderer<EntityHeadlessFamiliar> {

        @Override
        public void doRenderLayer(EntityHeadlessFamiliar entity, float limbSwing, float limbSwingAmount,
                float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
            if (entity.isInvisible() || !entity.isHeadlessDead()) {
                return;
            }
            if (entity.isRebuilt(Rebuilt.RIGHT_LEG)) {
                renderItem(new ItemStack(Items.WHEAT), -0.25F, 0.22F, 0.0F, 130.0F, 0.0F, 0.0F, 0.45F);
            }
            if (entity.isRebuilt(Rebuilt.LEFT_LEG)) {
                renderItem(new ItemStack(Items.WHEAT), 0.25F, 0.22F, 0.0F, 50.0F, 0.0F, 0.0F, 0.45F);
            }
            if (entity.isRebuilt(Rebuilt.BODY)) {
                renderItem(new ItemStack(Blocks.HAY_BLOCK), 0.0F, 0.55F, 0.0F, 0.0F, 0.0F, 0.0F, 0.55F);
                renderItem(new ItemStack(Blocks.HAY_BLOCK), 0.0F, 0.75F, 0.0F, 0.0F, 0.0F, 0.0F, 0.55F);
            }
            if (entity.isRebuilt(Rebuilt.RIGHT_ARM)) {
                renderItem(new ItemStack(Items.STICK), -0.35F, 0.8F, 0.05F, 180.0F, 0.0F, -25.0F, 0.5F);
            }
            if (entity.isRebuilt(Rebuilt.LEFT_ARM)) {
                renderItem(new ItemStack(Items.STICK), 0.35F, 0.8F, -0.05F, 0.0F, 0.0F, 25.0F, 0.5F);
            }
            if (entity.isRebuilt(Rebuilt.HEAD)) {
                renderItem(new ItemStack(Blocks.PUMPKIN), 0.0F, 1.15F, -0.06F, -netHeadYaw, 0.0F, 0.0F, 0.55F);
            }
        }

        @Override
        public boolean shouldCombineTextures() {
            return false;
        }
    }

    private static void renderItem(ItemStack stack, float x, float y, float z, float yaw, float pitch, float roll,
            float scale) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.rotate(yaw, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(pitch, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(roll, 0.0F, 0.0F, 1.0F);
        GlStateManager.scale(scale, scale, scale);
        Minecraft.getMinecraft().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.GROUND);
        GlStateManager.popMatrix();
    }
}
