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

import com.occultism.occultism.entity.EntityGreedyFamiliar;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelGreedyFamiliar extends ModelBase {

    private static final float PI = (float) Math.PI;

    public final ModelRenderer body;
    public final ModelRenderer rightArm;
    public final ModelRenderer chest1;
    public final ModelRenderer leftArm;
    public final ModelRenderer rightLeg;
    public final ModelRenderer leftLeg;
    public final ModelRenderer head;
    public final ModelRenderer chest2;
    public final ModelRenderer monster;
    public final ModelRenderer monsterLeftEye;
    public final ModelRenderer monsterRightEye;
    public final ModelRenderer monsterLeftEar;
    public final ModelRenderer monsterRightEar;
    public final ModelRenderer leftEar;
    public final ModelRenderer rightEar;
    public final ModelRenderer nose;

    public ModelGreedyFamiliar() {
        this.textureWidth = 32;
        this.textureHeight = 32;

        this.body = new ModelRenderer(this, 0, 10);
        this.body.addBox(-2.5F, -5.0F, -1.5F, 5, 5, 3);
        this.body.setRotationPoint(0.0F, 19.0F, 0.6F);

        this.rightArm = new ModelRenderer(this, 16, 21);
        this.rightArm.mirror = true;
        this.rightArm.addBox(-2.0F, 0.0F, -1.0F, 2, 6, 2);
        this.rightArm.setRotationPoint(-2.5F, -5.0F, 0.0F);
        this.body.addChild(this.rightArm);

        this.chest1 = new ModelRenderer(this, 12, 14);
        this.chest1.addBox(-2.0F, -3.0F, 0.0F, 4, 3, 4);
        this.chest1.setRotationPoint(0.0F, 0.5F, 1.5F);
        this.body.addChild(this.chest1);

        this.leftArm = new ModelRenderer(this, 16, 21);
        this.leftArm.addBox(0.0F, 0.0F, -1.0F, 2, 6, 2);
        this.leftArm.setRotationPoint(2.5F, -5.0F, 0.0F);
        this.body.addChild(this.leftArm);

        this.rightLeg = new ModelRenderer(this, 8, 24);
        this.rightLeg.mirror = true;
        this.rightLeg.addBox(-1.0F, 0.0F, -1.0F, 2, 5, 2);
        this.rightLeg.setRotationPoint(-1.5F, 0.0F, 0.0F);
        this.body.addChild(this.rightLeg);

        this.leftLeg = new ModelRenderer(this, 8, 24);
        this.leftLeg.addBox(-1.0F, 0.0F, -1.0F, 2, 5, 2);
        this.leftLeg.setRotationPoint(1.5F, 0.0F, 0.0F);
        this.body.addChild(this.leftLeg);

        this.head = new ModelRenderer(this, 0, 0);
        this.head.addBox(-2.5F, -5.0F, -2.5F, 5, 5, 5);
        this.head.setRotationPoint(0.0F, -5.0F, 0.0F);
        this.body.addChild(this.head);

        this.chest2 = new ModelRenderer(this, 0, 18);
        this.chest2.addBox(-2.0F, -2.0F, 0.0F, 4, 2, 4);
        this.chest2.setRotationPoint(0.0F, -3.0F, 0.0F);
        setRotation(this.chest2, 1.0555751F, 0.0F, 0.0F);
        this.chest1.addChild(this.chest2);

        this.monster = new ModelRenderer(this, 15, 0);
        this.monster.addBox(-1.0F, -2.0F, 0.0F, 2, 2, 2);
        this.monster.setRotationPoint(0.0F, -2.0F, 1.5F);
        setRotation(this.monster, 0.1563815F, -0.1172861F, 0.0F);
        this.chest1.addChild(this.monster);

        this.monsterLeftEye = new ModelRenderer(this, 21, 0);
        this.monsterLeftEye.addBox(-0.5F, -0.5F, -0.5F, 1, 1, 1);
        this.monsterLeftEye.setRotationPoint(-0.8F, -1.8F, 1.8F);
        this.monster.addChild(this.monsterLeftEye);

        this.monsterRightEye = new ModelRenderer(this, 21, 0);
        this.monsterRightEye.mirror = true;
        this.monsterRightEye.addBox(-0.5F, -0.5F, -0.5F, 1, 1, 1);
        this.monsterRightEye.setRotationPoint(0.8F, -1.8F, 1.8F);
        this.monster.addChild(this.monsterRightEye);

        this.monsterLeftEar = new ModelRenderer(this, 24, 0);
        this.monsterLeftEar.addBox(-1.0F, -1.0F, 0.0F, 1, 1, 0);
        this.monsterLeftEar.setRotationPoint(-0.6F, -1.6F, 0.3F);
        setRotation(this.monsterLeftEar, -0.312763F, 0.07819075F, -0.3518584F);
        this.monster.addChild(this.monsterLeftEar);

        this.monsterRightEar = new ModelRenderer(this, 24, 0);
        this.monsterRightEar.addBox(0.0F, -1.0F, 0.0F, 1, 1, 0);
        this.monsterRightEar.setRotationPoint(0.6F, -1.6F, 0.3F);
        setRotation(this.monsterRightEar, -0.312763F, 0.07819075F, 0.3518584F);
        this.monster.addChild(this.monsterRightEar);

        this.leftEar = new ModelRenderer(this, 0, 0);
        this.leftEar.addBox(-1.0F, -3.0F, 0.0F, 2, 3, 0);
        this.leftEar.setRotationPoint(2.0F, -4.5F, 0.0F);
        setRotation(this.leftEar, 0.0F, 0.0F, toRad(30.0F));
        this.head.addChild(this.leftEar);

        this.rightEar = new ModelRenderer(this, 0, 0);
        this.rightEar.mirror = true;
        this.rightEar.addBox(-1.0F, -3.0F, 0.0F, 2, 3, 0);
        this.rightEar.setRotationPoint(-2.0F, -4.5F, 0.0F);
        setRotation(this.rightEar, 0.0F, 0.0F, toRad(-30.0F));
        this.head.addChild(this.rightEar);

        this.nose = new ModelRenderer(this, 18, 8);
        this.nose.addBox(-1.0F, 0.0F, -2.0F, 2, 2, 2);
        this.nose.setRotationPoint(0.0F, -3.0F, -2.5F);
        this.head.addChild(this.nose);
    }

    @Override
    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
            float headPitch, float scale) {
        this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);
        this.body.render(scale);
    }

    @Override
    public void setLivingAnimations(EntityLivingBase entitylivingbaseIn, float limbSwing, float limbSwingAmount,
            float partialTicks) {
        EntityGreedyFamiliar greedy = (EntityGreedyFamiliar) entitylivingbaseIn;
        this.leftEar.rotateAngleZ = -greedy.getEarRotZ(partialTicks);
        this.rightEar.rotateAngleZ = greedy.getEarRotZ(partialTicks);
        this.leftEar.rotateAngleX = greedy.getEarRotX(partialTicks);
        this.rightEar.rotateAngleX = greedy.getEarRotX(partialTicks);

        float ageInTicks = greedy.ticksExisted + partialTicks;
        if (greedy.isPartying()) {
            this.chest2.rotateAngleX = toRad(40.0F);
            this.monster.rotationPointY = -2.5F;
            this.monster.rotateAngleY = 0.0F;
            this.monster.rotateAngleX = MathHelper.cos(ageInTicks) * toRad(15.0F);
        } else {
            float lidRotation = greedy.getLidRot(partialTicks);
            this.chest2.rotateAngleX = lidRotation;
            this.monster.rotationPointY = -0.2F - lidRotation * 3.0F;
            this.monster.rotateAngleY = greedy.getMonsterRot(partialTicks);
            this.monster.rotateAngleX = 0.0F;
        }
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
            float headPitch, float scaleFactor, Entity entityIn) {
        EntityGreedyFamiliar greedy = (EntityGreedyFamiliar) entityIn;
        this.head.rotateAngleY = netHeadYaw * PI / 180.0F;
        this.head.rotateAngleX = headPitch * PI / 180.0F;
        this.head.rotateAngleZ = 0.0F;
        this.rightArm.rotateAngleZ = 0.0F;
        this.leftArm.rotateAngleZ = 0.0F;

        if (greedy.isPartying()) {
            this.rightArm.rotateAngleX = MathHelper.cos(ageInTicks + PI) * toRad(20.0F) + toRad(180.0F);
            this.leftArm.rotateAngleX = MathHelper.cos(ageInTicks) * toRad(20.0F) + toRad(180.0F);
            this.rightArm.rotateAngleZ = -toRad(20.0F);
            this.leftArm.rotateAngleZ = toRad(20.0F);
            this.head.rotateAngleZ = MathHelper.sin(ageInTicks) * toRad(20.0F);
            if (greedy.isRiding()) {
                this.rightLeg.rotateAngleX = -PI / 2.0F;
                this.leftLeg.rotateAngleX = -PI / 2.0F;
            } else {
                this.rightLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.5F) * 1.4F * limbSwingAmount;
                this.leftLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.5F + PI) * 1.4F * limbSwingAmount;
            }
        } else if (greedy.isSitting() || greedy.isRiding()) {
            this.rightArm.rotateAngleX = 0.0F;
            this.leftArm.rotateAngleX = 0.0F;
            this.rightLeg.rotateAngleX = -PI / 2.0F;
            this.leftLeg.rotateAngleX = -PI / 2.0F;
        } else {
            this.rightArm.rotateAngleX = MathHelper.cos(limbSwing * 0.5F + PI) * limbSwingAmount;
            this.leftArm.rotateAngleX = MathHelper.cos(limbSwing * 0.5F) * limbSwingAmount;
            this.rightLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.5F) * 1.4F * limbSwingAmount;
            this.leftLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.5F + PI) * 1.4F * limbSwingAmount;
        }

        this.chest1.rotateAngleZ = MathHelper.cos(limbSwing * 0.5F + PI) * limbSwingAmount * 0.2F;
        if (greedy.getTargetBlock().isPresent()) {
            this.rightArm.rotateAngleX = -toRad(100.0F)
                    + MathHelper.cos(limbSwing * 0.5F + PI) * limbSwingAmount;
        }
    }

    private static void setRotation(ModelRenderer renderer, float x, float y, float z) {
        renderer.rotateAngleX = x;
        renderer.rotateAngleY = y;
        renderer.rotateAngleZ = z;
    }

    private static float toRad(float degrees) {
        return degrees * PI / 180.0F;
    }
}
