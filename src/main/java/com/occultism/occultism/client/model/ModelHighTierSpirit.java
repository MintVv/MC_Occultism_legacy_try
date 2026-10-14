package com.occultism.occultism.client.model;

import com.occultism.occultism.spirit.SpiritTier;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.MathHelper;

/**
 * 1.12 ModelRenderer equivalent of the upstream high-tier spirit hierarchy.
 *
 * The upstream model is a GeckoLib/Bedrock hierarchy. Keeping every part under one
 * root is important here: rendering the head and body as unrelated biped parts makes
 * the floating-head defect visible after tier scaling.
 */
public class ModelHighTierSpirit extends ModelBiped {

    private final SpiritTier tier;
    private final ModelRenderer root;
    private final ModelRenderer tailBase;
    private final ModelRenderer tailMiddle;
    private final ModelRenderer tailTip;
    private final ModelRenderer weapon;
    private final ModelRenderer maridCrown;

    public ModelHighTierSpirit(SpiritTier tier) {
        super(0.0F, 0.0F, 64, 64);
        this.tier = tier == null ? SpiritTier.DJINNI : tier;
        this.textureWidth = 64;
        this.textureHeight = 64;

        this.root = new ModelRenderer(this);
        this.root.setRotationPoint(0.0F, 24.0F, 0.0F);

        this.bipedHead = new ModelRenderer(this, 32, 0);
        this.bipedHead.setRotationPoint(0.0F, -15.0F, 0.0F);
        this.bipedHead.addBox(-2.5F, -6.0F, -3.0F, 5, 5, 5);
        this.root.addChild(this.bipedHead);

        ModelRenderer headCap = new ModelRenderer(this, 26, 9);
        headCap.addBox(-3.3F, -3.0F, -3.7F, 6, 3, 7, 0.15F);
        headCap.rotateAngleX = (float) Math.toRadians(-15.0D);
        this.bipedHead.addChild(headCap);

        ModelRenderer hornRight = new ModelRenderer(this, 50, 7);
        hornRight.addBox(-4.0F, -9.0F, -1.0F, 2, 3, 2);
        this.bipedHead.addChild(hornRight);

        ModelRenderer hornLeft = new ModelRenderer(this, 50, 7);
        hornLeft.addBox(2.0F, -9.0F, -1.0F, 2, 3, 2);
        this.bipedHead.addChild(hornLeft);

        ModelRenderer nose = new ModelRenderer(this, 14, 45);
        nose.addBox(-1.0F, -2.0F, -5.0F, 2, 3, 2);
        this.bipedHead.addChild(nose);

        this.maridCrown = new ModelRenderer(this, 0, 0);
        this.maridCrown.setRotationPoint(0.0F, -2.0F, -2.0F);
        this.maridCrown.addBox(-3.5F, -3.0F, -1.0F, 7, 1, 2);
        this.maridCrown.showModel = this.tier == SpiritTier.MARID;
        this.bipedHead.addChild(this.maridCrown);

        this.bipedHeadwear = new ModelRenderer(this);
        this.bipedHeadwear.showModel = false;

        this.bipedBody = new ModelRenderer(this, 0, 16);
        this.bipedBody.setRotationPoint(0.0F, -24.0F, 0.0F);
        this.bipedBody.addBox(-5.0F, 2.0F, -3.0F, 10, 6, 6);
        this.bipedBody.addBox(-3.0F, -4.0F, -2.3F, 6, 8, 5);
        this.root.addChild(this.bipedBody);

        ModelRenderer chestOverlay = new ModelRenderer(this, 0, 28);
        chestOverlay.addBox(-3.0F, -4.0F, -2.3F, 6, 8, 5, 0.1F);
        this.bipedBody.addChild(chestOverlay);

        ModelRenderer backFin = new ModelRenderer(this, 16, 36);
        backFin.addBox(-2.9F, 0.0F, 2.0F, 5, 8, 5);
        this.bipedBody.addChild(backFin);

        this.bipedRightArm = new ModelRenderer(this, 0, 40);
        this.bipedRightArm.setRotationPoint(-5.5F, -14.0F, 0.0F);
        this.bipedRightArm.addBox(-2.5F, -1.0F, -2.0F, 3, 8, 4);
        this.root.addChild(this.bipedRightArm);

        this.bipedLeftArm = new ModelRenderer(this, 46, 45);
        this.bipedLeftArm.setRotationPoint(5.5F, -14.0F, 0.0F);
        this.bipedLeftArm.addBox(-0.5F, -1.0F, -2.0F, 3, 8, 4);
        this.root.addChild(this.bipedLeftArm);

        ModelRenderer rightForearm = new ModelRenderer(this, 49, 16);
        rightForearm.setRotationPoint(-1.0F, 7.0F, 0.0F);
        rightForearm.addBox(-0.5F, 0.0F, -1.8F, 3, 6, 4);
        this.bipedRightArm.addChild(rightForearm);

        ModelRenderer leftForearm = new ModelRenderer(this, 50, 7);
        leftForearm.setRotationPoint(1.0F, 7.0F, 0.0F);
        leftForearm.addBox(-0.5F, 0.0F, -1.8F, 3, 6, 4);
        this.bipedLeftArm.addChild(leftForearm);

        this.weapon = new ModelRenderer(this, 26, 10);
        this.weapon.setRotationPoint(-1.0F, 8.0F, -1.0F);
        this.weapon.addBox(-0.2F, -1.0F, -0.2F, 1, 10, 1);
        this.weapon.addBox(-1.5F, 4.0F, -0.4F, 3, 1, 2);
        this.weapon.showModel = this.tier == SpiritTier.AFRIT || this.tier == SpiritTier.MARID;
        this.bipedRightArm.addChild(this.weapon);

        this.bipedRightLeg = new ModelRenderer(this, 14, 48);
        this.bipedRightLeg.setRotationPoint(-2.0F, -5.0F, 0.0F);
        this.bipedRightLeg.addBox(-2.0F, -1.0F, -2.0F, 3, 3, 4);
        this.root.addChild(this.bipedRightLeg);

        this.bipedLeftLeg = new ModelRenderer(this, 14, 48);
        this.bipedLeftLeg.setRotationPoint(2.0F, -5.0F, 0.0F);
        this.bipedLeftLeg.addBox(-1.0F, -1.0F, -2.0F, 3, 3, 4);
        this.root.addChild(this.bipedLeftLeg);

        ModelRenderer rightFoot = new ModelRenderer(this, 48, 0);
        rightFoot.addBox(-2.0F, 2.0F, -1.0F, 3, 3, 3);
        this.bipedRightLeg.addChild(rightFoot);

        ModelRenderer leftFoot = new ModelRenderer(this, 48, 0);
        leftFoot.addBox(-1.0F, 2.0F, -1.0F, 3, 3, 3);
        this.bipedLeftLeg.addChild(leftFoot);

        this.tailBase = new ModelRenderer(this, 0, 0);
        this.tailBase.setRotationPoint(0.0F, -5.0F, 2.0F);
        this.tailBase.addBox(-3.0F, -4.0F, 0.0F, 6, 7, 5);
        this.root.addChild(this.tailBase);

        this.tailMiddle = new ModelRenderer(this, 27, 23);
        this.tailMiddle.setRotationPoint(0.0F, 3.0F, 4.0F);
        this.tailMiddle.addBox(-2.0F, 0.0F, 0.0F, 4, 7, 4);
        this.tailBase.addChild(this.tailMiddle);

        this.tailTip = new ModelRenderer(this, 34, 36);
        this.tailTip.setRotationPoint(0.0F, 7.0F, 3.0F);
        this.tailTip.addBox(-1.0F, 0.0F, 0.0F, 2, 8, 2);
        this.tailMiddle.addChild(this.tailTip);
    }

