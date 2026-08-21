package com.occultism.occultism.ritual;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.minecraft.util.ResourceLocation;

public final class RitualEntityTagMappings {

    private static final Map<String, List<ResourceLocation>> MAPPINGS = createMappings();

    private RitualEntityTagMappings() {
    }

    public static List<ResourceLocation> getEntities(ResourceLocation tagId) {
        List<ResourceLocation> entities = MAPPINGS.get(normalize(tagId));
        return entities == null ? Collections.emptyList() : entities;
    }

    private static Map<String, List<ResourceLocation>> createMappings() {
        Map<String, List<ResourceLocation>> mappings = new HashMap<>();
        // ResourceLocation in 1.12 treats one-character domains such as "c" as minecraft.
        put(mappings, "occultism:villagers",
                "minecraft:villager");
        put(mappings, "occultism:random_animals_common",
                "minecraft:chicken",
                "minecraft:cow",
                "minecraft:pig",
                "minecraft:sheep",
                "minecraft:squid",
                "minecraft:wolf");
        put(mappings, "occultism:random_animals_rideable",
                "minecraft:pig",
                "minecraft:donkey",
                "minecraft:horse",
                "minecraft:skeleton_horse",
                "minecraft:zombie_horse",
                "minecraft:llama",
                "minecraft:mule");
        put(mappings, "occultism:random_animals_small",
                "minecraft:bat",
                "minecraft:ocelot",
                "minecraft:parrot",
                "minecraft:rabbit");
        put(mappings, "occultism:random_animals_special",
                "minecraft:villager_golem",
                "minecraft:mooshroom",
                "minecraft:polar_bear");
        put(mappings, "occultism:random_animals_water",
                "minecraft:squid",
                "minecraft:snowman");
        return Collections.unmodifiableMap(mappings);
    }

    private static void put(Map<String, List<ResourceLocation>> mappings, String tagId, String... entityIds) {
        List<ResourceLocation> locations = new ArrayList<>();
        for (String entityId : Arrays.asList(entityIds)) {
            locations.add(new ResourceLocation(entityId));
        }
        mappings.put(tagId.toLowerCase(Locale.ROOT), Collections.unmodifiableList(locations));
    }

    private static String normalize(ResourceLocation tagId) {
        return tagId.toString().toLowerCase(Locale.ROOT);
    }
}
