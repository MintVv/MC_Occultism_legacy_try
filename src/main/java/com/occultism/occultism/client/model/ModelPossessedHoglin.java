package com.occultism.occultism.client.model;

import com.occultism.occultism.entity.EntityPossessedHoglin;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Compact 1.12 quadruped model for the Hoglin compatibility entity. */
@SideOnly(Side.CLIENT)
public class ModelPossessedHoglin extends ModelBase {

    private final ModelRenderer body;
    private final ModelRenderer head;
    private final ModelRenderer leftFrontLeg;
    private final ModelRenderer rightFrontLeg;
    private final ModelRenderer leftBackLeg;
    private final ModelRenderer rightBackLeg;
    private final ModelRenderer leftTusk;
    private final ModelRenderer rightTusk;

    public ModelPossessedHoglin() {
        this.textureWidth = 64;
        this.textureHeight = 32;

        this.body = new ModelRenderer(this, 0, 0);
        this.body.addBox(-7.0F, -6.0F, -10.0F, 14, 12, 20);
        this.body.setRotationPoint(0.0F, 11.0F, 0.0F);

        this.head = new ModelRenderer(this, 0, 16);
        this.head.addBox(-6.0F, -5.0F, -8.0F, 12, 10, 8);
        this.head.setRotationPoint(0.0F, -1.0F, -8.0F);
        this.head.rotateAngleX = -0.12F;
        this.body.addChild(this.head);

        this.leftTusk = new ModelRenderer(this, 40, 16);
        this.leftTusk.addBox(0.0F, 0.0F, -2.0F, 2, 5, 2);
        this.leftTusk.setRotationPoint(3.5F, 2.0F, -7.0F);
        this.leftTusk.rotateAngleX = -0.45F;
        this.head.addChild(this.leftTusk);

        this.rightTusk = new ModelRenderer(this, 40, 16);
        this.rightTusk.mirror = true;
        this.rightTusk.addBox(-2.0F, 0.0F, -2.0F, 2, 5, 2);
        this.rightTusk.setRotationPoint(-3.5F, 2.0F, -7.0F);
        this.rightTusk.rotateAngleX = -0.45F;
        this.head.addChild(this.rightTusk);

        this.leftFrontLeg = createLeg(0, 0, -5.0F, 14.0F, -6.0F);
        this.rightFrontLeg = createLeg(0, 0, 5.0F, 14.0F, -6.0F);
        this.leftBackLeg = createLeg(0, 0, -5.0F, 14.0F, 6.0F);
        this.rightBackLeg = createLeg(0, 0, 5.0F, 14.0F, 6.0F);
        this.body.addChild(this.leftFrontLeg);
        this.body.addChild(this.rightFrontLeg);
        this.body.addChild(this.leftBackLeg);
        this.body.addChild(this.rightBackLeg);
    }

    private ModelRenderer createLeg(int textureX, int textureY, float x, float y, float z) {
        ModelRenderer leg = new ModelRenderer(this, textureX, textureY);
        leg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4);
        leg.setRotationPoint(x, y, z);
        return leg;
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
        EntityPossessedHoglin hoglin = (EntityPossessedHoglin) entityIn;
        float swing = MathHelper.cos(limbSwing * 0.6662F) * 1.2F * limbSwingAmount;
        this.leftFrontLeg.rotateAngleX = swing;
        this.rightBackLeg.rotateAngleX = swing;
        this.rightFrontLeg.rotateAngleX = -swing;
        this.leftBackLeg.rotateAngleX = -swing;
        this.head.rotateAngleY = netHeadYaw * 0.017453292F;
        this.head.rotateAngleX = -0.12F + headPitch * 0.017453292F;
        this.body.rotateAngleY = 0.0F;
        if (hoglin.isSprinting()) {
            this.body.rotateAngleX = MathHelper.cos(ageInTicks * 0.45F) * 0.04F;
        } else {
            this.body.rotateAngleX = 0.0F;
        }
    }
}
