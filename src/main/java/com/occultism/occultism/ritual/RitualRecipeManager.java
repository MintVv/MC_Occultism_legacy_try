package com.occultism.occultism.ritual;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.occultism.occultism.Occultism;
import com.occultism.occultism.Tags;
import com.occultism.occultism.spirit.SpiritJobType;
import com.occultism.occultism.spirit.SpiritTier;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public final class RitualRecipeManager {

    private static final String RITUAL_RESOURCE = "assets/" + Tags.MOD_ID + "/rituals/rituals.json";
    private static List<RitualDefinition> definitions = Collections.emptyList();

    private RitualRecipeManager() {
    }

    public static void load() {
        List<RitualDefinition> loadedDefinitions = new ArrayList<>();
        try (InputStream stream = RitualRecipeManager.class.getClassLoader().getResourceAsStream(RITUAL_RESOURCE)) {
            if (stream == null) {
                Occultism.LOGGER.error("Occultism ritual data not found: {}", RITUAL_RESOURCE);
                definitions = Collections.emptyList();
                return;
            }

            JsonElement root = new JsonParser().parse(new InputStreamReader(stream, StandardCharsets.UTF_8));
            JsonArray rituals = root.isJsonArray() ? root.getAsJsonArray() : JsonUtils.getJsonArray(root.getAsJsonObject(),
                    "rituals");
            for (JsonElement element : rituals) {
                loadedDefinitions.add(readRitual(element.getAsJsonObject()));
            }
            definitions = Collections.unmodifiableList(loadedDefinitions);
            Occultism.LOGGER.info("Loaded {} Occultism ritual recipes from JSON.", definitions.size());
        } catch (Exception e) {
            Occultism.LOGGER.error("Failed to load Occultism ritual data from {}.", RITUAL_RESOURCE, e);
            definitions = Collections.emptyList();
        }
    }

    public static List<RitualDefinition> getDefinitions() {
        if (definitions.isEmpty()) {
            load();
        }
        return definitions;
    }

    public static synchronized void addDefinition(RitualDefinition definition) {
        List<RitualDefinition> mutable = new ArrayList<>(getDefinitions());
        removeByIdInternal(mutable, definition.getId());
        mutable.add(definition);
        definitions = Collections.unmodifiableList(mutable);
    }

    public static synchronized int removeById(String id) {
        List<RitualDefinition> mutable = new ArrayList<>(getDefinitions());
        int removed = removeByIdInternal(mutable, cleanId(id));
        definitions = Collections.unmodifiableList(mutable);
        return removed;
    }

    public static synchronized int removeIf(Predicate<RitualDefinition> predicate) {
        List<RitualDefinition> mutable = new ArrayList<>(getDefinitions());
        int originalSize = mutable.size();
        mutable.removeIf(predicate);
        definitions = Collections.unmodifiableList(mutable);
        return originalSize - mutable.size();
    }

    public static RitualDefinition findMatching(List<PentacleMatch> pentacles, ItemStack stack) {
        for (PentacleMatch pentacle : pentacles) {
            for (RitualDefinition definition : getDefinitions()) {
                if (definition.matchesActivation(pentacle, stack)) {
                    return definition;
                }
            }
        }
        return null;
    }

    public static RitualDefinition getFirstForPentacle(String pentacleId) {
        for (RitualDefinition definition : getDefinitions()) {
            if (definition.getPentacleId().equals(pentacleId)) {
                return definition;
            }
        }
        return null;
    }

    public static List<RitualDefinition> getForPentacle(String pentacleId) {
        List<RitualDefinition> rituals = new ArrayList<>();
        for (RitualDefinition definition : getDefinitions()) {
            if (definition.getPentacleId().equals(pentacleId)) {
                rituals.add(definition);
            }
        }
        return rituals;
    }

    public static List<RitualDefinition> getForPentacleAndActivation(String pentacleId, ItemStack activationStack) {
        List<RitualDefinition> rituals = new ArrayList<>();
        for (RitualDefinition definition : getDefinitions()) {
            if (definition.getPentacleId().equals(pentacleId)
                    && definition.getActivationIngredient().matches(activationStack)) {
                rituals.add(definition);
            }
        }
        return rituals;
    }

    public static RitualDefinition getById(String id) {
        for (RitualDefinition definition : getDefinitions()) {
            if (definition.getId().equals(id)) {
                return definition;
            }
        }
        return null;
    }

    private static RitualDefinition readRitual(JsonObject json) {
        String id = cleanId(JsonUtils.getString(json, "id"));
        String displayName = JsonUtils.getString(json, "display_name", id);
        // The English display_name stays untouched (JEI classifies spirit jobs from it); the key carries localization.
        String displayNameKey = JsonUtils.getString(json, "display_name_key",
                "ritual." + Tags.MOD_ID + "." + id + ".name");
        String pentacleId = cleanId(JsonUtils.getString(json, "pentacle_id"));
        String ritualType = cleanId(JsonUtils.getString(json, "ritual_type", Tags.MOD_ID + ":craft"));
        int durationTicks = JsonUtils.getInt(json, "duration_ticks", JsonUtils.getInt(json, "duration", 30) * 20);
        RitualIngredient activation = readIngredient(json.get("activation_item"));
        RitualResult result = new RitualResult(readStack(JsonUtils.getJsonObject(json, "result")));
        RitualResult ritualDummy = json.has("ritual_dummy")
                ? new RitualResult(readStack(JsonUtils.getJsonObject(json, "ritual_dummy")))
                : result;
        RitualSacrifice sacrifice = json.has("entity_to_sacrifice")
                ? readSacrifice(JsonUtils.getJsonObject(json, "entity_to_sacrifice"))
                : null;
        RitualIngredient itemToUse = json.has("item_to_use") ? readIngredient(json.get("item_to_use")) : null;
        RitualEntityToSummon entityToSummon = json.has("entity_to_summon") || json.has("entity_tag_to_summon")
                ? readEntityToSummon(json, ritualType)
                : null;
        SpiritJobType spiritJobType = json.has("spirit_job_type")
                ? readSpiritJobType(JsonUtils.getString(json, "spirit_job_type"))
                : null;
        SpiritTier spiritTier = json.has("spirit_tier")
                ? readSpiritTier(JsonUtils.getString(json, "spirit_tier"))
                : null;
        int spiritMaxAge = JsonUtils.getInt(json, "spirit_max_age", -1);

        JsonArray ingredientJson = JsonUtils.getJsonArray(json, "ingredients", new JsonArray());
        List<RitualIngredient> ingredients = new ArrayList<>();
        for (JsonElement element : ingredientJson) {
            ingredients.add(readIngredient(element));
        }

        return new RitualDefinition(id, displayName, displayNameKey, pentacleId, ritualType, activation, result,
                ritualDummy, durationTicks, sacrifice, itemToUse, entityToSummon, spiritJobType, spiritTier,
                spiritMaxAge, ingredients.toArray(new RitualIngredient[0]));
    }

    private static int removeByIdInternal(List<RitualDefinition> mutable, String id) {
        int removed = 0;
        for (int i = mutable.size() - 1; i >= 0; i--) {
            if (cleanId(mutable.get(i).getId()).equals(cleanId(id))) {
                mutable.remove(i);
                removed++;
            }
        }
        return removed;
    }

    private static SpiritJobType readSpiritJobType(String id) {
        SpiritJobType type = SpiritJobType.fromId(id);
        if (type == null) {
            throw new IllegalArgumentException("Unknown ritual spirit job type: " + id);
        }
        return type;
    }

    private static SpiritTier readSpiritTier(String id) {
        SpiritTier tier = SpiritTier.fromId(id);
        if (tier == null) {
            throw new IllegalArgumentException("Unknown ritual spirit tier: " + id);
        }
        return tier;
    }

    private static RitualEntityToSummon readEntityToSummon(JsonObject json, String ritualType) {
        // The dedicated runtime branch always creates three bosses; keep recipe metadata and JEI in sync.
        int summonNumber = "summon_wild_hunt".equals(ritualType)
                ? 3
                : JsonUtils.getInt(json, "summon_number", 1);
        if (json.has("entity_to_summon")) {
            String entityId = JsonUtils.getString(json, "entity_to_summon");
            return new RitualEntityToSummon(new ResourceLocation(entityId), summonNumber);
        }

        ResourceLocation tagId = new ResourceLocation(JsonUtils.getString(json, "entity_tag_to_summon"));
        List<ResourceLocation> entityIds = RitualEntityTagMappings.getEntities(tagId);
        if (entityIds.isEmpty()) {
            throw new IllegalArgumentException("Unknown or empty ritual entity tag: " + tagId);
        }
        return new RitualEntityToSummon(tagId, entityIds, summonNumber);
    }

    private static RitualSacrifice readSacrifice(JsonObject json) {
        return new RitualSacrifice(JsonUtils.getString(json, "tag"),
                JsonUtils.getString(json, "display_name", ""));
    }

    private static RitualIngredient readIngredient(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            throw new IllegalArgumentException("Missing ritual ingredient.");
        }
        if (element.isJsonArray()) {
            List<RitualIngredient> alternatives = new ArrayList<>();
            for (JsonElement alternative : element.getAsJsonArray()) {
                alternatives.add(readIngredient(alternative));
            }
            return new RitualIngredient(Collections.unmodifiableList(alternatives));
        }
        if (element.isJsonPrimitive() && element.getAsString().startsWith("#")) {
            return new RitualIngredient(toOreDictionaryName(element.getAsString()), 1);
        }
        if (element.isJsonObject() && element.getAsJsonObject().has("tag")) {
            JsonObject object = element.getAsJsonObject();
            String tag = JsonUtils.getString(object, "tag");
            if (!tag.startsWith("#")) {
                tag = "#" + tag;
            }
            return new RitualIngredient(toOreDictionaryName(tag), JsonUtils.getInt(object, "count", 1));
        }
        ItemStack stack = element.isJsonPrimitive() ? readStack(element.getAsString(), 1, -1)
                : readStack(element.getAsJsonObject());
        return new RitualIngredient(stack.getItem(), stack.getCount(), stack.getMetadata());
    }

    private static ItemStack readStack(JsonObject json) {
        String itemId = JsonUtils.getString(json, "item", JsonUtils.getString(json, "id", ""));
        int count = JsonUtils.getInt(json, "count", 1);
        int metadata = JsonUtils.getInt(json, "metadata", JsonUtils.getInt(json, "meta", -1));
        return readStack(itemId, count, metadata);
    }

    private static ItemStack readStack(String itemId, int count, int metadata) {
        ResourceLocation location = new ResourceLocation(itemId);
        Item item = ForgeRegistries.ITEMS.getValue(location);
        if (item == null) {
            throw new IllegalArgumentException("Unknown ritual item: " + itemId);
        }
        return metadata < 0 ? new ItemStack(item, count) : new ItemStack(item, count, metadata);
    }

    private static String toOreDictionaryName(String tagId) {
        String path = tagId.substring(1);
        if ("minecraft:soul_fire_base_blocks".equals(path)) {
            return "soulFireBaseBlock";
        }
        int separator = path.indexOf(':');
        if (separator >= 0) {
            path = path.substring(separator + 1);
        }
        if ("ender_pearls".equals(path)) {
            return "enderpearl";
        }
        if ("gunpowders".equals(path)) {
            return "gunpowder";
        }
        if ("strings".equals(path)) {
            return "string";
        }
        if ("slime_balls".equals(path)) {
            return "slimeball";
        }
        if ("eggs".equals(path)) {
            return "egg";
        }
        if ("fishes".equals(path)) {
            return "foodRawFish";
        }
        if ("leaves".equals(path)) {
            return "treeLeaves";
        }
        if ("netherracks".equals(path)) {
            return "netherrack";
        }
        if ("tools/chalk".equals(path)) {
            return "toolChalk";
        }
        if ("tools/pickaxes".equals(path)) {
            return "toolPickaxe";
        }
        if ("tools".equals(path) || "armors".equals(path) || "miners".equals(path)) {
            return path;
        }
        String[] parts = path.split("/");
        if (parts.length == 1) {
            return parts[0];
        }

        String kind = parts[parts.length - 2];
        String material = parts[parts.length - 1];
        if ("storage_blocks".equals(kind)) {
            kind = "block";
        } else if (kind.endsWith("s") && kind.length() > 1) {
            kind = kind.substring(0, kind.length() - 1);
        }
        return kind + Character.toUpperCase(material.charAt(0)) + material.substring(1);
    }

    private static String cleanId(String id) {
        return id.startsWith(Tags.MOD_ID + ":") ? id.substring((Tags.MOD_ID + ":").length()) : id;
    }
}
