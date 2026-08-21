package com.occultism.occultism.client.model;

import com.occultism.occultism.entity.EntityDemonicPartner;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelDemonicHusband extends ModelBase {

    private final ModelRenderer root;
    private final ModelRenderer torso;
    private final ModelRenderer head;
    private final ModelRenderer leftArm;
    private final ModelRenderer rightArm;
    private final ModelRenderer leftLeg;
    private final ModelRenderer rightLeg;
    private final ModelRenderer leftWing;
    private final ModelRenderer rightWing;

    public ModelDemonicHusband() {
        this.textureWidth = 64;
        this.textureHeight = 64;

        this.root = new ModelRenderer(this, 0, 0);
        this.root.setRotationPoint(0.0F, 23.0F, 0.0F);

        this.torso = new ModelRenderer(this, 0, 16);
        this.torso.addBox(-3.5F, -10.0F, -2.25F, 7, 12, 4);
        this.torso.setRotationPoint(0.0F, 0.0F, 0.8F);
        setRotation(this.torso, toRad(5.0F), 0.0F, 0.0F);
        this.root.addChild(this.torso);

        this.head = new ModelRenderer(this, 0, 0);
        this.head.addBox(-4.0F, -8.0F, -4.25F, 8, 8, 8);
        this.head.setRotationPoint(0.0F, -10.0F, -0.1F);
        this.torso.addChild(this.head);

        ModelRenderer leftHorn = new ModelRenderer(this, 8, 38);
        leftHorn.addBox(-1.0F, -5.5F, -1.0F, 2, 5, 2);
        leftHorn.setRotationPoint(2.2F, -7.4F, -1.0F);
        setRotation(leftHorn, toRad(30.0F), 0.0F, toRad(20.0F));
        this.head.addChild(leftHorn);

        ModelRenderer rightHorn = new ModelRenderer(this, 8, 38);
        rightHorn.mirror = true;
        rightHorn.addBox(-1.0F, -5.5F, -1.0F, 2, 5, 2);
        rightHorn.setRotationPoint(-2.2F, -7.4F, -1.0F);
        setRotation(rightHorn, toRad(30.0F), 0.0F, toRad(-20.0F));
        this.head.addChild(rightHorn);

        ModelRenderer leftEar = new ModelRenderer(this, 40, 30);
        leftEar.addBox(0.0F, -3.0F, 0.0F, 0, 6, 6);
        leftEar.setRotationPoint(4.0F, -4.2F, -2.0F);
        setRotation(leftEar, toRad(-15.0F), toRad(-32.5F), toRad(25.0F));
        this.head.addChild(leftEar);

        ModelRenderer rightEar = new ModelRenderer(this, 40, 30);
        rightEar.mirror = true;
        rightEar.addBox(0.0F, -3.0F, 0.0F, 0, 6, 6);
        rightEar.setRotationPoint(-4.0F, -4.2F, -2.0F);
        setRotation(rightEar, toRad(-15.0F), toRad(32.5F), toRad(-25.0F));
        this.head.addChild(rightEar);

        this.leftArm = new ModelRenderer(this, 0, 38);
        this.leftArm.addBox(-1.0F, -1.0F, -1.0F, 2, 9, 2);
        this.leftArm.setRotationPoint(4.2F, -8.0F, -0.7F);
        setRotation(this.leftArm, toRad(36.0F), toRad(-17.0F), toRad(-21.0F));
        this.root.addChild(this.leftArm);

        this.rightArm = new ModelRenderer(this, 0, 38);
        this.rightArm.mirror = true;
        this.rightArm.addBox(-1.0F, -1.0F, -1.0F, 2, 9, 2);
        this.rightArm.setRotationPoint(-4.2F, -8.0F, -0.7F);
        setRotation(this.rightArm, toRad(36.0F), toRad(17.0F), toRad(21.0F));
        this.root.addChild(this.rightArm);

        this.leftLeg = new ModelRenderer(this, 22, 25);
        this.leftLeg.addBox(-1.5F, -0.5F, -2.0F, 4, 12, 4);
        this.leftLeg.setRotationPoint(1.4F, -1.5F, 0.0F);
        setRotation(this.leftLeg, toRad(-8.0F), 0.0F, toRad(-5.0F));
        this.root.addChild(this.leftLeg);

        this.rightLeg = new ModelRenderer(this, 22, 25);
        this.rightLeg.mirror = true;
        this.rightLeg.addBox(-2.5F, -0.5F, -2.0F, 4, 12, 4);
        this.rightLeg.setRotationPoint(-1.4F, -1.5F, 0.0F);
        setRotation(this.rightLeg, toRad(-8.0F), 0.0F, toRad(5.0F));
        this.root.addChild(this.rightLeg);

        this.leftWing = new ModelRenderer(this, 36, 31);
        this.leftWing.addBox(0.0F, -8.0F, 0.0F, 0, 9, 14);
        this.leftWing.setRotationPoint(2.0F, -7.5F, 1.7F);
        setRotation(this.leftWing, toRad(-10.0F), toRad(37.5F), 0.0F);
        this.root.addChild(this.leftWing);

        this.rightWing = new ModelRenderer(this, 36, 31);
        this.rightWing.mirror = true;
        this.rightWing.addBox(0.0F, -8.0F, 0.0F, 0, 9, 14);
        this.rightWing.setRotationPoint(-2.0F, -7.5F, 1.7F);
        setRotation(this.rightWing, toRad(-10.0F), toRad(-37.5F), 0.0F);
        this.root.addChild(this.rightWing);
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
        EntityDemonicPartner partner = (EntityDemonicPartner) entityIn;
        float idle = MathHelper.cos(ageInTicks * 0.05F);

        this.root.rotationPointY = 23.0F;
        setRotation(this.root, 0.0F, 0.0F, 0.0F);
        setRotation(this.torso, toRad(5.0F), 0.0F, idle * toRad(1.5F));
        setRotation(this.head, headPitch * 0.017453292F, netHeadYaw * 0.017453292F, 0.0F);
        setRotation(this.leftArm, toRad(36.0F), toRad(-17.0F), toRad(-21.0F));
        setRotation(this.rightArm, toRad(36.0F), toRad(17.0F), toRad(21.0F));
        setRotation(this.leftLeg, toRad(-8.0F), 0.0F, toRad(-5.0F));
        setRotation(this.rightLeg, toRad(-8.0F), 0.0F, toRad(5.0F));
        setRotation(this.leftWing, toRad(-10.0F), toRad(37.5F), 0.0F);
        setRotation(this.rightWing, toRad(-10.0F), toRad(-37.5F), 0.0F);

        this.root.rotationPointY += idle * 0.4F;
        this.leftLeg.rotateAngleX += MathHelper.cos(limbSwing * 0.66F) * 0.7F * limbSwingAmount;
        this.rightLeg.rotateAngleX += MathHelper.cos(limbSwing * 0.66F + (float) Math.PI) * 0.7F * limbSwingAmount;
        this.leftArm.rotateAngleX += MathHelper.cos(limbSwing * 0.66F + (float) Math.PI) * 0.35F * limbSwingAmount;
        this.rightArm.rotateAngleX += MathHelper.cos(limbSwing * 0.66F) * 0.35F * limbSwingAmount;

        if (this.swingProgress > 0.0F) {
            float attack = MathHelper.sin(this.swingProgress * (float) Math.PI);
            this.root.rotationPointZ += attack * 2.0F;
            this.leftArm.rotateAngleX -= attack * toRad(60.0F);
            this.rightArm.rotateAngleX -= attack * toRad(60.0F);
            this.torso.rotateAngleX += attack * toRad(10.0F);
        }

        if (partner.isSitting() || partner.isLying()) {
            this.root.rotationPointY = partner.isLying() ? 34.0F : 29.0F;
            this.root.rotateAngleX = partner.isLying() ? toRad(-75.0F) : toRad(-60.0F);
            this.leftArm.rotateAngleX = toRad(-158.0F);
            this.rightArm.rotateAngleX = toRad(60.0F);
            this.leftLeg.rotateAngleX = toRad(-35.0F);
            this.rightLeg.rotateAngleZ = toRad(15.0F);
            this.leftWing.showModel = false;
            this.rightWing.showModel = false;
        } else {
            this.leftWing.showModel = true;
            this.rightWing.showModel = true;
        }
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