    @Override
    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks,
            float netHeadYaw, float headPitch, float scale) {
        this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);
        this.root.render(scale);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks,
            float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
        this.root.rotateAngleX = 0.0F;
        this.root.rotateAngleY = 0.0F;
        this.root.rotateAngleZ = 0.0F;

        this.bipedHead.rotateAngleY = netHeadYaw * 0.017453292F;
        this.bipedHead.rotateAngleX = headPitch * 0.017453292F;
        this.bipedHead.rotateAngleZ = 0.0F;
        this.bipedBody.rotateAngleX = 0.0F;
        this.bipedBody.rotateAngleY = 0.0F;
        this.bipedBody.rotateAngleZ = 0.0F;

        float walk = limbSwing * 0.6662F;
        this.bipedRightArm.rotateAngleX = MathHelper.cos(walk + (float) Math.PI) * 1.0F * limbSwingAmount;
        this.bipedLeftArm.rotateAngleX = MathHelper.cos(walk) * 1.0F * limbSwingAmount;
        this.bipedRightArm.rotateAngleY = 0.0F;
        this.bipedLeftArm.rotateAngleY = 0.0F;
        this.bipedRightArm.rotateAngleZ = 0.0F;
        this.bipedLeftArm.rotateAngleZ = 0.0F;

        this.bipedRightLeg.rotateAngleX = MathHelper.cos(walk) * 0.75F * limbSwingAmount;
        this.bipedLeftLeg.rotateAngleX = MathHelper.cos(walk + (float) Math.PI) * 0.75F * limbSwingAmount;
        this.bipedRightLeg.rotateAngleY = 0.0F;
        this.bipedLeftLeg.rotateAngleY = 0.0F;
        this.bipedRightLeg.rotateAngleZ = 0.0F;
        this.bipedLeftLeg.rotateAngleZ = 0.0F;

        float idle = MathHelper.sin(ageInTicks * 0.08F);
        this.tailBase.rotateAngleX = idle * 0.08F;
        this.tailMiddle.rotateAngleX = idle * 0.14F;
        this.tailTip.rotateAngleX = idle * 0.20F;
        this.bipedRightArm.rotateAngleZ = idle * 0.04F;
        this.bipedLeftArm.rotateAngleZ = -idle * 0.04F;

        if (entityIn instanceof EntityLivingBase && ((EntityLivingBase) entityIn).swingProgress > 0.0F) {
            float swing = ((EntityLivingBase) entityIn).swingProgress;
            float attack = MathHelper.sin(MathHelper.sqrt(swing) * (float) Math.PI);
            this.bipedRightArm.rotateAngleX -= attack * 1.8F;
            this.bipedRightArm.rotateAngleY -= attack * 0.35F;
            this.bipedBody.rotateAngleY += attack * 0.35F;
        }

        this.weapon.rotateAngleX = this.tier == SpiritTier.MARID ? -0.45F : -0.25F;
        this.weapon.rotateAngleY = 0.0F;
        this.weapon.rotateAngleZ = this.tier == SpiritTier.AFRIT ? 0.18F : 0.0F;
        copyModelAngles(this.bipedHead, this.bipedHeadwear);
    }

    @Override
    public void postRenderArm(float scale, EnumHandSide side) {
        this.root.postRender(scale);
        this.getArmForSide(side).postRender(scale);
    }
}
