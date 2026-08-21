package com.occultism.occultism.client.model;

import com.occultism.occultism.entity.EntityPossessedPhantom;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** A compact 1.12 model using the vanilla Phantom texture layout. */
@SideOnly(Side.CLIENT)
public class ModelPossessedPhantom extends ModelBase {

    private final ModelRenderer body;
    private final ModelRenderer head;
    private final ModelRenderer tail;
    private final ModelRenderer tailTip;
    private final ModelRenderer leftWing;
    private final ModelRenderer rightWing;
    private final ModelRenderer leftWingTip;
    private final ModelRenderer rightWingTip;

    public ModelPossessedPhantom() {
        this.textureWidth = 64;
        this.textureHeight = 64;

        this.body = new ModelRenderer(this, 0, 0);
        this.body.addBox(-2.0F, -2.0F, -4.0F, 4, 4, 8);
        this.body.setRotationPoint(0.0F, 15.0F, 0.0F);

        this.head = new ModelRenderer(this, 0, 12);
        this.head.addBox(-2.5F, -1.5F, -4.0F, 5, 3, 4);
        this.head.setRotationPoint(0.0F, -0.5F, -3.5F);
        this.body.addChild(this.head);

        this.tail = new ModelRenderer(this, 0, 20);
        this.tail.addBox(-1.5F, -1.0F, 0.0F, 3, 2, 8);
        this.tail.setRotationPoint(0.0F, 0.0F, 3.5F);
        this.tail.rotateAngleX = 0.18F;
        this.body.addChild(this.tail);

        this.tailTip = new ModelRenderer(this, 0, 30);
        this.tailTip.addBox(-1.0F, -0.5F, 0.0F, 2, 1, 7);
        this.tailTip.setRotationPoint(0.0F, 0.0F, 7.5F);
        this.tailTip.rotateAngleX = 0.25F;
        this.tail.addChild(this.tailTip);

        this.leftWing = new ModelRenderer(this, 20, 0);
        this.leftWing.addBox(0.0F, -0.5F, -1.0F, 12, 1, 8);
        this.leftWing.setRotationPoint(2.0F, -0.5F, -1.0F);
        this.body.addChild(this.leftWing);

        this.rightWing = new ModelRenderer(this, 20, 0);
        this.rightWing.mirror = true;
        this.rightWing.addBox(-12.0F, -0.5F, -1.0F, 12, 1, 8);
        this.rightWing.setRotationPoint(-2.0F, -0.5F, -1.0F);
        this.body.addChild(this.rightWing);

        this.leftWingTip = new ModelRenderer(this, 20, 10);
        this.leftWingTip.addBox(0.0F, -0.5F, -1.0F, 9, 1, 6);
        this.leftWingTip.setRotationPoint(12.0F, 0.0F, 3.0F);
        this.leftWing.addChild(this.leftWingTip);

        this.rightWingTip = new ModelRenderer(this, 20, 10);
        this.rightWingTip.mirror = true;
        this.rightWingTip.addBox(-9.0F, -0.5F, -1.0F, 9, 1, 6);
        this.rightWingTip.setRotationPoint(-12.0F, 0.0F, 3.0F);
        this.rightWing.addChild(this.rightWingTip);
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
        EntityPossessedPhantom phantom = (EntityPossessedPhantom) entityIn;
        float flap = phantom.getWingAngle(0.0F);
        this.body.rotateAngleX = phantom.isSwooping() ? 0.42F : 0.12F;
        this.body.rotateAngleY = 0.0F;
        this.body.rotateAngleZ = MathHelper.cos(ageInTicks * 0.08F) * 0.06F;
        this.head.rotateAngleX = headPitch * 0.017453292F;
        this.head.rotateAngleY = netHeadYaw * 0.017453292F;
        this.head.rotateAngleZ = 0.0F;
        this.leftWing.rotateAngleY = flap;
        this.rightWing.rotateAngleY = -flap;
        this.leftWingTip.rotateAngleY = flap * 0.45F;
        this.rightWingTip.rotateAngleY = -flap * 0.45F;
        this.tail.rotateAngleX = 0.18F + MathHelper.cos(ageInTicks * 0.12F) * 0.08F;
        this.tailTip.rotateAngleX = 0.25F + MathHelper.cos(ageInTicks * 0.12F + 0.7F) * 0.08F;
    }
}
