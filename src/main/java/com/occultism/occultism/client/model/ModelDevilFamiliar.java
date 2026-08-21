package com.occultism.occultism.client.model;

import com.occultism.occultism.entity.EntityDevilFamiliar;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelDevilFamiliar extends ModelBase {

    private final ModelRenderer all;
    private final ModelRenderer torso;
    private final ModelRenderer head;
    private final ModelRenderer arms;
    private final ModelRenderer leftArm;
    private final ModelRenderer rightArm;
    private final ModelRenderer legs;
    private final ModelRenderer leftLeg;
    private final ModelRenderer rightLeg;
    private final ModelRenderer leftFoot;
    private final ModelRenderer rightFoot;

    public ModelDevilFamiliar() {
        this.textureWidth = 64;
        this.textureHeight = 64;

        this.all = new ModelRenderer(this, 0, 0);
        this.all.setRotationPoint(0.0F, 22.0F, 0.0F);

        this.torso = new ModelRenderer(this, 0, 0);
        this.torso.addBox(-3.0F, -10.0F, -2.0F, 6, 4, 4);
        this.torso.setRotationPoint(0.0F, 0.0F, 1.0F);
        setRotation(this.torso, toRad(27.5F), 0.0F, 0.0F);
        this.all.addChild(this.torso);

        ModelRenderer lowerTorso = new ModelRenderer(this, 0, 8);
        lowerTorso.addBox(-2.0F, -7.0F, -1.2F, 4, 6, 3);
        this.torso.addChild(lowerTorso);

        this.head = new ModelRenderer(this, 11, 14);
        this.head.addBox(-1.5F, -3.5F, -1.5F, 3, 4, 3);
        this.head.setRotationPoint(0.0F, -10.0F, 0.0F);
        setRotation(this.head, toRad(-27.5F), 0.0F, 0.0F);
        this.torso.addChild(this.head);

        ModelRenderer leftHorn = new ModelRenderer(this, 16, 0);
        leftHorn.addBox(-0.5F, -2.5F, -0.5F, 1, 3, 1);
        leftHorn.setRotationPoint(1.0F, -3.4F, -0.4F);
        setRotation(leftHorn, toRad(32.5F), toRad(-15.0F), 0.0F);
        this.head.addChild(leftHorn);

        ModelRenderer rightHorn = new ModelRenderer(this, 0, 0);
        rightHorn.addBox(-0.5F, -2.5F, -0.5F, 1, 3, 1);
        rightHorn.setRotationPoint(-1.0F, -3.4F, -0.4F);
        setRotation(rightHorn, toRad(32.5F), toRad(15.0F), 0.0F);
        this.head.addChild(rightHorn);

        ModelRenderer leftEar = new ModelRenderer(this, 1, 1);
        leftEar.addBox(0.0F, -2.0F, -0.3F, 0, 2, 1);
        leftEar.setRotationPoint(1.4F, -1.7F, 0.6F);
        setRotation(leftEar, toRad(122.5F), toRad(-15.0F), 0.0F);
        this.head.addChild(leftEar);

        ModelRenderer rightEar = new ModelRenderer(this, 17, 1);
        rightEar.addBox(0.0F, -2.0F, -0.3F, 0, 2, 1);
        rightEar.setRotationPoint(-1.4F, -1.7F, 0.6F);
        setRotation(rightEar, toRad(122.5F), toRad(15.0F), 0.0F);
        this.head.addChild(rightEar);

        this.arms = new ModelRenderer(this, 0, 0);
        this.arms.setRotationPoint(0.0F, -5.8F, -1.1F);
        this.all.addChild(this.arms);

        this.leftArm = new ModelRenderer(this, 6, 18);
        this.leftArm.addBox(0.0F, -0.2F, -1.0F, 2, 7, 2);
        this.leftArm.setRotationPoint(2.8F, -0.2F, 0.0F);
        setRotation(this.leftArm, 0.0F, 0.0F, toRad(-12.5F));
        this.arms.addChild(this.leftArm);

        this.rightArm = new ModelRenderer(this, 0, 17);
        this.rightArm.mirror = true;
        this.rightArm.addBox(-2.0F, -0.2F, -1.0F, 2, 7, 2);
        this.rightArm.setRotationPoint(-2.8F, -0.2F, 0.0F);
        setRotation(this.rightArm, 0.0F, 0.0F, toRad(12.5F));
        this.arms.addChild(this.rightArm);

        this.legs = new ModelRenderer(this, 0, 0);
        this.legs.setRotationPoint(0.0F, 0.0F, 0.9F);
        setRotation(this.legs, toRad(-25.0F), 0.0F, 0.0F);
        this.all.addChild(this.legs);

        this.leftLeg = new ModelRenderer(this, 12, 20);
        this.leftLeg.addBox(-1.0F, -5.0F, -1.0F, 2, 5, 2);
        this.leftLeg.setRotationPoint(1.7F, -0.2F, 0.0F);
        setRotation(this.leftLeg, 0.0F, 0.0F, toRad(-5.0F));
        this.legs.addChild(this.leftLeg);

        this.leftFoot = new ModelRenderer(this, 20, 20);
        this.leftFoot.addBox(-1.1F, -0.2F, -0.4F, 2, 4, 3);
        this.leftFoot.setRotationPoint(0.0F, -0.2F, 0.0F);
        setRotation(this.leftFoot, toRad(58.0F), 0.0F, 0.0F);
        this.leftLeg.addChild(this.leftFoot);

        this.rightLeg = new ModelRenderer(this, 20, 0);
        this.rightLeg.mirror = true;
        this.rightLeg.addBox(-1.0F, -5.0F, -1.0F, 2, 5, 2);
        this.rightLeg.setRotationPoint(-1.7F, -0.2F, 0.0F);
        setRotation(this.rightLeg, 0.0F, 0.0F, toRad(5.0F));
        this.legs.addChild(this.rightLeg);

        this.rightFoot = new ModelRenderer(this, 20, 12);
        this.rightFoot.mirror = true;
        this.rightFoot.addBox(-0.9F, -0.2F, -0.4F, 2, 4, 3);
        this.rightFoot.setRotationPoint(0.0F, -0.2F, 0.0F);
        setRotation(this.rightFoot, toRad(58.0F), 0.0F, 0.0F);
        this.rightLeg.addChild(this.rightFoot);
    }

    @Override
    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
            float headPitch, float scale) {
        this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);
        this.all.render(scale);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
            float headPitch, float scaleFactor, Entity entityIn) {
        EntityDevilFamiliar devil = (EntityDevilFamiliar) entityIn;

        this.all.rotationPointY = 22.0F;
        setRotation(this.all, 0.0F, 0.0F, 0.0F);
        setRotation(this.torso, toRad(27.5F), 0.0F, 0.0F);
        setRotation(this.head, toRad(-27.5F) + headPitch * 0.017453292F, netHeadYaw * 0.017453292F, 0.0F);
        setRotation(this.arms, 0.0F, 0.0F, 0.0F);
        setRotation(this.leftArm, 0.0F, 0.0F, toRad(-12.5F));
        setRotation(this.rightArm, 0.0F, 0.0F, toRad(12.5F));
        setRotation(this.legs, toRad(-25.0F), 0.0F, 0.0F);
        setRotation(this.leftLeg, 0.0F, 0.0F, toRad(-5.0F));
        setRotation(this.rightLeg, 0.0F, 0.0F, toRad(5.0F));
        setRotation(this.leftFoot, toRad(58.0F), 0.0F, 0.0F);
        setRotation(this.rightFoot, toRad(58.0F), 0.0F, 0.0F);

        float idle = MathHelper.cos(ageInTicks * 0.05F);
        this.all.rotationPointY += idle * 0.5F;
        this.leftArm.rotateAngleX = idle * toRad(5.0F);
        this.rightArm.rotateAngleX = idle * toRad(5.0F);
        this.torso.rotateAngleZ = idle * toRad(1.5F);

        this.all.rotateAngleX += toRad(10.0F) * limbSwingAmount;
        this.leftArm.rotateAngleX += MathHelper.cos(limbSwing * 0.65F) * toRad(7.5F) * limbSwingAmount;
        this.rightArm.rotateAngleX += toRad(27.5F)
                + MathHelper.cos(limbSwing * 0.65F + (float) Math.PI) * toRad(5.0F) * limbSwingAmount;
        this.legs.rotateAngleX += toRad(12.5F) * limbSwingAmount;
        this.leftLeg.rotateAngleX += toRad(12.5F) * limbSwingAmount;
        this.rightLeg.rotateAngleX += MathHelper.cos(limbSwing * 0.65F) * toRad(7.5F) * limbSwingAmount;

        if (this.swingProgress > 0.0F) {
            float attack = MathHelper.sin(this.swingProgress * (float) Math.PI);
            this.all.rotationPointZ += attack * 2.0F;
            this.arms.rotateAngleX = -toRad(72.5F) * attack;
            this.leftArm.rotateAngleZ -= toRad(15.0F) * attack;
            this.rightArm.rotateAngleZ += toRad(12.5F) * attack;
            this.torso.rotateAngleX += toRad(10.0F) * attack;
            this.legs.rotateAngleX -= toRad(32.5F) * attack;
        }

        if (devil.isSitting()) {
            this.all.rotationPointY = 30.0F + idle * 0.5F;
            this.all.rotateAngleX = toRad(-60.0F);
            this.leftArm.rotateAngleX = toRad(-158.0F);
            this.leftArm.rotateAngleY = toRad(-5.0F);
            this.leftArm.rotateAngleZ = toRad(12.0F);
            this.rightArm.rotateAngleX = toRad(60.0F);
            this.rightArm.rotateAngleZ = toRad(27.5F);
            this.leftLeg.rotateAngleX = toRad(-35.0F);
            this.leftLeg.rotateAngleY = toRad(8.0F);
            this.leftLeg.rotateAngleZ = toRad(-32.0F);
            this.rightLeg.rotateAngleZ = toRad(15.0F);
            this.head.rotateAngleX = toRad(25.0F) + headPitch * 0.017453292F;
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
