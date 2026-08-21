package com.occultism.occultism.client.model;

import com.occultism.occultism.entity.EntityCthulhuFamiliar;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelCthulhuFamiliar extends ModelBase {

    private static final float PI = (float) Math.PI;

    private final ModelRenderer body;
    private final ModelRenderer head;
    private final ModelRenderer leftLeg;
    private final ModelRenderer leftArm;
    private final ModelRenderer leftWing;
    private final ModelRenderer rightLeg;
    private final ModelRenderer rightArm;
    private final ModelRenderer rightWing;
    private final ModelRenderer leftEye;
    private final ModelRenderer rightEye;
    private final ModelRenderer leftEar;
    private final ModelRenderer rightEar;
    private final ModelRenderer tentacle1;
    private final ModelRenderer tentacle2;
    private final ModelRenderer tentacle3;
    private final ModelRenderer hat1;
    private final ModelRenderer trunk1;
    private final ModelRenderer trunk2;
    private final ModelRenderer trunk3;
    private final ModelRenderer lantern1;

    public ModelCthulhuFamiliar() {
        this.textureWidth = 64;
        this.textureHeight = 32;

        this.body = new ModelRenderer(this, 0, 0);
        this.body.addBox(-3.5F, -6.0F, -1.5F, 7, 7, 3);
        this.body.setRotationPoint(0.0F, 17.0F, 0.0F);
        setRotation(this.body, 0.4391248F, 0.0F, 0.0F);

        this.head = new ModelRenderer(this, 20, 0);
        this.head.addBox(-3.0F, -5.0F, -3.2F, 6, 5, 6);
        this.head.setRotationPoint(0.0F, -5.8F, -0.3F);
        setRotation(this.head, -0.2628466F, 0.0F, 0.0F);
        this.body.addChild(this.head);

        this.leftLeg = new ModelRenderer(this, 44, 0);
        this.leftLeg.addBox(-1.0F, 0.0F, -1.0F, 2, 5, 2);
        this.leftLeg.setRotationPoint(2.0F, 0.5F, 0.0F);
        setRotation(this.leftLeg, -0.3942699F, 0.0F, 0.0F);
        this.body.addChild(this.leftLeg);

        this.leftArm = new ModelRenderer(this, 52, 0);
        this.leftArm.addBox(0.0F, -1.0F, -1.0F, 2, 5, 2);
        this.leftArm.setRotationPoint(3.2F, -4.5F, 0.0F);
        setRotation(this.leftArm, -1.6891296F, 0.0F, 0.0F);
        this.body.addChild(this.leftArm);

        ModelRenderer tail = new ModelRenderer(this, 0, 22);
        tail.addBox(0.0F, -1.0F, 0.0F, 0, 5, 5);
        tail.setRotationPoint(0.0F, -2.0F, 1.0F);
        setRotation(tail, 0.6284930F, 0.0F, 0.0F);
        this.body.addChild(tail);

        this.leftWing = new ModelRenderer(this, 20, 21);
        this.leftWing.addBox(0.0F, -8.0F, 0.0F, 8, 8, 0);
        this.leftWing.setRotationPoint(2.4F, -5.0F, 1.4F);
        setRotation(this.leftWing, -0.1314233F, -0.4291765F, 0.5256932F);
        this.body.addChild(this.leftWing);

        this.rightLeg = new ModelRenderer(this, 44, 0);
        this.rightLeg.mirror = true;
        this.rightLeg.addBox(-1.0F, 0.0F, -1.0F, 2, 5, 2);
        this.rightLeg.setRotationPoint(-2.0F, 0.5F, 0.0F);
        setRotation(this.rightLeg, -0.3942699F, 0.0F, 0.0F);
        this.body.addChild(this.rightLeg);

        this.rightArm = new ModelRenderer(this, 52, 0);
        this.rightArm.mirror = true;
        this.rightArm.addBox(-2.0F, -1.0F, -1.0F, 2, 5, 2);
        this.rightArm.setRotationPoint(-3.2F, -4.5F, 0.0F);
        setRotation(this.rightArm, -0.4380777F, 0.0F, 0.0F);
        this.body.addChild(this.rightArm);

        this.rightWing = new ModelRenderer(this, 20, 21);
        this.rightWing.mirror = true;
        this.rightWing.addBox(-8.0F, -8.0F, 0.0F, 8, 8, 0);
        this.rightWing.setRotationPoint(-2.4F, -5.0F, 1.4F);
        setRotation(this.rightWing, -0.1314233F, 0.4291765F, -0.5256932F);
        this.body.addChild(this.rightWing);

        ModelRenderer hair = new ModelRenderer(this, 0, 0);
        hair.addBox(0.0F, -7.0F, -3.2F, 0, 7, 10);
        hair.setRotationPoint(0.0F, -2.0F, 0.0F);
        this.head.addChild(hair);

        this.leftEye = new ModelRenderer(this, 26, 12);
        this.leftEye.addBox(-1.0F, -1.0F, 0.0F, 2, 2, 2);
        this.leftEye.setRotationPoint(1.3F, -3.0F, -3.7F);
        this.head.addChild(this.leftEye);

        this.rightEye = new ModelRenderer(this, 26, 12);
        this.rightEye.addBox(-1.0F, -1.0F, 0.0F, 2, 2, 2);
        this.rightEye.setRotationPoint(-1.3F, -3.0F, -3.7F);
        this.head.addChild(this.rightEye);

        this.leftEar = new ModelRenderer(this, 0, 21);
        this.leftEar.addBox(0.0F, 0.0F, 0.0F, 3, 4, 0);
        this.leftEar.setRotationPoint(3.0F, -4.5F, -1.0F);
        this.head.addChild(this.leftEar);

        this.tentacle1 = new ModelRenderer(this, 0, 17);
        this.tentacle1.addBox(-0.5F, 0.0F, -1.0F, 1, 3, 1);
        this.tentacle1.setRotationPoint(-0.1F, -1.1F, -2.8F);
        setRotation(this.tentacle1, -0.1933825F, 0.0F, 0.5759587F);
        this.head.addChild(this.tentacle1);

        this.tentacle2 = new ModelRenderer(this, 0, 17);
        this.tentacle2.addBox(-0.5F, 0.0F, -1.0F, 1, 3, 1);
        this.tentacle2.setRotationPoint(0.0F, -1.4F, -2.8F);
        setRotation(this.tentacle2, -0.1933825F, 0.0F, 0.0F);
        this.head.addChild(this.tentacle2);

        this.tentacle3 = new ModelRenderer(this, 0, 17);
        this.tentacle3.addBox(-0.5F, 0.0F, -1.0F, 1, 3, 1);
        this.tentacle3.setRotationPoint(0.1F, -1.1F, -2.8F);
        setRotation(this.tentacle3, -0.1933825F, 0.0F, -0.5759587F);
        this.head.addChild(this.tentacle3);

        this.rightEar = new ModelRenderer(this, 0, 21);
        this.rightEar.mirror = true;
        this.rightEar.addBox(-3.0F, 0.0F, 0.0F, 3, 4, 0);
        this.rightEar.setRotationPoint(-3.0F, -4.5F, -1.0F);
        this.head.addChild(this.rightEar);

        this.hat1 = new ModelRenderer(this, 20, 16);
        this.hat1.addBox(-2.0F, 0.0F, -2.0F, 4, 1, 4);
        this.hat1.setRotationPoint(2.5F, -5.5F, -3.0F);
        setRotation(this.hat1, 0.2736676F, -0.0390954F, 0.2345722F);
        this.head.addChild(this.hat1);

        this.trunk1 = new ModelRenderer(this, 44, 7);
        this.trunk1.addBox(-1.5F, 0.0F, -1.0F, 3, 2, 2);
        this.trunk1.setRotationPoint(0.0F, -1.1F, -2.2F);
        setRotation(this.trunk1, -0.8600982F, 0.0F, 0.0F);
        this.head.addChild(this.trunk1);

        ModelRenderer hat2 = new ModelRenderer(this, 36, 16);
        hat2.addBox(-2.0F, 0.0F, -2.0F, 3, 2, 3);
        hat2.setRotationPoint(0.5F, -2.0F, 0.5F);
        this.hat1.addChild(hat2);

        this.trunk2 = new ModelRenderer(this, 44, 11);
        this.trunk2.addBox(-1.0F, 0.0F, -1.0F, 2, 2, 2);
        this.trunk2.setRotationPoint(0.0F, 1.5F, 0.0F);
        setRotation(this.trunk2, -0.4691445F, 0.0F, 0.0F);
        this.trunk1.addChild(this.trunk2);

        this.trunk3 = new ModelRenderer(this, 54, 7);
        this.trunk3.addBox(-0.5F, 0.0F, -1.0F, 1, 2, 2);
        this.trunk3.setRotationPoint(0.0F, 1.5F, 0.0F);
        setRotation(this.trunk3, 0.3909537F, 0.0F, 0.0F);
        this.trunk2.addChild(this.trunk3);

        this.lantern1 = new ModelRenderer(this, 58, 29);
        this.lantern1.addBox(-1.5F, 0.0F, 0.0F, 3, 3, 0);
        this.lantern1.setRotationPoint(1.0F, 3.5F, -0.5F);
        setRotation(this.lantern1, 1.2901474F, 0.0F, 0.0F);
        this.leftArm.addChild(this.lantern1);

        ModelRenderer lantern2 = new ModelRenderer(this, 42, 23);
        lantern2.addBox(-2.0F, 0.0F, -2.0F, 4, 5, 4);
        lantern2.setRotationPoint(0.0F, 2.5F, 0.0F);
        setRotation(lantern2, 0.0F, 0.7853982F, 0.0F);
        this.lantern1.addChild(lantern2);

        ModelRenderer lantern3 = new ModelRenderer(this, 34, 27);
        lantern3.addBox(-1.0F, -3.0F, -1.0F, 2, 3, 2);
        lantern3.setRotationPoint(0.0F, 4.99F, 0.0F);
        setRotation(lantern3, 0.0F, -0.7853982F, 0.0F);
        lantern2.addChild(lantern3);

        ModelRenderer lantern4 = new ModelRenderer(this, 0, 0);
        lantern4.addBox(-0.5F, -0.5F, 0.0F, 1, 1, 0);
        lantern4.setRotationPoint(0.0F, -3.3F, 0.0F);
        setRotation(lantern4, 0.0F, 0.0F, 0.7853982F);
        lantern3.addChild(lantern4);
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
        EntityCthulhuFamiliar cthulhu = (EntityCthulhuFamiliar) entityIn;
        this.showModels(cthulhu);

        setRotation(this.body, 0.4391248F, 0.0F, 0.0F);
        setRotation(this.head, headPitch * toRads(0.7F) - 0.2628466F,
                netHeadYaw * toRads(0.7F), 0.0F);
        setRotation(this.leftLeg, -0.3942699F, 0.0F, 0.0F);
        setRotation(this.rightLeg, -0.3942699F, 0.0F, 0.0F);
        setRotation(this.leftArm, -0.4380777F, 0.0F, 0.0F);
        setRotation(this.rightArm, -0.4380777F, 0.0F, 0.0F);
        setRotation(this.leftWing, -0.1314233F, -0.4291765F, 0.5256932F);
        setRotation(this.rightWing, -0.1314233F, 0.4291765F, -0.5256932F);
        setRotation(this.leftEye, 0.0F, 0.0F, 0.0F);
        setRotation(this.rightEye, 0.0F, 0.0F, 0.0F);
        setRotation(this.leftEar, 0.0F, 0.0F, 0.0F);
        setRotation(this.rightEar, 0.0F, 0.0F, 0.0F);
        setRotation(this.tentacle1, -0.1933825F, 0.0F, 0.5759587F);
        setRotation(this.tentacle2, -0.1933825F, 0.0F, 0.0F);
        setRotation(this.tentacle3, -0.1933825F, 0.0F, -0.5759587F);
        setRotation(this.trunk1, -0.8600982F, 0.0F, 0.0F);
        setRotation(this.trunk2, -0.4691445F, 0.0F, 0.0F);
        setRotation(this.trunk3, 0.3909537F, 0.0F, 0.0F);
        setRotation(this.lantern1, 1.2901474F, 0.0F, 0.0F);
        this.lantern1.setRotationPoint(1.0F, 3.5F, -0.5F);

        if (cthulhu.isSitting()) {
            this.rightArm.rotateAngleX = 0.0F;
            this.leftArm.rotateAngleX = 0.0F;
            this.rightLeg.rotateAngleX = -PI / 2.0F;
            this.leftLeg.rotateAngleX = -PI / 2.0F;
            this.body.rotateAngleX = 0.0F;
        } else {
            float armSwing = MathHelper.cos(limbSwing * 0.5F + PI) * limbSwingAmount * 0.2F;
            float legSwing = MathHelper.cos(limbSwing * 0.5F) * 1.4F * limbSwingAmount * 0.2F;
            this.rightArm.rotateAngleX = armSwing - 0.4380777F;
            this.leftArm.rotateAngleX = armSwing - 0.4380777F;
            this.rightLeg.rotateAngleX = legSwing - 0.3942699F;
            this.leftLeg.rotateAngleX = legSwing - 0.3942699F;
            this.body.rotateAngleX = cthulhu.isInWater() ? 1.0F : 0.4391248F;
        }

        if (cthulhu.isAngry()) {
            this.leftEye.rotateAngleZ = -toRads(45.0F);
            this.rightEye.rotateAngleZ = -toRads(45.0F);
            this.leftEar.rotateAngleZ = toRads(20.0F);
            this.rightEar.rotateAngleZ = -toRads(20.0F);
        }

        if (cthulhu.isGiving()) {
            this.leftArm.rotateAngleY = toRads(40.0F);
            this.rightArm.rotateAngleY = -toRads(40.0F);
            this.leftArm.rotateAngleX -= toRads(40.0F);
            this.rightArm.rotateAngleX -= toRads(40.0F);
        }

        float faceAnimation = MathHelper.cos(ageInTicks / 10.0F);
        this.trunk1.rotateAngleX += faceAnimation * 0.15F;
        this.trunk2.rotateAngleX += faceAnimation * 0.15F;
        this.trunk3.rotateAngleX += faceAnimation * 0.15F;
        this.tentacle1.rotateAngleZ += faceAnimation * 0.07F;
        this.tentacle3.rotateAngleZ -= faceAnimation * 0.07F;

        if (cthulhu.hasBlacksmithUpgrade()) {
            if (cthulhu.isSitting()) {
                this.lantern1.rotateAngleX = 0.0F;
                this.lantern1.setRotationPoint(3.0F, -1.6F, -5.0F);
            } else {
                this.leftArm.rotateAngleX = MathHelper.cos(limbSwing * 0.4F + PI)
                        * limbSwingAmount * 0.2F + toRads(-100.0F);
                this.lantern1.rotateAngleY = MathHelper.cos(ageInTicks * 0.2F) * toRads(5.0F);
                this.lantern1.rotateAngleZ = -MathHelper.cos(ageInTicks * 0.2F) * toRads(5.0F);
                this.lantern1.rotateAngleX = toRads(74.0F);
            }
        }

        if (cthulhu.isBeingRidden()) {
            float partialTicks = ageInTicks - cthulhu.ticksExisted;
            this.rightArm.rotateAngleX = toRads(40.0F - cthulhu.getAnimationHeight(partialTicks) * 15.0F);
        }

        if (cthulhu.isSitting()) {
            this.leftWing.rotateAngleY = -0.4291765F;
            this.rightWing.rotateAngleY = 0.4291765F;
        } else {
            float wingAnimation = MathHelper.cos(ageInTicks / 5.0F) * toRads(20.0F);
            this.leftWing.rotateAngleY = wingAnimation - 0.4291765F;
            this.rightWing.rotateAngleY = -wingAnimation + 0.4291765F;
        }
    }

    private void showModels(EntityCthulhuFamiliar cthulhu) {
        boolean hasTrunk = cthulhu.hasTrunk();
        this.hat1.showModel = cthulhu.hasHat();
        this.lantern1.showModel = cthulhu.hasBlacksmithUpgrade();
        this.trunk1.showModel = hasTrunk;
        this.tentacle1.showModel = !hasTrunk;
        this.tentacle2.showModel = !hasTrunk;
        this.tentacle3.showModel = !hasTrunk;
    }

    private static void setRotation(ModelRenderer renderer, float x, float y, float z) {
        renderer.rotateAngleX = x;
        renderer.rotateAngleY = y;
        renderer.rotateAngleZ = z;
    }

    private static float toRads(float degrees) {
        return PI / 180.0F * degrees;
    }
}
