package com.occultism.occultism.client.model;

import com.occultism.occultism.entity.EntityHeadlessFamiliar;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelHeadlessFamiliar extends ModelBase {

    private final ModelRenderer ratBody;
    private final ModelRenderer ratChest;
    private final ModelRenderer ratHead;
    private final ModelRenderer ratTail;
    private final ModelRenderer backLeftLeg;
    private final ModelRenderer backRightLeg;
    private final ModelRenderer frontLeftLeg;
    private final ModelRenderer frontRightLeg;
    private final ModelRenderer ratHair1;
    private final ModelRenderer ratHair2;
    private final ModelRenderer ratHair3;
    private final ModelRenderer ratGlasses;
    private final ModelRenderer riderBody;
    private final ModelRenderer leftLeg;
    private final ModelRenderer rightLeg;
    private final ModelRenderer leftArm;
    private final ModelRenderer rightArm;
    private final ModelRenderer collar;
    private final ModelRenderer helmet;
    private final ModelRenderer pumpkin;

    public ModelHeadlessFamiliar() {
        this.textureWidth = 64;
        this.textureHeight = 64;

        this.ratBody = new ModelRenderer(this, 0, 0);
        this.ratBody.addBox(-3.0F, -3.0F, -3.0F, 6, 4, 6);
        this.ratBody.setRotationPoint(0.0F, 19.4F, 3.0F);
        setRotation(this.ratBody, -0.0781908F, 0.0F, 0.0F);

        this.ratChest = new ModelRenderer(this, 24, 0);
        this.ratChest.addBox(-2.5F, -2.5F, -2.5F, 5, 4, 5);
        this.ratChest.setRotationPoint(0.0F, -0.1F, -5.0F);
        setRotation(this.ratChest, 0.1563815F, 0.0F, 0.0F);
        this.ratBody.addChild(this.ratChest);

        this.ratHead = new ModelRenderer(this, 44, 0);
        this.ratHead.addBox(-1.5F, -1.5F, -3.0F, 3, 3, 3);
        this.ratHead.setRotationPoint(0.0F, -0.5F, -2.3F);
        this.ratChest.addChild(this.ratHead);

        ModelRenderer nose = new ModelRenderer(this, 18, 0);
        nose.addBox(-1.0F, -1.0F, -2.0F, 2, 2, 2);
        nose.setRotationPoint(0.0F, 0.2F, -3.0F);
        this.ratHead.addChild(nose);

        ModelRenderer leftEar = new ModelRenderer(this, 0, 24);
        leftEar.addBox(-1.0F, -1.0F, 0.0F, 2, 2, 0);
        leftEar.setRotationPoint(1.5F, -1.5F, -1.5F);
        setRotation(leftEar, 0.0F, 0.0F, 0.5082399F);
        this.ratHead.addChild(leftEar);

        ModelRenderer rightEar = new ModelRenderer(this, 0, 24);
        rightEar.mirror = true;
        rightEar.addBox(-1.0F, -1.0F, 0.0F, 2, 2, 0);
        rightEar.setRotationPoint(-1.5F, -1.5F, -1.5F);
        setRotation(rightEar, 0.0F, 0.0F, -0.5082399F);
        this.ratHead.addChild(rightEar);

        ModelRenderer teeth = new ModelRenderer(this, 0, 17);
        teeth.addBox(-1.0F, 0.0F, -1.0F, 1, 1, 1);
        teeth.setRotationPoint(0.5F, 0.5F, -2.0F);
        nose.addChild(teeth);

        this.ratGlasses = new ModelRenderer(this, 0, 22);
        this.ratGlasses.addBox(-2.5F, 0.0F, 0.0F, 5, 2, 0);
        this.ratGlasses.setRotationPoint(0.0F, -2.8F, -0.2F);
        nose.addChild(this.ratGlasses);

        this.backLeftLeg = this.addRatLeg(this.ratBody, 3.0F, 0.5F, 0.5F, false, 0.3127630F);
        this.backRightLeg = this.addRatLeg(this.ratBody, -3.0F, 0.5F, 0.5F, true, 0.3127630F);
        this.frontLeftLeg = this.addRatLeg(this.ratChest, 2.5F, 0.8F, -0.5F, false, -0.1954769F);
        this.frontRightLeg = this.addRatLeg(this.ratChest, -2.5F, 0.8F, -0.5F, true, -0.1954769F);

        this.ratTail = new ModelRenderer(this, 48, 6);
        this.ratTail.addBox(-1.0F, -1.0F, 0.0F, 2, 2, 5);
        this.ratTail.setRotationPoint(0.0F, -0.5F, 2.5F);
        setRotation(this.ratTail, -0.3518584F, 0.0F, 0.0F);
        this.ratBody.addChild(this.ratTail);

        this.ratHair1 = addHair(this.ratBody, 2.5F, -2.0F, 0.0F, false, 0.2345722F, 0.1172861F,
                0.5082399F);
        this.ratHair2 = addHair(this.ratBody, -2.5F, -1.3F, 0.0F, true, 0.4300491F, 0.1172861F,
                -0.1174607F);
        this.ratHair3 = addHair(this.ratChest, 0.2F, -1.9F, -0.2F, false, 0.5082399F, 0.0F,
                -1.4213962F);

        this.riderBody = new ModelRenderer(this, 30, 9);
        this.riderBody.addBox(-2.5F, -6.0F, -1.5F, 5, 6, 3);
        this.riderBody.setRotationPoint(0.0F, -4.0F, -1.0F);
        this.ratBody.addChild(this.riderBody);

        this.leftLeg = new ModelRenderer(this, 0, 10);
        this.leftLeg.addBox(-1.0F, 0.0F, -1.0F, 2, 5, 2);
        this.leftLeg.setRotationPoint(1.5F, 0.0F, 0.0F);
        setRotation(this.leftLeg, -1.4144148F, -0.3518584F, 0.0F);
        this.riderBody.addChild(this.leftLeg);

        this.rightLeg = new ModelRenderer(this, 0, 10);
        this.rightLeg.mirror = true;
        this.rightLeg.addBox(-1.0F, 0.0F, -1.0F, 2, 5, 2);
        this.rightLeg.setRotationPoint(-1.5F, 0.0F, 0.0F);
        setRotation(this.rightLeg, -1.4144148F, 0.3518584F, 0.0F);
        this.riderBody.addChild(this.rightLeg);

        this.leftArm = new ModelRenderer(this, 8, 10);
        this.leftArm.addBox(0.0F, 0.0F, -1.0F, 2, 5, 2);
        this.leftArm.setRotationPoint(2.3F, -5.01F, 0.0F);
        setRotation(this.leftArm, -1.2901474F, -0.3909537F, 0.0F);
        this.riderBody.addChild(this.leftArm);

        this.rightArm = new ModelRenderer(this, 8, 10);
        this.rightArm.mirror = true;
        this.rightArm.addBox(-2.0F, 0.0F, -1.0F, 2, 5, 2);
        this.rightArm.setRotationPoint(-2.3F, -5.01F, 0.0F);
        setRotation(this.rightArm, -1.5707963F, 0.0F, 0.0F);
        this.riderBody.addChild(this.rightArm);

        ModelRenderer mantle = new ModelRenderer(this, 16, 25);
        mantle.addBox(-4.5F, 0.0F, 0.0F, 9, 7, 0);
        mantle.setRotationPoint(0.0F, -6.0F, 1.5F);
        setRotation(mantle, 0.4691445F, 0.0F, 0.0F);
        this.riderBody.addChild(mantle);

        this.collar = new ModelRenderer(this, 13, 14);
        this.collar.addBox(-1.5F, -1.0F, -1.5F, 3, 1, 3);
        this.collar.setRotationPoint(0.0F, -6.0F, 0.0F);
        this.riderBody.addChild(this.collar);

        this.helmet = new ModelRenderer(this, 0, 32);
        this.helmet.addBox(-2.5F, -6.0F, -2.5F, 5, 6, 5);
        this.helmet.setRotationPoint(0.0F, -6.0F, 0.0F);
        this.riderBody.addChild(this.helmet);

        this.pumpkin = new ModelRenderer(this, 44, 22);
        this.pumpkin.addBox(-2.5F, -2.5F, -2.5F, 5, 5, 5);
        this.pumpkin.setRotationPoint(1.0F, 5.0F, -3.51F);
        setRotation(this.pumpkin, 1.5868533F, 0.0F, 0.2736676F);
        this.leftArm.addChild(this.pumpkin);
    }

    @Override
    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
            float headPitch, float scale) {
        this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);
        this.ratBody.render(scale);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
            float headPitch, float scaleFactor, Entity entityIn) {
        EntityHeadlessFamiliar headless = (EntityHeadlessFamiliar) entityIn;
        this.ratGlasses.showModel = headless.hasGlasses();
        this.ratHair1.showModel = headless.isHairy();
        this.ratHair2.showModel = headless.isHairy();
        this.ratHair3.showModel = headless.isHairy();
        this.riderBody.showModel = !headless.isHeadlessDead();
        this.pumpkin.showModel = !headless.hasHead();
        this.helmet.showModel = headless.hasBlacksmithUpgrade();

        setRotation(this.ratBody, -0.0781908F, 0.0F, 0.0F);
        setRotation(this.ratTail, -0.3518584F, 0.0F, 0.0F);
        setRotation(this.riderBody, 0.0F, 0.0F, 0.0F);
        setRotation(this.ratHead, headPitch * 0.017453292F * 0.4F, netHeadYaw * 0.017453292F * 0.4F, 0.0F);
        setRotation(this.rightArm, -1.5707963F, 0.0F, 0.0F);
        setRotation(this.leftArm, -1.2901474F, -0.3909537F, 0.0F);
        setRotation(this.leftLeg, -1.4144148F, -0.3518584F, 0.0F);
        setRotation(this.rightLeg, -1.4144148F, 0.3518584F, 0.0F);

        this.ratTail.rotateAngleY = MathHelper.sin(ageInTicks * 0.2F) * toRad(15.0F);
        this.backLeftLeg.rotateAngleX = 0.31F + MathHelper.cos(limbSwing * 0.7F + (float) Math.PI)
                * limbSwingAmount * 0.5F;
        this.backRightLeg.rotateAngleX = 0.31F + MathHelper.cos(limbSwing * 0.7F) * limbSwingAmount * 0.5F;
        this.frontLeftLeg.rotateAngleX = -0.20F + MathHelper.cos(limbSwing * 0.7F) * limbSwingAmount * 0.5F;
        this.frontRightLeg.rotateAngleX = -0.20F + MathHelper.cos(limbSwing * 0.7F + (float) Math.PI)
                * limbSwingAmount * 0.5F;
        this.rightArm.rotateAngleX = -1.57F + MathHelper.cos(limbSwing * 0.4F) * limbSwingAmount * 0.2F;

        if (headless.isSitting()) {
            this.ratBody.rotateAngleX = -toRad(40.0F);
            this.ratHead.rotateAngleX += toRad(20.0F);
            this.backLeftLeg.rotateAngleX = -toRad(10.0F);
            this.backRightLeg.rotateAngleX = -toRad(10.0F);
            this.frontLeftLeg.rotateAngleX = toRad(25.0F);
            this.frontRightLeg.rotateAngleX = toRad(25.0F);
            this.ratTail.rotateAngleX = toRad(35.0F);
            this.riderBody.rotateAngleX = toRad(20.0F);
        }
    }

    public ModelRenderer getRatBody() {
        return this.ratBody;
    }

    public ModelRenderer getRiderBody() {
        return this.riderBody;
    }

    public ModelRenderer getLeftArm() {
        return this.leftArm;
    }

    public ModelRenderer getRightArm() {
        return this.rightArm;
    }

    private ModelRenderer addRatLeg(ModelRenderer parent, float x, float y, float z, boolean mirror,
            float xRot) {
        ModelRenderer leg = new ModelRenderer(this, 56, 3);
        leg.mirror = mirror;
        leg.addBox(-0.5F, 0.0F, -1.0F, 1, 2, 2);
        leg.setRotationPoint(x, y, z);
        setRotation(leg, xRot, 0.0F, 0.0F);
        parent.addChild(leg);
        return leg;
    }

    private ModelRenderer addHair(ModelRenderer parent, float x, float y, float z, boolean mirror, float xRot,
            float yRot, float zRot) {
        ModelRenderer hair = new ModelRenderer(this, 4, 26);
        hair.mirror = mirror;
        hair.addBox(mirror ? -3.0F : 0.0F, 0.0F, -3.0F, 3, 0, 6);
        hair.setRotationPoint(x, y, z);
        setRotation(hair, xRot, yRot, zRot);
        parent.addChild(hair);
        return hair;
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
