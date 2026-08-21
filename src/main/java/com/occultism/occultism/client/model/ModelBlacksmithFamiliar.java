package com.occultism.occultism.client.model;

import java.time.LocalDate;

import com.occultism.occultism.entity.EntityBlacksmithFamiliar;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelBlacksmithFamiliar extends ModelBase {

    private static final float PI = (float) Math.PI;

    private final ModelRenderer body;
    private final ModelRenderer wagon;
    private final ModelRenderer rightArm;
    private final ModelRenderer leftArm;
    private final ModelRenderer rightLeg;
    private final ModelRenderer leftLeg;
    private final ModelRenderer head;
    private final ModelRenderer hammer1;
    private final ModelRenderer mouth1;
    private final ModelRenderer leftEar;
    private final ModelRenderer hair1;
    private final ModelRenderer hair2;
    private final ModelRenderer mouth2;
    private final ModelRenderer christmasBeard;
    private final ModelRenderer earring;
    private final ModelRenderer wheel1;
    private final ModelRenderer wheel2;
    private final ModelRenderer wheel3;
    private final ModelRenderer wheel4;

    public ModelBlacksmithFamiliar() {
        this.textureWidth = 64;
        this.textureHeight = 64;

        this.body = new ModelRenderer(this, 0, 0);
        this.body.addBox(-2.5F, -5.0F, -1.5F, 5, 5, 3);
        this.body.setRotationPoint(0.0F, 19.0F, 3.0F);

        this.wagon = new ModelRenderer(this, 14, 12);
        this.wagon.addBox(-3.5F, 0.0F, -3.0F, 7, 1, 6);
        this.wagon.setRotationPoint(0.0F, 22.5F, -2.0F);

        this.rightArm = new ModelRenderer(this, 24, 0);
        this.rightArm.mirror = true;
        this.rightArm.addBox(-2.0F, 0.0F, -1.0F, 2, 6, 2);
        this.rightArm.setRotationPoint(-2.5F, -5.0F, 0.0F);
        this.body.addChild(this.rightArm);

        this.leftArm = new ModelRenderer(this, 24, 0);
        this.leftArm.addBox(0.0F, 0.0F, -1.0F, 2, 6, 2);
        this.leftArm.setRotationPoint(2.5F, -5.0F, 0.0F);
        this.body.addChild(this.leftArm);

        this.rightLeg = new ModelRenderer(this, 40, 0);
        this.rightLeg.mirror = true;
        this.rightLeg.addBox(-1.0F, 0.0F, -1.0F, 2, 5, 2);
        this.rightLeg.setRotationPoint(-1.5F, 0.0F, 0.0F);
        this.body.addChild(this.rightLeg);

        this.leftLeg = new ModelRenderer(this, 40, 0);
        this.leftLeg.addBox(-1.0F, 0.0F, -1.0F, 2, 5, 2);
        this.leftLeg.setRotationPoint(1.5F, 0.0F, 0.0F);
        this.body.addChild(this.leftLeg);

        this.head = new ModelRenderer(this, 43, 2);
        this.head.addBox(-2.5F, -5.0F, -2.5F, 5, 5, 5);
        this.head.setRotationPoint(0.0F, -5.0F, 0.0F);
        this.body.addChild(this.head);

        this.hammer1 = new ModelRenderer(this, 16, 0);
        this.hammer1.mirror = true;
        this.hammer1.addBox(-0.5F, -0.5F, -3.0F, 1, 1, 3);
        this.hammer1.setRotationPoint(-1.0F, 5.0F, 0.0F);
        setRotation(this.hammer1, 0.625526F, 1.7983873F, 0.0F);
        this.rightArm.addChild(this.hammer1);

        ModelRenderer hammer2 = new ModelRenderer(this, 14, 6);
        hammer2.mirror = true;
        hammer2.addBox(-1.0F, -1.0F, -1.0F, 3, 2, 2);
        hammer2.setRotationPoint(0.0F, 0.0F, -4.0F);
        this.hammer1.addChild(hammer2);

        ModelRenderer nose = new ModelRenderer(this, 30, 7);
        nose.addBox(-1.0F, 0.0F, -2.0F, 2, 3, 2);
        nose.setRotationPoint(0.0F, -3.2F, -1.5F);
        setRotation(nose, -0.23457225F, 0.0F, 0.0F);
        this.head.addChild(nose);

        this.mouth1 = new ModelRenderer(this, 8, 26);
        this.mouth1.addBox(-3.5F, -2.0F, 0.0F, 7, 5, 0);
        this.mouth1.setRotationPoint(0.0F, -1.1F, -3.2F);
        this.head.addChild(this.mouth1);

        this.leftEar = new ModelRenderer(this, 0, 26);
        this.leftEar.addBox(0.0F, -2.0F, 0.0F, 4, 3, 0);
        this.leftEar.setRotationPoint(2.4F, -3.0F, 0.0F);
        setRotation(this.leftEar, 0.2268928F, -0.87266463F, -0.5235988F);
        this.head.addChild(this.leftEar);

        this.hair1 = new ModelRenderer(this, 0, 8);
        this.hair1.addBox(-2.0F, -3.0F, -2.0F, 4, 3, 4);
        this.hair1.setRotationPoint(0.0F, -5.0F, 0.0F);
        this.head.addChild(this.hair1);

        ModelRenderer rightEar = new ModelRenderer(this, 0, 26);
        rightEar.mirror = true;
        rightEar.addBox(-4.0F, -2.0F, 0.0F, 4, 3, 0);
        rightEar.setRotationPoint(-2.4F, -3.0F, 0.0F);
        setRotation(rightEar, 0.2268928F, 0.87266463F, 0.5235988F);
        this.head.addChild(rightEar);

        this.hair2 = new ModelRenderer(this, 42, 21);
        this.hair2.addBox(-2.0F, -5.0F, -2.0F, 4, 5, 6);
        this.hair2.setRotationPoint(0.0F, -3.0F, 1.0F);
        this.head.addChild(this.hair2);

        this.mouth2 = new ModelRenderer(this, 22, 26);
        this.mouth2.addBox(-3.5F, -2.0F, 0.0F, 7, 5, 0);
        this.mouth2.setRotationPoint(0.0F, -1.1F, -3.2F);
        this.head.addChild(this.mouth2);

        this.christmasBeard = new ModelRenderer(this, 0, 32);
        this.christmasBeard.addBox(-3.5F, -2.0F, 0.0F, 7, 7, 0);
        this.christmasBeard.setRotationPoint(0.0F, 0.0F, -2.7F);
        setRotation(this.christmasBeard, -0.034906585F, 0.0F, 0.0F);
        this.head.addChild(this.christmasBeard);

        this.earring = new ModelRenderer(this, 0, 29);
        this.earring.addBox(-1.5F, -1.5F, 0.0F, 3, 3, 0);
        this.earring.setRotationPoint(1.4F, 1.0F, 0.0F);
        setRotation(this.earring, 0.0F, 2.391799F, 0.0F);
        this.leftEar.addChild(this.earring);

        ModelRenderer anvil1 = new ModelRenderer(this, 42, 12);
        anvil1.mirror = true;
        anvil1.addBox(-1.0F, -1.0F, -2.0F, 2, 2, 4);
        anvil1.setRotationPoint(-2.0F, -1.0F, 0.0F);
        this.wagon.addChild(anvil1);

        ModelRenderer anvil2 = new ModelRenderer(this, 42, 12);
        anvil2.addBox(-1.0F, -1.0F, -2.0F, 2, 2, 4);
        anvil2.setRotationPoint(2.0F, -1.0F, 0.0F);
        this.wagon.addChild(anvil2);

        ModelRenderer anvil3 = new ModelRenderer(this, 51, 15);
        anvil3.addBox(-1.0F, -2.0F, -1.5F, 2, 3, 3);
        anvil3.setRotationPoint(0.0F, -1.0F, 0.0F);
        this.wagon.addChild(anvil3);

        this.wheel1 = new ModelRenderer(this, 34, 14);
        this.wheel1.addBox(0.0F, -1.0F, -1.0F, 1, 2, 2);
        this.wheel1.setRotationPoint(3.5F, 0.5F, 2.5F);
        this.wagon.addChild(this.wheel1);

        this.wheel2 = new ModelRenderer(this, 34, 14);
        this.wheel2.addBox(0.0F, -1.0F, -1.0F, 1, 2, 2);
        this.wheel2.setRotationPoint(3.5F, 0.5F, -2.5F);
        this.wagon.addChild(this.wheel2);

        this.wheel3 = new ModelRenderer(this, 34, 14);
        this.wheel3.mirror = true;
        this.wheel3.addBox(-1.0F, -1.0F, -1.0F, 1, 2, 2);
        this.wheel3.setRotationPoint(-3.5F, 0.5F, -2.5F);
        this.wagon.addChild(this.wheel3);

        this.wheel4 = new ModelRenderer(this, 34, 14);
        this.wheel4.mirror = true;
        this.wheel4.addBox(-1.0F, -1.0F, -1.0F, 1, 2, 2);
        this.wheel4.setRotationPoint(-3.5F, 0.5F, 2.5F);
        this.wagon.addChild(this.wheel4);

        ModelRenderer anvil4 = new ModelRenderer(this, 0, 19);
        anvil4.addBox(-3.0F, -1.5F, -2.0F, 6, 3, 4);
        anvil4.setRotationPoint(0.0F, -3.5F, 0.0F);
        anvil3.addChild(anvil4);

        ModelRenderer anvil5 = new ModelRenderer(this, 36, 18);
        anvil5.addBox(-1.0F, -1.0F, -2.0F, 2, 2, 4);
        anvil5.setRotationPoint(-4.0F, -0.5F, 0.0F);
        anvil4.addChild(anvil5);

        ModelRenderer anvil6 = new ModelRenderer(this, 16, 19);
        anvil6.addBox(0.0F, -1.0F, -1.0F, 3, 2, 2);
        anvil6.setRotationPoint(2.5F, -0.51F, 0.5F);
        setRotation(anvil6, 0.0F, 0.2617994F, 0.0F);
        anvil4.addChild(anvil6);

        ModelRenderer anvil7 = new ModelRenderer(this, 16, 19);
        anvil7.addBox(0.0F, -1.0F, -1.0F, 3, 2, 2);
        anvil7.setRotationPoint(2.5F, -0.5F, -0.5F);
        setRotation(anvil7, 0.0F, -0.27366763F, 0.0F);
        anvil4.addChild(anvil7);
    }

    @Override
    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
            float headPitch, float scale) {
        this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);
        this.body.render(scale);
        this.wagon.render(scale);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
            float headPitch, float scaleFactor, Entity entityIn) {
        EntityBlacksmithFamiliar blacksmith = (EntityBlacksmithFamiliar) entityIn;
        this.showModels(blacksmith);

        this.head.rotateAngleY = netHeadYaw * (PI / 180.0F);
        this.head.rotateAngleX = headPitch * (PI / 180.0F);

        this.wheel1.rotateAngleX = limbSwing;
        this.wheel2.rotateAngleX = limbSwing;
        this.wheel3.rotateAngleX = limbSwing;
        this.wheel4.rotateAngleX = limbSwing;

        this.rightLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.5F) * limbSwingAmount * 0.6F;
        this.leftLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.5F + PI) * limbSwingAmount * 0.6F;

        this.leftArm.rotateAngleX = toRads(-30.0F)
                + MathHelper.cos(limbSwing * 0.5F + PI) * limbSwingAmount * 0.2F;
        this.leftArm.rotateAngleY = toRads(-15.0F);
        this.rightArm.rotateAngleX = toRads(-75.0F);
        this.rightArm.rotateAngleY = toRads(25.0F);
        setRotation(this.hammer1, 0.0F, 0.0F, toRads(90.0F));

        this.body.rotateAngleY = 0.0F;
        this.body.rotationPointY = 19.0F;
        this.body.rotateAngleX = 0.0F;
        this.body.rotationPointZ = 3.0F;
        this.leftArm.rotateAngleZ = 0.0F;
        this.rightArm.rotateAngleZ = 0.0F;

        if (blacksmith.isPartying()) {
            this.head.rotateAngleX = MathHelper.cos(ageInTicks * 0.8F) * toRads(25.0F);
            this.head.rotateAngleY = 0.0F;
            this.leftArm.rotateAngleX = toRads(-90.0F)
                    + MathHelper.cos(ageInTicks * 1.2F) * toRads(25.0F);
            this.leftArm.rotateAngleY = toRads(-5.0F);
            this.leftArm.rotateAngleZ = toRads(20.0F);
            this.rightArm.rotateAngleX = toRads(-90.0F)
                    + MathHelper.cos(ageInTicks * 1.2F + PI) * toRads(25.0F);
            this.rightArm.rotateAngleY = toRads(-5.0F);
            this.rightArm.rotateAngleZ = toRads(-20.0F);
        } else if (blacksmith.isSitting()) {
            this.body.rotateAngleY = toRads(180.0F);
            this.leftLeg.rotateAngleX = toRads(-70.0F);
            this.rightLeg.rotateAngleX = toRads(-70.0F);
            this.body.rotationPointY = 23.0F;
            this.body.rotationPointZ = 4.0F;
            this.body.rotateAngleX = toRads(-20.0F);
            this.head.rotateAngleX = toRads(20.0F);
            this.head.rotateAngleY = 0.0F;
            this.leftArm.rotateAngleX = toRads(-10.0F);
            this.rightArm.rotateAngleX = toRads(-10.0F);
        }
    }

    private void showModels(EntityBlacksmithFamiliar blacksmith) {
        boolean christmas = isChristmas();
        boolean marioMoustache = blacksmith.hasMarioMoustache();
        boolean squareHair = blacksmith.hasSquareHair();

        this.earring.showModel = blacksmith.hasEarring();
        this.mouth1.showModel = !marioMoustache && !christmas;
        this.mouth2.showModel = marioMoustache && !christmas;
        this.hair1.showModel = squareHair;
        this.hair2.showModel = !squareHair;
        this.christmasBeard.showModel = christmas;
    }

    private static boolean isChristmas() {
        LocalDate date = LocalDate.now();
        int month = date.getMonthValue();
        int day = date.getDayOfMonth();
        return month == 12 && day >= 15 || month == 1 && day <= 15;
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
