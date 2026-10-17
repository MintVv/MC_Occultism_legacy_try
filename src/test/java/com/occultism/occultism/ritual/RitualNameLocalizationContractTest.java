package com.occultism.occultism.ritual;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import org.junit.jupiter.api.Test;

/**
 * Locks in the ritual name localization contract.
 *
 * <p>The 1.12 port keeps the upstream four-key per-ritual protocol while retaining
 * extra local status messages for running, item use, sacrifices, and debug output.</p>
 */
class RitualNameLocalizationContractTest {

    private static final String RITUAL_RESOURCE = "/assets/occultism/rituals/rituals.json";
    private static final Path LANG_ROOT = Paths.get("src", "main", "resources", "assets", "occultism", "lang");
    private static final String ZH_SENTENCE_PREFIX = "仪式：";

    @Test
    void everyRitualHasANameKeyInBothLanguages() throws IOException {
        Map<String, String> english = readLang("en_us.lang");
        Map<String, String> chinese = readLang("zh_cn.lang");

        int checked = 0;
        for (JsonElement element : loadRituals()) {
            String id = element.getAsJsonObject().get("id").getAsString();
            String key = "ritual.occultism." + id + ".name";
            assertTrue(english.containsKey(key), "Missing English ritual name key " + key);
            assertTrue(chinese.containsKey(key), "Missing Chinese ritual name key " + key);
            checked++;
        }
        assertEquals(170, checked, "Unexpected ritual count; update the localization contract deliberately");
    }

    @Test
    void englishNameKeysMatchTheRitualDisplayName() throws IOException {
        Map<String, String> english = readLang("en_us.lang");
        for (JsonElement element : loadRituals()) {
            JsonObject ritual = element.getAsJsonObject();
            String id = ritual.get("id").getAsString();
            assertEquals(ritual.get("display_name").getAsString(),
                    english.get("ritual.occultism." + id + ".name"),
                    "English ritual name must stay in sync with display_name for " + id);
        }
    }

    @Test
    void chineseNameKeysAreTranslatedWithoutASentencePrefix() throws IOException {
        Map<String, String> chinese = readLang("zh_cn.lang");
        for (JsonElement element : loadRituals()) {
            JsonObject ritual = element.getAsJsonObject();
            String id = ritual.get("id").getAsString();
            String value = chinese.get("ritual.occultism." + id + ".name");
            assertNotNull(value, "Missing Chinese ritual name for " + id);
            assertFalse(value.trim().isEmpty(), "Empty Chinese ritual name for " + id);
            // The generic sentences already carry their own prefix; a name-level prefix would double it.
            assertFalse(value.startsWith(ZH_SENTENCE_PREFIX),
                    "Chinese ritual name must not repeat the sentence prefix for " + id + ": " + value);
        }
    }

    @Test
    void bothLanguageFilesDeclareTheSameKeysWithoutDuplicates() throws IOException {
        List<String> englishKeys = readKeys("en_us.lang");
        List<String> chineseKeys = readKeys("zh_cn.lang");
        assertEquals(englishKeys.size(), new HashSet<>(englishKeys).size(), "Duplicate key in en_us.lang");
        assertEquals(chineseKeys.size(), new HashSet<>(chineseKeys).size(), "Duplicate key in zh_cn.lang");

        Set<String> missingChinese = new HashSet<>(englishKeys);
        missingChinese.removeAll(chineseKeys);
        assertTrue(missingChinese.isEmpty(), "Keys missing from zh_cn.lang: " + missingChinese);

        Set<String> missingEnglish = new HashSet<>(chineseKeys);
        missingEnglish.removeAll(englishKeys);
        assertTrue(missingEnglish.isEmpty(), "Keys missing from en_us.lang: " + missingEnglish);
    }

