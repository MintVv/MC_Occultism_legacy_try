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
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.occultism.occultism.client.model;

import com.occultism.occultism.entity.EntityBeholderFamiliar;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;

public class ModelBeholderFamiliar extends ModelBase {

    private static final float PI = (float) Math.PI;

    public final ModelRenderer head;
    public final ModelRenderer eye11;
    public final ModelRenderer mouth;
    public final ModelRenderer bigEye;
    public final ModelRenderer spike1;
    public final ModelRenderer spike2;
    public final ModelRenderer upperTeeth1;
    public final ModelRenderer upperTeeth2;
    public final ModelRenderer spike3;
    public final ModelRenderer spike4;
    public final ModelRenderer spike5;
    public final ModelRenderer spike6;
    public final ModelRenderer upperTeeth3;
    public final ModelRenderer eye21;
    public final ModelRenderer eye31;
    public final ModelRenderer eye41;
    public final ModelRenderer glasses;
    public final ModelRenderer eye12;
    public final ModelRenderer eye13;
    public final ModelRenderer eye14;
    public final ModelRenderer eye15;
    public final ModelRenderer beard;
    public final ModelRenderer lowerTeeth1;
    public final ModelRenderer lowerTeeth2;
    public final ModelRenderer lowerTeeth3;
    public final ModelRenderer tongue;
    public final ModelRenderer bigPupil;
    public final ModelRenderer eye22;
    public final ModelRenderer eye23;
    public final ModelRenderer eye24;
    public final ModelRenderer eye25;
    public final ModelRenderer eye32;
    public final ModelRenderer eye33;
    public final ModelRenderer eye34;
    public final ModelRenderer eye35;
    public final ModelRenderer eye42;
    public final ModelRenderer eye43;
    public final ModelRenderer eye44;
    public final ModelRenderer eye45;

