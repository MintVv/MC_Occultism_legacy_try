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

import java.util.Arrays;
import java.util.List;

import com.occultism.occultism.entity.EntityCthulhuFamiliar;
import com.occultism.occultism.entity.EntityShubNiggurathFamiliar;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Created using Tabula 8.0.0. */
@SideOnly(Side.CLIENT)
public class ModelShubNiggurathFamiliar extends ModelBase {

    private static final float PI = (float) Math.PI;

    public final ModelRenderer body;
    public final ModelRenderer neck;
    public final ModelRenderer leftLeg1;
    public final ModelRenderer tentacleTop1;
    public final ModelRenderer tentacleMiddle1;
    public final ModelRenderer tentacleBottom1;
    public final ModelRenderer leftArm1;
    public final ModelRenderer rightLeg1;
    public final ModelRenderer rightArm1;
    public final ModelRenderer tail;
    public final ModelRenderer head;
    public final ModelRenderer bell1;
    public final ModelRenderer mouth;
    public final ModelRenderer leftHorn1;
    public final ModelRenderer leftSideHorn1;
    public final ModelRenderer rightHorn1;
    public final ModelRenderer rightSideHorn1;
    public final ModelRenderer leftEye;
    public final ModelRenderer rightEye;
    public final ModelRenderer beard;
    public final ModelRenderer ring;
    public final ModelRenderer leftHorn2;
    public final ModelRenderer leftHorn3;
    public final ModelRenderer leftSideHorn2;
    public final ModelRenderer leftSideHorn3;
    public final ModelRenderer rightHorn2;
    public final ModelRenderer rightHorn3;
    public final ModelRenderer rightSideHorn2;
    public final ModelRenderer rightSideHorn3;
    public final ModelRenderer leftPupil;
    public final ModelRenderer rightPupil;
    public final ModelRenderer bell2;
    public final ModelRenderer bell3;
    public final ModelRenderer leftLeg2;
    public final ModelRenderer leftLeg3;
    public final ModelRenderer leftLeg4;
    public final ModelRenderer tentacleTop2;
    public final ModelRenderer tentacleTop3;
    public final ModelRenderer tentacleMiddle2;
    public final ModelRenderer tentacleMiddle3;
    public final ModelRenderer tentacleBottom2;
    public final ModelRenderer tentacleBottom3;
    public final ModelRenderer leftArm2;
    public final ModelRenderer leftArm4;
    public final ModelRenderer leftArm3;
    public final ModelRenderer rightLeg2;
    public final ModelRenderer rightLeg3;
    public final ModelRenderer rightLeg4;
    public final ModelRenderer rightArm2;
    public final ModelRenderer rightArm4;
    public final ModelRenderer rightArm3;

