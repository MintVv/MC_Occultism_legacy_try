package com.occultism.occultism.client.model;

import com.occultism.occultism.entity.EntityMummyFamiliar;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelMummyFamiliar extends ModelBase {

    private final ModelRenderer body;
    private final ModelRenderer head;
    private final ModelRenderer leftArm1;
    private final ModelRenderer leftLeg1;
    private final ModelRenderer rightArm1;
    private final ModelRenderer rightLeg1;
    private final ModelRenderer crown;
    private final ModelRenderer tooth;
    private final ModelRenderer heka;
    private final ModelRenderer leftGlove;
    private final ModelRenderer rightGlove;
    private final ModelRenderer leftArm2;
    private final ModelRenderer rightArm2;
    private final ModelRenderer leftLeg2;
    private final ModelRenderer rightLeg2;

    public ModelMummyFamiliar() {
        this.textureWidth = 64;
        this.textureHeight = 32;

        this.body = new ModelRenderer(this, 0, 0);
        this.body.addBox(-2.5F, -7.0F, -1.5F, 5, 7, 3);
        this.body.setRotationPoint(0.0F, 19.0F, 0.0F);

        this.head = new ModelRenderer(this, 16, 0);
        this.head.addBox(-3.0F, -6.0F, -3.0F, 6, 6, 6);
        this.head.setRotationPoint(0.0F, -7.0F, 0.0F);
        this.body.addChild(this.head);

        this.leftArm1 = new ModelRenderer(this, 44, 0);
        this.leftArm1.addBox(0.0F, 0.0F, -1.0F, 2, 4, 2);
        this.leftArm1.setRotationPoint(2.51F, -7.0F, 0.0F);
        setRotation(this.leftArm1, -0.2736676F, 0.0F, 0.0F);
        this.body.addChild(this.leftArm1);

        this.rightArm1 = new ModelRenderer(this, 44, 0);
        this.rightArm1.mirror = true;
        this.rightArm1.addBox(-2.0F, 0.0F, -1.0F, 2, 4, 2);
        this.rightArm1.setRotationPoint(-2.49F, -7.0F, 0.0F);
        setRotation(this.rightArm1, -0.2736676F, 0.0F, 0.0F);
        this.body.addChild(this.rightArm1);

        this.leftLeg1 = new ModelRenderer(this, 40, 6);
        this.leftLeg1.addBox(-1.0F, 0.0F, -1.0F, 2, 3, 2);
        this.leftLeg1.setRotationPoint(1.49F, -0.5F, 0.0F);
        this.body.addChild(this.leftLeg1);

        this.rightLeg1 = new ModelRenderer(this, 40, 6);
        this.rightLeg1.mirror = true;
        this.rightLeg1.addBox(-1.0F, 0.0F, -1.0F, 2, 3, 2);
        this.rightLeg1.setRotationPoint(-1.51F, -0.5F, 0.0F);
        this.body.addChild(this.rightLeg1);

        ModelRenderer skeleton = new ModelRenderer(this, 0, 10);
        skeleton.addBox(-2.0F, -7.0F, -1.0F, 4, 7, 2);
        skeleton.setRotationPoint(0.0F, 0.0F, -0.01F);
        this.body.addChild(skeleton);

        ModelRenderer spine = new ModelRenderer(this, 0, 19);
        spine.addBox(-0.5F, -7.0F, -0.5F, 1, 7, 1);
        this.body.addChild(spine);

        ModelRenderer nose = new ModelRenderer(this, 34, 0);
        nose.addBox(-1.5F, -1.0F, -1.0F, 3, 2, 1);
        nose.setRotationPoint(0.0F, -1.0F, -3.0F);
        this.head.addChild(nose);

        ModelRenderer eyeSockets = new ModelRenderer(this, 52, 0);
        eyeSockets.addBox(-2.0F, -2.0F, 0.0F, 4, 3, 2);
        eyeSockets.setRotationPoint(0.0F, -3.0F, -3.0F);
        this.head.addChild(eyeSockets);

        this.addEye(true);
        this.addEye(false);

        this.crown = new ModelRenderer(this, 4, 22);
        this.crown.addBox(-5.0F, -10.0F, 0.0F, 10, 10, 0);
        this.crown.setRotationPoint(0.0F, 2.0F, -1.5F);
        setRotation(this.crown, -0.0781908F, 0.0F, 0.0F);
        this.head.addChild(this.crown);

        this.tooth = new ModelRenderer(this, 0, 31);
        this.tooth.addBox(-1.0F, 0.0F, 0.0F, 2, 1, 0);
        this.tooth.setRotationPoint(0.0F, 0.8F, -0.5F);
        nose.addChild(this.tooth);

        this.leftArm2 = new ModelRenderer(this, 48, 6);
        this.leftArm2.addBox(0.0F, 0.0F, -1.0F, 2, 4, 2);
        this.leftArm2.setRotationPoint(0.01F, 3.5F, 0.01F);
        setRotation(this.leftArm2, -0.4300491F, 0.0F, 0.0F);
        this.leftArm1.addChild(this.leftArm2);

        this.rightArm2 = new ModelRenderer(this, 48, 6);
        this.rightArm2.mirror = true;
        this.rightArm2.addBox(-2.0F, 0.0F, -1.0F, 2, 4, 2);
        this.rightArm2.setRotationPoint(-0.01F, 3.5F, 0.01F);
        setRotation(this.rightArm2, -0.4300491F, 0.0F, 0.0F);
        this.rightArm1.addChild(this.rightArm2);

        this.heka = new ModelRenderer(this, 24, 22);
        this.heka.addBox(0.0F, -2.0F, -6.0F, 0, 4, 6);
        this.heka.setRotationPoint(1.0F, 3.5F, -1.0F);
        this.leftArm2.addChild(this.heka);

        this.leftGlove = new ModelRenderer(this, 36, 26);
        this.leftGlove.addBox(-0.5F, -1.5F, -1.5F, 1, 3, 3);
        this.leftGlove.setRotationPoint(1.0F, 3.7F, 0.0F);
        this.leftArm2.addChild(this.leftGlove);

        this.rightGlove = new ModelRenderer(this, 36, 26);
        this.rightGlove.mirror = true;
        this.rightGlove.addBox(-0.5F, -1.5F, -1.5F, 1, 3, 3);
        this.rightGlove.setRotationPoint(-1.0F, 3.7F, 0.0F);
        this.rightArm2.addChild(this.rightGlove);

        this.leftLeg2 = new ModelRenderer(this, 40, 11);
        this.leftLeg2.addBox(-1.0F, 0.0F, -1.0F, 2, 3, 2);
        this.leftLeg2.setRotationPoint(0.01F, 2.5F, 0.01F);
        this.leftLeg1.addChild(this.leftLeg2);

        this.rightLeg2 = new ModelRenderer(this, 40, 11);
        this.rightLeg2.mirror = true;
        this.rightLeg2.addBox(-1.0F, 0.0F, -1.0F, 2, 3, 2);
        this.rightLeg2.setRotationPoint(-0.01F, 2.5F, 0.01F);
        this.rightLeg1.addChild(this.rightLeg2);

        this.addBandages();
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
        EntityMummyFamiliar mummy = (EntityMummyFamiliar) entityIn;
        this.crown.showModel = mummy.hasCrown();
        this.heka.showModel = mummy.hasHeka();
        this.tooth.showModel = mummy.hasTooth();
        this.leftGlove.showModel = mummy.hasBlacksmithUpgrade();
        this.rightGlove.showModel = mummy.hasBlacksmithUpgrade();

        setRotation(this.body, 0.0F, 0.0F, 0.0F);
        setRotation(this.head, headPitch * 0.017453292F, netHeadYaw * 0.017453292F, 0.0F);
        setRotation(this.rightArm1, -0.274F, 0.0F, 0.0F);
        setRotation(this.rightArm2, -0.43F, 0.0F, 0.0F);
        setRotation(this.leftArm1, -0.274F, 0.0F, 0.0F);
        setRotation(this.leftArm2, -0.43F, 0.0F, 0.0F);
        setRotation(this.rightLeg1, 0.0F, 0.0F, 0.0F);
        setRotation(this.rightLeg2, 0.0F, 0.0F, 0.0F);
        setRotation(this.leftLeg1, 0.0F, 0.0F, 0.0F);
        setRotation(this.leftLeg2, 0.0F, 0.0F, 0.0F);

        this.rightLeg1.rotateAngleX = MathHelper.cos(limbSwing * 0.5F + (float) Math.PI) * toRad(40.0F)
                * limbSwingAmount;
        this.leftLeg1.rotateAngleX = MathHelper.cos(limbSwing * 0.5F) * toRad(40.0F) * limbSwingAmount;
        this.rightLeg2.rotateAngleX = Math.abs(MathHelper.cos(limbSwing * 0.5F + (float) Math.PI))
                * toRad(40.0F) * limbSwingAmount;
        this.leftLeg2.rotateAngleX = Math.abs(MathHelper.cos(limbSwing * 0.5F) * toRad(40.0F))
                * limbSwingAmount;
        this.rightArm1.rotateAngleX = MathHelper.cos(limbSwing * 0.5F) * toRad(40.0F) * limbSwingAmount;
        this.leftArm1.rotateAngleX = MathHelper.cos(limbSwing * 0.5F + (float) Math.PI) * toRad(40.0F)
                * limbSwingAmount;
        this.rightArm2.rotateAngleX = toRad(-30.0F) + MathHelper.cos(limbSwing * 0.5F) * toRad(20.0F)
                * limbSwingAmount;
        this.leftArm2.rotateAngleX = toRad(-30.0F) + MathHelper.cos(limbSwing * 0.5F + (float) Math.PI)
                * toRad(20.0F) * limbSwingAmount;

        if (mummy.isSitting()) {
            this.head.rotateAngleX = toRad(40.0F);
            this.head.rotateAngleY = toRad(-20.0F);
            this.body.rotateAngleX = toRad(35.0F);
            this.leftArm1.rotateAngleX = toRad(-50.0F);
            this.leftArm2.rotateAngleX = toRad(10.0F);
            this.rightArm1.rotateAngleX = toRad(-50.0F);
            this.rightArm2.rotateAngleX = toRad(10.0F);
            this.leftLeg1.rotateAngleX = toRad(-35.0F);
            this.rightLeg1.rotateAngleX = toRad(-35.0F);
        }
    }

    private void addEye(boolean left) {
        ModelRenderer eye = new ModelRenderer(this, 13, 0);
        eye.mirror = !left;
        eye.addBox(-0.5F, -0.5F, -0.5F, 1, 1, 1);
        eye.setRotationPoint(left ? 1.0F : -1.0F, -3.0F, -2.0F);
        this.head.addChild(eye);
    }

    private void addBandages() {
        ModelRenderer leftArmBandage = new ModelRenderer(this, 44, 28);
        leftArmBandage.addBox(0.0F, 0.0F, -1.0F, 0, 2, 2);
        leftArmBandage.setRotationPoint(1.5F, 1.6F, 1.7F);
        setRotation(leftArmBandage, 0.0F, 0.0001745F, 2.9717722F);
        this.leftArm1.addChild(leftArmBandage);

        ModelRenderer rightArmBandage = new ModelRenderer(this, 44, 28);
        rightArmBandage.addBox(0.0F, 0.0F, -1.0F, 0, 2, 2);
        rightArmBandage.setRotationPoint(-1.0F, 2.6F, 1.7F);
        setRotation(rightArmBandage, 0.0390954F, 0.2347468F, 2.7263789F);
        this.rightArm2.addChild(rightArmBandage);

        ModelRenderer leftLegBandage = new ModelRenderer(this, 44, 28);
        leftLegBandage.addBox(0.0F, 0.0F, -1.0F, 0, 2, 2);
        leftLegBandage.setRotationPoint(0.0F, 0.5F, 1.5F);
        setRotation(leftLegBandage, 0.0F, 0.2736676F, 0.1563815F);
        this.leftLeg1.addChild(leftLegBandage);

        ModelRenderer rightLegBandage = new ModelRenderer(this, 44, 28);
        rightLegBandage.addBox(0.0F, 0.0F, -1.0F, 0, 2, 2);
        rightLegBandage.setRotationPoint(-0.5F, 1.9F, 1.5F);
        setRotation(rightLegBandage, 0.0781908F, -0.0390954F, -1.9957840F);
        this.rightLeg2.addChild(rightLegBandage);
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
