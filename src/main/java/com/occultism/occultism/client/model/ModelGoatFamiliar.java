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

import com.occultism.occultism.entity.EntityGoatFamiliar;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Created using Tabula 8.0.0. */
@SideOnly(Side.CLIENT)
public class ModelGoatFamiliar extends ModelBase {

    private static final float PI = (float) Math.PI;

    public final ModelRenderer body;
    public final ModelRenderer leftBackLeg1;
    public final ModelRenderer rightBackLeg1;
    public final ModelRenderer neck;
    public final ModelRenderer tail;
    public final ModelRenderer leftFrontLeg1;
    public final ModelRenderer rightFrontLeg1;
    public final ModelRenderer leftBackLeg2;
    public final ModelRenderer leftBackLeg3;
    public final ModelRenderer rightBackLeg2;
    public final ModelRenderer rightBackLeg3;
    public final ModelRenderer head;
    public final ModelRenderer bell1;
    public final ModelRenderer rightHorn1;
    public final ModelRenderer mouth;
    public final ModelRenderer leftHorn1;
    public final ModelRenderer leftEyeEvil;
    public final ModelRenderer rightEyeEvil;
    public final ModelRenderer leftSideHornEvil1;
    public final ModelRenderer rightSideHornEvil1;
    public final ModelRenderer rightHorn2;
    public final ModelRenderer beard;
    public final ModelRenderer ring;
    public final ModelRenderer leftHorn2;
    public final ModelRenderer leftPupilEvil;
    public final ModelRenderer rightPupilEvil;
    public final ModelRenderer leftSideHornEvil2;
    public final ModelRenderer leftSideHornEvil3;
    public final ModelRenderer rightSideHornEvil2;
    public final ModelRenderer rightSideHornEvil3;
    public final ModelRenderer bell2;
    public final ModelRenderer bell3;
    public final ModelRenderer leftFrontLeg2;
    public final ModelRenderer leftFrontLeg3;
    public final ModelRenderer rightFrontLeg2;
    public final ModelRenderer rightFrontLeg3;

