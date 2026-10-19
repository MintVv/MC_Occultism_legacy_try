package com.occultism.occultism.resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The ritual page must name a summoned spirit, not its registry id.
 *
 * <p>The summon line printed {@code occultism:possessed_warden} in every language. The translations
 * were present all along, so this is a code-side failure rather than a missing locale: the wrapper
 * passed the id straight into the format string. These checks keep the wrapper resolving names and
 * keep the names present for every spirit the rituals summon.</p>
 */
class RitualSummonNameTest {

    private static final Path RITUALS = Paths.get("src/main/resources/assets/occultism/rituals/rituals.json");
    private static final Path LANG = Paths.get("src/main/resources/assets/occultism/lang");
    private static final Path WRAPPER =
            Paths.get("src/main/java/com/occultism/occultism/compat/jei/RitualRecipeWrapper.java");

    private static JsonArray rituals;

    @BeforeAll
    static void load() throws IOException {
        String json = new String(Files.readAllBytes(RITUALS), StandardCharsets.UTF_8);
        JsonElement root = new JsonParser().parse(json);
        rituals = root.isJsonArray() ? root.getAsJsonArray() : root.getAsJsonObject().getAsJsonArray("rituals");
    }

    @Test
    void theWrapperResolvesEntityNames() throws IOException {
        String wrapper = new String(Files.readAllBytes(WRAPPER), StandardCharsets.UTF_8);
        assertTrue(wrapper.contains("displayEntityName("),
                "the summon line must translate the entity id");
        assertTrue(wrapper.contains("I18n.hasKey(ownKey)"),
                "mod entities resolve through entity.<namespace>.<path>.name");
    }

    @Test
    void everySummonedOccultismSpiritHasANameInBothLanguages() throws IOException {
        Set<String> ids = summonedIds();
        for (String locale : new String[] {"en_us.lang", "zh_cn.lang"}) {
            String lang = new String(Files.readAllBytes(LANG.resolve(locale)), StandardCharsets.UTF_8);
            for (String id : ids) {
                if (!id.startsWith("occultism:")) {
                    continue;
                }
                String key = "entity." + id.replace(':', '.') + ".name=";
                assertTrue(lang.contains(key), locale + " is missing " + key);
            }
        }
    }

    private static Set<String> summonedIds() {
        Set<String> ids = new LinkedHashSet<>();
        for (JsonElement element : rituals) {
            JsonElement summon = element.getAsJsonObject().get("entity_to_summon");
            if (summon == null) {
                continue;
            }
            if (summon.isJsonObject()) {
                if (summon.getAsJsonObject().has("id")) {
                    ids.add(summon.getAsJsonObject().get("id").getAsString());
                }
                JsonElement alternatives = summon.getAsJsonObject().get("alternatives");
                if (alternatives != null && alternatives.isJsonArray()) {
                    for (JsonElement alternative : alternatives.getAsJsonArray()) {
                        if (alternative.getAsJsonObject().has("id")) {
                            ids.add(alternative.getAsJsonObject().get("id").getAsString());
                        }
                    }
                }
            } else {
                ids.add(summon.getAsString());
            }
        }
        return ids;
    }
}
