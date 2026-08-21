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
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT
 * OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package com.occultism.occultism.client.model;

import com.occultism.occultism.entity.EntityFairyFamiliar;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Created using Tabula 8.0.0.
 */
@SideOnly(Side.CLIENT)
public class ModelFairyFamiliar extends ModelBase {

    private static final float PI = (float) Math.PI;

    public final ModelRenderer body;
    public final ModelRenderer head;
    public final ModelRenderer tail1;
    public final ModelRenderer leftArm;
    public final ModelRenderer leftLeg1;
    public final ModelRenderer leftWing;
    public final ModelRenderer rightLeg1;
    public final ModelRenderer rightArm;
    public final ModelRenderer rightWing;
    public final ModelRenderer nose;
    public final ModelRenderer leftHorn1;
    public final ModelRenderer leftEar1;
    public final ModelRenderer leftEye;
    public final ModelRenderer rightEye;
    public final ModelRenderer rightEar1;
    public final ModelRenderer rightHorn1;
    public final ModelRenderer whisker1;
    public final ModelRenderer whisker2;
    public final ModelRenderer whisker3;
    public final ModelRenderer whisker4;
    public final ModelRenderer tooth1;
    public final ModelRenderer tooth2;
    public final ModelRenderer tooth3;
    public final ModelRenderer leftHorn2;
    public final ModelRenderer flower;
    public final ModelRenderer leftEar2;
    public final ModelRenderer rightEar2;
    public final ModelRenderer rightHorn2;
    public final ModelRenderer tail2;
    public final ModelRenderer tail3;
    public final ModelRenderer leftWand;
    public final ModelRenderer leftLeg2;
    public final ModelRenderer leftLeg3;
    public final ModelRenderer rightLeg2;
    public final ModelRenderer rightLeg3;
    public final ModelRenderer rightWand;