    @Test
    void preExistingUpstreamStyleSentenceKeysAreRetained() throws IOException {
        Map<String, String> english = readLang("en_us.lang");
        Map<String, String> chinese = readLang("zh_cn.lang");
        for (JsonElement element : loadRituals()) {
            String id = element.getAsJsonObject().get("id").getAsString();
            for (String suffix : new String[] { "started", "interrupted", "finished", "conditions" }) {
                String key = "ritual.occultism." + id + "." + suffix;
                assertTrue(english.containsKey(key), "Dropped upstream-style key " + key + " from en_us.lang");
                assertTrue(chinese.containsKey(key), "Dropped upstream-style key " + key + " from zh_cn.lang");
            }
        }
    }

    @Test
    void ritualDisplayNamesStayEnglishForTheJeiJobHeuristic() throws IOException {
        // RitualRecipeWrapper#getJobLine lowercases display_name and looks for English job keywords such as
        // "crusher" or "smelter". Translating the field itself would silently break JEI job classification.
        String wrapper = readUtf8(Paths.get("src", "main", "java", "com", "occultism", "occultism", "compat", "jei",
                "RitualRecipeWrapper.java"));
        assertTrue(wrapper.contains("this.ritual.getDisplayName().toLowerCase(Locale.ROOT)"),
                "The JEI job heuristic must keep reading the raw English display name");

        for (JsonElement element : loadRituals()) {
            JsonObject ritual = element.getAsJsonObject();
            String displayName = ritual.get("display_name").getAsString();
            for (int index = 0; index < displayName.length(); index++) {
                assertTrue(displayName.charAt(index) < 0x2E80,
                        "Ritual display_name must remain English for the JEI heuristic: "
                                + ritual.get("id").getAsString() + " -> " + displayName);
            }
        }
    }

    @Test
    void definitionsWithAKeyProduceATranslatableNameComponent() {
        RitualDefinition definition = new RitualDefinition("familiar_fairy", "Fairy Familiar",
                "ritual.occultism.familiar_fairy.name", "summon_foliot", "craft",
                new RitualIngredient("dyeRed", 1), null, null, 100, null, null, null, null, null, -1);

        assertEquals("ritual.occultism.familiar_fairy.name", definition.getDisplayNameKey());
        assertTrue(definition.hasDisplayNameKey());
        ITextComponent component = definition.createNameComponent();
        assertTrue(component instanceof TextComponentTranslation,
                "A keyed definition must send a translatable component, not a literal");
        assertEquals("ritual.occultism.familiar_fairy.name", ((TextComponentTranslation) component).getKey());
    }

    @Test
    void definitionsWithoutAKeyFallBackToTheEnglishDisplayName() {
        RitualDefinition definition = new RitualDefinition("scripted_ritual", "Scripted Ritual", "summon_foliot",
                "craft", new RitualIngredient("dyeRed", 1), null, 100);

        assertNull(definition.getDisplayNameKey(), "Legacy constructors must not invent a translation key");
        assertFalse(definition.hasDisplayNameKey());
        ITextComponent component = definition.createNameComponent();
        assertTrue(component instanceof TextComponentString,
                "CraftTweaker and runtime definitions must fall back to the raw display name");
        assertEquals("Scripted Ritual", component.getUnformattedComponentText());
    }

    @Test
    void dynamicDefinitionsHaveTranslatedGenericMessagesForAllFourProtocolStates() throws IOException {
        RitualDefinition definition = new RitualDefinition("scripted_ritual", "Scripted Ritual", "summon_foliot",
                "craft", new RitualIngredient("dyeRed", 1), null, 100);
        ITextComponent[] components = {definition.createStartedComponent(), definition.createInterruptedComponent(),
                definition.createFinishedComponent(), definition.createConditionsComponent()};
        String[] states = {"started", "interrupted", "finished", "conditions"};
        for (int index = 0; index < states.length; index++) {
            TextComponentTranslation component = (TextComponentTranslation) components[index];
            assertEquals("occultism.ritual." + states[index], component.getKey());
            assertEquals("Scripted Ritual", ((ITextComponent) component.getFormatArgs()[0])
                    .getUnformattedComponentText());
            for (String locale : new String[] {"en_us.lang", "zh_cn.lang"}) {
                assertNotNull(readLang(locale).get(component.getKey()), locale + ": " + component.getKey());
            }
        }
    }

