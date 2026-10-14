package com.occultism.occultism.client.render;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class ParticleRitualWaiting extends Particle {

    private final double portalPosX;
    private final double portalPosY;
    private final double portalPosZ;
    private final float portalParticleScale;

    private ParticleRitualWaiting(World world, double x, double y, double z,
            double motionX, double motionY, double motionZ) {
        super(world, x, y, z, motionX, motionY, motionZ);
        this.motionX = motionX;
        this.motionY = motionY;
        this.motionZ = motionZ;
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        this.portalPosX = x;
        this.portalPosY = y;
        this.portalPosZ = z;
        this.particleScale = this.rand.nextFloat() * 0.2F + 0.5F;
        this.portalParticleScale = this.particleScale;
        float shade = this.rand.nextFloat() * 0.6F + 0.4F;
        this.particleRed = shade * 0.3F;
        this.particleGreen = shade * 0.3F;
        this.particleBlue = shade * 0.3F;
        this.particleMaxAge = this.rand.nextInt(10) + 40;
        this.setParticleTextureIndex(this.rand.nextInt(8));
    }

    @Override
    public void renderParticle(BufferBuilder buffer, Entity entity, float partialTicks,
            float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        float progress = ((float) this.particleAge + partialTicks) / (float) this.particleMaxAge;
        progress = 1.0F - progress;
        progress = progress * progress;
        progress = 1.0F - progress;
        this.particleScale = this.portalParticleScale * progress;
        super.renderParticle(buffer, entity, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY,
                rotationXZ);
    }

    @Override
    public int getBrightnessForRender(float partialTick) {
        int brightness = super.getBrightnessForRender(partialTick);
        float progress = (float) this.particleAge / (float) this.particleMaxAge;
        progress = progress * progress;
        progress = progress * progress;
        int blockLight = brightness & 255;
        int skyLight = brightness >> 16 & 255;
        skyLight = Math.min(240, skyLight + (int) (progress * 15.0F * 16.0F));
        return blockLight | skyLight << 16;
    }

    @Override
    public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        float progress = (float) this.particleAge / (float) this.particleMaxAge;
        float curve = -progress + progress * progress * 2.0F;
        float inverseCurve = 1.0F - curve;
        this.posX = this.portalPosX + this.motionX * (double) inverseCurve;
        this.posY = this.portalPosY + this.motionY * (double) inverseCurve + (double) (1.0F - progress);
        this.posZ = this.portalPosZ + this.motionZ * (double) inverseCurve;
        if (this.particleAge++ >= this.particleMaxAge) {
            this.setExpired();
        }
    }

    @Override
    public void move(double x, double y, double z) {
        this.setBoundingBox(this.getBoundingBox().offset(x, y, z));
        this.resetPositionToBB();
    }

    public static void spawn(World world, double x, double y, double z) {
        Particle particle = new ParticleRitualWaiting(world, x, y, z, 0.0D, 0.0D, 0.0D);
        net.minecraft.client.Minecraft.getMinecraft().effectRenderer.addEffect(particle);
    }
}
