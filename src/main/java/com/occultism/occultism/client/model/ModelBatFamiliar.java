package com.occultism.occultism.client.model;

import com.occultism.occultism.entity.EntityBatFamiliar;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelBatFamiliar extends ModelBase {

    private final ModelRenderer body1;
    private final ModelRenderer head;
    private final ModelRenderer body2;
    private final ModelRenderer hair;
    private final ModelRenderer ribbon;
    private final ModelRenderer leftWing1;
    private final ModelRenderer rightWing1;
    private final ModelRenderer leftWing2;
    private final ModelRenderer rightWing2;
    private final ModelRenderer leftLeg;
    private final ModelRenderer rightLeg;
    private final ModelRenderer tail;
    private final ModelRenderer stick;
    private final ModelRenderer goblet1;

    public ModelBatFamiliar() {
        this.textureWidth = 64;
        this.textureHeight = 32;

        this.body1 = new ModelRenderer(this, 8, 10);
        this.body1.addBox(-2.0F, 0.0F, -1.5F, 4, 1, 3);
        this.body1.setRotationPoint(0.0F, 14.0F, 0.0F);

        this.head = new ModelRenderer(this, 1, 0);
        this.head.addBox(-3.0F, -5.0F, -2.5F, 6, 5, 5);
        this.body1.addChild(this.head);

        this.addEye(true);
        this.addEye(false);
        this.addEar(true);
        this.addEar(false);

        this.hair = new ModelRenderer(this, 0, 18);
        this.hair.addBox(-1.5F, -2.0F, 0.0F, 3, 2, 0);
        this.hair.setRotationPoint(0.5F, -4.8F, -1.5F);
        setRotation(this.hair, -0.0390954F, 0.0F, -0.2345722F);
        this.head.addChild(this.hair);

        this.ribbon = new ModelRenderer(this, 0, 20);
        this.ribbon.addBox(-2.0F, -2.0F, 0.0F, 4, 4, 0);
        this.ribbon.setRotationPoint(0.0F, -5.0F, -2.51F);
        setRotation(this.ribbon, -0.1745329F, 0.1745329F, 0.7853982F);
        this.head.addChild(this.ribbon);

        ModelRenderer mouth = new ModelRenderer(this, 0, 12);
        mouth.addBox(-1.5F, -1.0F, -1.0F, 3, 2, 2);
        mouth.setRotationPoint(0.0F, -0.3F, -3.0F);
        setRotation(mouth, 0.1745329F, 0.0F, 0.0F);
        this.head.addChild(mouth);

        ModelRenderer nose = new ModelRenderer(this, 0, 16);
        nose.addBox(-1.0F, -1.0F, -1.0F, 2, 1, 1);
        nose.setRotationPoint(0.0F, -0.4F, -0.4F);
        mouth.addChild(nose);

        ModelRenderer tooth = new ModelRenderer(this, 0, 4);
        tooth.addBox(-0.5F, 0.0F, 0.0F, 1, 1, 0);
        tooth.setRotationPoint(0.5F, 0.9F, -0.5F);
        mouth.addChild(tooth);

        this.body2 = new ModelRenderer(this, 23, 0);
        this.body2.addBox(-2.5F, 0.0F, -2.0F, 5, 7, 4);
        this.body2.setRotationPoint(0.0F, 1.0F, 0.0F);
        this.body1.addChild(this.body2);

        this.leftLeg = new ModelRenderer(this, 24, 20);
        this.leftLeg.addBox(-1.0F, 0.0F, -0.5F, 2, 3, 1);
        this.leftLeg.setRotationPoint(1.1F, 6.3F, 0.0F);
        setRotation(this.leftLeg, 0.2366667F, 0.0F, 0.0174533F);
        this.body2.addChild(this.leftLeg);

        this.rightLeg = new ModelRenderer(this, 24, 20);
        this.rightLeg.mirror = true;
        this.rightLeg.addBox(-1.0F, 0.0F, -0.5F, 2, 3, 1);
        this.rightLeg.setRotationPoint(-1.1F, 6.3F, 0.0F);
        setRotation(this.rightLeg, 0.2366667F, 0.0F, -0.0174533F);
        this.body2.addChild(this.rightLeg);

        this.tail = new ModelRenderer(this, 34, 11);
        this.tail.addBox(0.0F, 0.0F, 0.0F, 0, 5, 5);
        this.tail.setRotationPoint(0.0F, 4.8F, 1.5F);
        setRotation(this.tail, 0.2366667F, 0.0F, 0.0F);
        this.body2.addChild(this.tail);

        this.leftWing1 = new ModelRenderer(this, 44, 26);
        this.leftWing1.addBox(0.0F, -6.0F, 0.0F, 10, 6, 0);
        this.leftWing1.setRotationPoint(1.8F, 4.0F, 0.0F);
        setRotation(this.leftWing1, 0.0F, 0.0F, 0.1745329F);
        this.body2.addChild(this.leftWing1);

        this.leftWing2 = new ModelRenderer(this, 44, 20);
        this.leftWing2.addBox(0.0F, -6.0F, 0.0F, 10, 6, 0);
        this.leftWing2.setRotationPoint(0.0F, -6.0F, 0.0F);
        this.leftWing1.addChild(this.leftWing2);

        this.rightWing1 = new ModelRenderer(this, 44, 26);
        this.rightWing1.mirror = true;
        this.rightWing1.addBox(-10.0F, -6.0F, 0.0F, 10, 6, 0);
        this.rightWing1.setRotationPoint(-1.8F, 4.0F, 0.0F);
        setRotation(this.rightWing1, 0.0F, 0.0F, -0.1745329F);
        this.body2.addChild(this.rightWing1);

        this.rightWing2 = new ModelRenderer(this, 44, 20);
        this.rightWing2.mirror = true;
        this.rightWing2.addBox(-10.0F, -6.0F, 0.0F, 10, 6, 0);
        this.rightWing2.setRotationPoint(0.0F, -6.0F, 0.0F);
        this.rightWing1.addChild(this.rightWing2);

        this.stick = new ModelRenderer(this, 10, 14);
        this.stick.addBox(-7.0F, 0.0F, 0.0F, 14, 1, 1);
        this.stick.setRotationPoint(0.0F, 9.0F, 0.0F);
        this.body2.addChild(this.stick);

        this.goblet1 = new ModelRenderer(this, 10, 30);
        this.goblet1.addBox(0.0F, -0.5F, -0.5F, 5, 1, 1);
        this.goblet1.setRotationPoint(-3.5F, 3.0F, 0.0F);
        setRotation(this.goblet1, 0.7853982F, 0.0F, 0.0F);
        this.leftLeg.addChild(this.goblet1);

        ModelRenderer goblet2 = new ModelRenderer(this, 10, 26);
        goblet2.addBox(0.0F, -1.0F, -1.0F, 0, 2, 2);
        this.goblet1.addChild(goblet2);

        ModelRenderer goblet3 = new ModelRenderer(this, 22, 26);
        goblet3.addBox(0.0F, -1.5F, -1.5F, 3, 3, 3);
        goblet3.setRotationPoint(5.01F, 0.0F, 0.0F);
        this.goblet1.addChild(goblet3);

        addGobletCorner(goblet3, 31, 27, 1.5F, -1.5F, 0.0F, 0.0F, 0.7853982F, 0.0F);
        addGobletCorner(goblet3, 35, 27, 1.5F, 1.5F, 0.0F, 0.0F, 0.7853982F, 0.0F);
        addGobletCorner(goblet3, 34, 29, 1.5F, 0.0F, 1.5F, 0.0F, 0.0F, 0.7853982F);
        addGobletCorner(goblet3, 38, 29, 1.5F, 0.0F, -1.5F, 0.0F, 0.0F, 0.7853982F);
    }

    @Override
    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
            float headPitch, float scale) {
        this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);
        this.body1.render(scale);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
            float headPitch, float scaleFactor, Entity entityIn) {
        EntityBatFamiliar bat = (EntityBatFamiliar) entityIn;
        this.hair.showModel = bat.hasHair();
        this.ribbon.showModel = bat.hasRibbon();
        this.tail.showModel = bat.hasTail();
        this.stick.showModel = bat.isSitting();
        this.goblet1.showModel = bat.hasBlacksmithUpgrade() && !bat.isSitting();

        this.body1.rotateAngleY = 0.0F;
        this.body1.rotateAngleZ = 0.0F;
        this.head.rotateAngleX = headPitch * 0.017453292F;
        this.head.rotateAngleY = netHeadYaw * 0.017453292F;
        this.head.rotateAngleZ = 0.0F;

        float height = MathHelper.cos(ageInTicks / 5.0F);
        this.leftWing1.rotateAngleX = height * toRads(20.0F) - 0.15F;
        this.rightWing1.rotateAngleX = this.leftWing1.rotateAngleX;
        this.leftWing1.rotateAngleY = height * toRads(20.0F) - 0.15F;
        this.rightWing1.rotateAngleY = -height * toRads(20.0F) + 0.15F;
        this.leftWing2.rotateAngleX = height * toRads(20.0F) + toRads(15.0F);
        this.rightWing2.rotateAngleX = this.leftWing2.rotateAngleX;
        this.body1.rotateAngleX = toRads(20.0F) + limbSwingAmount * toRads(30.0F);
        this.leftLeg.rotateAngleX = 0.24F + MathHelper.cos(ageInTicks * 0.1F) * toRads(20.0F);
        this.rightLeg.rotateAngleX = this.leftLeg.rotateAngleX;

        if (bat.isSitting()) {
            this.leftWing1.rotateAngleX = 0.0F;
            this.rightWing1.rotateAngleX = 0.0F;
            this.leftWing1.rotateAngleY = toRads(80.0F);
            this.rightWing1.rotateAngleY = -toRads(80.0F);
            this.leftWing2.rotateAngleX = toRads(15.0F);
            this.rightWing2.rotateAngleX = toRads(15.0F);
            this.head.rotateAngleX = 0.2F;
            this.body1.rotateAngleX = toRads(180.0F);
            this.body1.rotateAngleY = toRads(180.0F);
            this.leftLeg.rotateAngleX = 0.24F;
            this.rightLeg.rotateAngleX = 0.24F;
        }
    }

    private void addEye(boolean left) {
        ModelRenderer eye = new ModelRenderer(this, 0, 0);
        eye.addBox(-1.0F, -1.0F, 0.0F, 2, 3, 1);
        eye.setRotationPoint(left ? 1.4F : -1.4F, -3.2F, -3.1F);
        setRotation(eye, 0.0F, 0.0F, left ? 0.1972222F : -0.1972222F);
        this.head.addChild(eye);
    }

    private void addEar(boolean left) {
        ModelRenderer ear = new ModelRenderer(this, 0, 27);
        ear.mirror = !left;
        ear.addBox(-2.5F, -5.0F, 0.0F, 5, 5, 0);
        ear.setRotationPoint(left ? 2.0F : -2.0F, -4.5F, 0.5F);
        setRotation(ear, 0.0F, left ? -0.2364921F : 0.2364921F, left ? 0.355F : -0.355F);
        this.head.addChild(ear);

        ModelRenderer earTip = new ModelRenderer(this, 0, 25);
        earTip.mirror = !left;
        earTip.addBox(-2.5F, -2.0F, 0.0F, 5, 2, 0);
        earTip.setRotationPoint(0.0F, -5.0F, 0.0F);
        setRotation(earTip, 0.4363323F, 0.0F, 0.0F);
        ear.addChild(earTip);

        ModelRenderer earBack = new ModelRenderer(this, 8, 20);
        earBack.mirror = !left;
        earBack.addBox(-2.5F, -5.0F, 0.0F, 5, 5, 0);
        earBack.setRotationPoint(0.0F, 0.0F, 0.03F);
        ear.addChild(earBack);
    }

    private void addGobletCorner(ModelRenderer parent, int textureX, int textureY, float x, float y, float z,
            float rotationX, float rotationY, float rotationZ) {
        ModelRenderer corner = new ModelRenderer(this, textureX, textureY);
        corner.addBox(-0.5F, -0.5F, -0.5F, 1, 1, 1);
        corner.setRotationPoint(x, y, z);
        setRotation(corner, rotationX, rotationY, rotationZ);
        parent.addChild(corner);
    }

    private static void setRotation(ModelRenderer renderer, float x, float y, float z) {
        renderer.rotateAngleX = x;
        renderer.rotateAngleY = y;
        renderer.rotateAngleZ = z;
    }

    private static float toRads(float degrees) {
        return (float) Math.toRadians(degrees);
    }
}
