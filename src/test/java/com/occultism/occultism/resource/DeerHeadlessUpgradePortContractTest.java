package com.occultism.occultism.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.occultism.occultism.entity.EntityCthulhuFamiliar;
import com.occultism.occultism.entity.EntityDeerFamiliar;
import com.occultism.occultism.entity.EntityHeadlessFamiliar;

import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityHusk;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityStray;
import net.minecraft.entity.monster.EntityWitherSkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.init.Bootstrap;
import org.junit.jupiter.api.Test;

class DeerHeadlessUpgradePortContractTest {

    private static final Path ROOT = Paths.get("src", "main");
    private static final Path JAVA_ROOT = ROOT.resolve("java").resolve("com").resolve("occultism")
            .resolve("occultism");
    private static final Path ASSET_ROOT = ROOT.resolve("resources").resolve("assets").resolve("occultism");
    private static final String DEER_TEXTURE_SHA256 =
            "c84f4acf2a897367b98a3effe8f811cd56259e0e87af5c8c4ebdac5209ead096";
    private static final String HEADLESS_TEXTURE_SHA256 =
            "419dfbcbb3e5362790b60cd0ee8847e9e3030f5d97fbbbd215e2f73dcf5f3619";

    @Test
    void deerAndHeadlessAcceptExactlyOneBlacksmithUpgrade() {
        Bootstrap.register();

        EntityDeerFamiliar deer = new EntityDeerFamiliar(null);
        assertFalse(deer.hasBlacksmithUpgrade());
        assertTrue(deer.canBlacksmithUpgrade());
        assertSame(deer, deer.getFamiliarEntity());
        assertNull(deer.getFamiliarOwner());
        deer.blacksmithUpgrade();
        assertTrue(deer.hasBlacksmithUpgrade());
        assertFalse(deer.canBlacksmithUpgrade());
        deer.blacksmithUpgrade();
        assertTrue(deer.hasBlacksmithUpgrade());

        EntityHeadlessFamiliar headless = new EntityHeadlessFamiliar(null);
        assertFalse(headless.hasBlacksmithUpgrade());
        assertTrue(headless.canBlacksmithUpgrade());
        assertSame(headless, headless.getFamiliarEntity());
        assertNull(headless.getFamiliarOwner());
        headless.blacksmithUpgrade();
        assertTrue(headless.hasBlacksmithUpgrade());
        assertFalse(headless.canBlacksmithUpgrade());
        headless.blacksmithUpgrade();
        assertTrue(headless.hasBlacksmithUpgrade());
    }

    @Test
    void upgradeFlagsUseTheSharedUpstreamNbtKey() throws IOException {
        String deer = readUtf8(JAVA_ROOT.resolve("entity").resolve("EntityDeerFamiliar.java"));
        String headless = readUtf8(JAVA_ROOT.resolve("entity").resolve("EntityHeadlessFamiliar.java"));
        for (String entity : new String[] {deer, headless}) {
            assertTrue(entity.contains("this.setBlacksmithUpgrade(compound.getBoolean(\"hasBlacksmithUpgrade\"));"));
            assertTrue(entity.contains("compound.setBoolean(\"hasBlacksmithUpgrade\", this.hasBlacksmithUpgrade());"));
            assertTrue(entity.contains("EntityDataManager.createKey("));
            assertTrue(entity.contains("DataSerializers.BOOLEAN"));
        }
    }

