/*
 * MIT License
 *
 * Copyright 2021 vemerion
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions
 * of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package com.occultism.occultism.client.model;

import com.occultism.occultism.entity.EntityDragonFamiliar;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelDragonFamiliar extends ModelBase {

    private static final float PI = (float) Math.PI;

    public final ModelRenderer body;
    public final ModelRenderer neck1;
    public final ModelRenderer leftLeg1;
    public final ModelRenderer tail1;
    public final ModelRenderer leftWing1;
    public final ModelRenderer rightWing1;
    public final ModelRenderer rightLeg1;
    public final ModelRenderer leftArm1;
    public final ModelRenderer rightArm1;
    public final ModelRenderer neck2;
    public final ModelRenderer head;
    public final ModelRenderer jaw;
    public final ModelRenderer leftEye;
    public final ModelRenderer rightEye;
    public final ModelRenderer fez1;
    public final ModelRenderer leftHorn1;
    public final ModelRenderer leftEar;
    public final ModelRenderer rightEar;
    public final ModelRenderer rightHorn1;
    public final ModelRenderer tooth1;
    public final ModelRenderer tooth2;
    public final ModelRenderer tooth4;
    public final ModelRenderer leftNose;
    public final ModelRenderer rightNose;
    public final ModelRenderer fez2;
    public final ModelRenderer leftHorn2;
    public final ModelRenderer rightHorn2;
    public final ModelRenderer leftLeg2;
    public final ModelRenderer leftLeg3;
    public final ModelRenderer tail2;
    public final ModelRenderer spike1;
    public final ModelRenderer tail3;
    public final ModelRenderer spike2;
    public final ModelRenderer spike3;
    public final ModelRenderer leftWing2;
    public final ModelRenderer rightWing2;
    public final ModelRenderer rightLeg2;
    public final ModelRenderer rightLeg3;
    public final ModelRenderer leftArm2;
    public final ModelRenderer leftArm3;
    public final ModelRenderer rightArm2;
    public final ModelRenderer rightArm3;

    public ModelDragonFamiliar() {
        this.textureWidth = 64;
        this.textureHeight = 32;

        this.body = new ModelRenderer(this, 0, 0);
        this.body.addBox(-2.0F, -2.0F, -5.0F, 4, 4, 10);
        this.body.setRotationPoint(0.0F, 18.0F, 0.0F);
        setRotation(this.body, -0.06981317F, 0.0F, 0.0F);

        this.neck1 = new ModelRenderer(this, 18, 0);
        this.neck1.addBox(-1.5F, -1.5F, -3.0F, 3, 3, 3);
        this.neck1.setRotationPoint(0.0F, 0.2F, -4.1F);
        setRotation(this.neck1, -0.54733527F, 0.0F, 0.0F);
        this.body.addChild(this.neck1);

        this.leftLeg1 = new ModelRenderer(this, 44, 5);
        this.leftLeg1.addBox(-0.5F, 0.0F, -1.0F, 1, 3, 2);
        this.leftLeg1.setRotationPoint(1.8F, 0.5F, 2.0F);
        setRotation(this.leftLeg1, 0.43004912F, 0.0F, 0.0F);
        this.body.addChild(this.leftLeg1);

        this.tail1 = new ModelRenderer(this, 36, 10);
        this.tail1.addBox(-1.5F, -1.5F, 0.0F, 3, 3, 4);
        this.tail1.setRotationPoint(0.0F, 0.0F, 4.3F);
        setRotation(this.tail1, -0.23457225F, 0.0F, 0.0F);
        this.body.addChild(this.tail1);

        this.leftWing1 = new ModelRenderer(this, 0, 9);
        this.leftWing1.addBox(0.0F, -5.0F, -5.0F, 0, 5, 10);
        this.leftWing1.setRotationPoint(2.0F, 0.0F, -2.0F);
        setRotation(this.leftWing1, 0.0F, 0.0F, 1.1414454F);
        this.body.addChild(this.leftWing1);

        this.rightWing1 = new ModelRenderer(this, 0, 9);
        this.rightWing1.mirror = true;
        this.rightWing1.addBox(0.0F, -5.0F, -5.0F, 0, 5, 10);
        this.rightWing1.setRotationPoint(-2.0F, 0.0F, -2.0F);
        setRotation(this.rightWing1, 0.0F, 0.0F, -0.86009824F);
        this.body.addChild(this.rightWing1);

        this.rightLeg1 = new ModelRenderer(this, 44, 5);
        this.rightLeg1.mirror = true;
        this.rightLeg1.addBox(-0.5F, 0.0F, -1.0F, 1, 3, 2);
        this.rightLeg1.setRotationPoint(-1.8F, 0.5F, 2.0F);
        setRotation(this.rightLeg1, 0.43004912F, 0.0F, 0.0F);
        this.body.addChild(this.rightLeg1);

        this.leftArm1 = new ModelRenderer(this, 0, 5);
        this.leftArm1.addBox(-0.5F, 0.0F, -0.5F, 1, 2, 1);
        this.leftArm1.setRotationPoint(1.6F, 1.1F, -4.0F);
        this.body.addChild(this.leftArm1);

        this.rightArm1 = new ModelRenderer(this, 0, 5);
        this.rightArm1.addBox(-0.5F, 0.0F, -0.5F, 1, 2, 1);
        this.rightArm1.setRotationPoint(-1.6F, 1.1F, -4.0F);
        this.body.addChild(this.rightArm1);

        this.neck2 = new ModelRenderer(this, 0, 0);
        this.neck2.addBox(-1.0F, -1.0F, -3.0F, 2, 2, 3);
        this.neck2.setRotationPoint(0.0F, 0.0F, -2.4F);
        setRotation(this.neck2, -0.1563815F, 0.0F, 0.0F);
        this.neck1.addChild(this.neck2);

        this.head = new ModelRenderer(this, 30, 0);
        this.head.addBox(-2.0F, -1.5F, -4.0F, 4, 3, 4);
        this.head.setRotationPoint(0.0F, -0.4F, -2.4F);
        setRotation(this.head, 0.821352F, 0.0F, 0.0F);
        this.neck2.addChild(this.head);

        this.jaw = new ModelRenderer(this, 46, 0);
        this.jaw.addBox(-1.5F, -1.0F, -3.0F, 3, 2, 3);
        this.jaw.setRotationPoint(0.0F, 0.2F, -4.0F);
        this.head.addChild(this.jaw);

        this.leftEye = new ModelRenderer(this, 56, 3);
        this.leftEye.addBox(0.0F, -1.0F, -1.0F, 1, 2, 2);
        this.leftEye.setRotationPoint(1.5F, -0.1F, -2.4F);
        this.head.addChild(this.leftEye);

        this.rightEye = new ModelRenderer(this, 56, 3);
        this.rightEye.mirror = true;
        this.rightEye.addBox(0.0F, -1.0F, -1.0F, 1, 2, 2);
        this.rightEye.setRotationPoint(-2.5F, -0.1F, -2.4F);
        this.head.addChild(this.rightEye);

        this.fez1 = new ModelRenderer(this, 18, 14);
        this.fez1.addBox(-1.0F, -2.0F, -1.0F, 2, 2, 2);
        this.fez1.setRotationPoint(0.0F, -1.5F, -2.0F);
        this.head.addChild(this.fez1);

        this.leftHorn1 = new ModelRenderer(this, 0, 14);
        this.leftHorn1.addBox(-0.5F, -1.0F, 0.0F, 1, 2, 2);
        this.leftHorn1.setRotationPoint(1.7F, -0.5F, -0.5F);
        setRotation(this.leftHorn1, 0.2268928F, 0.0F, 0.0F);
        this.head.addChild(this.leftHorn1);

        this.leftEar = new ModelRenderer(this, 12, 14);
        this.leftEar.addBox(-0.5F, -1.0F, 0.0F, 1, 2, 2);
        this.leftEar.setRotationPoint(1.7F, -0.6F, -0.6F);
        setRotation(this.leftEar, -0.13613568F, 1.1798426F, -0.5899213F);
        this.head.addChild(this.leftEar);

        this.rightEar = new ModelRenderer(this, 12, 14);
        this.rightEar.mirror = true;
        this.rightEar.addBox(-0.5F, -1.0F, 0.0F, 1, 2, 2);
        this.rightEar.setRotationPoint(-1.7F, -0.6F, -0.6F);
        setRotation(this.rightEar, -0.13613568F, -1.1798426F, 0.5899213F);
        this.head.addChild(this.rightEar);

        this.rightHorn1 = new ModelRenderer(this, 0, 14);
        this.rightHorn1.mirror = true;
        this.rightHorn1.addBox(-0.5F, -1.0F, 0.0F, 1, 2, 2);
        this.rightHorn1.setRotationPoint(-1.7F, -0.5F, -0.5F);
        setRotation(this.rightHorn1, 0.2268928F, 0.0F, 0.0F);
        this.head.addChild(this.rightHorn1);

        this.tooth1 = new ModelRenderer(this, 59, 0);
        this.tooth1.addBox(0.0F, 0.0F, 0.0F, 1, 1, 1);
        this.tooth1.setRotationPoint(0.7F, -0.4F, -1.5F);
        this.jaw.addChild(this.tooth1);

        this.tooth2 = new ModelRenderer(this, 59, 0);
        this.tooth2.addBox(0.0F, 0.0F, 0.0F, 1, 1, 1);
        this.tooth2.setRotationPoint(0.0F, -0.3F, -3.2F);
        this.jaw.addChild(this.tooth2);

        this.tooth4 = new ModelRenderer(this, 59, 0);
        this.tooth4.addBox(0.0F, 0.0F, 0.0F, 1, 1, 1);
        this.tooth4.setRotationPoint(-1.7F, -0.4F, -1.6F);
        this.jaw.addChild(this.tooth4);

        this.leftNose = new ModelRenderer(this, 45, 1);
        this.leftNose.addBox(-0.5F, 0.0F, 0.0F, 1, 1, 1);
        this.leftNose.setRotationPoint(-0.8F, -1.4F, -2.6F);
        this.jaw.addChild(this.leftNose);

        this.rightNose = new ModelRenderer(this, 45, 1);
        this.rightNose.addBox(-0.5F, 0.0F, 0.0F, 1, 1, 1);
        this.rightNose.setRotationPoint(0.8F, -1.4F, -2.6F);
        this.jaw.addChild(this.rightNose);

        this.fez2 = new ModelRenderer(this, 24, 13);
        this.fez2.addBox(0.0F, 0.0F, -0.5F, 0, 1, 1);
        this.fez2.setRotationPoint(0.0F, -3.0F, 0.0F);
        this.fez1.addChild(this.fez2);

        this.leftHorn2 = new ModelRenderer(this, 6, 14);
        this.leftHorn2.addBox(-0.5F, -0.5F, 0.0F, 1, 1, 2);
        this.leftHorn2.setRotationPoint(0.01F, 0.0F, 1.4F);
        setRotation(this.leftHorn2, 0.34906584F, 0.0F, 0.0F);
        this.leftHorn1.addChild(this.leftHorn2);

        this.rightHorn2 = new ModelRenderer(this, 6, 14);
        this.rightHorn2.mirror = true;
        this.rightHorn2.addBox(-0.5F, -0.5F, 0.0F, 1, 1, 2);
        this.rightHorn2.setRotationPoint(-0.01F, 0.0F, 1.4F);
        setRotation(this.rightHorn2, 0.34906584F, 0.0F, 0.0F);
        this.rightHorn1.addChild(this.rightHorn2);

        this.leftLeg2 = new ModelRenderer(this, 50, 5);
        this.leftLeg2.addBox(-0.5F, 0.0F, -1.0F, 1, 3, 2);
        this.leftLeg2.setRotationPoint(0.01F, 2.4F, 0.5F);
        setRotation(this.leftLeg2, -0.74281216F, 0.0F, 0.0F);
        this.leftLeg1.addChild(this.leftLeg2);

        this.leftLeg3 = new ModelRenderer(this, 28, 7);
        this.leftLeg3.addBox(-1.0F, 0.0F, -4.0F, 2, 1, 4);
        this.leftLeg3.setRotationPoint(0.0F, 2.2F, 0.8F);
        setRotation(this.leftLeg3, 0.39095375F, 0.0F, 0.0F);
        this.leftLeg2.addChild(this.leftLeg3);

        this.tail2 = new ModelRenderer(this, 50, 10);
        this.tail2.addBox(-1.0F, -1.0F, 0.0F, 2, 2, 4);
        this.tail2.setRotationPoint(0.0F, 0.0F, 3.3F);
        setRotation(this.tail2, -0.23457225F, 0.0F, 0.0F);
        this.tail1.addChild(this.tail2);

        this.spike1 = new ModelRenderer(this, 0, 25);
        this.spike1.addBox(0.0F, 0.0F, 0.0F, 0, 3, 4);
        this.spike1.setRotationPoint(0.0F, -4.5F, 0.0F);
        this.tail1.addChild(this.spike1);

        this.tail3 = new ModelRenderer(this, 18, 6);
        this.tail3.addBox(-0.5F, -0.5F, 0.0F, 1, 1, 3);
        this.tail3.setRotationPoint(0.0F, 0.0F, 3.4F);
        setRotation(this.tail3, 0.19547687F, 0.0F, 0.0F);
        this.tail2.addChild(this.tail3);

        this.spike2 = new ModelRenderer(this, 0, 25);
        this.spike2.addBox(0.0F, 0.0F, 0.0F, 0, 3, 4);
        this.spike2.setRotationPoint(0.01F, -4.0F, 0.0F);
        this.tail2.addChild(this.spike2);

        this.spike3 = new ModelRenderer(this, 8, 26);
        this.spike3.addBox(0.0F, 0.0F, 0.0F, 0, 3, 3);
        this.spike3.setRotationPoint(0.0F, -3.5F, 0.0F);
        this.tail3.addChild(this.spike3);

        this.leftWing2 = new ModelRenderer(this, 0, 14);
        this.leftWing2.addBox(0.0F, -5.0F, -5.0F, 0, 5, 10);
        this.leftWing2.setRotationPoint(0.0F, -5.0F, 0.0F);
        setRotation(this.leftWing2, 0.0F, 0.0F, 0.86009824F);
        this.leftWing1.addChild(this.leftWing2);

        this.rightWing2 = new ModelRenderer(this, 0, 14);
        this.rightWing2.mirror = true;
        this.rightWing2.addBox(0.0F, -5.0F, -5.0F, 0, 5, 10);
        this.rightWing2.setRotationPoint(0.0F, -5.0F, 0.0F);
        setRotation(this.rightWing2, 0.0F, 0.0F, -0.86009824F);
        this.rightWing1.addChild(this.rightWing2);

        this.rightLeg2 = new ModelRenderer(this, 50, 5);
        this.rightLeg2.mirror = true;
        this.rightLeg2.addBox(-0.5F, 0.0F, -1.0F, 1, 3, 2);
        this.rightLeg2.setRotationPoint(-0.01F, 2.4F, 0.5F);
        setRotation(this.rightLeg2, -0.74281216F, 0.0F, 0.0F);
        this.rightLeg1.addChild(this.rightLeg2);

        this.rightLeg3 = new ModelRenderer(this, 28, 7);
        this.rightLeg3.mirror = true;
        this.rightLeg3.addBox(-1.0F, 0.0F, -4.0F, 2, 1, 4);
        this.rightLeg3.setRotationPoint(0.0F, 2.2F, 0.8F);
        setRotation(this.rightLeg3, 0.39095375F, 0.0F, 0.0F);
        this.rightLeg2.addChild(this.rightLeg3);

        this.leftArm2 = new ModelRenderer(this, 0, 8);
        this.leftArm2.addBox(-0.5F, 0.0F, -0.5F, 1, 1, 1);
        this.leftArm2.setRotationPoint(-0.1F, 2.1F, 0.0F);
        setRotation(this.leftArm2, 0.0F, 0.0F, 0.7853982F);
        this.leftArm1.addChild(this.leftArm2);

        this.leftArm3 = new ModelRenderer(this, 0, 8);
        this.leftArm3.addBox(-0.5F, 0.0F, -0.5F, 1, 1, 1);
        this.leftArm3.setRotationPoint(0.1F, 2.1F, 0.0F);
        setRotation(this.leftArm3, 0.0F, 0.0F, -0.7853982F);
        this.leftArm1.addChild(this.leftArm3);

        this.rightArm2 = new ModelRenderer(this, 0, 8);
        this.rightArm2.addBox(-0.5F, 0.0F, -0.5F, 1, 1, 1);
        this.rightArm2.setRotationPoint(-0.1F, 2.1F, 0.0F);
        setRotation(this.rightArm2, 0.0F, 0.0F, 0.7853982F);
        this.rightArm1.addChild(this.rightArm2);

        this.rightArm3 = new ModelRenderer(this, 0, 8);
        this.rightArm3.addBox(-0.5F, 0.0F, -0.5F, 1, 1, 1);
        this.rightArm3.setRotationPoint(0.1F, 2.1F, 0.0F);
        setRotation(this.rightArm3, 0.0F, 0.0F, -0.7853982F);
        this.rightArm1.addChild(this.rightArm3);
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
        EntityDragonFamiliar dragon = (EntityDragonFamiliar) entityIn;
        if (dragon.isPartying()) {
            this.head.rotateAngleX = toRads(50.0F) + MathHelper.sin(ageInTicks) * toRads(20.0F);
            this.head.rotateAngleY = MathHelper.sin(ageInTicks) * toRads(5.0F);
            this.head.rotateAngleZ = MathHelper.sin(ageInTicks) * toRads(5.0F);
        } else {
            this.head.rotateAngleX = toRads(50.0F) + 0.03F + headPitch * PI / 180.0F * 0.7F;
            this.head.rotateAngleY = netHeadYaw * PI / 180.0F * 0.5F;
            this.head.rotateAngleZ = netHeadYaw * PI / 180.0F * 0.5F;
        }
    }

    @Override
    public void setLivingAnimations(EntityLivingBase entitylivingbaseIn, float limbSwing, float limbSwingAmount,
            float partialTick) {
        EntityDragonFamiliar dragon = (EntityDragonFamiliar) entitylivingbaseIn;
        this.showModels(dragon);

        float ageInTicks = dragon.ticksExisted + partialTick;
        this.tail1.rotateAngleZ = 0.0F;
        this.tail2.rotateAngleZ = 0.0F;
        this.tail3.rotateAngleZ = 0.0F;
        this.jaw.rotateAngleZ = 0.0F;

        if (dragon.isPartying()) {
            this.tail1.rotateAngleZ = MathHelper.sin(ageInTicks) * toRads(30.0F);
            this.tail2.rotateAngleZ = -MathHelper.sin(ageInTicks) * toRads(60.0F);
            this.tail3.rotateAngleZ = MathHelper.sin(ageInTicks) * toRads(90.0F);
            this.leftWing1.rotateAngleY = MathHelper.sin(ageInTicks) * toRads(20.0F);
            this.rightWing1.rotateAngleY = -MathHelper.sin(ageInTicks) * toRads(20.0F);
        } else {
            this.leftWing1.rotateAngleY = 0.0F;
            this.rightWing1.rotateAngleY = 0.0F;
        }

        float petTimer = dragon.getPetTimer() + partialTick;
        float petDuration = EntityDragonFamiliar.MAX_PET_TIMER / 2.0F;
        if (petTimer < petDuration) {
            float petRotation = MathHelper.sin(petTimer / petDuration * PI * 6.0F);
            this.tail1.rotateAngleZ = petRotation * toRads(20.0F);
            this.tail2.rotateAngleZ = petRotation * toRads(20.0F);
            this.tail3.rotateAngleZ = petRotation * toRads(20.0F);
            this.jaw.rotateAngleZ = -petRotation * toRads(10.0F);
        }

        if (dragon.isSwingInProgress) {
            float attackProgress = dragon.getAttackProgress(partialTick);
            float attackRotation = MathHelper.sin(attackProgress * PI * 4.0F) * toRads(30.0F);
            this.tail1.rotateAngleY = attackRotation;
            this.tail2.rotateAngleY = attackRotation;
            this.tail3.rotateAngleY = attackRotation;
        } else {
            this.tail1.rotateAngleY = 0.0F;
            this.tail2.rotateAngleY = 0.0F;
            this.tail3.rotateAngleY = 0.0F;
        }

        if (!dragon.isSitting()) {
            this.leftLeg1.rotateAngleX = toRads(25.0F)
                    + MathHelper.cos(limbSwing * 0.7F + PI) * limbSwingAmount * 0.5F;
            this.rightLeg1.rotateAngleX = toRads(25.0F)
                    + MathHelper.cos(limbSwing * 0.7F) * limbSwingAmount * 0.5F;
            this.leftLeg3.rotateAngleX = toRads(23.0F);
            this.rightLeg3.rotateAngleX = toRads(23.0F);

            float flyingWingRotation = dragon.getFlyingTimer(partialTick) * 1.15F;
            float wingspan = toRads(dragon.getWingspan(partialTick));
            float wingCycle = MathHelper.cos(limbSwing * 0.7F + flyingWingRotation);
            this.leftWing1.rotateAngleZ = toRads(65.0F)
                    + wingCycle * (limbSwingAmount * 0.2F + wingspan);
            this.leftWing2.rotateAngleZ = toRads(50.0F)
                    + wingCycle * (limbSwingAmount * 0.2F + wingspan * 0.5F);
            this.rightWing1.rotateAngleZ = -toRads(65.0F)
                    - wingCycle * (limbSwingAmount * 0.2F + wingspan);
            this.rightWing2.rotateAngleZ = -toRads(50.0F)
                    - wingCycle * (limbSwingAmount * 0.2F + wingspan * 0.5F);

            float tailRotation = MathHelper.cos(ageInTicks / 20.0F) * toRads(10.0F);
            this.tail1.rotateAngleX = tailRotation;
            this.tail2.rotateAngleX = tailRotation;
            this.tail3.rotateAngleX = tailRotation;
            this.body.rotateAngleX = toRads(-4.0F);
            this.neck1.rotateAngleX = toRads(-30.0F);
            this.neck2.rotateAngleX = toRads(-9.0F);
        } else {
            this.leftLeg1.rotateAngleX = toRads(15.0F);
            this.rightLeg1.rotateAngleX = toRads(15.0F);
            this.leftLeg3.rotateAngleX = toRads(26.0F);
            this.rightLeg3.rotateAngleX = toRads(26.0F);
            this.leftWing1.rotateAngleZ = toRads(150.0F);
            this.leftWing2.rotateAngleZ = toRads(20.0F);
            this.rightWing1.rotateAngleZ = -toRads(150.0F);
            this.rightWing2.rotateAngleZ = -toRads(20.0F);
            this.tail1.rotateAngleX = toRads(30.0F);
            this.tail2.rotateAngleX = toRads(30.0F);
            this.tail3.rotateAngleX = toRads(30.0F);
            this.body.rotateAngleX = toRads(-50.0F);
            this.neck1.rotateAngleX = toRads(10.0F);
            this.neck2.rotateAngleX = toRads(5.0F);
        }
    }

    private void showModels(EntityDragonFamiliar dragon) {
        boolean hasEars = dragon.hasEars();
        boolean hasArms = dragon.hasArms();
        this.fez1.showModel = dragon.hasFez();
        this.leftEar.showModel = hasEars;
        this.rightEar.showModel = hasEars;
        this.leftHorn1.showModel = !hasEars;
        this.rightHorn1.showModel = !hasEars;
        this.leftArm1.showModel = hasArms;
        this.rightArm1.showModel = hasArms;
    }

    private static void setRotation(ModelRenderer renderer, float x, float y, float z) {
        renderer.rotateAngleX = x;
        renderer.rotateAngleY = y;
        renderer.rotateAngleZ = z;
    }

    private static float toRads(float degrees) {
        return degrees * PI / 180.0F;
    }
}
