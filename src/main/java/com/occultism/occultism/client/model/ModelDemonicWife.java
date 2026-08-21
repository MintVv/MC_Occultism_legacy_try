package com.occultism.occultism.client.model;

import com.occultism.occultism.entity.EntityDemonicPartner;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelDemonicWife extends ModelBase {

    private final ModelRenderer root;
    private final ModelRenderer body;
    private final ModelRenderer head;
    private final ModelRenderer leftArm;
    private final ModelRenderer rightArm;
    private final ModelRenderer leftLeg;
    private final ModelRenderer rightLeg;

    public ModelDemonicWife() {
        this.textureWidth = 64;
        this.textureHeight = 64;

        this.root = new ModelRenderer(this, 0, 0);
        this.root.setRotationPoint(0.0F, 23.0F, 0.0F);

        this.body = new ModelRenderer(this, 0, 26);
        this.body.addBox(-2.6F, -12.0F, -3.0F, 5, 10, 4);
        this.body.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.root.addChild(this.body);

        ModelRenderer chest = new ModelRenderer(this, 21, 0);
        chest.addBox(-3.0F, -5.2F, -1.0F, 6, 3, 3);
        chest.setRotationPoint(0.0F, -6.0F, -2.0F);
        setRotation(chest, toRad(-45.0F), 0.0F, 0.0F);
        this.body.addChild(chest);

        this.head = new ModelRenderer(this, 0, 0);
        this.head.addBox(-3.5F, -7.0F, -3.5F, 7, 7, 7);
        this.head.setRotationPoint(0.0F, -12.2F, -1.7F);
        setRotation(this.head, toRad(5.0F), 0.0F, 0.0F);
        this.root.addChild(this.head);

        ModelRenderer leftHair = new ModelRenderer(this, 20, 22);
        leftHair.addBox(0.0F, -4.0F, 0.0F, 4, 4, 4);
        leftHair.setRotationPoint(0.5F, -6.4F, 0.3F);
        this.head.addChild(leftHair);

        ModelRenderer rightHair = new ModelRenderer(this, 24, 10);
        rightHair.addBox(-4.0F, -4.0F, 0.0F, 4, 4, 4);
        rightHair.setRotationPoint(-0.5F, -6.4F, 0.3F);
        this.head.addChild(rightHair);

        ModelRenderer leftHorn = new ModelRenderer(this, 18, 16);
        leftHorn.addBox(-1.0F, -3.0F, -1.0F, 2, 3, 2);
        leftHorn.setRotationPoint(2.2F, -6.8F, -2.4F);
        setRotation(leftHorn, 0.0F, 0.0F, toRad(22.5F));
        this.head.addChild(leftHorn);

        ModelRenderer rightHorn = new ModelRenderer(this, 24, 18);
        rightHorn.addBox(-1.0F, -3.0F, -1.0F, 2, 3, 2);
        rightHorn.setRotationPoint(-2.2F, -6.8F, -2.4F);
        setRotation(rightHorn, 0.0F, 0.0F, toRad(-22.5F));
        this.head.addChild(rightHorn);

        this.leftArm = new ModelRenderer(this, 1, 41);
        this.leftArm.addBox(-1.0F, 0.0F, -1.5F, 2, 10, 3);
        this.leftArm.setRotationPoint(3.8F, -12.0F, -1.0F);
        setRotation(this.leftArm, 0.0F, 0.0F, toRad(-15.0F));
        this.root.addChild(this.leftArm);

        this.rightArm = new ModelRenderer(this, 21, 31);
        this.rightArm.addBox(-1.0F, 0.0F, -1.5F, 2, 10, 3);
        this.rightArm.setRotationPoint(-3.8F, -12.0F, -1.0F);
        setRotation(this.rightArm, 0.0F, 0.0F, toRad(10.0F));
        this.root.addChild(this.rightArm);

        this.leftLeg = new ModelRenderer(this, 36, 2);
        this.leftLeg.addBox(-1.2F, 0.0F, -2.0F, 3, 10, 4);
        this.leftLeg.setRotationPoint(1.0F, -2.0F, -1.0F);
        setRotation(this.leftLeg, toRad(-22.5F), toRad(-12.5F), 0.0F);
        this.root.addChild(this.leftLeg);

        this.rightLeg = new ModelRenderer(this, 36, 14);
        this.rightLeg.addBox(-1.8F, 0.0F, -2.0F, 3, 10, 4);
        this.rightLeg.setRotationPoint(-1.0F, -2.0F, -1.0F);
        setRotation(this.rightLeg, toRad(-5.0F), toRad(5.0F), 0.0F);
        this.root.addChild(this.rightLeg);
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
        float idle = MathHelper.cos(ageInTicks * 0.1F);

        this.root.rotationPointY = 23.0F + idle * 0.4F;
        setRotation(this.root, 0.0F, 0.0F, 0.0F);
        setRotation(this.head, toRad(5.0F) + headPitch * 0.017453292F, netHeadYaw * 0.017453292F, 0.0F);
        setRotation(this.leftArm, idle * toRad(4.0F), 0.0F, toRad(-15.0F));
        setRotation(this.rightArm, idle * toRad(6.0F), 0.0F, toRad(10.0F));
        setRotation(this.leftLeg, toRad(-22.5F), toRad(-12.5F), 0.0F);
        setRotation(this.rightLeg, toRad(-5.0F), toRad(5.0F), 0.0F);

        this.root.rotateAngleX += toRad(19.0F) * limbSwingAmount;
        this.leftLeg.rotateAngleX += MathHelper.cos(limbSwing * 0.66F) * 0.55F * limbSwingAmount;
        this.rightLeg.rotateAngleX += MathHelper.cos(limbSwing * 0.66F + (float) Math.PI) * 0.55F * limbSwingAmount;

        if (this.swingProgress > 0.0F) {
            float attack = MathHelper.sin(this.swingProgress * (float) Math.PI);
            this.root.rotateAngleX -= attack * toRad(17.5F);
            this.root.rotationPointZ += attack * 2.5F;
            this.leftArm.rotateAngleX -= attack * toRad(62.0F);
            this.rightArm.rotateAngleX -= attack * toRad(55.0F);
            this.head.rotateAngleX += attack * toRad(26.0F);
        }

        if (partner.isSitting() || partner.isLying()) {
            this.root.rotationPointY = partner.isLying() ? 31.0F : 30.0F;
            this.leftArm.rotateAngleX = toRad(-11.0F);
            this.leftArm.rotateAngleZ = toRad(33.0F);
            this.rightArm.rotateAngleX = toRad(-11.0F);
            this.rightArm.rotateAngleZ = toRad(-25.0F);
            this.leftLeg.rotateAngleX = toRad(-41.0F);
            this.rightLeg.rotateAngleX = toRad(18.0F);
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