    public ModelFairyFamiliar() {
        this.textureWidth = 64;
        this.textureHeight = 32;

        this.body = new ModelRenderer(this, 0, 0);
        this.body.addBox(-2.5F, -6.0F, -1.5F, 5, 6, 3);
        this.body.setRotationPoint(0.0F, 20.0F, 0.0F);

        this.head = new ModelRenderer(this, 16, 0);
        this.head.addBox(-2.5F, -5.0F, -2.5F, 5, 5, 5);
        this.head.setRotationPoint(0.0F, -6.0F, -0.5F);
        this.body.addChild(this.head);

        this.tail1 = new ModelRenderer(this, 45, 4);
        this.tail1.addBox(-1.0F, -1.0F, 0.0F, 2, 2, 3);
        this.tail1.setRotationPoint(0.0F, -1.0F, 1.0F);
        setRotateAngle(this.tail1, -0.5082398928281348F, 0.0F, 0.0F);
        this.body.addChild(this.tail1);

        this.leftArm = new ModelRenderer(this, 53, 8);
        this.leftArm.addBox(0.0F, -1.0F, -1.0F, 2, 6, 2);
        this.leftArm.setRotationPoint(2.5F, -5.0F, 0.0F);
        this.body.addChild(this.leftArm);

        this.leftLeg1 = new ModelRenderer(this, 0, 9);
        this.leftLeg1.addBox(-1.0F, 0.0F, -1.0F, 2, 5, 2);
        this.leftLeg1.setRotationPoint(1.2F, -0.8F, 0.0F);
        this.body.addChild(this.leftLeg1);

        this.leftWing = new ModelRenderer(this, 0, 21);
        this.leftWing.addBox(0.0F, -5.5F, 0.0F, 10, 11, 0);
        this.leftWing.setRotationPoint(0.0F, -4.0F, 1.0F);
        setRotateAngle(this.leftWing, 0.0F, -0.5864306020384839F, 0.0F);
        this.body.addChild(this.leftWing);

        this.rightLeg1 = new ModelRenderer(this, 0, 9);
        this.rightLeg1.mirror = true;
        this.rightLeg1.addBox(-1.0F, 0.0F, -1.0F, 2, 5, 2);
        this.rightLeg1.setRotationPoint(-1.2F, -0.8F, 0.0F);
        this.body.addChild(this.rightLeg1);

        this.rightArm = new ModelRenderer(this, 53, 8);
        this.rightArm.mirror = true;
        this.rightArm.addBox(-2.0F, -1.0F, -1.0F, 2, 6, 2);
        this.rightArm.setRotationPoint(-2.5F, -5.0F, 0.0F);
        this.body.addChild(this.rightArm);

        this.rightWing = new ModelRenderer(this, 0, 21);
        this.rightWing.mirror = true;
        this.rightWing.addBox(-10.0F, -5.5F, 0.0F, 10, 11, 0);
        this.rightWing.setRotationPoint(0.0F, -4.0F, 1.0F);
        setRotateAngle(this.rightWing, 0.0F, 0.5864306020384839F, 0.0F);
        this.body.addChild(this.rightWing);

        this.nose = new ModelRenderer(this, 36, 0);
        this.nose.addBox(-1.5F, -1.5F, -3.0F, 3, 3, 3);
        this.nose.setRotationPoint(0.0F, -1.5F, -2.5F);
        this.head.addChild(this.nose);

        this.leftHorn1 = new ModelRenderer(this, 48, 0);
        this.leftHorn1.addBox(-0.5F, -2.0F, -1.0F, 1, 2, 2);
        this.leftHorn1.setRotationPoint(2.2F, -4.5F, 1.0F);
        setRotateAngle(this.leftHorn1, -0.3909537457888271F, 0.0F, 0.0F);
        this.head.addChild(this.leftHorn1);

        this.leftEar1 = new ModelRenderer(this, 0, 16);
        this.leftEar1.addBox(0.0F, 0.0F, 0.0F, 2, 1, 0);
        this.leftEar1.setRotationPoint(2.1F, -3.4F, 1.9F);
        setRotateAngle(this.leftEar1, 0.3127630032889644F, -0.27366763203903305F,
                -0.4300491170387584F);
        this.head.addChild(this.leftEar1);

        this.leftEye = new ModelRenderer(this, 54, 0);
        this.leftEye.addBox(-0.5F, -1.0F, -1.0F, 1, 2, 2);
        this.leftEye.setRotationPoint(2.5F, -3.0F, -0.7F);
        setRotateAngle(this.leftEye, 0.3127630032889644F, 0.0F, 0.0F);
        this.head.addChild(this.leftEye);

        this.rightEye = new ModelRenderer(this, 54, 0);
        this.rightEye.mirror = true;
        this.rightEye.addBox(-0.5F, -1.0F, -1.0F, 1, 2, 2);
        this.rightEye.setRotationPoint(-2.5F, -3.0F, -0.7F);
        setRotateAngle(this.rightEye, 0.3127630032889644F, 0.0F, 0.0F);
        this.head.addChild(this.rightEye);

        this.rightEar1 = new ModelRenderer(this, 0, 16);
        this.rightEar1.mirror = true;
        this.rightEar1.addBox(-2.0F, 0.0F, 0.0F, 2, 1, 0);
        this.rightEar1.setRotationPoint(-2.1F, -3.4F, 1.9F);
        setRotateAngle(this.rightEar1, 0.3127630032889644F, 0.27366763203903305F,
                0.4300491170387584F);
        this.head.addChild(this.rightEar1);

        this.rightHorn1 = new ModelRenderer(this, 48, 0);
        this.rightHorn1.mirror = true;
        this.rightHorn1.addBox(-0.5F, -2.0F, -1.0F, 1, 2, 2);
        this.rightHorn1.setRotationPoint(-2.2F, -4.5F, 1.0F);
        setRotateAngle(this.rightHorn1, -0.3909537457888271F, 0.0F, 0.0F);
        this.head.addChild(this.rightHorn1);

        this.whisker1 = new ModelRenderer(this, 0, 17);
        this.whisker1.addBox(0.0F, -0.5F, 0.0F, 3, 2, 0);
        this.whisker1.setRotationPoint(1.0F, 0.4F, -1.0F);
        setRotateAngle(this.whisker1, 0.0F, 0.3127630032889644F, 0.3909537457888271F);
        this.nose.addChild(this.whisker1);

        this.whisker2 = new ModelRenderer(this, 0, 17);
        this.whisker2.addBox(0.0F, -0.5F, 0.0F, 3, 2, 0);
        this.whisker2.setRotationPoint(1.0F, -0.4F, -2.0F);
        setRotateAngle(this.whisker2, 0.03909537541112055F, 0.3909537457888271F,
                -0.27366763203903305F);
        this.nose.addChild(this.whisker2);

        this.whisker3 = new ModelRenderer(this, 0, 17);
        this.whisker3.mirror = true;
        this.whisker3.addBox(-3.0F, -0.5F, 0.0F, 3, 2, 0);
        this.whisker3.setRotationPoint(-1.0F, -0.4F, -2.0F);
        setRotateAngle(this.whisker3, 0.03909537541112055F, -0.3909537457888271F,
                0.27366763203903305F);
        this.nose.addChild(this.whisker3);

        this.whisker4 = new ModelRenderer(this, 0, 17);
        this.whisker4.mirror = true;
        this.whisker4.addBox(-3.0F, -0.5F, 0.0F, 3, 2, 0);
        this.whisker4.setRotationPoint(-1.0F, 0.4F, -1.0F);
        setRotateAngle(this.whisker4, 0.0F, -0.3127630032889644F, -0.3909537457888271F);
        this.nose.addChild(this.whisker4);

        this.tooth1 = new ModelRenderer(this, 31, 0);
        this.tooth1.addBox(-0.5F, -0.5F, -0.5F, 1, 1, 1);
        this.tooth1.setRotationPoint(1.4F, 0.4F, -1.5F);
        this.nose.addChild(this.tooth1);

        this.tooth2 = new ModelRenderer(this, 31, 0);
        this.tooth2.addBox(-0.5F, -0.5F, -0.5F, 1, 1, 1);
        this.tooth2.setRotationPoint(-1.4F, 0.5F, -1.7F);
        this.nose.addChild(this.tooth2);

        this.tooth3 = new ModelRenderer(this, 31, 0);
        this.tooth3.addBox(-0.5F, -0.5F, -0.5F, 1, 1, 1);
        this.tooth3.setRotationPoint(0.7F, 0.6F, -2.9F);
        this.nose.addChild(this.tooth3);

        this.leftHorn2 = new ModelRenderer(this, 13, 0);
        this.leftHorn2.addBox(-0.5F, -2.0F, -0.5F, 1, 2, 1);
        this.leftHorn2.setRotationPoint(0.01F, -1.5F, 0.0F);
        setRotateAngle(this.leftHorn2, -0.35185837453889574F, 0.0F, 0.0F);
        this.leftHorn1.addChild(this.leftHorn2);

        this.flower = new ModelRenderer(this, 25, 7);
        this.flower.addBox(0.0F, -1.5F, -1.5F, 0, 3, 3);
        this.flower.setRotationPoint(0.52F, -1.0F, 0.0F);
        setRotateAngle(this.flower, 0.782431135626991F, 0.0F, 0.0F);
        this.leftHorn1.addChild(this.flower);

        this.leftEar2 = new ModelRenderer(this, 0, 16);
        this.leftEar2.addBox(0.0F, 0.0F, 0.0F, 2, 1, 0);
        this.leftEar2.setRotationPoint(0.0F, 0.0F, 0.0F);
        setRotateAngle(this.leftEar2, -2.1111502472333745F, 0.0F, 0.0F);
        this.leftEar1.addChild(this.leftEar2);

        this.rightEar2 = new ModelRenderer(this, 0, 16);
        this.rightEar2.mirror = true;
        this.rightEar2.addBox(-2.0F, 0.0F, 0.0F, 2, 1, 0);
        this.rightEar2.setRotationPoint(0.0F, 0.0F, 0.0F);
        setRotateAngle(this.rightEar2, -2.1111502472333745F, 0.0F, 0.0F);
        this.rightEar1.addChild(this.rightEar2);

        this.rightHorn2 = new ModelRenderer(this, 13, 0);
        this.rightHorn2.mirror = true;
        this.rightHorn2.addBox(-0.5F, -2.0F, -0.5F, 1, 2, 1);
        this.rightHorn2.setRotationPoint(0.01F, -1.5F, 0.0F);
        setRotateAngle(this.rightHorn2, -0.35185837453889574F, 0.0F, 0.0F);
        this.rightHorn1.addChild(this.rightHorn2);

        this.tail2 = new ModelRenderer(this, 36, 6);
        this.tail2.addBox(-1.0F, -0.5F, 0.0F, 2, 1, 3);
        this.tail2.setRotationPoint(0.01F, 0.0F, 2.5F);
        setRotateAngle(this.tail2, 0.27366763203903305F, 0.0F, 0.0F);
        this.tail1.addChild(this.tail2);

        this.tail3 = new ModelRenderer(this, 55, 4);
        this.tail3.addBox(-0.5F, -0.5F, 0.0F, 1, 1, 3);
        this.tail3.setRotationPoint(0.0F, 0.0F, 2.5F);
        setRotateAngle(this.tail3, 0.27366763203903305F, 0.0F, 0.0F);
        this.tail2.addChild(this.tail3);

        this.leftWand = new ModelRenderer(this, 8, 9);
        this.leftWand.addBox(-0.5F, -0.5F, -5.0F, 1, 1, 5);
        this.leftWand.setRotationPoint(1.0F, 4.0F, -0.2F);
        this.leftArm.addChild(this.leftWand);

        this.leftLeg2 = new ModelRenderer(this, 17, 0);
        this.leftLeg2.addBox(0.0F, 0.0F, -1.0F, 1, 1, 1);
        this.leftLeg2.setRotationPoint(-0.3F, 4.01F, -0.3F);
        setRotateAngle(this.leftLeg2, 0.0F, 0.7853981633974483F, 0.0F);
        this.leftLeg1.addChild(this.leftLeg2);

        this.leftLeg3 = new ModelRenderer(this, 17, 0);
        this.leftLeg3.addBox(0.0F, 0.0F, -1.0F, 1, 1, 1);
        this.leftLeg3.setRotationPoint(0.3F, 4.01F, -0.3F);
        setRotateAngle(this.leftLeg3, 0.0F, 0.7853981633974483F, 0.0F);
        this.leftLeg1.addChild(this.leftLeg3);

        this.rightLeg2 = new ModelRenderer(this, 17, 0);
        this.rightLeg2.mirror = true;
        this.rightLeg2.addBox(0.0F, 0.0F, -1.0F, 1, 1, 1);
        this.rightLeg2.setRotationPoint(-0.3F, 4.01F, -0.3F);
        setRotateAngle(this.rightLeg2, 0.0F, 0.7853981633974483F, 0.0F);
        this.rightLeg1.addChild(this.rightLeg2);

        this.rightLeg3 = new ModelRenderer(this, 17, 0);
        this.rightLeg3.addBox(0.0F, 0.0F, -1.0F, 1, 1, 1);
        this.rightLeg3.setRotationPoint(0.3F, 4.01F, -0.3F);
        setRotateAngle(this.rightLeg3, 0.0F, 0.7853981633974483F, 0.0F);
        this.rightLeg1.addChild(this.rightLeg3);

        this.rightWand = new ModelRenderer(this, 8, 9);
        this.rightWand.mirror = true;
        this.rightWand.addBox(-0.5F, -0.5F, -5.0F, 1, 1, 5);
        this.rightWand.setRotationPoint(-1.0F, 4.0F, -0.2F);
        this.rightArm.addChild(this.rightWand);
    }