    public ModelBeholderFamiliar() {
        this.textureWidth = 64;
        this.textureHeight = 64;

        this.head = part(0, 0, -4.0F, -4.0F, -4.0F, 8, 7, 8, 0.0F, 13.0F, 0.0F);
        this.eye11 = part(8, 4, -0.5F, -2.0F, -0.5F, 1, 2, 1, -2.0F, -3.9F, 2.0F);
        this.mouth = part(0, 15, -3.5F, 0.0F, -7.0F, 7, 3, 7, 0.0F, 3.0F, 3.5F);
        this.setRotateAngle(this.mouth, 0.3490658503988659F, 0.0F, 0.0F);
        this.bigEye = part(0, 25, -3.0F, -3.0F, 0.0F, 6, 5, 1, 0.0F, 0.0F, -4.7F);
        this.spike1 = part(32, 0, -1.0F, -1.0F, -1.0F, 2, 2, 2, 4.0F, -1.6F, 1.8F);
        this.setRotateAngle(this.spike1, 0.0F, 0.0F, -0.7853981633974483F);
        this.spike2 = part(32, 0, -1.0F, -1.0F, -1.0F, 2, 2, 2, 4.0F, 0.2F, -1.5F);
        this.setRotateAngle(this.spike2, 0.27366763203903305F, -0.3127630032889644F,
                0.7853981633974483F);
        this.upperTeeth1 = part(0, 31, -4.0F, 0.0F, -4.0F, 8, 1, 8, 0.0F, 3.0F, 0.0F);
        this.upperTeeth2 = part(0, 40, -3.5F, 0.0F, -3.5F, 7, 1, 7, 0.01F, 3.0F, 0.01F);
        this.spike3 = part(32, 0, -1.0F, -1.0F, -1.0F, 2, 2, 2, 1.0F, -1.6F, 4.0F);
        this.setRotateAngle(this.spike3, -0.7853981633974483F, 0.0F, 0.4363323129985824F);
        this.spike4 = part(32, 0, -1.0F, -1.0F, -1.0F, 2, 2, 2, -2.0F, 0.4F, 4.0F);
        this.setRotateAngle(this.spike4, -0.7853981633974483F, 0.0F, -0.5235987755982988F);
        this.spike5 = part(32, 0, -1.0F, -1.0F, -1.0F, 2, 2, 2, -4.0F, 0.2F, -1.5F);
        this.setRotateAngle(this.spike5, 0.27366763203903305F, -0.3127630032889644F,
                0.7853981633974483F);
        this.spike6 = part(32, 0, -1.0F, -1.0F, -1.0F, 2, 2, 2, -4.0F, -1.6F, 1.8F);
        this.setRotateAngle(this.spike6, 0.0F, 0.0F, -0.7853981633974483F);
        this.upperTeeth3 = part(28, 41, -3.0F, 0.0F, -3.0F, 6, 1, 6, 0.0F, 3.0F, 0.0F);
        this.eye21 = part(8, 4, -0.5F, -2.0F, -0.5F, 1, 2, 1, 2.4F, -3.9F, 2.3F);
        this.eye31 = part(8, 4, -0.5F, -2.0F, -0.5F, 1, 2, 1, 2.8F, -3.9F, -2.3F);
        this.eye41 = part(8, 4, -0.5F, -2.0F, -0.5F, 1, 2, 1, -1.5F, -3.9F, -2.7F);
        this.glasses = part(28, 23, -4.5F, -2.0F, -4.5F, 9, 4, 9, 0.0F, -1.2F, -1.5F);

        this.eye12 = part(8, 4, -0.5F, -2.0F, -0.5F, 1, 2, 1, 0.01F, -1.9F, 0.0F);
        this.eye13 = part(8, 4, -0.5F, -2.0F, -0.5F, 1, 2, 1, 0.0F, -1.9F, 0.0F);
        this.eye14 = part(0, 0, -1.0F, -2.0F, -1.0F, 2, 2, 2, 0.0F, -1.9F, 0.0F);
        this.eye15 = part(0, 4, -0.5F, -0.5F, -0.5F, 1, 1, 1, 0.0F, -1.9F, 0.0F);
        this.beard = part(28, 15, -3.0F, 0.0F, -3.0F, 6, 2, 6, 0.0F, 3.0F, -3.5F);
        this.lowerTeeth1 = part(0, 48, -3.5F, 0.0F, -7.0F, 7, 1, 7, 0.0F, -1.0F, 0.0F);
        this.lowerTeeth2 = part(0, 56, -3.0F, 0.0F, -6.0F, 6, 1, 6, 0.0F, -1.0F, -0.5F);
        this.lowerTeeth3 = part(24, 57, -2.5F, 0.0F, -5.0F, 5, 1, 5, 0.0F, -1.0F, -1.0F);
        this.tongue = part(40, 0, -1.0F, 0.0F, -4.0F, 2, 1, 4, 0.0F, -1.0F, -5.0F);
        this.bigPupil = part(24, 0, -1.5F, -1.5F, 0.0F, 3, 3, 1, 0.0F, -1.0F, -0.7F);

        this.eye22 = part(8, 4, -0.5F, -2.0F, -0.5F, 1, 2, 1, 0.01F, -1.9F, 0.0F);
        this.eye23 = part(8, 4, -0.5F, -2.0F, -0.5F, 1, 2, 1, 0.0F, -1.9F, 0.0F);
        this.eye24 = part(0, 0, -1.0F, -2.0F, -1.0F, 2, 2, 2, 0.0F, -1.9F, 0.0F);
        this.eye25 = part(0, 4, -0.5F, -0.5F, -0.5F, 1, 1, 1, 0.0F, -1.9F, 0.0F);
        this.eye32 = part(8, 4, -0.5F, -2.0F, -0.5F, 1, 2, 1, 0.01F, -1.9F, 0.0F);
        this.eye33 = part(8, 4, -0.5F, -2.0F, -0.5F, 1, 2, 1, 0.0F, -1.9F, 0.0F);
        this.eye34 = part(0, 0, -1.0F, -2.0F, -1.0F, 2, 2, 2, 0.0F, -1.9F, 0.0F);
        this.eye35 = part(0, 4, -0.5F, -0.5F, -0.5F, 1, 1, 1, 0.0F, -1.9F, 0.0F);
        this.eye42 = part(8, 4, -0.5F, -2.0F, -0.5F, 1, 2, 1, 0.01F, -1.9F, 0.0F);
        this.eye43 = part(8, 4, -0.5F, -2.0F, -0.5F, 1, 2, 1, 0.0F, -1.9F, 0.0F);
        this.eye44 = part(0, 0, -1.0F, -2.0F, -1.0F, 2, 2, 2, 0.0F, -1.9F, 0.0F);
        this.eye45 = part(0, 4, -0.5F, -0.5F, -0.5F, 1, 1, 1, 0.0F, -1.9F, 0.0F);

        this.head.addChild(this.eye11);
        this.head.addChild(this.mouth);
        this.head.addChild(this.bigEye);
        this.head.addChild(this.spike1);
        this.head.addChild(this.spike2);
        this.head.addChild(this.upperTeeth1);
        this.head.addChild(this.upperTeeth2);
        this.head.addChild(this.spike3);
        this.head.addChild(this.spike4);
        this.head.addChild(this.spike5);
        this.head.addChild(this.spike6);
        this.head.addChild(this.upperTeeth3);
        this.head.addChild(this.eye21);
        this.head.addChild(this.eye31);
        this.head.addChild(this.eye41);
        this.head.addChild(this.glasses);
        chain(this.eye11, this.eye12, this.eye13, this.eye14, this.eye15);
        this.mouth.addChild(this.beard);
        this.mouth.addChild(this.lowerTeeth1);
        this.mouth.addChild(this.lowerTeeth2);
        this.mouth.addChild(this.lowerTeeth3);
        this.mouth.addChild(this.tongue);
        this.bigEye.addChild(this.bigPupil);
        chain(this.eye21, this.eye22, this.eye23, this.eye24, this.eye25);
        chain(this.eye31, this.eye32, this.eye33, this.eye34, this.eye35);
        chain(this.eye41, this.eye42, this.eye43, this.eye44, this.eye45);
    }

