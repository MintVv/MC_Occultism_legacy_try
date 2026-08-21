package com.occultism.occultism.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

/** Lightweight 1.12 model for the upstream wild Afrit silhouette. */
public class ModelAfritWild extends ModelBase {

    private final ModelRenderer root;
    private final ModelRenderer head;
    private final ModelRenderer body;
    private final ModelRenderer rightArm;
    private final ModelRenderer leftArm;
    private final ModelRenderer rightLeg;
    private final ModelRenderer leftLeg;
    private final ModelRenderer rightWing;
    private final ModelRenderer leftWing;

    public ModelAfritWild() {
        this.textureWidth = 64;
        this.textureHeight = 64;

        this.root = new ModelRenderer(this);
        this.root.setRotationPoint(0.0F, 24.0F, 0.0F);

        this.body = new ModelRenderer(this, 0, 18);
        this.body.setRotationPoint(0.0F, -20.0F, 0.0F);
        this.body.addBox(-5.0F, 7.0F, -3.0F, 10, 13, 6);
        this.root.addChild(this.body);

        this.head = new ModelRenderer(this, 0, 0);
        this.head.setRotationPoint(0.0F, -20.0F, 0.0F);
        this.head.addBox(-4.5F, -8.0F, -4.5F, 9, 9, 9);
        this.root.addChild(this.head);

        ModelRenderer rightHorn = new ModelRenderer(this, 36, 0);
        rightHorn.addBox(-5.5F, -12.0F, -1.0F, 2, 5, 2);
        this.head.addChild(rightHorn);
        ModelRenderer leftHorn = new ModelRenderer(this, 36, 0);
        leftHorn.addBox(3.5F, -12.0F, -1.0F, 2, 5, 2);
        this.head.addChild(leftHorn);

        this.rightArm = new ModelRenderer(this, 32, 18);
        this.rightArm.setRotationPoint(-6.0F, -14.0F, 0.0F);
        this.rightArm.addBox(-2.0F, -1.0F, -2.0F, 4, 14, 4);
        this.root.addChild(this.rightArm);
        this.leftArm = new ModelRenderer(this, 48, 18);
        this.leftArm.setRotationPoint(6.0F, -14.0F, 0.0F);
        this.leftArm.addBox(-2.0F, -1.0F, -2.0F, 4, 14, 4);
        this.root.addChild(this.leftArm);

        this.rightLeg = new ModelRenderer(this, 32, 38);
        this.rightLeg.setRotationPoint(-2.5F, -7.0F, 0.0F);
        this.rightLeg.addBox(-2.0F, -1.0F, -2.0F, 4, 9, 4);
        this.root.addChild(this.rightLeg);
        this.leftLeg = new ModelRenderer(this, 48, 38);
        this.leftLeg.setRotationPoint(2.5F, -7.0F, 0.0F);
        this.leftLeg.addBox(-2.0F, -1.0F, -2.0F, 4, 9, 4);
        this.root.addChild(this.leftLeg);

        this.rightWing = new ModelRenderer(this, 0, 48);
        this.rightWing.setRotationPoint(-4.0F, -14.0F, 2.5F);
        this.rightWing.addBox(-12.0F, -1.0F, 0.0F, 12, 16, 1);
        this.root.addChild(this.rightWing);
        this.leftWing = new ModelRenderer(this, 26, 48);
        this.leftWing.setRotationPoint(4.0F, -14.0F, 2.5F);
        this.leftWing.addBox(0.0F, -1.0F, 0.0F, 12, 16, 1);
        this.root.addChild(this.leftWing);
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
            float netHeadYaw, float headPitch, float scale) {
        this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entity);
        this.root.render(scale);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks,
            float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
        this.head.rotateAngleY = netHeadYaw * 0.017453292F;
        this.head.rotateAngleX = headPitch * 0.017453292F;
        this.rightArm.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float) Math.PI)
                * 1.1F * limbSwingAmount;
        this.leftArm.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.1F * limbSwingAmount;
        this.rightLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 0.8F * limbSwingAmount;
        this.leftLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float) Math.PI)
                * 0.8F * limbSwingAmount;
        float flap = MathHelper.cos(ageInTicks * 0.18F) * 0.12F;
        this.rightWing.rotateAngleY = 0.25F + flap;
        this.leftWing.rotateAngleY = -0.25F - flap;
        this.rightWing.rotateAngleZ = flap * 0.5F;
        this.leftWing.rotateAngleZ = -flap * 0.5F;
    }
}
