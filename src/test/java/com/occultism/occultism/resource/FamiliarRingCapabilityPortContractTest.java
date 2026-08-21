package com.occultism.occultism.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.occultism.occultism.capability.FamiliarRingCapability;
import com.occultism.occultism.item.ItemFamiliarRing;
import com.occultism.occultism.registry.ModItems;

import net.minecraft.init.Bootstrap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class FamiliarRingCapabilityPortContractTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        Bootstrap.register();
    }

    @Test
    void equippedRingCapabilityRoundTripsItemAndStoredEntityNbt() {
        ItemStack ring = new ItemStack(ModItems.FAMILIAR_RING);
        NBTTagCompound entity = new NBTTagCompound();
        entity.setString("id", "occultism:bat_familiar");
        entity.setString("UUID", "00000000-0000-0000-0000-000000000001");
        NBTTagCompound ringTag = new NBTTagCompound();
        ringTag.setTag("StoredFamiliarEntity", entity);
        ringTag.setString("StoredFamiliarEntityId", "occultism:bat_familiar");
        ringTag.setString("StoredFamiliarEntityName", "Bat");
        ring.setTagCompound(ringTag);

        assertTrue(ItemFamiliarRing.hasStoredFamiliar(ring));
        assertEquals("occultism:bat_familiar", ring.getTagCompound().getString("StoredFamiliarEntityId"));

        FamiliarRingCapability capability = new FamiliarRingCapability();
        assertFalse(capability.isEquipped());
        assertTrue(capability.serializeNBT().isEmpty());
        FamiliarRingCapability copied = new FamiliarRingCapability();
        copied.copyFrom(capability);
        assertFalse(copied.isEquipped());
    }

    @Test
    void capabilityRejectsEmptyUnfilledAndNonRingStacksWithoutAnOwner() {
        FamiliarRingCapability capability = new FamiliarRingCapability();
        assertFalse(capability.equip(null, ItemStack.EMPTY));
        assertFalse(capability.equip(null, new ItemStack(ModItems.FAMILIAR_RING)));
        assertTrue(capability.unequip(null).isEmpty());
    }

    @Test
    void ringLifecycleUsesNativeSlotAndServerValidatedUnequipPacket() throws Exception {
        String handler = readSource("src", "main", "java", "com", "occultism", "occultism", "handler",
                "FamiliarRingHandler.java");
        String capability = readSource("src", "main", "java", "com", "occultism", "occultism", "capability",
                "FamiliarRingCapability.java");
        String unequip = readSource("src", "main", "java", "com", "occultism", "occultism", "network",
                "MessageUnequipFamiliarRing.java");
        String networking = readSource("src", "main", "java", "com", "occultism", "occultism", "network",
                "ModNetworking.java");
        String util = readSource("src", "main", "java", "com", "occultism", "occultism", "util",
                "FamiliarUtil.java");

        assertTrue(handler.contains("AttachCapabilitiesEvent<Entity>"));
        assertTrue(handler.contains("PlayerEvent.Clone"));
        assertTrue(handler.contains("RightClickEmpty"));
        assertTrue(handler.contains("!player.world.isRemote"));
        assertTrue(handler.contains("sendToServer(new MessageUnequipFamiliarRing())"));
        assertFalse(handler.contains("event.setCanceled(true)"));
        assertTrue(capability.contains("persistFamiliar()"));
        assertTrue(capability.contains("new PotionEffect(effect)"));
        assertTrue(unequip.contains("player.isSneaking()"));
        assertTrue(unequip.contains("getHeldItem(EnumHand.MAIN_HAND).isEmpty()"));
        assertTrue(unequip.contains("capability.unequip(player)"));
        assertTrue(networking.contains("MessageSyncFamiliarRing"));
        assertTrue(networking.contains("MessageUnequipFamiliarRing"));
        assertTrue(util.contains("getActiveFamiliar"));
    }

    @Test
    void storedEntityCreationDefendsAgainstMissingIdsAndReleaseRestoresBoundName() throws Exception {
        String ring = readSource("src", "main", "java", "com", "occultism", "occultism", "item",
                "ItemFamiliarRing.java");
        assertTrue(ring.contains("if (id.isEmpty())"));
        assertTrue(ring.contains("catch (RuntimeException ignored)"));
        assertTrue(ring.contains("setCustomNameTag(storedName)"));
        assertTrue(ring.contains("updateStoredFamiliarState"));
    }

    @Test
    void familiarTypeLootTagsRemainRetryableAndExposeTypeTooltip() throws Exception {
        ItemStack ring = new ItemStack(ModItems.FAMILIAR_RING);
        ring.setTagCompound(new NBTTagCompound());
        ring.getTagCompound().setString(ItemFamiliarRing.TAG_FAMILIAR_TYPE, "occultism:greedy_familiar");

        assertTrue(ItemFamiliarRing.hasStoredFamiliar(ring));
        assertFalse(ItemFamiliarRing.materializeFamiliarType(ring, null));
        assertTrue(ring.getTagCompound().hasKey(ItemFamiliarRing.TAG_FAMILIAR_TYPE, 8));

        String source = readSource("src", "main", "java", "com", "occultism", "occultism", "item",
                "ItemFamiliarRing.java");
        assertTrue(source.contains("EntityList.createEntityByIDFromName"));
        assertTrue(source.contains("getOrCreateBoundSpiritName"));
        assertTrue(source.contains("tooltip.familiar_type"));
        String english = readSource("src", "main", "resources", "assets", "occultism", "lang", "en_us.lang");
        String chinese = readSource("src", "main", "resources", "assets", "occultism", "lang", "zh_cn.lang");
        assertTrue(english.contains("item.occultism.familiar_ring.tooltip.familiar_type="));
        assertTrue(chinese.contains("item.occultism.familiar_ring.tooltip.familiar_type="));
    }

    @Test
    void clientFoilUsesEffectSettingAndLocalizesStoredEntityType() throws Exception {
        String source = readSource("src", "main", "java", "com", "occultism", "occultism", "item",
                "ItemFamiliarRing.java");
        assertTrue(source.contains("FMLCommonHandler.instance().getSide() != Side.CLIENT"));
        assertTrue(source.contains("hasClientFamiliarEffect(stack)"));
        assertTrue(source.contains("active.isEffectEnabled(player)"));
        assertTrue(source.contains("EntityList.getTranslationName(new ResourceLocation(id))"));
        assertTrue(source.contains("\"entity.\" + translationName + \".name\""));
        assertTrue(source.contains("Keep malformed/unknown IDs visible"));

        String english = readSource("src", "main", "resources", "assets", "occultism", "lang", "en_us.lang");
        String chinese = readSource("src", "main", "resources", "assets", "occultism", "lang", "zh_cn.lang");
        assertTrue(english.contains("item.occultism.familiar_ring.tooltip.transport_only="));
        assertTrue(chinese.contains("item.occultism.familiar_ring.tooltip.transport_only="));
    }

    @Test
    void transportOnlyCargoCannotEnterNativeEquippedSlot() throws Exception {
        String ring = readSource("src", "main", "java", "com", "occultism", "occultism", "item",
                "ItemFamiliarRing.java");
        String capability = readSource("src", "main", "java", "com", "occultism", "occultism", "capability",
                "FamiliarRingCapability.java");
        assertTrue(ring.contains("public static boolean canEquipFamiliar"));
        assertTrue(ring.contains("public static boolean isTransportOnlyFamiliar"));
        assertTrue(ring.contains("if (isTransportOnlyFamiliar(entity))"));
        assertTrue(ring.contains("Non-IFamiliar transport entities deliberately return false"));
        assertTrue(capability.contains("ItemFamiliarRing.canEquipFamiliar(preview)"));
        assertTrue(capability.contains("isEquippedStack"));
    }

    @Test
    void ringUsesTheIfamiliarContractInsteadOfAClosedEntityAllowlist() throws Exception {
        String ring = readSource("src", "main", "java", "com", "occultism", "occultism", "item",
                "ItemFamiliarRing.java");
        assertTrue(ring.contains("return entity instanceof EntityLivingBase && entity instanceof IFamiliar;"));
        assertTrue(ring.contains("EntityLivingBase owner = familiar.getFamiliarOwner();"));
        assertTrue(ring.contains("return owner == null || owner == player;"));
        assertTrue(ring.contains("if (entity instanceof IFamiliar)"));
        assertTrue(ring.contains("((IFamiliar) entity).setFamiliarOwner(player);"));
        assertTrue(ring.contains("new familiars automatically"));
    }

    @Test
    void upgradedBeholderBlindnessImmunityIncludesTheNativeEquippedRing() throws Exception {
        String beholder = readSource("src", "main", "java", "com", "occultism", "occultism", "entity",
                "EntityBeholderFamiliar.java");
        assertTrue(beholder.contains("FamiliarUtil.hasFamiliar(owner, EntityBeholderFamiliar.class"));
        assertTrue(beholder.contains("beholder.hasBlacksmithUpgrade()"));
        assertTrue(beholder.contains("event.setResult(Result.DENY)"));
        assertFalse(beholder.contains("owner.world.getEntitiesWithinAABB(EntityBeholderFamiliar.class"),
                "Blindness immunity must also see an equipped, NBT-backed Beholder instead of only world entities");
    }

    private static String readSource(String... path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path[0], java.util.Arrays.copyOfRange(path, 1, path.length))),
                StandardCharsets.UTF_8);
    }
}
