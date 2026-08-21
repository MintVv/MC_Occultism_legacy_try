package com.occultism.occultism.client.model;

import com.occultism.occultism.entity.EntityPossessedWarden;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Compact 1.12 Warden model. The server-side Warden Brain has no 1.12 equivalent;
 * this model therefore only mirrors the silhouette and low-cost locomotion animation.
 */
@SideOnly(Side.CLIENT)
public class ModelPossessedWarden extends ModelBase {

    private final ModelRenderer body;
    private final ModelRenderer head;
    private final ModelRenderer jaw;
    private final ModelRenderer leftHorn;
    private final ModelRenderer rightHorn;
    private final ModelRenderer leftArm;
    private final ModelRenderer rightArm;
    private final ModelRenderer leftLeg;
    private final ModelRenderer rightLeg;
    private final ModelRenderer leftTendril;
    private final ModelRenderer rightTendril;
    private final ModelRenderer backTendril;
    private final ModelRenderer frontTendril;

    public ModelPossessedWarden() {
        this.textureWidth = 128;
        this.textureHeight = 128;

        this.body = new ModelRenderer(this, 0, 0);
        this.body.addBox(-8.0F, 0.0F, -5.0F, 16, 26, 10);

        this.head = new ModelRenderer(this, 0, 40);
        this.head.addBox(-9.0F, -22.0F, -6.0F, 18, 18, 12);
        this.body.addChild(this.head);

        this.jaw = new ModelRenderer(this, 40, 40);
        this.jaw.addBox(-7.0F, -8.0F, -8.0F, 14, 6, 4);
        this.head.addChild(this.jaw);

        this.leftHorn = new ModelRenderer(this, 64, 0);
        this.leftHorn.addBox(0.0F, -20.0F, -3.0F, 5, 8, 6);
        this.leftHorn.setRotationPoint(7.0F, 0.0F, 0.0F);
        this.body.addChild(this.leftHorn);

        this.rightHorn = new ModelRenderer(this, 64, 0);
        this.rightHorn.mirror = true;
        this.rightHorn.addBox(-5.0F, -20.0F, -3.0F, 5, 8, 6);
        this.rightHorn.setRotationPoint(-7.0F, 0.0F, 0.0F);
        this.body.addChild(this.rightHorn);

        this.leftArm = new ModelRenderer(this, 64, 30);
        this.leftArm.addBox(-3.0F, 0.0F, -4.0F, 6, 28, 8);
        this.leftArm.setRotationPoint(8.0F, 4.0F, 0.0F);
        this.body.addChild(this.leftArm);

        this.rightArm = new ModelRenderer(this, 64, 30);
        this.rightArm.mirror = true;
        this.rightArm.addBox(-3.0F, 0.0F, -4.0F, 6, 28, 8);
        this.rightArm.setRotationPoint(-8.0F, 4.0F, 0.0F);
        this.body.addChild(this.rightArm);

        this.leftLeg = new ModelRenderer(this, 90, 0);
        this.leftLeg.addBox(-4.0F, 0.0F, -4.0F, 8, 28, 8);
        this.leftLeg.setRotationPoint(4.0F, 26.0F, 0.0F);
        this.body.addChild(this.leftLeg);

        this.rightLeg = new ModelRenderer(this, 90, 0);
        this.rightLeg.mirror = true;
        this.rightLeg.addBox(-4.0F, 0.0F, -4.0F, 8, 28, 8);
        this.rightLeg.setRotationPoint(-4.0F, 26.0F, 0.0F);
        this.body.addChild(this.rightLeg);

        this.leftTendril = new ModelRenderer(this, 90, 50);
        this.leftTendril.addBox(-2.0F, 0.0F, 0.0F, 4, 20, 3);
        this.leftTendril.setRotationPoint(4.0F, -18.0F, 3.0F);
        this.body.addChild(this.leftTendril);

        this.rightTendril = new ModelRenderer(this, 90, 50);
        this.rightTendril.mirror = true;
        this.rightTendril.addBox(-2.0F, 0.0F, 0.0F, 4, 20, 3);
        this.rightTendril.setRotationPoint(-4.0F, -18.0F, 3.0F);
        this.body.addChild(this.rightTendril);

        this.backTendril = new ModelRenderer(this, 104, 50);
        this.backTendril.addBox(-2.0F, 0.0F, 0.0F, 4, 18, 3);
        this.backTendril.setRotationPoint(0.0F, -16.0F, 5.0F);
        this.body.addChild(this.backTendril);

        this.frontTendril = new ModelRenderer(this, 104, 50);
        this.frontTendril.addBox(-2.0F, 0.0F, -3.0F, 4, 18, 3);
        this.frontTendril.setRotationPoint(0.0F, -16.0F, -5.0F);
        this.body.addChild(this.frontTendril);
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
        EntityPossessedWarden warden = (EntityPossessedWarden) entityIn;
        float walk = MathHelper.cos(limbSwing * 0.6662F) * 1.15F * limbSwingAmount;
        float idle = MathHelper.cos(ageInTicks * 0.08F);

        this.body.rotateAngleX = MathHelper.sin(ageInTicks * 0.05F) * 0.025F;
        this.body.rotateAngleY = 0.0F;
        this.body.rotateAngleZ = 0.0F;

        this.head.rotateAngleY = netHeadYaw * 0.017453292F;
        this.head.rotateAngleX = headPitch * 0.017453292F;
        this.head.rotateAngleZ = idle * 0.025F;
        this.jaw.rotateAngleX = 0.08F + MathHelper.sin(ageInTicks * 0.12F) * 0.025F;

        this.leftArm.rotateAngleX = walk + warden.swingProgress * 0.75F;
        this.rightArm.rotateAngleX = -walk - warden.swingProgress * 0.75F;
        this.leftArm.rotateAngleZ = 0.04F;
        this.rightArm.rotateAngleZ = -0.04F;
        this.leftLeg.rotateAngleX = -walk * 0.7F;
        this.rightLeg.rotateAngleX = walk * 0.7F;

        float tendril = MathHelper.sin(ageInTicks * 0.11F);
        this.leftTendril.rotateAngleX = 0.16F + tendril * 0.08F;
        this.rightTendril.rotateAngleX = 0.16F - tendril * 0.08F;
        this.backTendril.rotateAngleX = 0.22F + idle * 0.06F;
        this.frontTendril.rotateAngleX = -0.18F - idle * 0.06F;
    }
}
