package com.occultism.occultism.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import com.occultism.occultism.entity.EntityIesniumGolem;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.init.Bootstrap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Independent contract coverage for the 1.12 Iesnium Golem migration. */
class IesniumGolemPortContractTest {

    private static final Path ASSET_ROOT = Paths.get("src", "main", "resources", "assets", "occultism");
    private static final Path JAVA_ROOT = Paths.get("src", "main", "java", "com", "occultism", "occultism");

    @BeforeAll
    static void bootstrapMinecraft() {
        Bootstrap.register();
    }

    @Test
    void ritualKeepsTheMaridRecipeAndSummonsTheDedicatedEntity() throws IOException {
        JsonArray rituals = new JsonParser().parse(readUtf8(ASSET_ROOT.resolve("rituals/rituals.json")))
                .getAsJsonArray();
        JsonObject ritual = findRitual(rituals, "possess_iesnium_golem");

        assertEquals("Possess Iesnium Golem", string(ritual, "display_name"));
        assertEquals("occultism:ritual", string(ritual, "type"));
        assertEquals("occultism:summon", string(ritual, "ritual_type"));
        assertEquals("occultism:book_of_binding_bound_marid", string(ritual, "activation_item"));
        assertEquals(1600, ritual.get("duration_ticks").getAsInt());
        assertEquals("ritual.occultism.sacrifice.iron_golem",
                string(ritual.getAsJsonObject("entity_to_sacrifice"), "display_name"));
        assertEquals("c:iron_golems", string(ritual.getAsJsonObject("entity_to_sacrifice"), "tag"));
        assertEquals("occultism:iesnium_golem", string(ritual, "entity_to_summon"));
        assertEquals("occultism:possess_marid", string(ritual, "pentacle_id"));
        assertEquals("occultism:soul_gem", string(ritual.getAsJsonObject("result"), "id"));
        assertEquals("occultism:ritual_dummy/possess_iesnium_golem",
                string(ritual.getAsJsonObject("ritual_dummy"), "id"));

        List<String> expectedIngredients = Arrays.asList(
                "occultism:iesnium_block", "occultism:iesnium_block", "occultism:iesnium_block",
                "occultism:marid_essence", "occultism:spirit_attuned_crystal", "minecraft:nether_star",
                "occultism:soul_gem");
        JsonArray ingredients = ritual.getAsJsonArray("ingredients");
        assertEquals(expectedIngredients.size(), ingredients.size());
        for (int index = 0; index < expectedIngredients.size(); index++) {
            assertEquals(expectedIngredients.get(index), ingredients.get(index).getAsString());
        }
    }

    @Test
    void registrationAndClientRendererAppendWithoutChangingEarlierEntityIds() throws IOException {
        String entities = readJava("registry", "ModEntities.java");
        int bird = entities.indexOf("\"otherworld_bird\"");
        int golem = entities.indexOf("\"iesnium_golem\"");
        assertTrue(bird >= 0);
        assertTrue(golem > bird, "Iesnium Golem must append after otherworld_bird");
        assertEquals(1, countOccurrences(entities, "new ResourceLocation(Tags.MOD_ID, \"iesnium_golem\")"));
        assertTrue(entities.contains("EntityIesniumGolem.class, \"iesnium_golem\", nextEntityId++"));
        assertEquals(59, countOccurrences(entities, "nextEntityId++"));

        String renderers = readJava("client/render", "ModEntityRenderers.java");
        assertTrue(renderers.contains("registerEntityRenderingHandler(EntityIesniumGolem.class"));
        assertTrue(renderers.contains("return new RenderIesniumGolem(manager);"));

        String renderer = readJava("client/render", "RenderIesniumGolem.java");
        assertTrue(renderer.contains("extends RenderIronGolem"));
        assertTrue(renderer.contains("textures/entity/iron_golem.png"));
    }

