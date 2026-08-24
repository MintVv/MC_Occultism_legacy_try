package com.occultism.occultism.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.reflect.Method;
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
import com.occultism.occultism.entity.EntityWildHordeCreeper;
import com.occultism.occultism.entity.EntityWildHordeDrowned;
import com.occultism.occultism.entity.EntityWildHordeEvoker;
import com.occultism.occultism.entity.EntityWildHordeHusk;
import com.occultism.occultism.entity.EntityWildHordeParched;
import com.occultism.occultism.entity.EntityWildHordeSilverfish;

import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEvoker;
import net.minecraft.entity.monster.EntityHusk;
import net.minecraft.entity.monster.EntitySilverfish;
import net.minecraft.entity.monster.EntityStray;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.init.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Contract coverage for the registered 1.12 wild horde compatibility entities. */
class WildHordePortContractTest {

    private static final Path ROOT = Paths.get("src", "main");
    private static final Path JAVA_ROOT = ROOT.resolve("java").resolve("com").resolve("occultism")
            .resolve("occultism");
    private static final Path ASSET_ROOT = ROOT.resolve("resources").resolve("assets").resolve("occultism");
    private static final List<String> ENTITY_IDS = Arrays.asList(
            "wild_horde_illager", "wild_horde_husk", "wild_horde_parched", "wild_horde_drowned",
            "wild_horde_creeper", "wild_horde_silverfish");

    @BeforeAll
    static void bootstrapMinecraft() {
        Bootstrap.register();
    }

    @Test
    void entitiesAppendAfterTheExistingRegistryAndReuseMatchingVanillaRenderers() throws IOException {
        String entities = readJava("registry", "ModEntities.java");
        int previous = entities.indexOf("\"possessed_zombie_piglin\"");
        for (String id : ENTITY_IDS) {
            int current = entities.indexOf("\"" + id + "\"");
            assertTrue(current > previous, id + " must append without changing existing entity IDs");
            assertEquals(1, countOccurrences(entities,
                    "new ResourceLocation(Tags.MOD_ID, \"" + id + "\")"));
            previous = current;
        }
        assertEquals(59, countOccurrences(entities, "nextEntityId++"));
        assertTrue(entities.contains("EntityWildHordeEvoker.class, \"wild_horde_illager\""));
        assertTrue(entities.contains("EntityWildHordeHusk.class, \"wild_horde_husk\""));
        assertTrue(entities.contains("EntityWildHordeParched.class, \"wild_horde_parched\""));
        assertTrue(entities.contains("EntityWildHordeDrowned.class, \"wild_horde_drowned\""));
        assertTrue(entities.contains("EntityWildHordeCreeper.class, \"wild_horde_creeper\""));
        assertTrue(entities.contains("EntityWildHordeSilverfish.class, \"wild_horde_silverfish\""));

        String renderers = readJava("client/render", "ModEntityRenderers.java");
        assertRenderer(renderers, "EntityWildHordeEvoker", "RenderEvoker");
        assertRenderer(renderers, "EntityWildHordeHusk", "RenderHusk");
        assertRenderer(renderers, "EntityWildHordeParched", "RenderSkeleton");
        assertRenderer(renderers, "EntityWildHordeDrowned", "RenderZombie");
        assertRenderer(renderers, "EntityWildHordeCreeper", "RenderCreeper");
        assertRenderer(renderers, "EntityWildHordeSilverfish", "RenderSilverfish");
    }

