package com.occultism.occultism.client.model;

import com.occultism.occultism.entity.EntityBeaverFamiliar;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelBeaverFamiliar extends ModelBase {

    private final ModelRenderer body;
    private final ModelRenderer tail;
    private final ModelRenderer tail2;
    private final ModelRenderer leftLeg1;
    private final ModelRenderer rightLeg1;
    private final ModelRenderer leftArm1;
    private final ModelRenderer rightArm1;
    private final ModelRenderer head;
    private final ModelRenderer leftEar;
    private final ModelRenderer rightEar;
    private final ModelRenderer whiskers1;
    private final ModelRenderer whiskers2;

    public ModelBeaverFamiliar() {
        this.textureWidth = 64;
        this.textureHeight = 32;

        this.body = new ModelRenderer(this, 0, 0);
        this.body.addBox(-3.0F, -3.0F, -5.0F, 6, 6, 10);
        this.body.setRotationPoint(0.0F, 19.5F, 0.0F);
        setRotation(this.body, 0.0872665F, 0.0F, 0.0F);

        this.tail = new ModelRenderer(this, -10, 22);
        this.tail.addBox(-4.0F, 0.0F, 0.0F, 8, 0, 10);
        this.tail.setRotationPoint(0.0F, 2.0F, 5.0F);
        setRotation(this.tail, 0.5082399F, 0.0F, 0.0F);
        this.body.addChild(this.tail);

        this.tail2 = new ModelRenderer(this, 16, 22);
        this.tail2.addBox(-3.5F, -0.5F, 0.0F, 7, 1, 9);
        this.tail2.setRotationPoint(0.0F, 2.0F, 5.0F);
        setRotation(this.tail2, 0.5082399F, 0.0F, 0.0F);
        this.body.addChild(this.tail2);

        this.leftLeg1 = new ModelRenderer(this, 22, 0);
        this.leftLeg1.addBox(0.0F, 0.0F, -2.0F, 2, 6, 4);
        this.leftLeg1.setRotationPoint(2.0F, -1.5F, 2.5F);
        this.body.addChild(this.leftLeg1);

        ModelRenderer leftLeg2 = new ModelRenderer(this, 34, 0);
        leftLeg2.addBox(-1.5F, -0.5F, -4.0F, 3, 1, 4);
        leftLeg2.setRotationPoint(1.48F, 5.7F, 0.5F);
        this.leftLeg1.addChild(leftLeg2);

        this.rightLeg1 = new ModelRenderer(this, 22, 0);
        this.rightLeg1.mirror = true;
        this.rightLeg1.addBox(-2.0F, 0.0F, -2.0F, 2, 6, 4);
        this.rightLeg1.setRotationPoint(-2.0F, -1.5F, 2.5F);
        this.body.addChild(this.rightLeg1);

        ModelRenderer rightLeg2 = new ModelRenderer(this, 34, 0);
        rightLeg2.mirror = true;
        rightLeg2.addBox(-1.5F, -0.5F, -4.0F, 3, 1, 4);
        rightLeg2.setRotationPoint(-1.48F, 5.7F, 0.5F);
        this.rightLeg1.addChild(rightLeg2);

        this.leftArm1 = new ModelRenderer(this, 32, 9);
        this.leftArm1.addBox(0.0F, 0.0F, -1.0F, 2, 4, 2);
        this.leftArm1.setRotationPoint(1.7F, 0.0F, -3.0F);
        this.body.addChild(this.leftArm1);

        ModelRenderer leftArm2 = new ModelRenderer(this, 40, 10);
        leftArm2.addBox(-1.0F, -0.5F, -2.0F, 2, 1, 3);
        leftArm2.setRotationPoint(1.02F, 3.7F, 0.02F);
        this.leftArm1.addChild(leftArm2);

        this.rightArm1 = new ModelRenderer(this, 32, 9);
        this.rightArm1.mirror = true;
        this.rightArm1.addBox(-2.0F, 0.0F, -1.0F, 2, 4, 2);
        this.rightArm1.setRotationPoint(-1.7F, 0.0F, -3.0F);
        this.body.addChild(this.rightArm1);

        ModelRenderer rightArm2 = new ModelRenderer(this, 40, 10);
        rightArm2.mirror = true;
        rightArm2.addBox(-1.0F, -0.5F, -2.0F, 2, 1, 3);
        rightArm2.setRotationPoint(-1.02F, 3.7F, 0.02F);
        this.rightArm1.addChild(rightArm2);

        this.head = new ModelRenderer(this, 43, 0);
        this.head.addBox(-2.5F, -2.5F, -5.0F, 5, 5, 5);
        this.head.setRotationPoint(0.0F, -1.0F, -4.0F);
        setRotation(this.head, -0.0872665F, 0.0F, 0.0F);
        this.body.addChild(this.head);

        ModelRenderer mouth = new ModelRenderer(this, 0, 0);
        mouth.addBox(-1.5F, -1.5F, -2.0F, 3, 3, 2);
        mouth.setRotationPoint(0.0F, 0.3F, -5.0F);
        this.head.addChild(mouth);

        ModelRenderer nose = new ModelRenderer(this, 30, 0);
        nose.addBox(-1.0F, -1.0F, -1.0F, 2, 2, 1);
        nose.setRotationPoint(0.0F, -0.2F, -1.7F);
        mouth.addChild(nose);

        ModelRenderer teeth = new ModelRenderer(this, 0, 5);
        teeth.addBox(-1.0F, 0.0F, -1.0F, 2, 2, 1);
        teeth.setRotationPoint(0.0F, 1.0F, -0.8F);
        setRotation(teeth, 0.1172861F, 0.0F, 0.0F);
        mouth.addChild(teeth);

        this.whiskers1 = new ModelRenderer(this, 4, 20);
        this.whiskers1.addBox(-4.0F, 0.0F, 0.0F, 8, 2, 0);
        this.whiskers1.setRotationPoint(0.0F, 0.3F, -1.7F);
        setRotation(this.whiskers1, 0.3909537F, 0.0392699F, 0.0174533F);
        mouth.addChild(this.whiskers1);

        this.whiskers2 = new ModelRenderer(this, 4, 20);
        this.whiskers2.addBox(-4.0F, 0.0F, 0.0F, 8, 2, 0);
        this.whiskers2.setRotationPoint(0.0F, -0.8F, -1.3F);
        setRotation(this.whiskers2, 0.3909537F, 0.0392699F, -0.0174533F);
        mouth.addChild(this.whiskers2);

        this.addEye(true);
        this.addEye(false);
        this.leftEar = this.addEar(true);
        this.rightEar = this.addEar(false);
    }

    @Override
    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
            float headPitch, float scale) {
        this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);
        this.body.render(scale);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
            float headPitch, float scaleFactor, Entity entityIn) {
        EntityBeaverFamiliar beaver = (EntityBeaverFamiliar) entityIn;
        this.leftEar.showModel = beaver.hasEars();
        this.rightEar.showModel = beaver.hasEars();
        this.whiskers1.showModel = beaver.hasWhiskers();
        this.whiskers2.showModel = beaver.hasWhiskers();
        this.tail.showModel = !beaver.hasBigTail();
        this.tail2.showModel = beaver.hasBigTail();

        this.body.rotateAngleX = 0.0872665F;
        this.body.rotationPointY = 19.5F;
        this.head.rotateAngleX = -0.0872665F + headPitch * 0.017453292F;
        this.head.rotateAngleY = netHeadYaw * 0.017453292F;
        this.leftLeg1.rotateAngleX = -0.0698132F + MathHelper.cos(limbSwing * 0.7F + (float) Math.PI)
                * 0.8F * limbSwingAmount;
        this.rightLeg1.rotateAngleX = -0.0698132F + MathHelper.cos(limbSwing * 0.7F) * 0.8F * limbSwingAmount;
        this.leftArm1.rotateAngleX = -0.0698132F + MathHelper.cos(limbSwing * 0.7F) * 0.8F * limbSwingAmount;
        this.rightArm1.rotateAngleX = -0.0698132F + MathHelper.cos(limbSwing * 0.7F + (float) Math.PI)
                * 0.8F * limbSwingAmount;
        this.tail.rotateAngleX = 0.5082399F + MathHelper.cos(ageInTicks * 0.1F) * toRad(20.0F);
        this.tail2.rotateAngleX = this.tail.rotateAngleX;

        if (beaver.isInWater()) {
            this.rightLeg1.rotateAngleZ = toRad(40.0F);
            this.leftLeg1.rotateAngleZ = -toRad(40.0F);
            this.rightArm1.rotateAngleZ = toRad(40.0F);
            this.leftArm1.rotateAngleZ = -toRad(40.0F);
        } else {
            this.rightLeg1.rotateAngleZ = 0.0F;
            this.leftLeg1.rotateAngleZ = 0.0F;
            this.rightArm1.rotateAngleZ = 0.0F;
            this.leftArm1.rotateAngleZ = 0.0F;
        }

        if (beaver.isSitting()) {
            this.body.rotateAngleX = toRad(-40.0F);
            this.head.rotateAngleX = toRad(25.0F);
            this.tail.rotateAngleX = toRad(70.0F);
            this.tail2.rotateAngleX = toRad(70.0F);
            this.leftLeg1.rotateAngleX = toRad(-20.0F);
            this.rightLeg1.rotateAngleX = toRad(-20.0F);
            this.leftArm1.rotateAngleX = toRad(10.0F);
            this.rightArm1.rotateAngleX = toRad(10.0F);
        }
    }

    private void addEye(boolean left) {
        ModelRenderer eye = new ModelRenderer(this, 34, 5);
        eye.mirror = !left;
        eye.addBox(left ? 0.0F : -1.0F, -1.0F, -1.0F, 1, 2, 2);
        eye.setRotationPoint(left ? 2.0F : -2.0F, -0.5F, -3.0F);
        this.head.addChild(eye);
    }

    private ModelRenderer addEar(boolean left) {
        ModelRenderer ear = new ModelRenderer(this, 0, 20);
        ear.mirror = !left;
        ear.addBox(left ? 0.0F : -2.0F, -2.0F, 0.0F, 2, 2, 0);
        ear.setRotationPoint(left ? 1.5F : -1.5F, -2.1F, -2.1F);
        setRotation(ear, -0.2345722F, left ? -0.3518584F : 0.3518584F, left ? 0.5082399F : -0.5082399F);
        this.head.addChild(ear);
        return ear;
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
