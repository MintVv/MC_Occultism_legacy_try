/*
 * MIT License
 *
 * Copyright 2021 vemerion
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.occultism.occultism.client.model;

import com.occultism.occultism.entity.EntityChimeraFamiliar;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;

public class ModelChimeraFamiliar extends ModelBase {

    private static final float PI = (float) Math.PI;

    public final ModelRenderer body;
    public final ModelRenderer head;
    public final ModelRenderer leftBackLeg1;
    public final ModelRenderer snake1;
    public final ModelRenderer leftLeg1;
    public final ModelRenderer goatNeck;
    public final ModelRenderer rightBackLeg1;
    public final ModelRenderer rightLeg1;
    public final ModelRenderer mouth;
    public final ModelRenderer leftEye;
    public final ModelRenderer leftEar;
    public final ModelRenderer mane1;
    public final ModelRenderer mane2;
    public final ModelRenderer mane3;
    public final ModelRenderer mane4;
    public final ModelRenderer mane7;
    public final ModelRenderer rightEar;
    public final ModelRenderer rightEye;
    public final ModelRenderer mane5;
    public final ModelRenderer mane6;
    public final ModelRenderer mane8;
    public final ModelRenderer jaw;
    public final ModelRenderer nose;
    public final ModelRenderer leftBackLeg2;
    public final ModelRenderer leftBackLeg3;
    public final ModelRenderer snake2;
    public final ModelRenderer snake3;
    public final ModelRenderer snake4;
    public final ModelRenderer snakeFlap1;
    public final ModelRenderer snakeFlap2;
    public final ModelRenderer snake5;
    public final ModelRenderer snakeTooth;
    public final ModelRenderer snakeHat1;
    public final ModelRenderer snakeHat2;
    public final ModelRenderer leftLeg2;
    public final ModelRenderer leftLeg3;
    public final ModelRenderer goatHead;
    public final ModelRenderer goatLeftHorn1;
    public final ModelRenderer goatMouth;
    public final ModelRenderer goatRightHorn1;
    public final ModelRenderer goatLeftHorn2;
    public final ModelRenderer goatBeard;
    public final ModelRenderer goatRing;
    public final ModelRenderer goatRightHorn2;
    public final ModelRenderer rightBackLeg2;
    public final ModelRenderer rightBackLeg3;
    public final ModelRenderer rightLeg2;
    public final ModelRenderer rightLeg3;

    public ModelChimeraFamiliar() {
        this.textureWidth = 64;
        this.textureHeight = 32;

        this.body = new ModelRenderer(this, 0, 0);
        this.body.addBox(-2.5F, -2.5F, -5.0F, 5, 5, 10);
        this.body.setRotationPoint(0.0F, 17.0F, 0.0F);

        this.head = new ModelRenderer(this, 20, 0);
        this.head.addBox(-2.5F, -4.0F, -4.0F, 5, 5, 5);
        this.head.setRotationPoint(0.0F, 0.0F, -6.0F);
        this.body.addChild(this.head);

        this.leftBackLeg1 = new ModelRenderer(this, 4, 0);
        this.leftBackLeg1.addBox(-0.5F, 0.0F, -1.0F, 1, 4, 2);
        this.leftBackLeg1.setRotationPoint(2.3F, -0.1F, 3.9F);
        setRotation(this.leftBackLeg1, -0.23457224F, 0.0F, 0.0F);
        this.body.addChild(this.leftBackLeg1);

        this.snake1 = new ModelRenderer(this, 45, 4);
        this.snake1.addBox(-1.0F, -4.0F, -1.0F, 2, 4, 2);
        this.snake1.setRotationPoint(0.0F, 0.0F, 4.5F);
        setRotation(this.snake1, -0.6646214F, 0.0F, 0.0F);
        this.body.addChild(this.snake1);

        this.leftLeg1 = new ModelRenderer(this, 46, 11);
        this.leftLeg1.addBox(-1.0F, 0.0F, -1.0F, 2, 4, 2);
        this.leftLeg1.setRotationPoint(2.0F, -0.2F, -3.0F);
        setRotation(this.leftLeg1, 0.43004912F, 0.0F, 0.0F);
        this.body.addChild(this.leftLeg1);

        this.goatNeck = new ModelRenderer(this, 0, 15);
        this.goatNeck.addBox(-4.0F, -2.0F, -1.0F, 4, 3, 2);
        this.goatNeck.setRotationPoint(-1.5F, -1.0F, 0.0F);
        setRotation(this.goatNeck, 0.039095376F, -0.23457224F, 0.89901906F);
        this.body.addChild(this.goatNeck);

        this.rightBackLeg1 = new ModelRenderer(this, 4, 0);
        this.rightBackLeg1.mirror = true;
        this.rightBackLeg1.addBox(-0.5F, 0.0F, -1.0F, 1, 4, 2);
        this.rightBackLeg1.setRotationPoint(-2.3F, -0.1F, 3.9F);
        setRotation(this.rightBackLeg1, -0.23457224F, 0.0F, 0.0F);
        this.body.addChild(this.rightBackLeg1);

        this.rightLeg1 = new ModelRenderer(this, 46, 11);
        this.rightLeg1.mirror = true;
        this.rightLeg1.addBox(-1.0F, 0.0F, -1.0F, 2, 4, 2);
        this.rightLeg1.setRotationPoint(-2.0F, -0.2F, -3.0F);
        setRotation(this.rightLeg1, 0.43004912F, 0.0F, 0.0F);
        this.body.addChild(this.rightLeg1);

        this.mouth = new ModelRenderer(this, 35, 0);
        this.mouth.addBox(-1.5F, -1.0F, -3.0F, 3, 2, 3);
        this.mouth.setRotationPoint(0.0F, -1.2F, -3.3F);
        setRotation(this.mouth, 0.43004912F, 0.0F, 0.0F);
        this.head.addChild(this.mouth);

        this.leftEye = new ModelRenderer(this, 44, 0);
        this.leftEye.addBox(-1.0F, -1.0F, -0.5F, 2, 1, 1);
        this.leftEye.setRotationPoint(1.7F, -2.4F, -3.9F);
        setRotation(this.leftEye, 0.0F, 0.0F, -0.43633232F);
        this.head.addChild(this.leftEye);

        this.leftEar = new ModelRenderer(this, 56, 0);
        this.leftEar.addBox(0.0F, -2.0F, -1.5F, 2, 2, 1);
        this.leftEar.setRotationPoint(2.0F, -2.9F, -2.0F);
        setRotation(this.leftEar, -0.11728612F, -0.11728612F, -0.35185838F);
        this.head.addChild(this.leftEar);

        this.mane1 = new ModelRenderer(this, 3, 28);
        this.mane1.addBox(-3.5F, 0.0F, 0.0F, 7, 4, 0);
        this.mane1.setRotationPoint(0.0F, 0.5F, -3.0F);
        setRotation(this.mane1, 0.61086524F, 0.0F, 0.0F);
        this.head.addChild(this.mane1);

        this.mane2 = new ModelRenderer(this, 3, 28);
        this.mane2.addBox(-3.5F, 0.0F, 0.0F, 7, 4, 0);
        this.mane2.setRotationPoint(-2.0F, -1.0F, -2.9F);
        setRotation(this.mane2, 0.6981317F, 0.0F, 1.5707964F);
        this.head.addChild(this.mane2);

        this.mane3 = new ModelRenderer(this, 3, 28);
        this.mane3.mirror = true;
        this.mane3.addBox(-3.5F, 0.0F, 0.0F, 7, 4, 0);
        this.mane3.setRotationPoint(2.0F, -1.0F, -2.9F);
        setRotation(this.mane3, 0.61086524F, 0.0F, -1.5707964F);
        this.head.addChild(this.mane3);

        this.mane4 = new ModelRenderer(this, 3, 28);
        this.mane4.addBox(-3.5F, 0.0F, 0.0F, 7, 4, 0);
        this.mane4.setRotationPoint(0.0F, -3.0F, -3.0F);
        setRotation(this.mane4, 0.61086524F, 0.0F, PI);
        this.head.addChild(this.mane4);

        this.mane7 = new ModelRenderer(this, 17, 27);
        this.mane7.addBox(-4.0F, 0.0F, 0.0F, 8, 5, 0);
        this.mane7.setRotationPoint(2.0F, -1.0F, -1.0F);
        setRotation(this.mane7, 0.6981317F, 0.0F, -1.5707964F);
        this.head.addChild(this.mane7);

        this.rightEar = new ModelRenderer(this, 56, 0);
        this.rightEar.addBox(0.0F, -2.0F, -1.5F, 2, 2, 1);
        this.rightEar.setRotationPoint(-2.0F, -2.9F, -2.0F);
        setRotation(this.rightEar, -0.11728612F, -0.11728612F, -1.1644837F);
        this.head.addChild(this.rightEar);

        this.rightEye = new ModelRenderer(this, 44, 0);
        this.rightEye.mirror = true;
        this.rightEye.addBox(-1.0F, -1.0F, -0.5F, 2, 1, 1);
        this.rightEye.setRotationPoint(-1.7F, -2.4F, -3.9F);
        setRotation(this.rightEye, 0.0F, 0.0F, 0.43633232F);
        this.head.addChild(this.rightEye);

        this.mane5 = new ModelRenderer(this, 17, 27);
        this.mane5.addBox(-4.0F, 0.0F, 0.0F, 8, 5, 0);
        this.mane5.setRotationPoint(0.0F, 0.5F, -1.0F);
        setRotation(this.mane5, 0.6981317F, 0.0F, 0.0F);
        this.head.addChild(this.mane5);

        this.mane6 = new ModelRenderer(this, 17, 27);
        this.mane6.addBox(-4.0F, 0.0F, 0.0F, 8, 5, 0);
        this.mane6.setRotationPoint(-2.0F, -1.0F, -1.0F);
        setRotation(this.mane6, 0.6981317F, 0.0F, 1.5707964F);
        this.head.addChild(this.mane6);

        this.mane8 = new ModelRenderer(this, 17, 27);
        this.mane8.addBox(-4.0F, 0.0F, 0.0F, 8, 5, 0);
        this.mane8.setRotationPoint(0.0F, -3.0F, -1.0F);
        setRotation(this.mane8, 0.6981317F, 0.0F, PI);
        this.head.addChild(this.mane8);

        this.jaw = new ModelRenderer(this, 47, 0);
        this.jaw.addBox(-1.5F, -1.0F, -3.0F, 3, 1, 3);
        this.jaw.setRotationPoint(0.0F, 2.0F, 0.3F);
        setRotation(this.jaw, 0.11728612F, 0.0F, 0.0F);
        this.mouth.addChild(this.jaw);

        this.nose = new ModelRenderer(this, 0, 0);
        this.nose.addBox(-1.0F, -1.0F, -1.0F, 2, 1, 1);
        this.nose.setRotationPoint(0.0F, -0.4F, -2.4F);
        this.mouth.addChild(this.nose);

        this.leftBackLeg2 = new ModelRenderer(this, 57, 3);
        this.leftBackLeg2.addBox(-0.5F, 0.0F, -1.0F, 1, 2, 2);
        this.leftBackLeg2.setRotationPoint(-0.01F, 3.3F, -0.1F);
        setRotation(this.leftBackLeg2, 0.86009824F, 0.0F, 0.0F);
        this.leftBackLeg1.addChild(this.leftBackLeg2);

        this.leftBackLeg3 = new ModelRenderer(this, 20, 0);
        this.leftBackLeg3.addBox(-0.5F, 0.0F, -1.0F, 1, 3, 1);
        this.leftBackLeg3.setRotationPoint(0.01F, 2.2F, 0.5F);
        setRotation(this.leftBackLeg3, -1.2119566F, 0.0F, 0.0F);
        this.leftBackLeg2.addChild(this.leftBackLeg3);

        this.snake2 = new ModelRenderer(this, 45, 4);
        this.snake2.addBox(-1.0F, -4.0F, -1.0F, 2, 4, 2);
        this.snake2.setRotationPoint(0.0F, -3.5F, 0.0F);
        setRotation(this.snake2, 0.4691445F, -0.4691445F, 0.0F);
        this.snake1.addChild(this.snake2);

        this.snake3 = new ModelRenderer(this, 45, 4);
        this.snake3.addBox(-1.0F, -4.0F, -1.0F, 2, 4, 2);
        this.snake3.setRotationPoint(0.0F, -3.5F, 0.0F);
        setRotation(this.snake3, 0.4691445F, -0.4691445F, 0.0F);
        this.snake2.addChild(this.snake3);

        this.snake4 = new ModelRenderer(this, 0, 6);
        this.snake4.addBox(-1.0F, -3.0F, 0.0F, 2, 3, 1);
        this.snake4.setRotationPoint(0.0F, -4.0F, 0.3F);
        setRotation(this.snake4, 0.5864306F, 0.0F, 0.0F);
        this.snake3.addChild(this.snake4);

        this.snakeFlap1 = new ModelRenderer(this, 33, 28);
        this.snakeFlap1.addBox(0.0F, -2.0F, 0.0F, 3, 4, 0);
        this.snakeFlap1.setRotationPoint(1.0F, -2.0F, 0.0F);
        setRotation(this.snakeFlap1, 0.0F, 0.17453292F, 0.0F);
        this.snake3.addChild(this.snakeFlap1);

        this.snakeFlap2 = new ModelRenderer(this, 33, 28);
        this.snakeFlap2.mirror = true;
        this.snakeFlap2.addBox(-3.0F, -2.0F, 0.0F, 3, 4, 0);
        this.snakeFlap2.setRotationPoint(-1.0F, -2.0F, 0.0F);
        setRotation(this.snakeFlap2, 0.0F, -0.17453292F, 0.0F);
        this.snake3.addChild(this.snakeFlap2);

        this.snake5 = new ModelRenderer(this, 30, 10);
        this.snake5.addBox(-1.0F, -3.0F, -1.0F, 2, 3, 1);
        this.snake5.setRotationPoint(0.01F, 0.0F, 0.0F);
        setRotation(this.snake5, 0.50823987F, 0.0F, 0.0F);
        this.snake4.addChild(this.snake5);

        this.snakeTooth = new ModelRenderer(this, 0, 31);
        this.snakeTooth.addBox(0.0F, 0.0F, -1.0F, 1, 0, 1);
        this.snakeTooth.setRotationPoint(-0.4F, -2.8F, 0.0F);
        this.snake4.addChild(this.snakeTooth);

        this.snakeHat1 = new ModelRenderer(this, 45, 28);
        this.snakeHat1.addBox(-2.0F, -2.0F, 0.0F, 4, 4, 0);
        this.snakeHat1.setRotationPoint(0.0F, -1.5F, 1.01F);
        this.snake4.addChild(this.snakeHat1);

        this.snakeHat2 = new ModelRenderer(this, 53, 28);
        this.snakeHat2.addBox(-1.0F, -1.0F, 0.0F, 2, 2, 2);
        this.snakeHat1.addChild(this.snakeHat2);

        this.leftLeg2 = new ModelRenderer(this, 54, 13);
        this.leftLeg2.addBox(-1.0F, 0.0F, -1.0F, 2, 4, 2);
        this.leftLeg2.setRotationPoint(0.01F, 3.4F, 0.5F);
        setRotation(this.leftLeg2, -0.74281216F, 0.0F, 0.0F);
        this.leftLeg1.addChild(this.leftLeg2);

        this.leftLeg3 = new ModelRenderer(this, 27, 14);
        this.leftLeg3.addBox(-1.0F, 0.0F, -3.0F, 2, 1, 3);
        this.leftLeg3.setRotationPoint(0.01F, 3.2F, 0.8F);
        setRotation(this.leftLeg3, 0.312763F, 0.0F, 0.0F);
        this.leftLeg2.addChild(this.leftLeg3);

        this.goatHead = new ModelRenderer(this, 37, 14);
        this.goatHead.addBox(-3.0F, -1.5F, -1.5F, 3, 3, 3);
        this.goatHead.setRotationPoint(-3.0F, -1.5F, 0.2F);
        setRotation(this.goatHead, 0.0F, -0.1972222F, -0.938289F);
        this.goatNeck.addChild(this.goatHead);

        this.goatLeftHorn1 = new ModelRenderer(this, 34, 12);
        this.goatLeftHorn1.addBox(-0.5F, -1.0F, 0.0F, 1, 2, 2);
        this.goatLeftHorn1.setRotationPoint(-0.3F, -0.7F, -0.8F);
        setRotation(this.goatLeftHorn1, -0.17453292F, 1.5707964F, 0.0F);
        this.goatHead.addChild(this.goatLeftHorn1);

        this.goatMouth = new ModelRenderer(this, 16, 16);
        this.goatMouth.addBox(-3.0F, -1.5F, -1.0F, 3, 3, 2);
        this.goatMouth.setRotationPoint(-2.5F, 0.0F, 0.0F);
        setRotation(this.goatMouth, 0.0F, 0.0F, -0.19547687F);
        this.goatHead.addChild(this.goatMouth);

        this.goatRightHorn1 = new ModelRenderer(this, 34, 12);
        this.goatRightHorn1.mirror = true;
        this.goatRightHorn1.addBox(-0.5F, -1.0F, 0.0F, 1, 2, 2);
        this.goatRightHorn1.setRotationPoint(-0.3F, -0.7F, 0.8F);
        setRotation(this.goatRightHorn1, -0.17453292F, 1.5707964F, 0.0F);
        this.goatHead.addChild(this.goatRightHorn1);

        this.goatLeftHorn2 = new ModelRenderer(this, 12, 15);
        this.goatLeftHorn2.addBox(-0.5F, -0.5F, 0.0F, 1, 1, 2);
        this.goatLeftHorn2.setRotationPoint(0.01F, 0.2F, 1.4F);
        setRotation(this.goatLeftHorn2, -0.2617994F, 0.0F, 0.0F);
        this.goatLeftHorn1.addChild(this.goatLeftHorn2);

        this.goatBeard = new ModelRenderer(this, 26, 18);
        this.goatBeard.addBox(-3.0F, -1.5F, -1.0F, 3, 2, 2);
        this.goatBeard.setRotationPoint(0.0F, 3.0F, 0.0F);
        this.goatMouth.addChild(this.goatBeard);

        this.goatRing = new ModelRenderer(this, 35, 28);
        this.goatRing.addBox(-3.0F, 0.0F, -2.0F, 3, 0, 4);
        this.goatRing.setRotationPoint(-2.0F, -1.0F, 0.0F);
        setRotation(this.goatRing, 0.0F, 0.0F, -0.5864306F);
        this.goatMouth.addChild(this.goatRing);

        this.goatRightHorn2 = new ModelRenderer(this, 12, 15);
        this.goatRightHorn2.mirror = true;
        this.goatRightHorn2.addBox(-0.5F, -0.5F, 0.0F, 1, 1, 2);
        this.goatRightHorn2.setRotationPoint(0.01F, 0.2F, 1.4F);
        setRotation(this.goatRightHorn2, -0.2617994F, 0.0F, 0.0F);
        this.goatRightHorn1.addChild(this.goatRightHorn2);

        this.rightBackLeg2 = new ModelRenderer(this, 57, 3);
        this.rightBackLeg2.mirror = true;
        this.rightBackLeg2.addBox(-0.5F, 0.0F, -1.0F, 1, 2, 2);
        this.rightBackLeg2.setRotationPoint(-0.01F, 3.3F, -0.1F);
        setRotation(this.rightBackLeg2, 0.86009824F, 0.0F, 0.0F);
        this.rightBackLeg1.addChild(this.rightBackLeg2);

        this.rightBackLeg3 = new ModelRenderer(this, 20, 0);
        this.rightBackLeg3.mirror = true;
        this.rightBackLeg3.addBox(-0.5F, 0.0F, -1.0F, 1, 3, 1);
        this.rightBackLeg3.setRotationPoint(0.01F, 2.2F, 0.5F);
        setRotation(this.rightBackLeg3, -1.2119566F, 0.0F, 0.0F);
        this.rightBackLeg2.addChild(this.rightBackLeg3);

        this.rightLeg2 = new ModelRenderer(this, 54, 13);
        this.rightLeg2.mirror = true;
        this.rightLeg2.addBox(-1.0F, 0.0F, -1.0F, 2, 4, 2);
        this.rightLeg2.setRotationPoint(0.01F, 3.4F, 0.5F);
        setRotation(this.rightLeg2, -0.74281216F, 0.0F, 0.0F);
        this.rightLeg1.addChild(this.rightLeg2);

        this.rightLeg3 = new ModelRenderer(this, 27, 14);
        this.rightLeg3.mirror = true;
        this.rightLeg3.addBox(-1.0F, 0.0F, -3.0F, 2, 1, 3);
        this.rightLeg3.setRotationPoint(0.01F, 3.2F, 0.8F);
        setRotation(this.rightLeg3, 0.312763F, 0.0F, 0.0F);
        this.rightLeg2.addChild(this.rightLeg3);
    }

    @Override
    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks,
            float netHeadYaw, float headPitch, float scale) {
        this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);
        this.body.render(scale);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
            float headPitch, float scaleFactor, Entity entityIn) {
        EntityChimeraFamiliar chimera = (EntityChimeraFamiliar) entityIn;
        boolean snakeAttacking = chimera.getAttackProgress(0.0F) > 0.0F
                && chimera.getAttacker() == EntityChimeraFamiliar.SNAKE_ATTACKER;

        this.showModels(chimera);
        this.snake2.rotateAngleY = 0.0F;
        this.snake3.rotateAngleY = 0.0F;
        this.leftLeg3.rotateAngleX = 0.31F;
        this.rightLeg3.rotateAngleX = 0.31F;
        this.body.rotateAngleX = 0.0F;
        if (!snakeAttacking) {
            this.snake1.rotateAngleX = -0.66F;
            this.snake2.rotateAngleX = 0.47F;
            this.snake3.rotateAngleX = 0.47F;
            this.snake4.rotateAngleX = 0.59F;
        }
        this.goatHead.rotateAngleZ = -0.94F;
        this.leftLeg1.rotateAngleY = 0.0F;
        this.rightLeg1.rotateAngleY = 0.0F;

        this.head.rotateAngleY = toRads(netHeadYaw) * 0.7F;
        this.head.rotateAngleX = toRads(headPitch) * 0.7F;
        this.snake4.rotateAngleY = toRads(netHeadYaw) * 0.3F;
        this.snake4.rotateAngleZ = -toRads(netHeadYaw) * 0.3F;

        this.snake1.rotateAngleZ = MathHelper.cos(limbSwing * 0.3F) * 0.1F * limbSwingAmount;
        this.snake2.rotateAngleZ = MathHelper.cos(limbSwing * 0.3F) * 0.1F * limbSwingAmount;
        this.snake3.rotateAngleZ = MathHelper.cos(limbSwing * 0.3F) * 0.25F * limbSwingAmount;
        this.snake4.rotateAngleZ = -MathHelper.cos(limbSwing * 0.3F) * 0.25F * limbSwingAmount;
        this.snake5.rotateAngleX = MathHelper.cos(ageInTicks * 0.1F) * toRads(15.0F) + toRads(15.0F);

        this.rightBackLeg1.rotateAngleX = MathHelper.cos(limbSwing * 0.7F) * 0.8F * limbSwingAmount - 0.23F;
        this.leftBackLeg1.rotateAngleX = MathHelper.cos(limbSwing * 0.7F + PI) * 0.8F * limbSwingAmount - 0.23F;
        this.rightLeg1.rotateAngleX = MathHelper.cos(limbSwing * 0.7F + PI) * 0.8F * limbSwingAmount + 0.43F;
        this.leftLeg1.rotateAngleX = MathHelper.cos(limbSwing * 0.7F) * 0.8F * limbSwingAmount + 0.43F;

        if (chimera.isSitting()) {
            this.leftLeg1.rotateAngleX = -toRads(15.0F);
            this.rightLeg1.rotateAngleX = -toRads(15.0F);
            this.leftLeg3.rotateAngleX = toRads(30.0F);
            this.rightLeg3.rotateAngleX = toRads(30.0F);
            this.leftBackLeg1.rotateAngleX = -toRads(62.0F);
            this.rightBackLeg1.rotateAngleX = -toRads(62.0F);
            this.body.rotateAngleX = -toRads(20.0F);
            this.leftLeg1.rotateAngleY = -toRads(10.0F);
            this.rightLeg1.rotateAngleY = toRads(10.0F);
            this.snake1.rotateAngleZ = toRads(80.0F);
            this.snake2.rotateAngleZ = 0.0F;
            this.snake3.rotateAngleZ = -toRads(20.0F);
            this.snake4.rotateAngleZ = -toRads(65.0F);
            this.snake2.rotateAngleY = -toRads(20.0F);
            this.snake3.rotateAngleY = -toRads(20.0F);
            this.snake4.rotateAngleY = -toRads(20.0F);
            this.snake2.rotateAngleX = toRads(40.0F);
            this.snake3.rotateAngleX = toRads(50.0F);
            this.snake4.rotateAngleX = toRads(60.0F);
            this.snake5.rotateAngleX = toRads(7.0F);
        }

        if (chimera.isPartying()) {
            this.head.rotateAngleX = MathHelper.cos(ageInTicks * 0.4F) * toRads(30.0F);
            this.goatHead.rotateAngleZ = -MathHelper.cos(ageInTicks * 0.4F) * toRads(15.0F) - 0.94F;
            this.goatHead.rotateAngleY = -0.20F;
            this.snake4.rotateAngleX = MathHelper.cos(ageInTicks * 0.4F) * toRads(30.0F) + 0.59F;
        }
    }

    @Override
    public void setLivingAnimations(EntityLivingBase entitylivingbaseIn, float limbSwing, float limbSwingAmount,
            float partialTick) {
        EntityChimeraFamiliar chimera = (EntityChimeraFamiliar) entitylivingbaseIn;
        this.goatMouth.rotateAngleZ = chimera.getNoseGoatRot(partialTick) - 0.2F;

        float attackProgress = chimera.getAttackProgress(partialTick);
        if (attackProgress <= 0.0F) {
            return;
        }
        switch (chimera.getAttacker()) {
            case EntityChimeraFamiliar.GOAT_ATTACKER:
                this.goatNeck.rotateAngleZ = -MathHelper.sin(attackProgress * PI * 2.0F) * toRads(30.0F) + 0.9F;
                break;
            case EntityChimeraFamiliar.SNAKE_ATTACKER:
                this.snake1.rotateAngleX = MathHelper.sin(attackProgress * PI) * toRads(25.0F) - 0.66F;
                this.snake2.rotateAngleX = MathHelper.sin(attackProgress * PI) * toRads(25.0F) + 0.47F;
                this.snake3.rotateAngleX = MathHelper.sin(attackProgress * PI) * toRads(25.0F) + 0.47F;
                this.snake4.rotateAngleX = -MathHelper.sin(attackProgress * PI) * toRads(30.0F) + 0.59F;
                break;
            default:
                break;
        }
    }

    private void showModels(EntityChimeraFamiliar chimera) {
        this.snakeFlap1.showModel = chimera.hasFlaps();
        this.snakeFlap2.showModel = chimera.hasFlaps();
        this.goatRing.showModel = chimera.hasRing();
        this.snakeHat1.showModel = chimera.hasHat();
        this.goatNeck.showModel = chimera.hasGoat();
        this.goatBeard.showModel = chimera.hasBeard();
    }

    private static void setRotation(ModelRenderer renderer, float x, float y, float z) {
        renderer.rotateAngleX = x;
        renderer.rotateAngleY = y;
        renderer.rotateAngleZ = z;
    }

    private static float toRads(float degrees) {
        return (float) Math.toRadians(degrees);
    }
}