    @Test
    void entitiesUseTheCorrect112InheritanceAndSunlightRules() throws Exception {
        assertTrue(new EntityWildHordeEvoker(null) instanceof EntityEvoker);
        assertTrue(new EntityWildHordeHusk(null) instanceof EntityHusk);
        assertTrue(new EntityWildHordeParched(null) instanceof EntityStray);
        EntityWildHordeDrowned drowned = new EntityWildHordeDrowned(null);
        assertTrue(drowned instanceof EntityZombie);
        assertFalse(invokeShouldBurnInDay(drowned));
        assertTrue(new EntityWildHordeCreeper(null) instanceof EntityCreeper);
        assertTrue(new EntityWildHordeSilverfish(null) instanceof EntitySilverfish);

        String creeper = readJava("entity", "EntityWildHordeCreeper.java");
        assertTrue(creeper.contains("public IEntityLivingData onInitialSpawn"));
        assertTrue(creeper.contains("onStruckByLightning"));
        assertTrue(creeper.contains("new EntityLightningBolt"));
    }

    @Test
    void wildHordeRitualsUseCanonicalRegisteredEntityIdsAndDictionaryPages() throws IOException {
        JsonArray rituals = readJson(ASSET_ROOT.resolve("rituals").resolve("rituals.json")).getAsJsonArray();
        assertEquals("occultism:wild_horde_creeper", summonId(rituals, "wild_creeper"));
        assertEquals("occultism:wild_horde_illager", summonId(rituals, "wild_horde_illager"));
        assertEquals("occultism:wild_horde_husk", summonId(rituals, "wild_husk"));
        assertEquals("occultism:wild_horde_parched", summonId(rituals, "wild_parched"));
        assertEquals("occultism:wild_horde_drowned", summonId(rituals, "wild_drowned"));
        assertEquals("occultism:wild_horde_silverfish", summonId(rituals, "wild_silverfish"));

        String dictionary = readJava("client/gui", "GuiDictionaryOfSpirits.java");
        for (String id : ENTITY_IDS) {
            assertFalse(dictionary.contains("case \"occultism:" + id + "\""),
                    id + " must resolve through EntityList after registration");
        }

        String english = readUtf8(ASSET_ROOT.resolve("lang").resolve("en_us.lang"));
        String chinese = readUtf8(ASSET_ROOT.resolve("lang").resolve("zh_cn.lang"));
        assertTrue(english.contains("registered Wild Horde"));
        assertTrue(chinese.contains("已注册"));
        assertTrue(english.contains("compatibility entities instead of bare vanilla IDs"));
        assertTrue(chinese.contains("不再只使用原版实体 ID"));
    }

    private static boolean invokeShouldBurnInDay(EntityWildHordeDrowned drowned) throws Exception {
        Method method = EntityWildHordeDrowned.class.getDeclaredMethod("shouldBurnInDay");
        method.setAccessible(true);
        return (Boolean) method.invoke(drowned);
    }

    private static void assertRenderer(String source, String entityClass, String rendererClass) {
        String marker = "registerEntityRenderingHandler(" + entityClass + ".class";
        int start = source.indexOf(marker);
        assertTrue(start >= 0, entityClass + " renderer registration is missing");
        int end = source.indexOf("});", start);
        assertTrue(end > start, entityClass + " renderer factory is incomplete");
        assertTrue(source.substring(start, end).contains("new " + rendererClass + "(manager)"),
                entityClass + " must use " + rendererClass);
    }

    private static String summonId(JsonArray rituals, String id) {
        for (JsonElement element : rituals) {
            JsonObject ritual = element.getAsJsonObject();
            if (id.equals(ritual.get("id").getAsString())) {
                return ritual.get("entity_to_summon").getAsString();
            }
        }
        throw new AssertionError("Missing ritual: " + id);
    }

    private static JsonElement readJson(Path path) throws IOException {
        return new JsonParser().parse(readUtf8(path));
    }

    private static String readJava(String directory, String fileName) throws IOException {
        return readUtf8(JAVA_ROOT.resolve(directory).resolve(fileName));
    }

    private static String readUtf8(Path path) throws IOException {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }

    private static int countOccurrences(String source, String needle) {
        int count = 0;
        int offset = 0;
        while ((offset = source.indexOf(needle, offset)) >= 0) {
            count++;
            offset += needle.length();
        }
        return count;
    }
}
