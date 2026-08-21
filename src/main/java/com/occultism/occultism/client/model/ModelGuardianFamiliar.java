package com.occultism.occultism.client.model;

import com.occultism.occultism.entity.EntityGuardianFamiliar;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelGuardianFamiliar extends ModelBase {

    private final ModelRenderer body;
    private final ModelRenderer bodyLower;
    private final ModelRenderer head;
    private final ModelRenderer leftArm1;
    private final ModelRenderer rightArm1;
    private final ModelRenderer leftArm2;
    private final ModelRenderer rightArm2;
    private final ModelRenderer leftArm3;
    private final ModelRenderer rightArm3;
    private final ModelRenderer leftLeg1;
    private final ModelRenderer rightLeg1;
    private final ModelRenderer leftLeg2;
    private final ModelRenderer rightLeg2;
    private final ModelRenderer treeRoot;
    private final ModelRenderer birdRoot;
    private final ModelRenderer birdHead;
    private final ModelRenderer birdLeftWing;
    private final ModelRenderer birdRightWing;
    private boolean treeLayerOnly;

    public ModelGuardianFamiliar() {
        this.textureWidth = 64;
        this.textureHeight = 32;

        this.body = new ModelRenderer(this, 0, 0);
        this.body.addBox(-3.5F, -3.0F, -3.0F, 7, 6, 6);
        this.body.setRotationPoint(0.0F, 9.1F, 0.0F);

        this.bodyLower = new ModelRenderer(this, 26, 0);
        this.bodyLower.addBox(-2.5F, -2.0F, -2.5F, 5, 4, 5);
        this.bodyLower.setRotationPoint(0.0F, 5.0F, 0.0F);
        this.body.addChild(this.bodyLower);

        this.head = new ModelRenderer(this, 22, 9);
        this.head.addBox(-2.0F, -2.0F, -2.0F, 4, 4, 4);
        this.head.setRotationPoint(0.0F, -5.0F, 0.0F);
        this.body.addChild(this.head);

        this.addHeadDetails();

        this.leftArm1 = new ModelRenderer(this, 0, 12);
        this.leftArm1.addBox(-2.0F, -2.0F, -2.0F, 4, 6, 4);
        this.leftArm1.setRotationPoint(5.0F, -3.0F, 0.0F);
        setRotation(this.leftArm1, 0.0F, 0.0F, -0.1954769F);
        this.body.addChild(this.leftArm1);

        this.rightArm1 = new ModelRenderer(this, 0, 12);
        this.rightArm1.mirror = true;
        this.rightArm1.addBox(-2.0F, -2.0F, -2.0F, 4, 6, 4);
        this.rightArm1.setRotationPoint(-5.0F, -3.0F, 0.0F);
        setRotation(this.rightArm1, 0.0F, 0.0F, 0.1919862F);
        this.body.addChild(this.rightArm1);

        this.leftArm2 = new ModelRenderer(this, 35, 16);
        this.leftArm2.addBox(-1.5F, -1.5F, -1.5F, 3, 5, 3);
        this.leftArm2.setRotationPoint(0.0F, 6.0F, 0.0F);
        setRotation(this.leftArm2, 0.0F, 0.0F, 0.0781908F);
        this.leftArm1.addChild(this.leftArm2);

        this.rightArm2 = new ModelRenderer(this, 35, 16);
        this.rightArm2.mirror = true;
        this.rightArm2.addBox(-1.5F, -1.5F, -1.5F, 3, 5, 3);
        this.rightArm2.setRotationPoint(0.0F, 6.0F, 0.0F);
        setRotation(this.rightArm2, 0.0F, 0.0F, -0.0781908F);
        this.rightArm1.addChild(this.rightArm2);

        this.leftArm3 = new ModelRenderer(this, 52, 13);
        this.leftArm3.addBox(-1.5F, -1.5F, -1.5F, 2, 3, 3);
        this.leftArm3.setRotationPoint(0.0F, 5.5F, 0.0F);
        setRotation(this.leftArm3, 0.0F, 0.0F, 0.0390954F);
        this.leftArm2.addChild(this.leftArm3);

        this.rightArm3 = new ModelRenderer(this, 52, 13);
        this.rightArm3.mirror = true;
        this.rightArm3.addBox(-1.5F, -1.5F, -1.5F, 2, 3, 3);
        this.rightArm3.setRotationPoint(1.0F, 5.5F, 0.0F);
        setRotation(this.rightArm3, 0.0F, 0.0F, -0.0390954F);
        this.rightArm2.addChild(this.rightArm3);

        this.leftLeg1 = new ModelRenderer(this, 46, 0);
        this.leftLeg1.addBox(-1.5F, 0.0F, -1.5F, 3, 5, 3);
        this.leftLeg1.setRotationPoint(2.0F, 2.5F, 0.0F);
        setRotation(this.leftLeg1, 0.0F, 0.0F, -0.1563815F);
        this.bodyLower.addChild(this.leftLeg1);

        this.rightLeg1 = new ModelRenderer(this, 46, 0);
        this.rightLeg1.mirror = true;
        this.rightLeg1.addBox(-1.5F, 0.0F, -1.5F, 3, 5, 3);
        this.rightLeg1.setRotationPoint(-2.0F, 2.5F, 0.0F);
        setRotation(this.rightLeg1, 0.0F, 0.0F, 0.1563815F);
        this.bodyLower.addChild(this.rightLeg1);

        this.leftLeg2 = new ModelRenderer(this, 20, 0);
        this.leftLeg2.addBox(-1.0F, 0.0F, -1.0F, 2, 2, 2);
        this.leftLeg2.setRotationPoint(0.0F, 5.5F, 0.0F);
        setRotation(this.leftLeg2, 0.0F, 0.0F, 0.1172861F);
        this.leftLeg1.addChild(this.leftLeg2);

        this.rightLeg2 = new ModelRenderer(this, 20, 0);
        this.rightLeg2.mirror = true;
        this.rightLeg2.addBox(-1.0F, 0.0F, -1.0F, 2, 2, 2);
        this.rightLeg2.setRotationPoint(0.0F, 5.5F, 0.0F);
        setRotation(this.rightLeg2, 0.0F, 0.0F, -0.1172861F);
        this.rightLeg1.addChild(this.rightLeg2);

        this.treeRoot = new ModelRenderer(this, 0, 0);
        this.treeRoot.setRotationPoint(0.0F, 2.1F, 0.0F);
        ModelRenderer tree1 = new ModelRenderer(this, 12, 22);
        tree1.addBox(-5.0F, -10.0F, 0.0F, 10, 10, 0);
        setRotation(tree1, 0.0F, 0.7853982F, 0.0F);
        this.treeRoot.addChild(tree1);
        ModelRenderer tree2 = new ModelRenderer(this, 12, 22);
        tree2.addBox(-5.0F, -10.0F, 0.0F, 10, 10, 0);
        setRotation(tree2, 0.0F, -0.7853982F, 0.0F);
        this.treeRoot.addChild(tree2);

        this.birdRoot = new ModelRenderer(this, 56, 28);
        this.birdRoot.addBox(-1.0F, -1.0F, -1.0F, 2, 1, 2);
        this.birdRoot.setRotationPoint(5.3F, 3.2F, 0.0F);
        setRotation(this.birdRoot, -0.5473353F, 0.1745329F, 0.0F);

        this.birdHead = new ModelRenderer(this, 44, 30);
        this.birdHead.addBox(-0.5F, -1.0F, -1.0F, 1, 1, 1);
        this.birdHead.setRotationPoint(0.0F, -0.5F, -0.6F);
        setRotation(this.birdHead, 0.5082399F, 0.0F, 0.0F);
        this.birdRoot.addChild(this.birdHead);

        ModelRenderer birdNose = new ModelRenderer(this, 41, 31);
        birdNose.addBox(-0.5F, 0.0F, -1.0F, 1, 0, 1);
        birdNose.setRotationPoint(0.0F, -0.3F, -0.7F);
        this.birdHead.addChild(birdNose);

        this.birdLeftWing = new ModelRenderer(this, 50, 30);
        this.birdLeftWing.addBox(0.0F, 0.0F, -1.0F, 2, 0, 2);
        this.birdLeftWing.setRotationPoint(0.7F, -0.6F, 0.0F);
        this.birdRoot.addChild(this.birdLeftWing);

        this.birdRightWing = new ModelRenderer(this, 50, 30);
        this.birdRightWing.mirror = true;
        this.birdRightWing.addBox(-2.0F, 0.0F, -1.0F, 2, 0, 2);
        this.birdRightWing.setRotationPoint(-0.7F, -0.6F, 0.0F);
        this.birdRoot.addChild(this.birdRightWing);
    }

    public void setTreeLayerOnly(boolean treeLayerOnly) {
        this.treeLayerOnly = treeLayerOnly;
    }

    @Override
    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
            float headPitch, float scale) {
        this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);
        EntityGuardianFamiliar guardian = (EntityGuardianFamiliar) entityIn;
        if (this.treeLayerOnly) {
            if (guardian.hasTree()) {
                this.treeRoot.render(scale);
            }
            return;
        }
        this.body.render(scale);
        if (guardian.hasBird()) {
            this.birdRoot.render(scale);
        }
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
            float headPitch, float scaleFactor, Entity entityIn) {
        EntityGuardianFamiliar guardian = (EntityGuardianFamiliar) entityIn;
        int lives = guardian.getLives();
        this.leftArm1.showModel = lives > EntityGuardianFamiliar.ONE_ARMED;
        this.leftLeg1.showModel = lives > EntityGuardianFamiliar.ONE_LEGGED;
        this.rightLeg1.showModel = lives > EntityGuardianFamiliar.FLOATING;
        this.rightArm1.showModel = lives > EntityGuardianFamiliar.DEATHS_DOOR;

        setRotation(this.body, 0.0F, 0.0F, 0.0F);
        setRotation(this.head, headPitch * 0.017453292F, netHeadYaw * 0.017453292F, 0.0F);
        setRotation(this.leftArm1, 0.0F, 0.0F, -0.1954769F);
        setRotation(this.rightArm1, 0.0F, 0.0F, 0.1919862F);
        setRotation(this.leftArm2, 0.0F, 0.0F, 0.0781908F);
        setRotation(this.rightArm2, 0.0F, 0.0F, -0.0781908F);
        setRotation(this.leftArm3, 0.0F, 0.0F, 0.0390954F);
        setRotation(this.rightArm3, 0.0F, 0.0F, -0.0390954F);
        setRotation(this.leftLeg1, 0.0F, 0.0F, -0.1563815F);
        setRotation(this.rightLeg1, 0.0F, 0.0F, 0.1563815F);

        if (guardian.isSitting()) {
            this.leftLeg1.rotateAngleX = toRad(-90.0F);
            this.rightLeg1.rotateAngleX = toRad(-90.0F);
            this.leftArm1.rotateAngleX = toRad(-30.0F);
            this.rightArm1.rotateAngleX = toRad(-30.0F);
            this.leftArm2.rotateAngleX = toRad(-30.0F);
            this.rightArm2.rotateAngleX = toRad(-30.0F);
            this.leftArm3.rotateAngleX = toRad(-30.0F);
            this.rightArm3.rotateAngleX = toRad(-30.0F);
        } else {
            this.leftLeg1.rotateAngleX = MathHelper.cos(limbSwing * 0.5F + (float) Math.PI)
                    * limbSwingAmount * 0.5F;
            this.rightLeg1.rotateAngleX = MathHelper.cos(limbSwing * 0.5F) * limbSwingAmount * 0.5F;
            this.leftArm1.rotateAngleX = MathHelper.cos(limbSwing * 0.5F) * limbSwingAmount * 0.5F;
            this.rightArm1.rotateAngleX = MathHelper.cos(limbSwing * 0.5F + (float) Math.PI)
                    * limbSwingAmount * 0.5F;
        }

        if (lives == EntityGuardianFamiliar.ONE_LEGGED) {
            this.body.rotateAngleZ = toRad(-20.0F);
            this.rightLeg1.rotateAngleZ = toRad(20.0F);
            this.rightArm1.rotateAngleZ = toRad(20.0F);
        }

        this.birdHead.rotateAngleY = netHeadYaw * 0.017453292F * 0.4F;
        this.birdHead.rotateAngleX = headPitch * 0.017453292F * 0.4F + toRad(30.0F);
        this.birdLeftWing.rotateAngleZ = toRad(20.0F) + MathHelper.sin(ageInTicks / 2.0F) * toRad(25.0F);
        this.birdRightWing.rotateAngleZ = toRad(-20.0F) - MathHelper.sin(ageInTicks / 2.0F) * toRad(25.0F);
        this.birdRoot.rotationPointY = 3.2F;
        if (lives <= EntityGuardianFamiliar.ONE_ARMED) {
            this.birdRoot.rotationPointY -= MathHelper.sin(ageInTicks / 2.0F) * 0.4F;
        }
    }

    private void addHeadDetails() {
        ModelRenderer crystal1 = new ModelRenderer(this, 8, 28);
        crystal1.addBox(-1.0F, -4.0F, 0.0F, 2, 4, 0);
        crystal1.setRotationPoint(0.9F, -1.0F, 0.0F);
        setRotation(crystal1, -0.0781908F, -0.3518584F, 0.4300491F);
        this.head.addChild(crystal1);

        ModelRenderer crystal2 = new ModelRenderer(this, 4, 28);
        crystal2.addBox(-1.0F, -4.0F, 0.0F, 2, 4, 0);
        crystal2.setRotationPoint(-1.0F, -1.0F, -1.0F);
        setRotation(crystal2, 0.1563815F, 0.2345722F, 0.1954769F);
        this.head.addChild(crystal2);

        ModelRenderer crystal3 = new ModelRenderer(this, 0, 28);
        crystal3.addBox(-1.0F, -4.0F, 0.0F, 2, 4, 0);
        crystal3.setRotationPoint(-1.1F, -1.0F, 1.0F);
        setRotation(crystal3, -0.1172861F, 0.8210029F, -0.2345722F);
        this.head.addChild(crystal3);

        ModelRenderer leftEye = new ModelRenderer(this, 0, 25);
        leftEye.addBox(-1.0F, -1.5F, 0.0F, 2, 3, 0);
        leftEye.setRotationPoint(1.2F, -0.5F, -2.4F);
        setRotation(leftEye, 0.0F, -0.1221730F, 0.1221730F);
        this.head.addChild(leftEye);

        ModelRenderer rightEye = new ModelRenderer(this, 0, 25);
        rightEye.mirror = true;
        rightEye.addBox(-1.0F, -1.5F, 0.0F, 2, 3, 0);
        rightEye.setRotationPoint(-1.2F, -0.5F, -2.4F);
        setRotation(rightEye, 0.0F, 0.1221730F, -0.1221730F);
        this.head.addChild(rightEye);
    }

    private static void setRotation(ModelRenderer renderer, float x, float y, float z) {
        renderer.rotateAngleX = x;
        renderer.rotateAngleY = y;
        renderer.rotateAngleZ = z;
    }

    private static float toRad(float degrees) {
        return (float) Math.toRadians(degrees);
    }
}
