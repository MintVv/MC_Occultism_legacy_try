package com.occultism.occultism.client.model;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.MathHelper;

public class ModelFoliotJob extends ModelBiped {

    private final ModelRenderer root;

    public ModelFoliotJob() {
        super(0.0F, 0.0F, 64, 64);
        this.textureWidth = 64;
        this.textureHeight = 64;

        this.root = new ModelRenderer(this);
        this.root.setRotationPoint(0.0F, 24.0F, 0.0F);

        this.bipedHead = new ModelRenderer(this, 24, 12);
        this.bipedHead.setRotationPoint(0.0F, -15.0F, 0.0F);
        this.bipedHead.addBox(-3.0F, -6.0F, -3.0F, 6, 6, 6);
        this.root.addChild(this.bipedHead);

        ModelRenderer headOverlay = new ModelRenderer(this, 24, 0);
        headOverlay.addBox(-3.0F, -6.7F, -3.0F, 6, 6, 6, 0.35F);
        this.bipedHead.addChild(headOverlay);

        ModelRenderer nose = new ModelRenderer(this, 14, 45);
        nose.addBox(-1.0F, -2.0F, -5.0F, 2, 3, 2);
        this.bipedHead.addChild(nose);

        ModelRenderer hornRight = new ModelRenderer(this, 48, 14);
        hornRight.addBox(-3.0F, -9.0F, -1.0F, 2, 3, 2);
        this.bipedHead.addChild(hornRight);

        ModelRenderer hornLeft = new ModelRenderer(this, 48, 14);
        hornLeft.addBox(1.0F, -9.0F, -1.0F, 2, 3, 2);
        this.bipedHead.addChild(hornLeft);

        ModelRenderer earRight = new ModelRenderer(this, 48, 6);
        earRight.setRotationPoint(-3.0F, -3.0F, 0.0F);
        earRight.rotateAngleY = (float) Math.toRadians(15.0D);
        earRight.addBox(-6.0F, -3.0F, 0.0F, 6, 4, 1);
        this.bipedHead.addChild(earRight);

        ModelRenderer earLeft = new ModelRenderer(this, 48, 10);
        earLeft.setRotationPoint(3.0F, -3.0F, 0.0F);
        earLeft.rotateAngleY = (float) Math.toRadians(-15.0D);
        earLeft.addBox(0.0F, -3.0F, 0.0F, 6, 4, 1);
        this.bipedHead.addChild(earLeft);

        ModelRenderer earLeftTip = new ModelRenderer(this, 60, 0);
        earLeftTip.setRotationPoint(1.0F, -1.0F, 0.0F);
        earLeftTip.rotateAngleY = (float) Math.toRadians(-45.0D);
        earLeftTip.addBox(0.0F, -1.0F, 0.0F, 2, 2, 1);
        earLeft.addChild(earLeftTip);

        this.bipedHeadwear = new ModelRenderer(this);
        this.bipedHeadwear.showModel = false;

        this.bipedBody = new ModelRenderer(this, 0, 0);
        this.bipedBody.setRotationPoint(0.0F, -24.0F, 0.0F);
        this.bipedBody.addBox(-4.0F, 9.0F, -2.0F, 8, 14, 4, 0.25F);
        this.root.addChild(this.bipedBody);

        ModelRenderer torsoOverlay = new ModelRenderer(this, 0, 18);
        torsoOverlay.addBox(-4.0F, 9.0F, -2.0F, 8, 9, 4);
        this.bipedBody.addChild(torsoOverlay);

        ModelRenderer backFin = new ModelRenderer(this, 24, 18);
        backFin.addBox(-0.5F, 12.0F, 2.0F, 1, 6, 7);
        this.bipedBody.addChild(backFin);

        this.bipedRightArm = new ModelRenderer(this, 36, 31);
        this.bipedRightArm.setRotationPoint(-5.0F, -14.0F, 0.0F);
        this.bipedRightArm.addBox(-2.0F, -1.0F, -1.0F, 3, 11, 3);
        this.root.addChild(this.bipedRightArm);

        ModelRenderer rightArmOverlay = new ModelRenderer(this, 0, 31);
        rightArmOverlay.addBox(-2.0F, -1.0F, -1.0F, 3, 11, 3, 0.1F);
        this.bipedRightArm.addChild(rightArmOverlay);

        this.bipedLeftArm = new ModelRenderer(this, 24, 31);
        this.bipedLeftArm.setRotationPoint(5.0F, -14.0F, 0.0F);
        this.bipedLeftArm.addBox(-1.0F, -1.0F, -1.0F, 3, 11, 3);
        this.root.addChild(this.bipedLeftArm);

        ModelRenderer leftArmOverlay = new ModelRenderer(this, 12, 31);
        leftArmOverlay.addBox(-1.0F, -1.0F, -1.0F, 3, 11, 3, 0.1F);
        this.bipedLeftArm.addChild(leftArmOverlay);

        this.bipedRightLeg = new ModelRenderer(this, 38, 24);
        this.bipedRightLeg.setRotationPoint(-2.0F, -5.0F, 0.0F);
        this.bipedRightLeg.addBox(-2.0F, -1.0F, -2.0F, 3, 3, 4);
        this.root.addChild(this.bipedRightLeg);

        ModelRenderer rightFoot = new ModelRenderer(this, 48, 0);
        rightFoot.addBox(-2.0F, 2.0F, -1.0F, 3, 3, 3);
        this.bipedRightLeg.addChild(rightFoot);

        this.bipedLeftLeg = new ModelRenderer(this, 0, 45);
        this.bipedLeftLeg.setRotationPoint(2.0F, -5.0F, 0.0F);
        this.bipedLeftLeg.addBox(-1.0F, -1.0F, -2.0F, 3, 3, 4);
        this.root.addChild(this.bipedLeftLeg);

        ModelRenderer leftFoot = new ModelRenderer(this, 48, 0);
        leftFoot.addBox(-1.0F, 2.0F, -1.0F, 3, 3, 3);
        this.bipedLeftLeg.addChild(leftFoot);
    }