    @Override
    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks,
            float netHeadYaw, float headPitch, float scale) {
        this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);
        this.head.render(scale);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks,
            float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
        EntityBeholderFamiliar beholder = (EntityBeholderFamiliar) entityIn;
        float partialTicks = ageInTicks - beholder.ticksExisted;
        this.showModels(beholder);

        this.setRotateAngle(this.head, 0.0F, 0.0F, 0.0F);
        this.setEyeRot(beholder, partialTicks,
                new ModelRenderer[] {this.eye11, this.eye12, this.eye13, this.eye14}, 0);
        this.setEyeRot(beholder, partialTicks,
                new ModelRenderer[] {this.eye21, this.eye22, this.eye23, this.eye24}, 1);
        this.setEyeRot(beholder, partialTicks,
                new ModelRenderer[] {this.eye31, this.eye32, this.eye33, this.eye34}, 2);
        this.setEyeRot(beholder, partialTicks,
                new ModelRenderer[] {this.eye41, this.eye42, this.eye43, this.eye44}, 3);

        Vec2f bigEyePosition = beholder.getBigEyePos(partialTicks);
        this.bigPupil.rotationPointX = bigEyePosition.x;
        this.bigPupil.rotationPointY = bigEyePosition.y - 0.5F;
        this.mouth.rotateAngleX = beholder.getMouthRot(partialTicks);

        if (beholder.isPartying()) {
            float eyeRotation = -MathHelper.cos(ageInTicks * 0.2F) * this.toRads(15.0F);
            for (ModelRenderer eye : this.animatedEyeSegments()) {
                eye.rotateAngleY = 0.0F;
                eye.rotateAngleX = eyeRotation;
            }
            this.head.rotateAngleX = MathHelper.cos(ageInTicks * 0.2F) * this.toRads(20.0F);
            this.head.rotateAngleY = MathHelper.cos(ageInTicks * 0.2F + PI) * this.toRads(20.0F);
            this.head.rotateAngleZ = MathHelper.cos(ageInTicks * 0.1F) * this.toRads(30.0F);
        } else if (beholder.isSitting()) {
            this.head.rotateAngleZ = this.toRads(90.0F);
            this.mouth.rotateAngleX = this.toRads(20.0F);
            for (ModelRenderer eye : this.animatedEyeSegments()) {
                eye.rotateAngleY = 0.0F;
                eye.rotateAngleX = 0.0F;
            }

            this.setEyeChainPose(new ModelRenderer[] {this.eye11, this.eye12, this.eye13, this.eye14},
                    25.0F, -40.0F, 0.0F);
            this.setEyeChainPose(new ModelRenderer[] {this.eye41, this.eye42, this.eye43, this.eye44},
                    -10.0F, -40.0F, 0.0F);

            this.eye21.rotateAngleX = this.toRads(-20.0F);
            this.eye22.rotateAngleX = this.toRads(-15.0F);
            this.eye23.rotateAngleX = this.toRads(-10.0F);
            this.eye24.rotateAngleX = this.toRads(-5.0F);
            this.eye21.rotateAngleZ = this.toRads(2.0F);
            this.eye22.rotateAngleZ = this.toRads(2.0F);
            this.eye23.rotateAngleZ = this.toRads(2.0F);
            this.eye24.rotateAngleZ = this.toRads(2.0F);

            this.eye31.rotateAngleX = this.toRads(20.0F);
            this.eye32.rotateAngleX = this.toRads(15.0F);
            this.eye33.rotateAngleX = this.toRads(10.0F);
            this.eye34.rotateAngleX = this.toRads(5.0F);
            this.eye31.rotateAngleZ = this.toRads(2.0F);
            this.eye32.rotateAngleZ = this.toRads(2.0F);
            this.eye33.rotateAngleZ = this.toRads(2.0F);
            this.eye34.rotateAngleZ = this.toRads(2.0F);
        }
    }

    private ModelRenderer part(int textureX, int textureY, float boxX, float boxY, float boxZ,
            int width, int height, int depth, float pointX, float pointY, float pointZ) {
        ModelRenderer part = new ModelRenderer(this, textureX, textureY);
        part.addBox(boxX, boxY, boxZ, width, height, depth);
        part.setRotationPoint(pointX, pointY, pointZ);
        return part;
    }

    private static void chain(ModelRenderer... parts) {
        for (int i = 0; i + 1 < parts.length; i++) {
            parts[i].addChild(parts[i + 1]);
        }
    }

    private ModelRenderer[] animatedEyeSegments() {
        return new ModelRenderer[] {
                this.eye11, this.eye12, this.eye13, this.eye14,
                this.eye21, this.eye22, this.eye23, this.eye24,
                this.eye31, this.eye32, this.eye33, this.eye34,
                this.eye41, this.eye42, this.eye43, this.eye44
        };
    }

    private void setEyeRot(EntityBeholderFamiliar beholder, float partialTicks,
            ModelRenderer[] models, int index) {
        Vec2f rotation = beholder.getEyeRot(partialTicks, index);
        for (ModelRenderer model : models) {
            model.rotateAngleX = rotation.x;
            model.rotateAngleY = 0.0F;
            model.rotateAngleZ = 0.0F;
        }
        models[0].rotateAngleX = 0.0F;
        models[0].rotateAngleY = rotation.y;
    }

    private void setEyeChainPose(ModelRenderer[] models, float x, float z, float y) {
        for (ModelRenderer model : models) {
            model.rotateAngleX = this.toRads(x);
            model.rotateAngleY = this.toRads(y);
            model.rotateAngleZ = this.toRads(z);
        }
    }

    private void showModels(EntityBeholderFamiliar beholder) {
        boolean spikes = beholder.hasSpikes();
        this.tongue.showModel = beholder.hasTongue();
        this.beard.showModel = beholder.hasBeard();
        this.spike1.showModel = spikes;
        this.spike2.showModel = spikes;
        this.spike3.showModel = spikes;
        this.spike4.showModel = spikes;
        this.spike5.showModel = spikes;
        this.spike6.showModel = spikes;
        this.bigPupil.showModel = !beholder.isSitting();
        this.glasses.showModel = beholder.hasBlacksmithUpgrade();
    }

    private float toRads(float degrees) {
        return PI / 180.0F * degrees;
    }

    public void setRotateAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
