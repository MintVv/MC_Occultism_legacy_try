package com.occultism.occultism.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.occultism.occultism.entity.EntityBatFamiliar;
import com.occultism.occultism.entity.EntityBeaverFamiliar;
import com.occultism.occultism.entity.EntityBeholderFamiliar;
import com.occultism.occultism.entity.EntityBlacksmithFamiliar;
import com.occultism.occultism.entity.EntityBoundSpirit;
import com.occultism.occultism.entity.EntityChimeraFamiliar;
import com.occultism.occultism.entity.EntityCthulhuFamiliar;
import com.occultism.occultism.entity.EntityDeerFamiliar;
import com.occultism.occultism.entity.EntityDemonicHusband;
import com.occultism.occultism.entity.EntityDevilFamiliar;
import com.occultism.occultism.entity.EntityDragonFamiliar;
import com.occultism.occultism.entity.EntityFairyFamiliar;
import com.occultism.occultism.entity.EntityFamiliarBase;
import com.occultism.occultism.entity.EntityFoliotJob;
import com.occultism.occultism.entity.EntityGoatFamiliar;
import com.occultism.occultism.entity.EntityGreedyFamiliar;
import com.occultism.occultism.entity.EntityGuardianFamiliar;
import com.occultism.occultism.entity.EntityHeadlessFamiliar;
import com.occultism.occultism.entity.EntityMummyFamiliar;
import com.occultism.occultism.entity.EntityShubNiggurathFamiliar;
import com.occultism.occultism.entity.EntityShubNiggurathSpawn;
import com.occultism.occultism.entity.IFamiliar;
import com.occultism.occultism.item.ItemSoulShard;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityParrot;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class FamiliarSoulShardPortContractTest {

    private static final Path ROOT = Paths.get("src", "main");
    private static final Path JAVA_ROOT = ROOT.resolve("java").resolve("com").resolve("occultism")
            .resolve("occultism");
    private static final Path ASSET_ROOT = ROOT.resolve("resources").resolve("assets").resolve("occultism");

    @BeforeAll
    static void bootstrapMinecraft() {
        Bootstrap.register();
    }

    @Test
    void allSixteenPortedFamiliarsUseTheSharedDeathShardPath() {
        List<Class<? extends EntityLivingBase>> familiars = Arrays.asList(
                EntityBatFamiliar.class,
                EntityBeaverFamiliar.class,
                EntityBeholderFamiliar.class,
                EntityBlacksmithFamiliar.class,
                EntityChimeraFamiliar.class,
                EntityCthulhuFamiliar.class,
                EntityDeerFamiliar.class,
                EntityDevilFamiliar.class,
                EntityDragonFamiliar.class,
                EntityFairyFamiliar.class,
                EntityGoatFamiliar.class,
                EntityGreedyFamiliar.class,
                EntityGuardianFamiliar.class,
                EntityHeadlessFamiliar.class,
                EntityMummyFamiliar.class,
                EntityShubNiggurathFamiliar.class);

        assertEquals(16, familiars.size());
        for (Class<? extends EntityLivingBase> familiar : familiars) {
            assertTrue(EntityFamiliarBase.class.isAssignableFrom(familiar), familiar.getSimpleName());
            assertTrue(IFamiliar.class.isAssignableFrom(familiar), familiar.getSimpleName());
        }

        assertFalse(EntityFamiliarBase.class.isAssignableFrom(EntityDemonicHusband.class));
        assertFalse(IFamiliar.class.isAssignableFrom(EntityDemonicHusband.class));
        assertFalse(EntityFamiliarBase.class.isAssignableFrom(EntityParrot.class));
        assertFalse(EntityFamiliarBase.class.isAssignableFrom(EntityShubNiggurathSpawn.class));
        assertFalse(IFamiliar.class.isAssignableFrom(EntityFoliotJob.class));
        assertFalse(IFamiliar.class.isAssignableFrom(EntityBoundSpirit.class));
    }

    @Test
    void allSixteenFamiliarsExposeTheUpstreamCommonCombatAttributes() throws IOException {
        List<EntityLivingBase> familiars = Arrays.asList(
                new EntityBatFamiliar(null), new EntityBeaverFamiliar(null), new EntityBeholderFamiliar(null),
                new EntityBlacksmithFamiliar(null), new EntityChimeraFamiliar(null),
                new EntityCthulhuFamiliar(null), new EntityDeerFamiliar(null), new EntityDevilFamiliar(null),
                new EntityDragonFamiliar(null), new EntityFairyFamiliar(null), new EntityGoatFamiliar(null),
                new EntityGreedyFamiliar(null), new EntityGuardianFamiliar(null), new EntityHeadlessFamiliar(null),
                new EntityMummyFamiliar(null), new EntityShubNiggurathFamiliar(null));
        assertEquals(16, familiars.size());
        for (EntityLivingBase familiar : familiars) {
            double expectedArmor = familiar instanceof EntityChimeraFamiliar ? 0.0D
                    : familiar instanceof EntityDragonFamiliar ? 25.0D : 15.0D;
            double expectedToughness = familiar instanceof EntityChimeraFamiliar ? 0.0D : 10.0D;
            assertEquals(expectedArmor, familiar.getEntityAttribute(SharedMonsterAttributes.ARMOR).getBaseValue(),
                    0.000001D, familiar.getClass().getSimpleName());
            assertEquals(expectedToughness,
                    familiar.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getBaseValue(),
                    0.000001D, familiar.getClass().getSimpleName());
            double expectedAttack = familiar instanceof EntityChimeraFamiliar ? 4.0D
                    : familiar instanceof EntityMummyFamiliar ? 9.0D : 6.0D;
            assertEquals(expectedAttack,
                    familiar.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getBaseValue(),
                    0.000001D, familiar.getClass().getSimpleName());
        }

        String base = readJava("entity", "EntityFamiliarBase.java");
        assertTrue(base.contains("SharedMonsterAttributes.ARMOR_TOUGHNESS"));
        assertTrue(base.contains("SharedMonsterAttributes.ATTACK_DAMAGE"));
    }

    @Test
    void sharedFamiliarBasePreservesUpstreamWallCollisionImmunity() throws IOException {
        EntityBatFamiliar familiar = new EntityBatFamiliar(null);
        assertTrue(familiar.isEntityInvulnerable(DamageSource.IN_WALL));
        assertTrue(familiar.isEntityInvulnerable(DamageSource.FLY_INTO_WALL));
        assertFalse(familiar.isEntityInvulnerable(DamageSource.GENERIC));

        String base = readJava("entity", "EntityFamiliarBase.java");
        assertTrue(base.contains("source == DamageSource.IN_WALL"));
        assertTrue(base.contains("source == DamageSource.FLY_INTO_WALL"));
        assertTrue(base.contains("this.enablePersistence();"));
        assertTrue(base.contains("protected boolean canDespawn()"));
        assertTrue(base.contains("protected boolean canDespawn() {\n        return false;"));
    }

    @Test
    void boundSpiritsAndFoliotJobsPreserveUpstreamWallCollisionImmunity() throws IOException {
        EntityBoundSpirit bound = new EntityBoundSpirit(null, com.occultism.occultism.spirit.SpiritTier.DJINNI);
        EntityFoliotJob job = new EntityFoliotJob(null, com.occultism.occultism.spirit.SpiritJobType.CRUSHER);
        for (EntityLivingBase spirit : Arrays.asList(bound, job)) {
            assertTrue(spirit.isEntityInvulnerable(DamageSource.IN_WALL));
            assertTrue(spirit.isEntityInvulnerable(DamageSource.FLY_INTO_WALL));
            assertFalse(spirit.isEntityInvulnerable(DamageSource.GENERIC));
        }

        assertTrue(readJava("entity", "EntityBoundSpirit.java")
                .contains("source == DamageSource.FLY_INTO_WALL"));
        assertTrue(readJava("entity", "EntityBoundSpirit.java")
                .contains("amount = (amount + 1.0F) / 2.0F"));
        assertTrue(readJava("entity", "EntityFoliotJob.java")
                .contains("source == DamageSource.FLY_INTO_WALL"));
        assertTrue(readJava("entity", "EntityFoliotJob.java")
                .contains("amount = (amount + 1.0F) / 2.0F"));
    }

    @Test
    void sharedDeathPathDeliversOneFilledShardToOwnerOrCorpse() throws IOException {
        String base = readJava("entity", "EntityFamiliarBase.java");
        String dropLoot = section(base, "protected void dropLoot", "protected boolean storeSoulShard");
        assertTrue(dropLoot.contains("super.dropLoot(wasRecentlyHit, lootingModifier, source);"));
        assertTrue(dropLoot.contains("this.dropSoulShard();"));
        assertEquals(1, countOccurrences(base, "new ItemStack(ModItems.SOUL_SHARD)"));
        assertTrue(base.contains("ItemSoulShard.storeEntity(shard, this)"));
        assertTrue(base.contains("ItemHandlerHelper.giveItemToPlayer((EntityPlayer) owner, shard);"));
        assertTrue(base.contains("item.setPickupDelay(5);"));
        assertTrue(base.contains("item.motionX = 0.0D;"));
        assertTrue(base.contains("item.motionZ = 0.0D;"));

        String handler = readJava("handler", "FamiliarEventHandler.java");
        assertFalse(handler.contains("LivingDropsEvent"),
                "The port uses the entity loot lifecycle so canceled Forge drops cannot discard the shard");
    }

    @Test
    void soulShardStoresFullUpstreamEntityDataAtFullHealth() {
        EntityZombie zombie = new EntityZombie(null);
        zombie.setHealth(3.0F);
        zombie.motionX = 0.25D;
        zombie.motionY = -0.5D;
        zombie.motionZ = 0.75D;
        zombie.setCustomNameTag("Shard Test Zombie");

        ItemStack shard = new ItemStack(Items.PAPER);
        assertTrue(ItemSoulShard.storeEntity(shard, zombie));
        assertEquals(3.0F, zombie.getHealth(), 0.000001F, "Serializing must restore the dying entity state");
        assertTrue(shard.getTagCompound().hasKey("entityData", 10));
        assertFalse(shard.getTagCompound().hasKey("OccultismEntity"));
        assertFalse(shard.getTagCompound().hasKey("EntityId"));
        assertFalse(shard.getTagCompound().hasKey("EntityName"));

        NBTTagCompound entityData = ItemSoulShard.getStoredEntityData(shard);
        assertNotNull(entityData);
        assertEquals("minecraft:zombie", entityData.getString("id"));
        assertEquals(zombie.getMaxHealth(), entityData.getFloat("Health"), 0.000001F);
        assertEquals("Shard Test Zombie", entityData.getString("CustomName"));
        assertTrue(entityData.hasKey("UUIDMost"));
        assertTrue(entityData.hasKey("Pos", 9));
        assertTrue(entityData.hasKey("Motion", 9));
        assertEquals(0.25D, entityData.getTagList("Motion", 6).getDoubleAt(0), 0.000001D);
        assertEquals(-0.5D, entityData.getTagList("Motion", 6).getDoubleAt(1), 0.000001D);
        assertEquals(0.75D, entityData.getTagList("Motion", 6).getDoubleAt(2), 0.000001D);
    }

    @Test
    void canonicalWriterAndLegacyReaderRemainCompatible() {
        NBTTagCompound entityData = new NBTTagCompound();
        entityData.setString("id", "minecraft:pig");
        entityData.setInteger("StateMarker", 42);

        ItemStack canonical = new ItemStack(Items.PAPER);
        ItemSoulShard.setStoredEntity(canonical, entityData, "minecraft:pig", "Ignored Canonical Name");
        assertTrue(ItemSoulShard.hasStoredEntity(canonical));
        assertEquals("minecraft:pig", ItemSoulShard.getStoredEntityId(canonical));
        assertEquals(42, ItemSoulShard.getStoredEntityData(canonical).getInteger("StateMarker"));
        assertFalse(canonical.getTagCompound().hasKey("EntityName"));

        ItemStack legacy = new ItemStack(Items.PAPER);
        NBTTagCompound legacyTag = new NBTTagCompound();
        legacyTag.setTag("OccultismEntity", entityData.copy());
        legacyTag.setString("EntityId", "minecraft:pig");
        legacyTag.setString("EntityName", "Legacy Pig");
        legacy.setTagCompound(legacyTag);
        assertTrue(ItemSoulShard.hasStoredEntity(legacy));
        assertEquals("minecraft:pig", ItemSoulShard.getStoredEntityId(legacy));
        assertEquals("Legacy Pig", ItemSoulShard.getStoredEntityDisplayName(legacy));
        assertEquals(42, ItemSoulShard.getStoredEntityData(legacy).getInteger("StateMarker"));
    }

    @Test
    void codecOnlyRemovesPositionWhenRecreatingAnEntity() throws IOException {
        String shard = readJava("item", "ItemSoulShard.java");
        String create = section(shard, "public static Entity createStoredEntity", "\n    }");
        assertTrue(create.contains("entityData.removeTag(\"Pos\");"));
        assertFalse(create.contains("removeTag(\"Motion\")"));
        assertFalse(create.contains("removeTag(\"Rotation\")"));
        assertFalse(shard.contains("removeTag(\"ActiveEffects\")"));
        assertFalse(shard.contains("setFloat(\"FallDistance\""));
        assertTrue(shard.contains("target.setHealth(target.getMaxHealth());"));
        assertTrue(shard.contains("target.writeToNBT(entityData);"));
        assertFalse(shard.contains("writeToNBTOptional"),
                "Riding Familiars must serialize independently instead of being omitted with their mount");
        assertTrue(shard.contains("finally"));
        assertTrue(shard.contains("target.setHealth(health);"));

        String playerHandler = readJava("handler", "PlayerEventHandler.java");
        String denyList = section(playerHandler, "private static boolean isShatteredSoulDenied", "\n    }");
        assertTrue(denyList.contains("entity instanceof IFamiliar"));
        assertFalse(denyList.contains("EntityDemonicPartner"));
    }

    @Test
    void guardianAndHeadlessApplyTheDocumentedUpstreamDefectFixes() throws IOException {
        String guardian = readJava("entity", "EntityGuardianFamiliar.java");
        String guardianStore = section(guardian, "protected boolean storeSoulShard", "public EntityAgeable createChild");
        assertTrue(guardianStore.contains("this.rand.nextInt(MAX_LIVES) + 1"));
        assertTrue(guardianStore.contains("return super.storeSoulShard(shard);"));
        assertTrue(guardianStore.contains("finally"));
        assertTrue(guardianStore.contains("this.setLives(lives);"));
        assertFalse(guardian.contains("new ItemStack(ModItems.SOUL_SHARD)"),
                "Guardian must customize the one base shard instead of creating a duplicate");
        assertTrue(guardian.contains("this.attackEntityFrom(DamageSource.OUT_OF_WORLD, Float.MAX_VALUE);"));
        assertFalse(guardian.contains("this.setDead();"),
                "The final sacrifice must pass through the death and shard lifecycle");

        String headless = readJava("entity", "EntityHeadlessFamiliar.java");
        assertFalse(headless.contains("droppedSoulShard"));
        assertFalse(headless.contains("OccultismHeadlessDroppedSoulShard"));
        assertFalse(headless.contains("dropSoulShard"));
        assertFalse(headless.contains("this.setHeadlessDead(false);"));
        assertFalse(headless.contains("this.setRebuilt((byte) 0);"));
        assertTrue(headless.contains("protected void damageEntity(DamageSource source, float amount)"));
    }

    @Test
    void headlessDeathAndRebuildStateRoundTripsWithoutShardSanitizing() {
        NBTTagCompound input = new NBTTagCompound();
        input.setString("OwnerUUID", "");
        input.setByte("OccultismHeadlessVariants", (byte) 4);
        input.setByte("OccultismHeadlessHead", (byte) 5);
        input.setInteger("OccultismHeadlessHeadTimer", 321);
        input.setByte("OccultismHeadlessWeapon", (byte) 2);
        input.setByte("OccultismHeadlessRebuilt", (byte) 45);
        input.setBoolean("hasBlacksmithUpgrade", true);

        EntityHeadlessFamiliar headless = new TestHeadlessFamiliar();
        headless.readEntityFromNBT(input);
        assertTrue(headless.isHeadlessDead());

        NBTTagCompound output = new NBTTagCompound();
        headless.writeEntityToNBT(output);
        assertEquals(4, output.getByte("OccultismHeadlessVariants"));
        assertEquals(5, output.getByte("OccultismHeadlessHead"));
        assertEquals(321, output.getInteger("OccultismHeadlessHeadTimer"));
        assertEquals(2, output.getByte("OccultismHeadlessWeapon"));
        assertEquals(45, output.getByte("OccultismHeadlessRebuilt"));
        assertTrue(output.getBoolean("hasBlacksmithUpgrade"));
        assertFalse(output.hasKey("OccultismHeadlessDroppedSoulShard"));
    }

    @Test
    void resurrectedFamiliarsBindToTheCastingPlayerOnly() throws IOException {
        EntityGuardianFamiliar familiar = new EntityGuardianFamiliar(null);
        EntityPig owner = new EntityPig(null);
        familiar.setFamiliarOwner(owner);
        assertTrue(familiar.isTamed());
        assertEquals(owner.getUniqueID(), familiar.getOwnerId());

        String bowl = readJava("tileentity", "TileEntitySacrificialBowl.java");
        String resurrection = section(bowl, "private boolean tryResurrectStoredEntity", "private EntityPlayer findCastingPlayer");
        assertTrue(resurrection.contains("entity instanceof IFamiliar"));
        assertTrue(resurrection.contains("((IFamiliar) entity).setFamiliarOwner(castingPlayer);"));
        assertFalse(resurrection.contains("entity instanceof EntityTameable"));
        assertFalse(resurrection.contains("motionX = 0.0D"));
        assertFalse(resurrection.contains("setHealth(living.getMaxHealth())"));

        String lookup = section(bowl, "private EntityPlayer findCastingPlayer", "private boolean trySpawnRitualEntity");
        assertTrue(lookup.contains("this.world.getPlayerEntityByUUID(this.castingPlayerId)"));
        assertTrue(lookup.contains("getPlayerList().getPlayerByUUID(this.castingPlayerId)"));
        assertFalse(IFamiliar.class.isAssignableFrom(EntityDemonicHusband.class),
                "Demonic Partners retain their stored owner on resurrection");
    }

    @Test
    void resurrectionRecipeDictionaryAndBilingualTextMatchTheFixedSnapshot() throws IOException {
        JsonArray rituals = readJson(ASSET_ROOT.resolve("rituals").resolve("rituals.json")).getAsJsonArray();
        JsonObject ritual = findObject(rituals, "id", "resurrect_familiar");
        assertNotNull(ritual);
        assertEquals("occultism:resurrect_familiar", ritual.get("ritual_type").getAsString());
        assertEquals("occultism:soul_shard", ritual.get("activation_item").getAsString());
        assertEquals("occultism:summon_foliot", ritual.get("pentacle_id").getAsString());
        assertEquals(15, ritual.get("duration").getAsInt());
        JsonArray ingredients = ritual.getAsJsonArray("ingredients");
        assertEquals(4, ingredients.size());
        for (JsonElement ingredient : ingredients) {
            assertEquals("occultism:otherworld_essence", ingredient.getAsString());
        }

        JsonObject dictionary = readJson(ASSET_ROOT.resolve("dictionary").resolve("modonomicon_index.json"))
                .getAsJsonObject();
        JsonObject category = findObject(dictionary.getAsJsonArray("categories"), "id", "familiar_rituals");
        JsonObject resurrection = findObject(category.getAsJsonArray("entries"), "id", "resurrection");
        assertEquals("occultism:pentacles/summon_foliot",
                resurrection.getAsJsonObject("condition").get("entry_id").getAsString());

        String english = readUtf8(ASSET_ROOT.resolve("lang").resolve("en_us.lang"));
        String chinese = readUtf8(ASSET_ROOT.resolve("lang").resolve("zh_cn.lang"));
        assertTrue(english.contains("pentacles.summon_foliot.uses.text=- [Resurrect Familiar]"));
        assertFalse(line(english, "pentacles.resurrect_spirit.uses.text=").contains("Resurrect Familiar"));
        assertTrue(chinese.contains("pentacles.summon_foliot.uses.text=- [复活使魔]"));
        assertFalse(line(chinese, "pentacles.resurrect_spirit.uses.text=").contains("复活使魔"));
        assertTrue(english.contains("binds it to the caster"));
        assertTrue(chinese.contains("重新绑定给施法者"));
    }

    private static String readJava(String folder, String file) throws IOException {
        return readUtf8(JAVA_ROOT.resolve(folder).resolve(file));
    }

    private static String section(String source, String start, String end) {
        int startIndex = source.indexOf(start);
        assertTrue(startIndex >= 0, "Missing marker: " + start);
        int endIndex = source.indexOf(end, startIndex + start.length());
        assertTrue(endIndex >= 0, "Missing marker: " + end);
        return source.substring(startIndex, endIndex);
    }

    private static String line(String source, String marker) {
        int start = source.indexOf(marker);
        assertTrue(start >= 0, "Missing marker: " + marker);
        int end = source.indexOf('\n', start);
        return source.substring(start, end < 0 ? source.length() : end);
    }

    private static int countOccurrences(String source, String token) {
        int count = 0;
        for (int index = 0; (index = source.indexOf(token, index)) >= 0; index += token.length()) {
            count++;
        }
        return count;
    }

    private static JsonObject findObject(JsonArray array, String key, String value) {
        if (array == null) {
            return null;
        }
        for (JsonElement element : array) {
            JsonObject object = element.getAsJsonObject();
            if (object.has(key) && value.equals(object.get(key).getAsString())) {
                return object;
            }
        }
        return null;
    }

    private static JsonElement readJson(Path path) throws IOException {
        return new JsonParser().parse(readUtf8(path));
    }

    private static String readUtf8(Path path) throws IOException {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }

    private static final class TestHeadlessFamiliar extends EntityHeadlessFamiliar {

        private TestHeadlessFamiliar() {
            super(null);
        }

        @Override
        public int getGrowingAge() {
            return 0;
        }
    }

}
