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

import java.time.LocalDate;
import java.util.Arrays;

import com.occultism.occultism.entity.EntityShubNiggurathSpawn;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Created using Tabula 8.0.0. */
@SideOnly(Side.CLIENT)
public class ModelShubNiggurathSpawn extends ModelBase {

    private static final float PI = (float) Math.PI;

    public final ModelRenderer head;
    public final ModelRenderer rightHorn1;
    public final ModelRenderer mouth;
    public final ModelRenderer leftHorn1;
    public final ModelRenderer tentacleTop1;
    public final ModelRenderer tentacleMiddle1;
    public final ModelRenderer tentacleBottom1;
    public final ModelRenderer eye2;
    public final ModelRenderer eye3;
    public final ModelRenderer eye4;
    public final ModelRenderer christmasPresent1;
    public final ModelRenderer rightHorn2;
    public final ModelRenderer jaw;
    public final ModelRenderer upperTeeth;
    public final ModelRenderer eye1;
    public final ModelRenderer lowerTeeth;
    public final ModelRenderer leftHorn2;
    public final ModelRenderer tentacleTop2;
    public final ModelRenderer tentacleTop3;
    public final ModelRenderer tentacleMiddle2;
    public final ModelRenderer tentacleMiddle3;
    public final ModelRenderer tentacleBottom2;
    public final ModelRenderer tentacleBottom3;
    public final ModelRenderer christmasPresent2;
    public final ModelRenderer christmasPresent3;