    @Test
    void entityUsesTheUpstreamAttributesPersistenceAndNoLootContract() throws IOException {
        EntityIesniumGolem golem = new EntityIesniumGolem(null);
        assertTrue(golem instanceof EntityIronGolem);
        assertEquals(1.4F, golem.width, 0.000001F);
        assertEquals(2.7F, golem.height, 0.000001F);
        assertEquals(1.0D, golem.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getBaseValue(),
                0.000001D);
        assertEquals(0.5D, golem.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getBaseValue(),
                0.000001D);
        assertEquals(100.0D, golem.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getBaseValue(),
                0.000001D);
        assertEquals(64.0D, golem.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).getBaseValue(),
                0.000001D);
        assertEquals(1.0D,
                golem.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).getBaseValue(),
                0.000001D);
        assertTrue(golem.isPlayerCreated());

        NBTTagCompound saved = new NBTTagCompound();
        golem.writeEntityToNBT(saved);
        assertTrue(saved.getBoolean("PlayerCreated"));
        assertTrue(saved.getBoolean("PersistenceRequired"));

        String source = readJava("entity", "EntityIesniumGolem.java");
        assertTrue(source.contains("this.enablePersistence();"));
        assertTrue(source.contains("protected ResourceLocation getLootTable()"));
        assertTrue(source.contains("return null;"));
        assertTrue(source.contains("protected void dropFewItems"));
    }

    @Test
    void onlySneakingDirectPlayerDamageCanDisbandAndVoidDamageReturnsToSpawn() throws IOException {
        EntityIesniumGolem golem = new EntityIesniumGolem(null);
        EntityZombie zombie = new EntityZombie(null);

        assertTrue(golem.isEntityInvulnerable(DamageSource.GENERIC));
        assertTrue(golem.isEntityInvulnerable(DamageSource.OUT_OF_WORLD));
        assertTrue(golem.isEntityInvulnerable(new EntityDamageSource("player", zombie)));
        assertTrue(golem.isEntityInvulnerable(new EntityDamageSourceIndirect("player", zombie, zombie)));
        assertFalse(EntityIesniumGolem.isValidDisbandAttack(null));
        assertFalse(EntityIesniumGolem.isValidDisbandAttack(DamageSource.GENERIC));

        String source = readJava("entity", "EntityIesniumGolem.java");
        assertTrue(source.contains("source instanceof EntityDamageSource"));
        assertTrue(source.contains("source instanceof net.minecraft.util.EntityDamageSourceIndirect"));
        assertTrue(source.contains("\"player\".equals(source.getDamageType())"));
        assertTrue(source.contains("source.getImmediateSource() == attacker"));
        assertTrue(source.contains("((EntityPlayer) attacker).isSneaking()"));
        assertTrue(source.contains("protected void outOfWorld()"));
        assertTrue(source.contains("this.world.getTopSolidOrLiquidBlock(this.world.getSpawnPoint())"));
        assertTrue(source.contains("this.motionX = 0.0D"));
    }

    @Test
    void soulGemAndFamiliarRingRejectTheDedicatedGolemWhileDisbandingUsesCanonicalStorage() throws IOException {
        String soulGem = readJava("item", "ItemSoulGem.java");
        assertTrue(soulGem.contains("target instanceof EntityIesniumGolem"));
        assertTrue(soulGem.contains("public static boolean storeEntity(ItemStack stack, EntityLivingBase target)"));
        assertTrue(soulGem.contains("target.setHealth(target.getMaxHealth())"));
        assertTrue(soulGem.contains("finally"));
        assertTrue(soulGem.contains("entityData.removeTag(\"Pos\")"));
        assertTrue(soulGem.contains("setStoredEntity(stack, entityData"));

        String ring = readJava("item", "ItemFamiliarRing.java");
        assertTrue(ring.contains("if (entity instanceof EntityIesniumGolem)"));
        assertTrue(ring.contains("return false;"));

        String golem = readJava("entity", "EntityIesniumGolem.java");
        assertTrue(golem.contains("new ItemStack(ModItems.FRAGILE_SOUL_GEM)"));
        assertTrue(golem.contains("ItemSoulGem.storeEntity(gem, this)"));
        assertTrue(golem.contains("player.addItemStackToInventory(gem)"));
        assertTrue(golem.contains("new net.minecraft.entity.item.EntityItem"));
        assertTrue(golem.contains("entity.occultism.iesnium_golem.disbanded"));
    }

    @Test
    void dictionaryAndBilingualTextDescribeTheDedicatedEntityWithoutTheOldPlaceholder() throws IOException {
        String english = readUtf8(ASSET_ROOT.resolve("lang/en_us.lang"));
        String chinese = readUtf8(ASSET_ROOT.resolve("lang/zh_cn.lang"));
        assertTrue(english.contains("entity.occultism.iesnium_golem.name="));
        assertTrue(chinese.contains("entity.occultism.iesnium_golem.name="));
        assertTrue(english.contains("entity.occultism.iesnium_golem.disbanded="));
        assertTrue(chinese.contains("entity.occultism.iesnium_golem.disbanded="));

        String englishTooltip = languageValue(english,
                "item.occultism.ritual_dummy.possess_iesnium_golem.tooltip");
        String chineseTooltip = languageValue(chinese,
                "item.occultism.ritual_dummy.possess_iesnium_golem.tooltip");
        assertTrue(englishTooltip.contains("registered Iesnium Golem"));
        assertTrue(chineseTooltip.contains("已注册的艾瑟金属傀儡"));
        assertFalse(englishTooltip.contains("local substitute"));
        assertFalse(englishTooltip.contains("does not have the upstream"));
        assertFalse(chineseTooltip.contains("原版铁傀儡"));
        assertFalse(chineseTooltip.contains("尚未移植"));

        String dictionary = readUtf8(ASSET_ROOT.resolve("dictionary/modonomicon_index.json"));
        assertTrue(dictionary.contains("entity_id\": \"occultism:iesnium_golem\""));
        assertTrue(dictionary.contains("ritual/possess_iesnium_golem"));
        String gui = readJava("client/gui", "GuiDictionaryOfSpirits.java");
        assertTrue(gui.contains("case \"occultism:iesnium_golem\":"));
        assertTrue(gui.contains("return \"occultism:iesnium_golem\";"));
    }

    private static JsonObject findRitual(JsonArray rituals, String id) {
        for (JsonElement element : rituals) {
            JsonObject ritual = element.getAsJsonObject();
            if (id.equals(string(ritual, "id"))) {
                return ritual;
            }
        }
        throw new AssertionError("Missing ritual definition: " + id);
    }

    private static String languageValue(String language, String key) {
        String prefix = key + "=";
        for (String line : language.split("\\r?\\n")) {
            if (line.startsWith(prefix)) {
                return line.substring(prefix.length());
            }
        }
        throw new AssertionError("Missing language key: " + key);
    }

    private static String string(JsonObject object, String key) {
        return object.get(key).getAsString();
    }

    private static int countOccurrences(String source, String token) {
        int count = 0;
        for (int index = 0; (index = source.indexOf(token, index)) >= 0; index += token.length()) {
            count++;
        }
        return count;
    }

    private static String readJava(String directory, String file) throws IOException {
        return readUtf8(JAVA_ROOT.resolve(directory).resolve(file));
    }

    private static String readUtf8(Path path) throws IOException {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}
