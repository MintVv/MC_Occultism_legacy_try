package com.occultism.occultism.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.lang.reflect.Method;
import java.util.UUID;

import com.occultism.occultism.entity.EntityDemonicHusband;
import com.occultism.occultism.entity.EntityDemonicWife;
import com.occultism.occultism.entity.EntityDevilFamiliar;
import com.occultism.occultism.entity.EntityGuardianFamiliar;
import com.occultism.occultism.entity.EntityHeadlessFamiliar;
import com.occultism.occultism.entity.EntityMummyFamiliar;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.init.Bootstrap;
import org.junit.jupiter.api.Test;

class FamiliarUpgradePortContractTest {

    private static final UUID MUMMY_UPGRADE_ID =
            UUID.fromString("6aa62086-7009-402b-9c13-c2de74bf077d");
    private static final Path ROOT = Paths.get("src", "main");

    @Test
    void guardianUpgradeRestoresExactlyOneLife() {
        Bootstrap.register();

        EntityGuardianFamiliar guardian = guardianWithLives((byte) 3);
        assertFalse(guardian.hasBlacksmithUpgrade());
        assertTrue(guardian.canBlacksmithUpgrade());

        guardian.blacksmithUpgrade();
        assertEquals(4, guardian.getLives());
        assertTrue(guardian.hasBlacksmithUpgrade());
        assertFalse(guardian.canBlacksmithUpgrade());

        guardian.blacksmithUpgrade();
        assertEquals(4, guardian.getLives(), "The one-time upgrade must not restore a second life");

    }

    @Test
    void fullLifeGuardianRejectsTheUpgradeWithoutConsumingItsOneUse() {
        Bootstrap.register();

        EntityGuardianFamiliar guardian = guardianWithLives(EntityGuardianFamiliar.MAX_LIVES);
        assertFalse(guardian.canBlacksmithUpgrade());
        guardian.blacksmithUpgrade();
        assertEquals(EntityGuardianFamiliar.MAX_LIVES, guardian.getLives());
        assertFalse(guardian.hasBlacksmithUpgrade());
    }

    @Test
    void mummyUpgradeAddsOnePersistentThreeDamageModifier() {
        Bootstrap.register();

        EntityMummyFamiliar mummy = new EntityMummyFamiliar(null);
        IAttributeInstance damage = mummy.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
        assertNotNull(damage);
        assertEquals(9.0D, damage.getBaseValue(), 0.000001D);
        assertEquals(9.0D, damage.getAttributeValue(), 0.000001D);
        assertNull(damage.getModifier(MUMMY_UPGRADE_ID));

        mummy.blacksmithUpgrade();
        assertTrue(mummy.hasBlacksmithUpgrade());
        assertFalse(mummy.canBlacksmithUpgrade());
        assertMummyUpgrade(damage);

        mummy.blacksmithUpgrade();
        assertMummyUpgrade(damage);

        EntityMummyFamiliar loaded = new EntityMummyFamiliar(null);
        setBlacksmithUpgrade(loaded, true);
        setBlacksmithUpgrade(loaded, true);
        assertTrue(loaded.hasBlacksmithUpgrade());
        assertFalse(loaded.canBlacksmithUpgrade());
        IAttributeInstance loadedDamage = loaded.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
        assertMummyUpgrade(loadedDamage);
        assertEquals(1L, loadedDamage.getModifiers().stream()
                .filter(modifier -> MUMMY_UPGRADE_ID.equals(modifier.getID())).count(),
                "Repeated state restoration must retain exactly one upgrade modifier");
    }

    @Test
    void allTameableCombatantsRegisterTheirAttackDamageAttribute() {
        Bootstrap.register();

        assertAttackDamage(new EntityMummyFamiliar(null), 9.0D);
        assertAttackDamage(new EntityDevilFamiliar(null), 6.0D);
        assertAttackDamage(new EntityHeadlessFamiliar(null), 6.0D);
        assertAttackDamage(new EntityDemonicWife(null), 9.0D);
        assertAttackDamage(new EntityDemonicHusband(null), 9.0D);
    }