    @Override
    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks,
            float netHeadYaw, float headPitch, float scale) {
        this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);
        this.body.render(scale);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks,
            float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
        EntityFairyFamiliar fairy = (EntityFairyFamiliar) entityIn;
        this.showModels(fairy);

        float partialTicks = ageInTicks - fairy.ticksExisted;
        ModelRenderer mainArm = this.getMainArm(fairy);

        this.leftArm.rotateAngleY = 0.0F;
        this.rightArm.rotateAngleY = 0.0F;
        this.leftWand.rotateAngleX = 0.0F;
        this.rightWand.rotateAngleX = 0.0F;

        this.head.rotateAngleX = 0.0F;
        this.head.rotateAngleY = this.toRads(netHeadYaw) * 0.8F;
        this.head.rotateAngleZ = this.toRads(headPitch) * 0.8F;

        float animationHeight = fairy.getWingRot(partialTicks);
        this.leftWing.rotateAngleY = animationHeight * this.toRads(15.0F) - 0.59F;
        this.rightWing.rotateAngleY = -animationHeight * this.toRads(15.0F) + 0.59F;

        float bodyRot = limbSwingAmount * this.toRads(100.0F);
        this.body.rotateAngleX = bodyRot;
        this.leftArm.rotateAngleX = -bodyRot * 3.0F
                + MathHelper.cos(ageInTicks * 0.2F) * this.toRads(10.0F);
        this.rightArm.rotateAngleX = -bodyRot * 3.0F
                + MathHelper.cos(ageInTicks * 0.2F + PI) * this.toRads(10.0F);
        this.leftLeg1.rotateAngleX = this.body.rotateAngleX;
        this.rightLeg1.rotateAngleX = this.body.rotateAngleX;

        this.tail1.rotateAngleX = -0.51F + MathHelper.cos(ageInTicks * 0.2F) * this.toRads(10.0F);
        this.tail2.rotateAngleX = 0.27F + MathHelper.cos(ageInTicks * 0.2F) * this.toRads(10.0F);
        this.tail3.rotateAngleX = 0.27F + MathHelper.cos(ageInTicks * 0.2F) * this.toRads(10.0F);

        if (fairy.hasMagicTarget()) {
            this.body.rotateAngleX = this.toRads(20.0F);
            this.setRotateAngle(this.head, 0.0F, 0.0F, 0.0F);
            this.leftArm.rotateAngleX = this.toRads(-80.0F)
                    + MathHelper.cos(ageInTicks * 0.2F) * this.toRads(20.0F);
            this.rightArm.rotateAngleX = this.toRads(-80.0F)
                    + MathHelper.cos(ageInTicks * 0.2F) * this.toRads(20.0F);
            this.leftLeg1.rotateAngleX = this.toRads(20.0F);
            this.rightLeg1.rotateAngleX = this.toRads(20.0F);
        } else if (fairy.isPartying()) {
            this.body.rotateAngleX = 0.0F;
            mainArm.rotateAngleX = fairy.getPartyArmRotX(partialTicks);
            mainArm.rotateAngleY = fairy.getPartyArmRotY(partialTicks);
        } else if (fairy.isSitting()) {
            this.body.rotateAngleX = this.toRads(90.0F);
            this.head.rotateAngleX = this.toRads(-10.0F);
            this.head.rotateAngleY = 0.0F;
            this.head.rotateAngleZ = 0.0F;
            this.leftArm.rotateAngleX = this.toRads(-80.0F)
                    + MathHelper.cos(ageInTicks * 0.1F) * this.toRads(10.0F);
            this.rightArm.rotateAngleX = this.toRads(-80.0F)
                    + MathHelper.cos(ageInTicks * 0.1F + PI) * this.toRads(10.0F);
            this.leftWand.rotateAngleX = this.toRads(60.0F);
            this.rightWand.rotateAngleX = this.toRads(60.0F);
            this.leftLeg1.rotateAngleX = this.toRads(-80.0F)
                    + MathHelper.cos(ageInTicks * 0.1F + PI) * this.toRads(10.0F);
            this.rightLeg1.rotateAngleX = this.toRads(-80.0F)
                    + MathHelper.cos(ageInTicks * 0.1F) * this.toRads(10.0F);
            this.tail1.rotateAngleX = this.toRads(-130.0F)
                    + MathHelper.cos(ageInTicks * 0.1F) * this.toRads(5.0F);
            this.tail2.rotateAngleX = this.toRads(-15.0F)
                    + MathHelper.cos(ageInTicks * 0.05F) * this.toRads(5.0F);
            this.tail3.rotateAngleX = this.toRads(-15.0F)
                    + MathHelper.cos(ageInTicks * 0.05F) * this.toRads(5.0F);
        }

        float supportAnim = fairy.getSupportAnim(partialTicks);
        if (supportAnim != 0.0F) {
            mainArm.rotateAngleX = this.toRads(-80.0F)
                    + MathHelper.sin(supportAnim * PI * 3.0F) * this.toRads(35.0F);
            mainArm.rotateAngleY = MathHelper.cos(supportAnim * PI * 3.0F) * this.toRads(35.0F);
        }
    }

    private float toRads(float degrees) {
        return PI / 180.0F * degrees;
    }

    private ModelRenderer getMainArm(EntityFairyFamiliar fairy) {
        return fairy.isLeftHanded() ? this.leftArm : this.rightArm;
    }

    private void showModels(EntityFairyFamiliar fairy) {
        boolean hasTeeth = fairy.hasTeeth();
        boolean isLeftHanded = fairy.isLeftHanded();

        this.flower.showModel = fairy.hasFlower();
        this.tooth1.showModel = hasTeeth;
        this.tooth2.showModel = hasTeeth;
        this.tooth3.showModel = hasTeeth;
        this.whisker1.showModel = !hasTeeth;
        this.whisker2.showModel = !hasTeeth;
        this.whisker3.showModel = !hasTeeth;
        this.whisker4.showModel = !hasTeeth;
        this.leftWand.showModel = isLeftHanded;
        this.rightWand.showModel = !isLeftHanded;
    }

    public void setRotateAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