    public ModelShubNiggurathFamiliar() {
        this.textureWidth = 64;
        this.textureHeight = 32;

        this.body = part(null, 0, 0, false, 0, 18.6F, 0, -2.5F, -3.5F, -1.5F, 5, 8, 3, 0.5864306F, 0, 0);
        this.neck = part(this.body, 16, 0, false, 0, -3.2F, 0, -1, -2, -1, 2, 2, 2, -0.11728612F, 0, 0);
        this.leftLeg1 = part(this.body, 32, 10, false, 2.5F, 4.4F, 0.2F, -0.5F, 0, -1, 1, 4, 2, -2.5258405F, 0, 0);
        this.tentacleTop1 = part(this.body, 6, 11, false, -1.5F, -1.5F, 1.3F, -0.5F, -0.5F, 0, 1, 1, 2, 0.23457224F, 0.23457224F, 0);
        this.tentacleMiddle1 = part(this.body, 6, 11, false, 1.2F, 0.3F, 1.3F, -0.5F, -0.5F, 0, 1, 1, 2, 0, 0, 0);
        this.tentacleBottom1 = part(this.body, 6, 11, false, -0.9F, 2.5F, 1.3F, -0.5F, -0.5F, 0, 1, 1, 2, 0, 0, 0);
        this.leftArm1 = part(this.body, 32, 10, false, 2.5F, -2.1F, -0.1F, -0.5F, 0, -1, 1, 4, 2, -1.2117821F, 0, 0);
        this.rightLeg1 = part(this.body, 32, 10, true, -2.5F, 4.4F, 0.2F, -0.5F, 0, -1, 1, 4, 2, -2.5258405F, 0, 0);
        this.rightArm1 = part(this.body, 32, 10, false, -2.5F, -2.1F, -0.1F, -0.5F, 0, -1, 1, 4, 2, -1.2117821F, 0, 0);
        this.tail = part(this.body, 0, 14, false, 0, 3.3F, 0.5F, -1, 0, -1, 2, 2, 2, 0.4845034F, 0, 0);
        this.head = part(this.neck, 24, 0, false, 0, -1.3F, -0.3F, -1.5F, -3, -1.5F, 3, 3, 3, 0, 0, 0);
        this.bell1 = part(this.neck, 8, 14, false, 0.01F, -1.2F, 1.4F, -1.5F, 0, -0.5F, 3, 4, 1, -1.2911947F, 0, 0);
        this.mouth = part(this.head, 36, 0, false, 0, -1.5F, -1, -1, -1.5F, -3, 2, 3, 3, 0.19198622F, 0, 0);
        this.leftHorn1 = part(this.head, 56, 0, false, 0.8F, -2.8F, 0.9F, -0.5F, -1, -0.5F, 1, 2, 2, 0.9773844F, 0.17453293F, 0);
        this.leftSideHorn1 = part(this.head, 56, 0, false, 1, -1.1F, 1.2F, -0.5F, -1, -0.5F, 1, 2, 2, 0.6981317F, 0, 0.89919364F);
        this.rightHorn1 = part(this.head, 56, 0, true, -0.8F, -2.8F, 0.9F, -0.5F, -1, -0.5F, 1, 2, 2, 0.9773844F, -0.17453293F, 0);
        this.rightSideHorn1 = part(this.head, 56, 0, true, -1, -1.1F, 1.2F, -0.5F, -1, -0.5F, 1, 2, 2, 0.6981317F, 0, -0.89919364F);
        this.leftEye = part(this.head, 16, 8, false, 1.3F, -1.7F, -0.2F, -0.5F, -1, -1, 1, 2, 2, 0, 0, 0);
        this.rightEye = part(this.head, 16, 8, true, -1.3F, -1.7F, -0.2F, -0.5F, -1, -1, 1, 2, 2, 0, 0, 0);
        this.beard = part(this.mouth, 46, 0, false, 0, 3, 0, -1, -1.5F, -3, 2, 2, 3, 0, 0, 0);
        this.ring = part(this.mouth, 0, 29, false, 0, -1, -2, -2, 0, -3, 4, 0, 3, 0.57595865F, 0, 0);
        this.leftHorn2 = part(this.leftHorn1, 33, 0, false, -0.01F, 0, 0.9F, -0.5F, -0.5F, 0, 1, 1, 2, -0.25272366F, 0, 0);
        this.leftHorn3 = part(this.leftHorn2, 33, 0, false, -0.01F, 0, 1, -0.5F, -0.5F, 0, 1, 1, 2, -0.3309144F, 0, 0);
        this.leftSideHorn2 = part(this.leftSideHorn1, 33, 0, false, -0.01F, -0.2F, 0.9F, -0.5F, -0.5F, 0, 1, 1, 2, -0.44820055F, 0, 0);
        this.leftSideHorn3 = part(this.leftSideHorn2, 33, 0, false, -0.01F, 0, 1, -0.5F, -0.5F, 0, 1, 1, 2, 0.4509931F, 0, 0);
        this.rightHorn2 = part(this.rightHorn1, 33, 0, true, -0.01F, 0, 0.9F, -0.5F, -0.5F, 0, 1, 1, 2, -0.25272366F, 0, 0);
        this.rightHorn3 = part(this.rightHorn2, 33, 0, true, -0.01F, 0, 1, -0.5F, -0.5F, 0, 1, 1, 2, -0.3309144F, 0, 0);
        this.rightSideHorn2 = part(this.rightSideHorn1, 33, 0, true, -0.01F, -0.2F, 0.9F, -0.5F, -0.5F, 0, 1, 1, 2, -0.44820055F, 0, 0);
        this.rightSideHorn3 = part(this.rightSideHorn2, 33, 0, true, -0.01F, 0, 1, -0.5F, -0.5F, 0, 1, 1, 2, 0.4509931F, 0, 0);
        this.leftPupil = part(this.leftEye, 13, 0, false, 0.2F, 0, 0, -0.5F, -0.5F, -0.5F, 1, 1, 1, 0.7853982F, 0, 0);
        this.rightPupil = part(this.rightEye, 13, 0, true, -0.2F, 0, 0, -0.5F, -0.5F, -0.5F, 1, 1, 1, 0.7853982F, 0, 0);
        this.bell2 = part(this.bell1, 8, 20, false, 0, 4, -0.1F, -1, 0, -1, 2, 2, 2, 0.8210029F, 0, 0);
        this.bell3 = part(this.bell2, 8, 24, false, 0, 2, 0, -1.5F, 0, -1.5F, 3, 1, 3, 0, 0, 0);
        this.leftLeg2 = part(this.leftLeg1, 28, 9, false, -0.01F, 3, -0.6F, -0.5F, 0, -1, 1, 3, 1, 1.993864F, 0, 0);
        this.leftLeg3 = part(this.leftLeg2, 0, 11, true, -0.2F, 2.5F, 0, -0.5F, 0, -2, 1, 1, 2, -0.07819075F, 0.27366763F, 0);
        this.leftLeg4 = part(this.leftLeg2, 0, 11, false, 0.2F, 2.5F, 0, -0.5F, 0, -2, 1, 1, 2, -0.07819075F, -0.27366763F, 0);
        this.tentacleTop2 = part(this.tentacleTop1, 6, 11, false, 0, 0, 1.9F, -0.5F, -0.5F, 0, 1, 1, 2, 0.35185838F, 0.312763F, 0);
        this.tentacleTop3 = part(this.tentacleTop2, 6, 11, false, 0, 0, 1.9F, -0.5F, -0.5F, 0, 1, 1, 2, 0.27366763F, 0.23457224F, 0);
        this.tentacleMiddle2 = part(this.tentacleMiddle1, 6, 11, false, 0, 0, 1.9F, -0.5F, -0.5F, 0, 1, 1, 2, 0, 0, 0);
        this.tentacleMiddle3 = part(this.tentacleMiddle2, 6, 11, false, 0, 0, 1.9F, -0.5F, -0.5F, 0, 1, 1, 2, 0, 0, 0);
        this.tentacleBottom2 = part(this.tentacleBottom1, 6, 11, false, 0, 0, 1.9F, -0.5F, -0.5F, 0, 1, 1, 2, 0, 0, 0);
        this.tentacleBottom3 = part(this.tentacleBottom2, 6, 11, false, 0, 0, 1.9F, -0.5F, -0.5F, 0, 1, 1, 2, 0, 0, 0);
        this.leftArm2 = part(this.leftArm1, 59, 6, false, -0.01F, 3.6F, 0.5F, -0.5F, 0, -1, 1, 2, 1, -0.07819075F, 0, 0);
        this.leftArm4 = part(this.leftArm2, 0, 11, false, 0.2F, 1.3F, -0.6F, -0.5F, 0, -2, 1, 1, 2, 1.1728612F, -0.23457224F, -0.34906584F);
        this.leftArm3 = part(this.leftArm2, 0, 11, true, -0.2F, 1.3F, -0.6F, -0.5F, 0, -2, 1, 1, 2, 1.1728612F, 0.23457224F, 0.34906584F);
        this.rightLeg2 = part(this.rightLeg1, 28, 9, true, 0.01F, 3, -0.6F, -0.5F, 0, -1, 1, 3, 1, 1.993864F, 0, 0);
        this.rightLeg3 = part(this.rightLeg2, 0, 11, true, -0.2F, 2.5F, 0, -0.5F, 0, -2, 1, 1, 2, -0.07819075F, 0.27366763F, 0);
        this.rightLeg4 = part(this.rightLeg2, 0, 11, false, 0.2F, 2.5F, 0, -0.5F, 0, -2, 1, 1, 2, -0.07819075F, -0.27366763F, 0);
        this.rightArm2 = part(this.rightArm1, 59, 6, false, 0.01F, 3.6F, 0.5F, -0.5F, 0, -1, 1, 2, 1, -0.07819075F, 0, 0);
        this.rightArm4 = part(this.rightArm2, 0, 11, false, 0.2F, 1.3F, -0.6F, -0.5F, 0, -2, 1, 1, 2, 1.1728612F, -0.23457224F, -0.34906584F);
        this.rightArm3 = part(this.rightArm2, 0, 11, true, -0.2F, 1.3F, -0.6F, -0.5F, 0, -2, 1, 1, 2, 1.1728612F, 0.23457224F, 0.34906584F);
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
        EntityShubNiggurathFamiliar shub = (EntityShubNiggurathFamiliar) entityIn;
        this.showModels(shub);
        EntityCthulhuFamiliar friend = shub.getCthulhuFriend();
        float partialTicks = ageInTicks - shub.ticksExisted;

        this.body.rotateAngleX = 0.59F;
        this.body.rotateAngleY = 0.0F;
        this.body.rotateAngleZ = 0.0F;
        this.leftLeg2.rotateAngleX = 1.99F;
        this.rightLeg2.rotateAngleX = 1.99F;
        if (friend != null) {
            limbSwing = friend.limbSwing;
            limbSwingAmount = friend.limbSwingAmount;
        }

        this.head.rotateAngleY = toRads(netHeadYaw) * 0.7F;
        this.head.rotateAngleX = toRads(headPitch) * 0.7F;
        rotateTentacles(Arrays.asList(this.tentacleBottom1, this.tentacleBottom2, this.tentacleBottom3), ageInTicks, 0.0F);
        rotateTentacles(Arrays.asList(this.tentacleMiddle1, this.tentacleMiddle2, this.tentacleMiddle3), ageInTicks, 0.5F);
        rotateTentacles(Arrays.asList(this.tentacleTop1, this.tentacleTop2, this.tentacleTop3), ageInTicks, 1.0F);

        this.rightArm1.rotateAngleX = MathHelper.cos(limbSwing * 0.5F + PI) * limbSwingAmount * 0.5F - 1.21F;
        this.leftArm1.rotateAngleX = MathHelper.cos(limbSwing * 0.5F) * limbSwingAmount * 0.5F - 1.21F;
        this.leftLeg1.rotateAngleX = MathHelper.cos(limbSwing * 0.5F + PI) * limbSwingAmount * 0.5F - 2.53F;
        this.rightLeg1.rotateAngleX = MathHelper.cos(limbSwing * 0.5F) * limbSwingAmount * 0.5F - 2.53F;
        this.body.rotationPointY = 18.6F - Math.abs(MathHelper.cos(limbSwing * 0.5F + PI)) * limbSwingAmount * 1.5F;
        if (friend != null) {
            this.leftArm1.rotateAngleX = toRads(-143.0F - 5.0F * friend.getAnimationHeight(partialTicks)
                    - 10.0F * (this.body.rotationPointY - 18.6F));
        }

        if (shub.isPartying()) {
            this.body.rotateAngleX = toRads(-90.0F) + MathHelper.cos(ageInTicks * 0.3F) * toRads(5.0F);
            this.body.rotateAngleY = MathHelper.cos(ageInTicks * 0.3F + PI * 1.5F) * toRads(5.0F);
            this.body.rotateAngleZ = MathHelper.cos(ageInTicks * 0.3F + PI) * toRads(5.0F);
            this.leftArm1.rotateAngleX = MathHelper.cos(ageInTicks * 0.3F) * toRads(30.0F) - toRads(90.0F);
            this.rightArm1.rotateAngleX = MathHelper.cos(ageInTicks * 0.3F + PI) * toRads(30.0F) - toRads(90.0F);
            this.leftLeg1.rotateAngleX = MathHelper.cos(ageInTicks * 0.3F + PI) * toRads(30.0F) - toRads(90.0F);
            this.rightLeg1.rotateAngleX = MathHelper.cos(ageInTicks * 0.3F + PI) * toRads(30.0F) - toRads(90.0F);
        } else if (shub.isSitting()) {
            this.body.rotateAngleX = toRads(100.0F);
            this.leftLeg1.rotateAngleX = -toRads(60.0F);
            this.rightLeg1.rotateAngleX = -toRads(60.0F);
            this.leftLeg2.rotateAngleX = toRads(75.0F);
            this.rightLeg2.rotateAngleX = toRads(75.0F);
            this.rightArm1.rotateAngleX = -toRads(170.0F);
            this.leftArm1.rotateAngleX = -toRads(170.0F);
            this.head.rotateAngleY = toRads(115.0F);
        }
    }