    public ModelGoatFamiliar() {
        this.textureWidth = 64;
        this.textureHeight = 32;

        this.body = part(null, 0, 0, false, 0, 17, 1, -2, -3, -5, 4, 5, 10, 0, 0, 0);
        this.leftBackLeg1 = part(this.body, 0, 0, false, 1.8F, -0.1F, 3.9F, -0.5F, 0, -1, 1, 4, 2, -0.23457224F, 0, 0);
        this.rightBackLeg1 = part(this.body, 0, 0, true, -1.8F, -0.1F, 3.9F, -0.5F, 0, -1, 1, 4, 2, -0.23457224F, 0, 0);
        this.neck = part(this.body, 36, 0, false, 0, -1.8F, -3.6F, -1, -1.5F, -4, 2, 3, 4, -0.87266463F, 0, 0);
        this.tail = part(this.body, 55, 6, false, 0, -1.6F, 4.3F, -1, -1, 0, 2, 2, 2, 0.19547687F, 0, 0);
        this.leftFrontLeg1 = part(this.body, 37, 7, false, 1.8F, -0.1F, -3.1F, -0.5F, 0, -1, 1, 5, 2, -0.12217305F, 0, 0);
        this.rightFrontLeg1 = part(this.body, 37, 7, true, -1.8F, -0.1F, -3.1F, -0.5F, 0, -1, 1, 5, 2, -0.12217305F, 0, 0);
        this.leftBackLeg2 = part(this.leftBackLeg1, 18, 0, false, -0.01F, 3.3F, -0.1F, -0.5F, 0, -1, 1, 2, 2, 0.86009823F, 0, 0);
        this.leftBackLeg3 = part(this.leftBackLeg2, 6, 0, false, 0.01F, 2.2F, 0.5F, -0.5F, 0, -1, 1, 3, 1, -1.2119567F, 0, 0);
        this.rightBackLeg2 = part(this.rightBackLeg1, 18, 0, true, -0.01F, 3.3F, -0.1F, -0.5F, 0, -1, 1, 2, 2, 0.86009823F, 0, 0);
        this.rightBackLeg3 = part(this.rightBackLeg2, 6, 0, true, 0.01F, 2.2F, 0.5F, -0.5F, 0, -1, 1, 3, 1, -1.2119567F, 0, 0);
        this.head = part(this.neck, 48, 0, false, 0, -0.8F, -3.1F, -1.5F, -1.5F, -3, 3, 3, 3, 0.9641199F, 0, 0);
        this.bell1 = part(this.neck, 0, 15, false, 0.01F, -1.6F, -2.1F, -1.5F, 0, -0.5F, 3, 4, 1, 0.1563815F, 0, 0);
        this.rightHorn1 = part(this.head, 18, 4, true, -0.9F, -0.6F, 0.2F, -0.5F, -1, -0.5F, 1, 2, 2, -0.17453293F, 0, 0);
        this.mouth = part(this.head, 27, 4, false, 0, 0, -2.5F, -1, -1.5F, -3, 2, 3, 3, 0.19198622F, 0, 0);
        this.leftHorn1 = part(this.head, 18, 4, false, 0.9F, -0.6F, 0.2F, -0.5F, -1, -0.5F, 1, 2, 2, -0.17453293F, 0, 0);
        this.leftEyeEvil = part(this.head, 24, 15, false, 1.3F, -0.2F, -1.7F, -0.5F, -1, -1, 1, 2, 2, 0, 0, 0);
        this.rightEyeEvil = part(this.head, 24, 15, true, -1.3F, -0.2F, -1.7F, -0.5F, -1, -1, 1, 2, 2, 0, 0, 0);
        this.leftSideHornEvil1 = part(this.head, 27, 0, false, 1, 0.4F, -0.3F, -0.5F, -1, -0.5F, 1, 2, 2, 0.6981317F, 0, 0.89919364F);
        this.rightSideHornEvil1 = part(this.head, 27, 0, true, -1, 0.4F, -0.3F, -0.5F, -1, -0.5F, 1, 2, 2, 0.6981317F, 0, -0.89919364F);
        this.rightHorn2 = part(this.rightHorn1, 57, 0, true, 0.01F, 0.2F, 1, -0.5F, -0.5F, 0, 1, 1, 2, -0.17453293F, 0, 0);
        this.beard = part(this.mouth, 45, 6, false, 0, 3, 0, -1, -1.5F, -3, 2, 2, 3, 0, 0, 0);
        this.ring = part(this.mouth, 0, 29, false, 0, -1, -2, -2, 0, -3, 4, 0, 3, 0.57595865F, 0, 0);
        this.leftHorn2 = part(this.leftHorn1, 57, 0, false, -0.01F, 0.2F, 1, -0.5F, -0.5F, 0, 1, 1, 2, -0.17453293F, 0, 0);
        this.leftPupilEvil = part(this.leftEyeEvil, 22, 15, false, 0.2F, 0, 0, -0.5F, -0.5F, -0.5F, 1, 1, 1, 0.7853982F, 0, 0);
        this.rightPupilEvil = part(this.rightEyeEvil, 22, 15, true, -0.2F, 0, 0, -0.5F, -0.5F, -0.5F, 1, 1, 1, 0.7853982F, 0, 0);
        this.leftSideHornEvil2 = part(this.leftSideHornEvil1, 33, 0, false, -0.01F, -0.2F, 0.9F, -0.5F, -0.5F, 0, 1, 1, 2, -0.44820055F, 0, 0);
        this.leftSideHornEvil3 = part(this.leftSideHornEvil2, 33, 0, false, -0.01F, 0, 1, -0.5F, -0.5F, 0, 1, 1, 2, 0.4509931F, 0, 0);
        this.rightSideHornEvil2 = part(this.rightSideHornEvil1, 33, 0, true, -0.01F, -0.2F, 0.9F, -0.5F, -0.5F, 0, 1, 1, 2, -0.44820055F, 0, 0);
        this.rightSideHornEvil3 = part(this.rightSideHornEvil2, 33, 0, true, -0.01F, 0, 1, -0.5F, -0.5F, 0, 1, 1, 2, 0.4509931F, 0, 0);
        this.bell2 = part(this.bell1, 0, 21, false, 0, 4, -0.1F, -1, 0, -1, 2, 2, 2, 0.6646214F, 0, 0);
        this.bell3 = part(this.bell2, 0, 25, false, 0, 2, 0, -1.5F, 0, -1.5F, 3, 1, 3, 0, 0, 0);
        this.leftFrontLeg2 = part(this.leftFrontLeg1, 0, 6, false, -0.01F, 4.6F, 0, -0.5F, 0, -0.5F, 1, 2, 1, 0.312763F, 0, 0);
        this.leftFrontLeg3 = part(this.leftFrontLeg2, 22, 0, false, 0.01F, 1.5F, 0, -0.5F, 0, -0.5F, 1, 1, 1, -0.19495328F, 0, 0);
        this.rightFrontLeg2 = part(this.rightFrontLeg1, 0, 6, true, -0.01F, 4.6F, 0, -0.5F, 0, -0.5F, 1, 2, 1, 0.312763F, 0, 0);
        this.rightFrontLeg3 = part(this.rightFrontLeg2, 22, 0, true, -0.01F, 1.5F, -0.1F, -0.5F, 0, -0.5F, 1, 1, 1, -0.19495328F, 0, 0);
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
        EntityGoatFamiliar goat = (EntityGoatFamiliar) entityIn;
        this.showModels(goat);

        this.body.rotateAngleZ = 0.0F;
        this.neck.rotateAngleX = -0.87F;
        this.head.rotateAngleY = toRads(netHeadYaw) * 0.5F;
        this.head.rotateAngleZ = toRads(netHeadYaw) * 0.5F;
        this.head.rotateAngleX = toRads(headPitch) * 0.5F + 0.96F;

        this.rightBackLeg1.rotateAngleX = MathHelper.cos(limbSwing * 0.7F) * 0.8F * limbSwingAmount - 0.23F;
        this.leftBackLeg1.rotateAngleX = MathHelper.cos(limbSwing * 0.7F + PI) * 0.8F * limbSwingAmount - 0.23F;
        this.rightFrontLeg1.rotateAngleX = MathHelper.cos(limbSwing * 0.7F + PI) * 0.8F * limbSwingAmount - 0.12F;
        this.leftFrontLeg1.rotateAngleX = MathHelper.cos(limbSwing * 0.7F) * 0.8F * limbSwingAmount - 0.12F;

        if (goat.isSitting()) {
            this.body.rotateAngleZ = toRads(90.0F);
            this.neck.rotateAngleX = toRads(10.0F);
        }
        if (goat.isPartying()) {
            this.body.rotateAngleZ = toRads(180.0F);
            this.neck.rotateAngleX = toRads(10.0F);
            this.rightBackLeg1.rotateAngleX = MathHelper.cos(ageInTicks * 0.5F) * toRads(25.0F) - 0.23F;
            this.leftBackLeg1.rotateAngleX = MathHelper.cos(ageInTicks * 0.5F + PI) * toRads(25.0F) - 0.23F;
            this.rightFrontLeg1.rotateAngleX = MathHelper.cos(ageInTicks * 0.5F + PI) * toRads(25.0F) - 0.12F;
            this.leftFrontLeg1.rotateAngleX = MathHelper.cos(ageInTicks * 0.5F) * toRads(25.0F) - 0.12F;
        }
        this.bell2.rotateAngleZ = MathHelper.cos(limbSwing * 0.7F) * 0.5F * limbSwingAmount;
    }

    @Override
    public void setLivingAnimations(EntityLivingBase entityLivingBase, float limbSwing, float limbSwingAmount,
            float partialTick) {
        super.setLivingAnimations(entityLivingBase, limbSwing, limbSwingAmount, partialTick);
        this.neck.rotateAngleY = ((EntityGoatFamiliar) entityLivingBase).getNeckYRot(partialTick);
    }

    private void showModels(EntityGoatFamiliar goat) {
        boolean redEyes = goat.hasRedEyes();
        boolean evilHorns = goat.hasEvilHorns();
        this.ring.showModel = goat.hasRing();
        this.beard.showModel = goat.hasBeard();
        this.bell1.showModel = goat.hasBlacksmithUpgrade();
        this.leftEyeEvil.showModel = redEyes;
        this.rightEyeEvil.showModel = redEyes;
        this.leftHorn1.showModel = !evilHorns;
        this.rightHorn1.showModel = !evilHorns;
        this.leftSideHornEvil1.showModel = evilHorns;
        this.rightSideHornEvil1.showModel = evilHorns;
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