    @Test
    void theRitualLoaderDerivesTheNameKeyFromTheRitualId() throws IOException {
        String manager = readUtf8(Paths.get("src", "main", "java", "com", "occultism", "occultism", "ritual",
                "RitualRecipeManager.java"));
        assertTrue(manager.contains("\"ritual.\" + Tags.MOD_ID + \".\" + id + \".name\""),
                "readRitual must derive the ritual name key from the ritual id");
        assertTrue(manager.contains("\"display_name_key\""),
                "readRitual must allow ritual data to override the derived name key");
    }

    @Test
    void chatMessagesPassTheRitualNameAsANestedComponent() throws IOException {
        String bowl = readUtf8(Paths.get("src", "main", "java", "com", "occultism", "occultism", "block",
                "BlockGoldenSacrificialBowl.java"));
        String tileEntity = readUtf8(Paths.get("src", "main", "java", "com", "occultism", "occultism", "tileentity",
                "TileEntitySacrificialBowl.java"));
        String handler = readUtf8(Paths.get("src", "main", "java", "com", "occultism", "occultism", "handler",
                "PlayerEventHandler.java"));

        assertTrue(bowl.contains("ITextComponent ritualName"),
                "The golden bowl must build the ritual name as a component");
        assertFalse(bowl.contains("\"occultism.ritual.started\", ritual.getDisplayName()"),
                "The started message must not pass the English literal");
        assertTrue(bowl.contains("activationMatch.createConditionsComponent()"),
                "The condition failure must use the per-ritual protocol");
        assertTrue(bowl.contains("ritual.createStartedComponent()"),
                "The started message must use the per-ritual protocol");
        assertTrue(tileEntity.contains("definition.createFinishedComponent()"),
                "The completion message must use the per-ritual protocol");
        assertTrue(tileEntity.contains("definition.createInterruptedComponent()"),
                "The interruption message must use the per-ritual protocol");
        assertTrue(handler.contains("ritual.createNameComponent()"),
                "The item use message must pass a translatable name");
        assertFalse(handler.contains("String ritualName"),
                "The item use message must not fall back to a String argument");
    }

    private static Map<String, String> readLang(String fileName) throws IOException {
        Map<String, String> entries = new LinkedHashMap<>();
        for (String line : Files.readAllLines(LANG_ROOT.resolve(fileName), StandardCharsets.UTF_8)) {
            if (line.isEmpty() || line.startsWith("#") || line.indexOf('=') < 0) {
                continue;
            }
            int separator = line.indexOf('=');
            entries.put(line.substring(0, separator), line.substring(separator + 1));
        }
        return entries;
    }

    private static List<String> readKeys(String fileName) throws IOException {
        List<String> keys = new ArrayList<>();
        for (String line : Files.readAllLines(LANG_ROOT.resolve(fileName), StandardCharsets.UTF_8)) {
            if (line.isEmpty() || line.startsWith("#") || line.indexOf('=') < 0) {
                continue;
            }
            keys.add(line.substring(0, line.indexOf('=')));
        }
        return keys;
    }

    private static String readUtf8(Path path) throws IOException {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }

    private static JsonArray loadRituals() throws IOException {
        InputStream stream = RitualNameLocalizationContractTest.class.getResourceAsStream(RITUAL_RESOURCE);
        assertNotNull(stream, "Missing ritual resource " + RITUAL_RESOURCE);
        try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            JsonElement root = new JsonParser().parse(reader);
            assertTrue(root.isJsonArray(), "Ritual resource root must be an array");
            return root.getAsJsonArray();
        }
    }
}