    @Test
    void mummyModelAndRitualTooltipsExposeTheMigratedUpgrades() throws IOException {
        String guardianEntity = readUtf8(ROOT.resolve("java").resolve("com").resolve("occultism")
                .resolve("occultism").resolve("entity").resolve("EntityGuardianFamiliar.java"));
        String mummyEntity = readUtf8(ROOT.resolve("java").resolve("com").resolve("occultism")
                .resolve("occultism").resolve("entity").resolve("EntityMummyFamiliar.java"));
        assertTrue(guardianEntity.contains("compound.setBoolean(\"hasBlacksmithUpgrade\","
                + " this.hasBlacksmithUpgrade());"));
        assertTrue(guardianEntity.contains("this.setBlacksmithUpgrade("
                + "compound.getBoolean(\"hasBlacksmithUpgrade\"));"));
        assertTrue(mummyEntity.contains("compound.setBoolean(\"hasBlacksmithUpgrade\","
                + " this.hasBlacksmithUpgrade());"));
        assertTrue(mummyEntity.contains("this.setBlacksmithUpgrade("
                + "compound.getBoolean(\"hasBlacksmithUpgrade\"));"));

        String model = readUtf8(ROOT.resolve("java").resolve("com").resolve("occultism").resolve("occultism")
                .resolve("client").resolve("model").resolve("ModelMummyFamiliar.java"));
        assertTrue(model.contains("this.leftGlove.showModel = mummy.hasBlacksmithUpgrade();"));
        assertTrue(model.contains("this.rightGlove.showModel = mummy.hasBlacksmithUpgrade();"));

        String english = readUtf8(ROOT.resolve("resources").resolve("assets").resolve("occultism")
                .resolve("lang").resolve("en_us.lang"));
        String chinese = readUtf8(ROOT.resolve("resources").resolve("assets").resolve("occultism")
                .resolve("lang").resolve("zh_cn.lang"));
        assertTrue(english.contains("A Blacksmith can upgrade it once for +3 attack damage."));
        assertTrue(english.contains("a Blacksmith can upgrade it once to restore one life."));
        assertTrue(chinese.contains("使攻击伤害增加 3 点"));
        assertTrue(chinese.contains("升级一次并恢复一条生命"));
    }

    private static EntityGuardianFamiliar guardianWithLives(byte lives) {
        EntityGuardianFamiliar guardian = new EntityGuardianFamiliar(null);
        try {
            Method setter = EntityGuardianFamiliar.class.getDeclaredMethod("setLives", byte.class);
            setter.setAccessible(true);
            setter.invoke(guardian, Byte.valueOf(lives));
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("Unable to arrange Guardian lives for the contract test", exception);
        }
        return guardian;
    }

    private static void setBlacksmithUpgrade(EntityMummyFamiliar mummy, boolean value) {
        try {
            Method setter = EntityMummyFamiliar.class.getDeclaredMethod("setBlacksmithUpgrade", boolean.class);
            setter.setAccessible(true);
            setter.invoke(mummy, Boolean.valueOf(value));
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("Unable to simulate the Mummy NBT upgrade restore", exception);
        }
    }

    private static void assertMummyUpgrade(IAttributeInstance damage) {
        AttributeModifier modifier = damage.getModifier(MUMMY_UPGRADE_ID);
        assertNotNull(modifier);
        assertEquals(3.0D, modifier.getAmount(), 0.000001D);
        assertEquals(0, modifier.getOperation());
        assertEquals(12.0D, damage.getAttributeValue(), 0.000001D);
    }

    private static void assertAttackDamage(EntityLivingBase entity, double expectedBaseValue) {
        IAttributeInstance damage = entity.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
        assertNotNull(damage, entity.getClass().getSimpleName() + " must register generic.attackDamage");
        assertEquals(expectedBaseValue, damage.getBaseValue(), 0.000001D);
    }

    private static String readUtf8(Path path) throws IOException {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}