    @Test
    void sharedFireAttackMatchesThe1814TargetAndCooldownContract() throws IOException {
        String devil = readUtf8(JAVA_ROOT.resolve("entity").resolve("EntityDevilFamiliar.java"));
        String goal = devil.substring(devil.indexOf("public static class AttackGoal"));
        assertTrue(goal.contains("private static final int MAX_COOLDOWN = 20;"));
        assertTrue(goal.contains("this.cooldown-- < 0"));
        assertTrue(goal.contains("this.familiar.getOwner() instanceof EntityPlayer"));
        assertTrue(goal.contains("owner.getRevengeTarget()"));
        assertTrue(goal.contains("owner.getLastAttackedEntity()"));
        assertTrue(goal.contains("this.familiar.getDistanceSq(target) < this.range"));
        assertFalse(goal.contains("targets.contains(target)"),
                "The 1.81.4 helper intentionally retains the same target twice");
        assertFalse(goal.contains("isSitting()"), "Sitting is not an upstream fire-attack gate");
        assertFalse(goal.contains("spawnFireParticles"));
        assertFalse(goal.contains("EnumParticleTypes.FLAME"),
                "The shared upstream attack only ignites its targets");
        assertTrue(goal.contains("DamageSource.causePlayerDamage(owner), 4.0F"));
        assertTrue(goal.contains("target.setFire(4);"));

        String devilUpdate = section(devil, "public void onLivingUpdate() {", "public boolean processInteract");
        assertTrue(devilUpdate.contains("this.world.isRemote && this.isSwingInProgress"));
        String devilParticles = section(devil, "private void spawnAttackParticles() {",
                "public static class AttackGoal");
        assertTrue(devilParticles.contains("for (int i = 0; i < 5; i++)"));
        assertTrue(devilParticles.contains("EnumParticleTypes.FLAME"),
                "Only Devil retains the upstream client-side swing flames");

        String deer = readUtf8(JAVA_ROOT.resolve("entity").resolve("EntityDeerFamiliar.java"));
        assertTrue(deer.contains("new EntityDevilFamiliar.AttackGoal(this, 5.0F)"));
        assertTrue(deer.contains("return super.shouldExecute()"
                + " && EntityDeerFamiliar.this.hasBlacksmithUpgrade();"),
                "Calling super first preserves the upstream cooldown pre-decrement while unupgraded");

        String headless = readUtf8(JAVA_ROOT.resolve("entity").resolve("EntityHeadlessFamiliar.java"));
        assertTrue(headless.contains("return super.shouldExecute()"
                + " && !EntityHeadlessFamiliar.this.isHeadlessDead();"));
        assertFalse(headless.contains("EntityAIAttackMelee"),
                "Headless uses the upstream fire attack instead of a second melee attack");
        assertFalse(headless.contains("OwnerAttackTargetGoal"));

        String handler = readUtf8(JAVA_ROOT.resolve("handler").resolve("FamiliarEventHandler.java"));
        assertTrue(handler.contains("onLivingDamage(LivingDamageEvent event)"),
                "The bonus applies at the upstream final-damage event stage");
        assertTrue(handler.contains("event.setAmount(event.getAmount() * 1.3F);"),
                "A matching Headless familiar grants the upstream thirty-percent damage bonus");
    }

    @Test
    void upgradedDeerShowsAndSwingsTheCompleteWarhammer() throws IOException, NoSuchAlgorithmException {
        String model = readUtf8(JAVA_ROOT.resolve("client").resolve("model")
                .resolve("ModelDeerFamiliar.java"));
        assertTrue(model.contains("this.hammerHandle.showModel = deer.hasBlacksmithUpgrade();"));
        assertTrue(model.contains("this.swingProgress > 0.01F"));
        assertTrue(model.contains("this.head.rotateAngleY = swing * toRad(50.0F);"));
        assertTrue(model.contains("this.nose.rotateAngleZ = swing * toRad(-18.0F);"));
        assertEquals(8, countOccurrences(model, "ModelRenderer spike"));
        assertEquals(8, countOccurrences(model, "head.addChild(spike"));
        assertTrue(model.contains("spike1.setRotationPoint(-0.2F, -1.2F, -0.6F);"));
        assertTrue(model.contains("spike8.setRotationPoint(-0.5F, 1.2F, 0.3F);"));
        assertEquals(DEER_TEXTURE_SHA256,
                sha256(ASSET_ROOT.resolve("textures").resolve("entity").resolve("deer_familiar.png")));
    }

    @Test
    void headlessAuraParticleAndHelmetMatchThePortContract() throws IOException, NoSuchAlgorithmException {
        String entity = readUtf8(JAVA_ROOT.resolve("entity").resolve("EntityHeadlessFamiliar.java"));
        String aura = section(entity, "private void applyBlacksmithWeaknessAura() {",
                "private void spawnBlacksmithUpgradeParticle() {");
        assertTrue(aura.contains("!this.hasBlacksmithUpgrade()"));
        assertTrue(aura.contains("this.isHeadlessDead()"));
        assertTrue(aura.contains("this.ticksExisted % 10 != 0"));
        assertTrue(aura.contains("HEAD_TYPES.containsValue(Byte.valueOf(this.getHead()))"));
        assertTrue(aura.contains("this.getEntityBoundingBox().grow(5.0D)"));
        assertTrue(aura.contains("candidate != owner && this.hasMatchingHead(candidate)"));
        assertTrue(aura.contains("new PotionEffect(MobEffects.WEAKNESS, 60, 0)"));

        String update = section(entity, "public void onLivingUpdate() {", "public boolean processInteract");
        assertTrue(update.contains("this.hasBlacksmithUpgrade() && !this.isHeadlessDead()"
                + " && this.ticksExisted % 10 == 0"));
        String particle = section(entity, "private void spawnBlacksmithUpgradeParticle() {",
                "private void setVariant");
        assertTrue(particle.contains("EnumParticleTypes.SPELL_MOB"),
                "1.12 uses a cyan spell particle because SOUL_FIRE_FLAME does not exist");

        String model = readUtf8(JAVA_ROOT.resolve("client").resolve("model")
                .resolve("ModelHeadlessFamiliar.java"));
        assertTrue(model.contains("this.helmet = new ModelRenderer(this, 0, 32);"));
        assertTrue(model.contains("this.helmet.addBox(-2.5F, -6.0F, -2.5F, 5, 6, 5);"));
        assertTrue(model.contains("this.helmet.setRotationPoint(0.0F, -6.0F, 0.0F);"));
        assertTrue(model.contains("this.helmet.showModel = headless.hasBlacksmithUpgrade();"));
        assertEquals(HEADLESS_TEXTURE_SHA256,
                sha256(ASSET_ROOT.resolve("textures").resolve("entity").resolve("headless_familiar.png")));
    }

