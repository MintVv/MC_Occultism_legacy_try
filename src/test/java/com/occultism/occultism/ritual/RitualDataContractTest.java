package com.occultism.occultism.ritual;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.occultism.occultism.spirit.SpiritJobType;
import com.occultism.occultism.spirit.SpiritTier;
import net.minecraft.util.ResourceLocation;
import org.junit.jupiter.api.Test;

class RitualDataContractTest {

    private static final String RITUAL_RESOURCE = "/assets/occultism/rituals/rituals.json";

    @Test
    void ritualIdsArePresentAndUnique() throws IOException {
        Set<String> ids = new HashSet<>();
        for (JsonElement element : loadRituals()) {
            JsonObject ritual = element.getAsJsonObject();
            assertTrue(ritual.has("id"), "Ritual is missing id: " + ritual);
            String id = ritual.get("id").getAsString();
            assertFalse(id.trim().isEmpty(), "Ritual has an empty id");
            assertTrue(ids.add(id), "Duplicate ritual id: " + id);
        }
    }

    @Test
    void everyEntityTagHasA12CompatibleMapping() throws IOException {
        Set<ResourceLocation> checkedTags = new HashSet<>();
        for (JsonElement element : loadRituals()) {
            JsonObject ritual = element.getAsJsonObject();
            if (!ritual.has("entity_tag_to_summon")) {
                continue;
            }

            ResourceLocation tagId = new ResourceLocation(ritual.get("entity_tag_to_summon").getAsString());
            if (!checkedTags.add(tagId)) {
                continue;
            }

            List<ResourceLocation> entities = RitualEntityTagMappings.getEntities(tagId);
            assertFalse(entities.isEmpty(), "No 1.12 entity mapping for ritual tag " + tagId);
        }
        assertFalse(checkedTags.isEmpty(), "Ritual data did not contain any entity tags");
    }

    @Test
    void everySpiritJobIdentifierIsRecognized() throws IOException {
        for (JsonElement element : loadRituals()) {
            JsonObject ritual = element.getAsJsonObject();
            if (!ritual.has("spirit_job_type")) {
                continue;
            }

            String ritualId = ritual.get("id").getAsString();
            String jobId = ritual.get("spirit_job_type").getAsString();
            assertNotNull(SpiritJobType.fromId(jobId),
                    "Unknown spirit_job_type " + jobId + " in ritual " + ritualId);
        }
    }

    @Test
    void everySpiritTierIdentifierIsRecognized() throws IOException {
        for (JsonElement element : loadRituals()) {
            JsonObject ritual = element.getAsJsonObject();
            if (!ritual.has("spirit_tier")) {
                continue;
            }

            String ritualId = ritual.get("id").getAsString();
            String tierId = ritual.get("spirit_tier").getAsString();
            assertNotNull(SpiritTier.fromId(tierId),
                    "Unknown spirit_tier " + tierId + " in ritual " + ritualId);
        }
    }

    private static JsonArray loadRituals() throws IOException {
        InputStream stream = RitualDataContractTest.class.getResourceAsStream(RITUAL_RESOURCE);
        assertNotNull(stream, "Missing ritual resource " + RITUAL_RESOURCE);
        try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            JsonElement root = new JsonParser().parse(reader);
            assertTrue(root.isJsonArray(), "Ritual resource root must be an array");
            JsonArray rituals = root.getAsJsonArray();
            assertFalse(rituals.size() == 0, "Ritual resource must not be empty");
            return rituals;
        }
    }
}
