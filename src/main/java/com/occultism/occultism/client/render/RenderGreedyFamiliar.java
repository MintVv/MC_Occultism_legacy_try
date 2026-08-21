package com.occultism.occultism.client.render;

import java.time.LocalDate;

import com.occultism.occultism.Tags;
import com.occultism.occultism.client.model.ModelGreedyFamiliar;
import com.occultism.occultism.entity.EntityGreedyFamiliar;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderGreedyFamiliar extends RenderLiving<EntityGreedyFamiliar> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Tags.MOD_ID, "textures/entity/greedy_familiar.png");

    public RenderGreedyFamiliar(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelGreedyFamiliar(), 0.3F);
        this.addLayer(new ChestLayer(this));
        this.addLayer(new ItemLayer(this));
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityGreedyFamiliar entity) {
        return TEXTURE;
    }

    @Override
    protected void preRenderCallback(EntityGreedyFamiliar entity, float partialTickTime) {
        if (entity.isSitting() && !entity.isPartying()) {
            GlStateManager.translate(0.0F, -0.25F, 0.0F);
        }
    }

    private static class ChestLayer implements LayerRenderer<EntityGreedyFamiliar> {

        private static final ResourceLocation CHEST =
                new ResourceLocation(Tags.MOD_ID, "textures/entity/greedy_familiar_chest.png");
        private static final ResourceLocation CHRISTMAS =
                new ResourceLocation(Tags.MOD_ID, "textures/entity/greedy_familiar_christmas.png");

        private final RenderGreedyFamiliar renderer;
        private final ModelGreedyFamiliar model = new ModelGreedyFamiliar();

        private ChestLayer(RenderGreedyFamiliar renderer) {
            this.renderer = renderer;
        }

        @Override
        public void doRenderLayer(EntityGreedyFamiliar entity, float limbSwing, float limbSwingAmount,
                float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
            if (entity.isInvisible()) {
                return;
            }
            this.renderer.bindTexture(isChristmas() ? CHRISTMAS : CHEST);
            this.model.setModelAttributes(this.renderer.getMainModel());
            this.model.setLivingAnimations(entity, limbSwing, limbSwingAmount, partialTicks);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
                    GlStateManager.DestFactor.ZERO);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.model.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            GlStateManager.disableBlend();
        }

        @Override
        public boolean shouldCombineTextures() {
            return true;
        }
    }

    private static class ItemLayer implements LayerRenderer<EntityGreedyFamiliar> {

        private static final ItemStack IRON_PICKAXE = new ItemStack(Items.IRON_PICKAXE);

        private final RenderGreedyFamiliar renderer;

        private ItemLayer(RenderGreedyFamiliar renderer) {
            this.renderer = renderer;
        }

        @Override
        public void doRenderLayer(EntityGreedyFamiliar entity, float limbSwing, float limbSwingAmount,
                float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
            boolean hasBlacksmithUpgrade = entity.hasBlacksmithUpgrade();
            ItemStack offhand = entity.getHeldItemOffhand();
            if (!hasBlacksmithUpgrade && offhand.isEmpty()) {
                return;
            }

            ModelGreedyFamiliar model = (ModelGreedyFamiliar) this.renderer.getMainModel();
            if (hasBlacksmithUpgrade) {
                GlStateManager.pushMatrix();
                model.body.postRender(scale);
                model.rightArm.postRender(scale);
                GlStateManager.translate(-0.06F, 0.2F, -0.1F);
                GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
                GlStateManager.rotate(-45.0F, 0.0F, 0.0F, 1.0F);
                Minecraft.getMinecraft().getRenderItem().renderItem(IRON_PICKAXE,
                        ItemCameraTransforms.TransformType.GROUND);
                GlStateManager.popMatrix();
            }

            if (!offhand.isEmpty()) {
                GlStateManager.pushMatrix();
                model.body.postRender(scale);
                model.leftArm.postRender(scale);
                GlStateManager.translate(0.06F, 0.2F, -0.17F);
                GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
                GlStateManager.scale(0.75F, 0.75F, 0.75F);
                Minecraft.getMinecraft().getRenderItem().renderItem(offhand,
                        ItemCameraTransforms.TransformType.GROUND);
                GlStateManager.popMatrix();
            }
        }

        @Override
        public boolean shouldCombineTextures() {
            return false;
        }
    }

    private static boolean isChristmas() {
        LocalDate date = LocalDate.now();
        int month = date.getMonthValue();
        int day = date.getDayOfMonth();
        return month == 12 && day >= 15 || month == 1 && day <= 15;
    }
}
