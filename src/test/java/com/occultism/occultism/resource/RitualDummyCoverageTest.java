package com.occultism.occultism.resource;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

/**
 * Every ritual dummy the mod registers must be reachable from some ritual.
 *
 * <p>A dummy that no ritual references is invisible content: it is not in the creative tab (the
 * legacy factory deliberately omits it), nothing crafts it and no ritual shows it. One such
 * leftover existed for the renamed wild hunt ritual and only surfaced through a manual
 * reachability audit, so it is pinned here.</p>
 */
class RitualDummyCoverageTest {

    private static final Path MOD_ITEMS = Paths.get("src/main/java/com/occultism/occultism/registry/ModItems.java");
    private static final String RITUAL_RESOURCE = "/assets/occultism/rituals/rituals.json";

    /**
     * The one documented exception: the id used before {@code summon_wild_hunt} was renamed. See
     * the field javadoc in ModItems for why it is kept instead of deleted.
     */
    private static final Set<String> KNOWN_UNREFERENCED = new HashSet<>(
            java.util.Collections.singletonList("ritual_dummy/wild_hunt"));

    @Test
    void everyRegisteredRitualDummyIsUsedByARitual() throws Exception {
        Set<String> declared = new LinkedHashSet<>();
        Matcher matcher = Pattern.compile("\"(ritual_dummy/[a-z_/0-9]+)\"").matcher(read(MOD_ITEMS));
        while (matcher.find()) {
            declared.add(matcher.group(1));
        }
        assertTrue(declared.size() > 100, "Expected to find the ritual dummy family, got " + declared.size());

        Set<String> referenced = new HashSet<>();
        for (JsonElement element : new JsonParser().parse(readBundle()).getAsJsonArray()) {
            JsonObject ritual = element.getAsJsonObject();
            for (String field : new String[] {"ritual_dummy", "result"}) {
                if (!ritual.has(field) || !ritual.get(field).isJsonObject()) {
                    continue;
                }
                JsonObject stack = ritual.getAsJsonObject(field);
                if (stack.has("id") && stack.get("id").getAsString().contains("ritual_dummy/")) {
                    referenced.add(stack.get("id").getAsString().replace("occultism:", ""));
                }
            }
        }

        Set<String> orphans = new LinkedHashSet<>(declared);
        orphans.removeAll(referenced);
        orphans.removeAll(KNOWN_UNREFERENCED);

        assertTrue(orphans.isEmpty(),
                "Ritual dummies no ritual references (invisible content): " + orphans);
    }

    private static String read(Path path) throws IOException {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }

    private static InputStreamReader readBundle() {
        InputStream stream = RitualDummyCoverageTest.class.getResourceAsStream(RITUAL_RESOURCE);
        assertTrue(stream != null, "Missing ritual resource " + RITUAL_RESOURCE);
        return new InputStreamReader(stream, StandardCharsets.UTF_8);
    }
}