    @Override
    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
            float headPitch, float scale) {
        this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);
        this.root.render(scale);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
            float headPitch, float scaleFactor, Entity entityIn) {
        this.root.setRotationPoint(0.0F, 24.0F, 0.0F);
        this.root.rotateAngleX = 0.0F;
        this.root.rotateAngleY = 0.0F;
        this.root.rotateAngleZ = 0.0F;

        this.bipedHead.setRotationPoint(0.0F, -15.0F, 0.0F);
        this.bipedHead.rotateAngleY = netHeadYaw * 0.017453292F;
        this.bipedHead.rotateAngleX = headPitch * 0.017453292F;
        this.bipedHead.rotateAngleZ = 0.0F;

        this.bipedBody.setRotationPoint(0.0F, -24.0F, 0.0F);
        this.bipedBody.rotateAngleX = 0.0F;
        this.bipedBody.rotateAngleY = 0.0F;
        this.bipedBody.rotateAngleZ = 0.0F;

        this.bipedRightArm.setRotationPoint(-5.0F, -14.0F, 0.0F);
        this.bipedLeftArm.setRotationPoint(5.0F, -14.0F, 0.0F);
        this.bipedRightArm.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float) Math.PI)
                * 1.2F * limbSwingAmount;
        this.bipedLeftArm.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.2F * limbSwingAmount;
        this.bipedRightArm.rotateAngleY = 0.0F;
        this.bipedLeftArm.rotateAngleY = 0.0F;
        this.bipedRightArm.rotateAngleZ = 0.0F;
        this.bipedLeftArm.rotateAngleZ = 0.0F;

        this.bipedRightLeg.setRotationPoint(-2.0F, -5.0F, 0.0F);
        this.bipedLeftLeg.setRotationPoint(2.0F, -5.0F, 0.0F);
        this.bipedRightLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 0.9F * limbSwingAmount;
        this.bipedLeftLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float) Math.PI)
                * 0.9F * limbSwingAmount;
        this.bipedRightLeg.rotateAngleY = 0.0F;
        this.bipedLeftLeg.rotateAngleY = 0.0F;
        this.bipedRightLeg.rotateAngleZ = 0.0F;
        this.bipedLeftLeg.rotateAngleZ = 0.0F;

        this.bipedRightArm.rotateAngleZ += MathHelper.cos(ageInTicks * 0.09F) * 0.05F - 0.05F;
        this.bipedLeftArm.rotateAngleZ -= MathHelper.cos(ageInTicks * 0.09F) * 0.05F - 0.05F;
        copyModelAngles(this.bipedHead, this.bipedHeadwear);
    }

    @Override
    public void postRenderArm(float scale, EnumHandSide side) {
        this.root.postRender(scale);
        this.getArmForSide(side).postRender(scale);
    }
}