    public ModelShubNiggurathSpawn() {
        this.textureWidth = 32;
        this.textureHeight = 32;

        this.head = part(null, 0, 0, false, -1.5F, 22.5F, -0.8F, -1.5F, -1.5F, -3, 3, 3, 3, 1.1798426F, 0, -1.5707964F);
        this.rightHorn1 = part(this.head, 12, 0, true, -0.9F, -0.6F, 0.2F, -0.5F, -1, -0.5F, 1, 2, 2, -0.17453293F, 0, 0);
        this.mouth = part(this.head, 21, 0, false, 0, 0, -2.5F, -1, -1.5F, -3, 2, 2, 3, 0.19198622F, 0, 0);
        this.leftHorn1 = part(this.head, 12, 0, false, 0.9F, -0.6F, 0.2F, -0.5F, -1, -0.5F, 1, 2, 2, -0.17453293F, 0, 0);
        this.tentacleTop1 = part(this.head, 4, 11, false, 0.7F, 1.4F, -0.7F, -0.5F, -0.5F, 0, 1, 1, 2, -1.5707964F, 0, 0);
        this.tentacleMiddle1 = part(this.head, 4, 11, false, 0.3F, 1.4F, -2.2F, -0.5F, -0.5F, 0, 1, 1, 2, -1.5707964F, 0, 0);
        this.tentacleBottom1 = part(this.head, 4, 11, false, -0.8F, 1.4F, -0.7F, -0.5F, -0.5F, 0, 1, 1, 2, -1.5707964F, 0, 0);
        this.eye2 = part(this.head, 11, 15, false, -0.2F, -0.8F, -1.8F, -1, -1, -0.5F, 2, 1, 1, 0, 0.43004912F, 0);
        this.eye3 = part(this.head, 11, 15, true, 0.8F, 0, -2.1F, -1, -1, -0.5F, 2, 1, 1, 0, 0.27366763F, 1.5707964F);
        this.eye4 = part(this.head, 11, 15, false, 0.8F, 0, -0.8F, -1, -1, -0.5F, 2, 1, 1, 0, 0.07819075F, 1.5707964F);
        this.christmasPresent1 = part(this.head, 0, 18, false, -0.01F, 0, 0, -1.5F, -3.5F, -3.5F, 5, 7, 7, 0, 0, 0);
        this.rightHorn2 = part(this.rightHorn1, 18, 0, true, 0.01F, 0.2F, 1, -0.5F, -0.5F, 0, 1, 1, 2, -0.17453293F, 0, 0);
        this.jaw = part(this.mouth, 9, 4, false, 0, 0.1F, 0, -1, 0, -3, 2, 1, 3, 0.3483677F, 0, 0);
        this.upperTeeth = part(this.mouth, 0, 6, false, 0, 0.5F, 0, -1, 0, -3, 2, 1, 3, 0, 0, 0);
        this.eye1 = part(this.mouth, 11, 15, true, 0, -0.8F, -1.7F, -1, -1, -0.5F, 2, 1, 1, 0, -1.0159562F, 0);
        this.lowerTeeth = part(this.jaw, 16, 5, false, 0.01F, -1, 0, -1, 0, -3, 2, 1, 3, 0, 0, 0);
        this.leftHorn2 = part(this.leftHorn1, 18, 0, false, -0.01F, 0.2F, 1, -0.5F, -0.5F, 0, 1, 1, 2, -0.17453293F, 0, 0);
        this.tentacleTop2 = part(this.tentacleTop1, 4, 11, false, 0, 0, 1.9F, -0.5F, -0.5F, 0, 1, 1, 2, 0.35185838F, 0.312763F, 0);
        this.tentacleTop3 = part(this.tentacleTop2, 4, 11, false, 0, 0, 1.9F, -0.5F, -0.5F, 0, 1, 1, 2, 0.27366763F, 0.23457224F, 0);
        this.tentacleMiddle2 = part(this.tentacleMiddle1, 4, 11, false, 0, 0, 1.9F, -0.5F, -0.5F, 0, 1, 1, 2, 0, 0, 0);
        this.tentacleMiddle3 = part(this.tentacleMiddle2, 4, 11, false, 0, 0, 1.9F, -0.5F, -0.5F, 0, 1, 1, 2, 0, 0, 0);
        this.tentacleBottom2 = part(this.tentacleBottom1, 4, 11, false, 0, 0, 1.9F, -0.5F, -0.5F, 0, 1, 1, 2, 0, 0, 0);
        this.tentacleBottom3 = part(this.tentacleBottom2, 4, 11, false, 0, 0, 1.9F, -0.5F, -0.5F, 0, 1, 1, 2, 0, 0, 0);
        this.christmasPresent2 = part(this.christmasPresent1, 0, 22, false, 3.5F, 0, 0, 0, 0, -0.5F, 0, 2, 1, 0.7044149F, 0.54733527F, -0.5085889F);
        this.christmasPresent3 = part(this.christmasPresent1, 0, 22, false, 3.5F, 0, 0, 0, 0, -0.5F, 0, 2, 1, -0.5082399F, 0.665145F, -2.4635322F);
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
        this.showModels();
        this.jaw.rotateAngleX = 0.35F + MathHelper.cos(ageInTicks * 0.3F) * toRads(15.0F);
        this.head.rotationPointY = 22.5F - Math.abs(MathHelper.cos(ageInTicks * 0.15F)) * 7.0F;
        this.head.rotateAngleY = MathHelper.cos(ageInTicks * 0.15F) * toRads(20.0F);
        this.head.rotateAngleZ = MathHelper.cos(ageInTicks * 0.3F) * toRads(20.0F) - 1.57F;

        float tentacleAge = ageInTicks * 2.25F;
        ModelShubNiggurathFamiliar.rotateTentacles(
                Arrays.asList(this.tentacleBottom1, this.tentacleBottom2, this.tentacleBottom3), tentacleAge, 0.0F);
        ModelShubNiggurathFamiliar.rotateTentacles(
                Arrays.asList(this.tentacleMiddle1, this.tentacleMiddle2, this.tentacleMiddle3), tentacleAge, 0.5F);
        ModelShubNiggurathFamiliar.rotateTentacles(
                Arrays.asList(this.tentacleTop1, this.tentacleTop2, this.tentacleTop3), tentacleAge, 1.0F);
        this.tentacleBottom1.rotateAngleX -= toRads(90.0F);
        this.tentacleMiddle1.rotateAngleX -= toRads(90.0F);
        this.tentacleTop1.rotateAngleX -= toRads(90.0F);
        this.tentacleBottom1.rotateAngleZ = 0.0F;
        this.tentacleMiddle1.rotateAngleZ = 0.0F;
        this.tentacleTop1.rotateAngleZ = 0.0F;
    }

    private void showModels() {
        boolean christmas = isChristmas();
        this.tentacleBottom1.showModel = !christmas;
        this.tentacleMiddle1.showModel = !christmas;
        this.tentacleTop1.showModel = !christmas;
        this.mouth.showModel = !christmas;
        this.christmasPresent1.showModel = christmas;
    }

    private static boolean isChristmas() {
        LocalDate date = LocalDate.now();
        int month = date.getMonthValue();
        int day = date.getDayOfMonth();
        return month == 12 && day >= 15 || month == 1 && day <= 15;
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