    @Test
    void headlessMatchingUsesTheSevenExact1814Types() throws IOException {
        Bootstrap.register();

        String source = readUtf8(JAVA_ROOT.resolve("entity").resolve("EntityHeadlessFamiliar.java"));
        assertEquals(7, countOccurrences(source, "registerHead((byte) "));
        assertTrue(source.contains("registerHead((byte) 1, EntityPlayer.class);"));
        assertTrue(source.contains("registerHead((byte) 7, EntityCthulhuFamiliar.class);"));

        EntityHeadlessFamiliar headless = new EntityHeadlessFamiliar(null);
        assertTrue(headless.stealHeadFrom(new EntityZombie(null)));
        assertTrue(headless.hasMatchingHead(new EntityZombie(null)));
        assertFalse(headless.stealHeadFrom(new EntityHusk(null)));
        assertFalse(headless.stealHeadFrom(new EntityZombie(null) { }),
                "Unregistered subclasses must not collapse into the vanilla Zombie head type");

        assertTrue(headless.stealHeadFrom(new EntitySkeleton(null)));
        assertTrue(headless.hasMatchingHead(new EntitySkeleton(null)));
        assertFalse(headless.stealHeadFrom(new EntityStray(null)));

        assertTrue(headless.stealHeadFrom(new EntityWitherSkeleton(null)));
        assertTrue(headless.stealHeadFrom(new EntityCreeper(null)));
        assertTrue(headless.stealHeadFrom(new EntitySpider(null)));
        assertTrue(headless.stealHeadFrom(new EntityCthulhuFamiliar(null)));
        assertTrue(headless.hasMatchingHead(new EntityCthulhuFamiliar(null)));
    }

    @Test
    void ritualTooltipsDescribeBothNewUpgradeEffects() throws IOException {
        String english = readUtf8(ASSET_ROOT.resolve("lang").resolve("en_us.lang"));
        String chinese = readUtf8(ASSET_ROOT.resolve("lang").resolve("zh_cn.lang"));
        assertTrue(english.contains("A Blacksmith can upgrade it once with a warhammer"));
        assertTrue(english.contains("weaken nearby enemies matching its current head"));
        assertTrue(chinese.contains("使其用战锤攻击并点燃主人最近的敌人"));
        assertTrue(chinese.contains("使附近与当前头型相同的敌人陷入虚弱"));
        assertFalse(english.contains("item.occultism.ritual_dummy.familiar_deer.tooltip="
                + "The Deer familiar provides Speed, Jump Boost, and step assist to its master while nearby. "
                + "Familiar Ring storage is supported; blacksmith hammer upgrades are not implemented."));
        assertFalse(chinese.contains("item.occultism.ritual_dummy.familiar_deer.tooltip="
                + "小鹿使魔在主人附近时提供速度、跳跃提升和台阶辅助。支持使魔之戒储存；不实现铁匠战锤升级。"));
        assertFalse(chinese.contains("item.occultism.ritual_dummy.familiar_headless.tooltip="
                + "无头鼠骑士使魔会偷取附近被杀敌人的头颅，并提高主人对同类敌人的伤害。"
                + "支持使魔之戒储存；不实现铁匠升级或 Curios 戒指效果。"));
    }

    private static String section(String source, String start, String end) {
        int startIndex = source.indexOf(start);
        assertTrue(startIndex >= 0, "Missing source marker: " + start);
        int endIndex = source.indexOf(end, startIndex + start.length());
        assertTrue(endIndex >= 0, "Missing source marker after " + start + ": " + end);
        return source.substring(startIndex, endIndex);
    }

    private static int countOccurrences(String source, String token) {
        int count = 0;
        int index = 0;
        while ((index = source.indexOf(token, index)) >= 0) {
            count++;
            index += token.length();
        }
        return count;
    }

    private static String sha256(Path path) throws IOException, NoSuchAlgorithmException {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path));
        StringBuilder hex = new StringBuilder(digest.length * 2);
        for (byte value : digest) {
            hex.append(String.format("%02x", value & 0xff));
        }
        return hex.toString();
    }

    private static String readUtf8(Path path) throws IOException {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}
