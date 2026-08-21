package com.occultism.occultism.client.model;

import com.occultism.occultism.entity.EntityDeerFamiliar;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelDeerFamiliar extends ModelBase {

    private final ModelRenderer body;
    private final ModelRenderer leftFrontLeg1;
    private final ModelRenderer tail;
    private final ModelRenderer neck;
    private final ModelRenderer rightFrontLeg1;
    private final ModelRenderer leftBackLeg1;
    private final ModelRenderer rightBackLeg1;
    private final ModelRenderer leftFrontLeg2;
    private final ModelRenderer head;
    private final ModelRenderer nose;
    private final ModelRenderer hammerHandle;
    private final ModelRenderer rightFrontLeg2;
    private final ModelRenderer leftBackLeg2;
    private final ModelRenderer rightBackLeg2;

    public ModelDeerFamiliar() {
        this.textureWidth = 64;
        this.textureHeight = 16;

        this.body = new ModelRenderer(this, 0, 0);
        this.body.addBox(-2.0F, -5.0F, -5.0F, 4, 5, 10);
        this.body.setRotationPoint(0.0F, 16.4F, 0.0F);

        this.leftFrontLeg1 = new ModelRenderer(this, 0, 0);
        this.leftFrontLeg1.addBox(-0.5F, 0.0F, -1.0F, 1, 4, 2);
        this.leftFrontLeg1.setRotationPoint(1.4F, -0.2F, -3.9F);
        setRotation(this.leftFrontLeg1, -toRad(80.0F), -toRad(10.0F), -toRad(40.0F));
        this.body.addChild(this.leftFrontLeg1);

        this.tail = new ModelRenderer(this, 22, 0);
        this.tail.addBox(-1.0F, -0.5F, 0.0F, 2, 1, 2);
        this.tail.setRotationPoint(0.0F, -4.0F, 4.5F);
        setRotation(this.tail, -0.4363323F, 0.0F, 0.0F);
        this.body.addChild(this.tail);

        this.neck = new ModelRenderer(this, 30, 0);
        this.neck.addBox(-1.0F, -3.0F, -1.0F, 2, 3, 2);
        this.neck.setRotationPoint(0.0F, -4.0F, -4.0F);
        setRotation(this.neck, 0.3909537F, 0.0F, 0.0F);
        this.body.addChild(this.neck);

        this.rightFrontLeg1 = new ModelRenderer(this, 0, 0);
        this.rightFrontLeg1.mirror = true;
        this.rightFrontLeg1.addBox(-0.5F, 0.0F, -1.0F, 1, 4, 2);
        this.rightFrontLeg1.setRotationPoint(-1.4F, -0.2F, -3.9F);
        setRotation(this.rightFrontLeg1, -toRad(80.0F), -toRad(10.0F), -toRad(40.0F));
        this.body.addChild(this.rightFrontLeg1);

        this.leftBackLeg1 = new ModelRenderer(this, 28, 9);
        this.leftBackLeg1.addBox(-0.5F, 0.0F, -1.0F, 1, 4, 2);
        this.leftBackLeg1.setRotationPoint(1.4F, -0.2F, 3.9F);
        setRotation(this.leftBackLeg1, toRad(80.0F), toRad(10.0F), -toRad(40.0F));
        this.body.addChild(this.leftBackLeg1);

        this.rightBackLeg1 = new ModelRenderer(this, 28, 9);
        this.rightBackLeg1.mirror = true;
        this.rightBackLeg1.addBox(-0.5F, 0.0F, -1.0F, 1, 4, 2);
        this.rightBackLeg1.setRotationPoint(-1.4F, -0.2F, 3.9F);
        setRotation(this.rightBackLeg1, toRad(80.0F), toRad(10.0F), -toRad(40.0F));
        this.body.addChild(this.rightBackLeg1);

        this.leftFrontLeg2 = new ModelRenderer(this, 18, 0);
        this.leftFrontLeg2.addBox(-0.5F, 0.0F, -0.5F, 1, 4, 1);
        this.leftFrontLeg2.setRotationPoint(-0.01F, 3.8F, 0.0F);
        setRotation(this.leftFrontLeg2, 0.1172861F, 0.0F, 0.0F);
        this.leftFrontLeg1.addChild(this.leftFrontLeg2);

        this.head = new ModelRenderer(this, 38, 0);
        this.head.addBox(-1.5F, -3.0F, -1.5F, 3, 3, 3);
        this.head.setRotationPoint(0.0F, -2.5F, -0.3F);
        this.neck.addChild(this.head);

        this.nose = new ModelRenderer(this, 51, 5);
        this.nose.addBox(-1.0F, -1.0F, -2.0F, 2, 2, 3);
        this.nose.setRotationPoint(0.0F, -1.01F, -1.5F);
        this.head.addChild(this.nose);

        ModelRenderer leftEar = new ModelRenderer(this, 22, 3);
        leftEar.addBox(-1.0F, -2.0F, 0.0F, 2, 2, 1);
        leftEar.setRotationPoint(1.0F, -2.5F, 0.5F);
        setRotation(leftEar, -0.3909537F, 0.0F, 0.7819075F);
        this.head.addChild(leftEar);

        ModelRenderer rightEar = new ModelRenderer(this, 22, 3);
        rightEar.mirror = true;
        rightEar.addBox(-1.0F, -2.0F, 0.0F, 2, 2, 1);
        rightEar.setRotationPoint(-1.0F, -2.5F, 0.5F);
        setRotation(rightEar, -0.3909537F, 0.0F, -0.7819075F);
        this.head.addChild(rightEar);

        this.addHorns();
        this.hammerHandle = this.addHammer();

        this.rightFrontLeg2 = new ModelRenderer(this, 18, 0);
        this.rightFrontLeg2.mirror = true;
        this.rightFrontLeg2.addBox(-0.5F, 0.0F, -0.5F, 1, 4, 1);
        this.rightFrontLeg2.setRotationPoint(0.01F, 3.8F, 0.0F);
        setRotation(this.rightFrontLeg2, 0.1172861F, 0.0F, 0.0F);
        this.rightFrontLeg1.addChild(this.rightFrontLeg2);

        this.leftBackLeg2 = new ModelRenderer(this, 34, 10);
        this.leftBackLeg2.addBox(-0.5F, 0.0F, -0.5F, 1, 4, 1);
        this.leftBackLeg2.setRotationPoint(-0.01F, 3.8F, 0.0F);
        setRotation(this.leftBackLeg2, -0.1172861F, 0.0F, 0.0F);
        this.leftBackLeg1.addChild(this.leftBackLeg2);

        this.rightBackLeg2 = new ModelRenderer(this, 34, 10);
        this.rightBackLeg2.mirror = true;
        this.rightBackLeg2.addBox(-0.5F, 0.0F, -0.5F, 1, 4, 1);
        this.rightBackLeg2.setRotationPoint(0.01F, 3.8F, 0.0F);
        setRotation(this.rightBackLeg2, -0.1172861F, 0.0F, 0.0F);
        this.rightBackLeg1.addChild(this.rightBackLeg2);
    }

    @Override
    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
            float headPitch, float scale) {
        this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);
        this.body.render(scale);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
            float headPitch, float scaleFactor, Entity entityIn) {
        EntityDeerFamiliar deer = (EntityDeerFamiliar) entityIn;
        this.neck.rotateAngleX = deer.getNeckRot(ageInTicks - deer.ticksExisted);
        this.hammerHandle.showModel = deer.hasBlacksmithUpgrade();

        if (deer.isEating()) {
            this.head.rotateAngleX = MathHelper.cos(ageInTicks * 0.8F) * 0.2F;
            this.head.rotateAngleY = 0.0F;
        } else {
            this.head.rotateAngleY = netHeadYaw * 0.017453292F * 0.5F;
            this.head.rotateAngleX = headPitch * 0.017453292F * 0.5F;
        }

        if (this.swingProgress > 0.01F) {
            float swing = MathHelper.sin(this.swingProgress * (float) Math.PI);
            this.head.rotateAngleY = swing * toRad(50.0F);
            this.head.rotateAngleZ = swing * toRad(-40.0F);
            this.nose.rotateAngleY = swing * toRad(18.0F);
            this.nose.rotateAngleZ = swing * toRad(-18.0F);
        } else {
            this.head.rotateAngleZ = 0.0F;
            this.nose.rotateAngleY = 0.0F;
            this.nose.rotateAngleZ = 0.0F;
        }

        this.tail.rotateAngleX = MathHelper.cos(limbSwing * 0.7F) * 0.4F * limbSwingAmount - 0.3F;
        this.body.rotateAngleX = 0.0F;

        if (deer.isSitting()) {
            setRotation(this.rightBackLeg1, toRad(80.0F), toRad(10.0F), -toRad(40.0F));
            setRotation(this.leftBackLeg1, toRad(80.0F), toRad(10.0F), -toRad(40.0F));
            setRotation(this.rightFrontLeg1, -toRad(80.0F), -toRad(10.0F), -toRad(40.0F));
            setRotation(this.leftFrontLeg1, -toRad(80.0F), -toRad(10.0F), -toRad(40.0F));
            this.rightBackLeg2.rotationPointZ = -0.5F;
            this.leftBackLeg2.rotationPointZ = -0.5F;
            this.rightFrontLeg2.rotationPointZ = 0.5F;
            this.leftFrontLeg2.rotationPointZ = 0.5F;
            this.rightBackLeg2.rotateAngleX = -toRad(150.0F);
            this.leftBackLeg2.rotateAngleX = -toRad(150.0F);
            this.rightFrontLeg2.rotateAngleX = toRad(150.0F);
            this.leftFrontLeg2.rotateAngleX = toRad(150.0F);
            return;
        }

        boolean fast = limbSwingAmount > 0.8F;
        setRotation(this.rightBackLeg1, MathHelper.cos(limbSwing * 0.7F + (fast ? (float) Math.PI : 0.0F))
                * 1.4F * limbSwingAmount, 0.0F, 0.0F);
        setRotation(this.leftBackLeg1, MathHelper.cos(limbSwing * 0.7F + (float) Math.PI)
                * 1.4F * limbSwingAmount, 0.0F, 0.0F);
        setRotation(this.rightFrontLeg1, MathHelper.cos(limbSwing * 0.7F + (fast ? 0.0F : (float) Math.PI))
                * 1.4F * limbSwingAmount, 0.0F, 0.0F);
        setRotation(this.leftFrontLeg1, MathHelper.cos(limbSwing * 0.7F) * 1.4F * limbSwingAmount, 0.0F, 0.0F);

        this.rightBackLeg2.rotationPointZ = 0.0F;
        this.leftBackLeg2.rotationPointZ = 0.0F;
        this.rightFrontLeg2.rotationPointZ = 0.0F;
        this.leftFrontLeg2.rotationPointZ = 0.0F;
        this.rightBackLeg2.rotateAngleX = -0.1F;
        this.leftBackLeg2.rotateAngleX = -0.1F;
        this.rightFrontLeg2.rotateAngleX = 0.1F;
        this.leftFrontLeg2.rotateAngleX = 0.1F;
    }

    private void addHorns() {
        ModelRenderer leftHorn1 = new ModelRenderer(this, 50, 4);
        leftHorn1.addBox(-0.5F, -3.0F, -0.5F, 1, 3, 1);
        leftHorn1.setRotationPoint(0.7F, -2.5F, 0.3F);
        setRotation(leftHorn1, 0.0F, 0.0F, 0.3127630F);
        this.head.addChild(leftHorn1);

        ModelRenderer rightHorn1 = new ModelRenderer(this, 50, 4);
        rightHorn1.addBox(-0.5F, -3.0F, -0.5F, 1, 3, 1);
        rightHorn1.setRotationPoint(-0.7F, -2.5F, 0.3F);
        setRotation(rightHorn1, 0.0F, 0.0F, -0.3127630F);
        this.head.addChild(rightHorn1);

        ModelRenderer leftHorn2 = new ModelRenderer(this, 58, 0);
        leftHorn2.addBox(-0.5F, -2.0F, -0.5F, 1, 2, 1);
        leftHorn2.setRotationPoint(0.3F, -2.5F, 0.0F);
        setRotation(leftHorn2, 0.0F, 0.0F, 1.1344640F);
        leftHorn1.addChild(leftHorn2);

        ModelRenderer leftHorn3 = new ModelRenderer(this, 18, 5);
        leftHorn3.addBox(-0.5F, -2.0F, -0.5F, 1, 2, 1);
        leftHorn3.setRotationPoint(0.0F, -2.7F, 0.0F);
        setRotation(leftHorn3, 0.5864306F, 0.0F, 0.0F);
        leftHorn1.addChild(leftHorn3);

        ModelRenderer rightHorn2 = new ModelRenderer(this, 58, 0);
        rightHorn2.addBox(-0.5F, -2.0F, -0.5F, 1, 2, 1);
        rightHorn2.setRotationPoint(-0.3F, -2.5F, 0.0F);
        setRotation(rightHorn2, 0.0F, 0.0F, -1.1344640F);
        rightHorn1.addChild(rightHorn2);

        ModelRenderer rightHorn3 = new ModelRenderer(this, 18, 5);
        rightHorn3.addBox(-0.5F, -2.0F, -0.5F, 1, 2, 1);
        rightHorn3.setRotationPoint(0.0F, -2.7F, 0.0F);
        setRotation(rightHorn3, 0.5864306F, 0.0F, 0.0F);
        rightHorn1.addChild(rightHorn3);
    }

    private ModelRenderer addHammer() {
        ModelRenderer handle = new ModelRenderer(this, 38, 14);
        handle.addBox(0.0F, -0.5F, -0.5F, 5, 1, 1);
        handle.setRotationPoint(-1.8F, 0.3F, -1.3F);
        this.nose.addChild(handle);

        ModelRenderer head = new ModelRenderer(this, 50, 12);
        head.addBox(-1.0F, -1.0F, -1.0F, 2, 2, 2);
        head.setRotationPoint(6.0F, 0.0F, 0.0F);
        setRotation(head, 0.0F, 0.7853982F, 0.0F);
        handle.addChild(head);

        ModelRenderer spike1 = new ModelRenderer(this, 58, 10);
        spike1.addBox(-0.5F, -0.5F, 0.0F, 1, 1, 0);
        spike1.setRotationPoint(-0.2F, -1.2F, -0.6F);
        setRotation(spike1, 0.0F, 0.0F, 0.7853982F);
        head.addChild(spike1);

        ModelRenderer spike2 = new ModelRenderer(this, 58, 10);
        spike2.addBox(-0.5F, -0.5F, 0.0F, 1, 1, 0);
        spike2.setRotationPoint(0.2F, -1.2F, 0.4F);
        setRotation(spike2, 0.0F, 0.0F, 0.7853982F);
        head.addChild(spike2);

        ModelRenderer spike3 = new ModelRenderer(this, 58, 10);
        spike3.addBox(-0.5F, -0.5F, 0.0F, 1, 1, 0);
        spike3.setRotationPoint(1.2F, -0.3F, -0.6F);
        setRotation(spike3, 0.0F, 0.0F, 0.7853982F);
        head.addChild(spike3);

        ModelRenderer spike4 = new ModelRenderer(this, 58, 10);
        spike4.addBox(-0.5F, -0.5F, 0.0F, 1, 1, 0);
        spike4.setRotationPoint(1.2F, 0.3F, 0.4F);
        setRotation(spike4, 0.0F, 0.0F, 0.7853982F);
        head.addChild(spike4);

        ModelRenderer spike5 = new ModelRenderer(this, 58, 9);
        spike5.addBox(0.0F, -0.5F, -0.5F, 0, 1, 1);
        spike5.setRotationPoint(0.5F, -0.2F, 1.2F);
        setRotation(spike5, 0.7853982F, 0.0F, 0.0F);
        head.addChild(spike5);

        ModelRenderer spike6 = new ModelRenderer(this, 58, 9);
        spike6.addBox(0.0F, -0.5F, -0.5F, 0, 1, 1);
        spike6.setRotationPoint(-0.4F, 0.3F, 1.2F);
        setRotation(spike6, 0.7853982F, 0.0F, 0.0F);
        head.addChild(spike6);

        ModelRenderer spike7 = new ModelRenderer(this, 58, 9);
        spike7.addBox(0.0F, -0.5F, -0.5F, 0, 1, 1);
        spike7.setRotationPoint(0.6F, 1.2F, -0.1F);
        setRotation(spike7, 0.7853982F, 0.0F, 0.0F);
        head.addChild(spike7);

        ModelRenderer spike8 = new ModelRenderer(this, 58, 9);
        spike8.addBox(0.0F, -0.5F, -0.5F, 0, 1, 1);
        spike8.setRotationPoint(-0.5F, 1.2F, 0.3F);
        setRotation(spike8, 0.7853982F, 0.0F, 0.0F);
        head.addChild(spike8);
        return handle;
    }

    private static void setRotation(ModelRenderer renderer, float x, float y, float z) {
        renderer.rotateAngleX = x;
        renderer.rotateAngleY = y;
        renderer.rotateAngleZ = z;
    }

    private static float toRad(float degrees) {
        return (float) Math.toRadians(degrees);
    }
}