    public static void rotateTentacles(List<ModelRenderer> tentacles, float ageInTicks, float offset) {
        float rotation = MathHelper.cos(ageInTicks * 0.1F + offset) * toRads(12.0F);
        for (ModelRenderer tentacle : tentacles) {
            tentacle.rotateAngleX = rotation;
            tentacle.rotateAngleY = rotation;
        }
        tentacles.get(0).rotateAngleZ = ageInTicks * 0.01F + offset;
    }

    private void showModels(EntityShubNiggurathFamiliar shub) {
        this.ring.showModel = shub.hasRing();
        this.beard.showModel = shub.hasBeard();
        this.bell1.showModel = shub.hasBlacksmithUpgrade();
    }

    private ModelRenderer part(ModelRenderer parent, int textureX, int textureY, boolean mirror,
            float pointX, float pointY, float pointZ, float boxX, float boxY, float boxZ,
            int width, int height, int depth, float rotateX, float rotateY, float rotateZ) {
        ModelRenderer renderer = new ModelRenderer(this, textureX, textureY);
        renderer.mirror = mirror;
        renderer.addBox(boxX, boxY, boxZ, width, height, depth);
        renderer.setRotationPoint(pointX, pointY, pointZ);
        renderer.rotateAngleX = rotateX;
        renderer.rotateAngleY = rotateY;
        renderer.rotateAngleZ = rotateZ;
        if (parent != null) {
            parent.addChild(renderer);
        }
        return renderer;
    }

    private static float toRads(float degrees) {
        return degrees * PI / 180.0F;
    }
}
