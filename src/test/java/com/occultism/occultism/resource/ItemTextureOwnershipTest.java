package com.occultism.occultism.resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Item models must not borrow another item's texture by accident.
 *
 * <p>Demonic meat was pointing at {@code cursed_honey}, so it rendered as a jar of honey. Nothing
 * caught it: the texture existed, the JSON parsed and the asset counter was happy. The only thing
 * wrong was that the two names had nothing to do with each other.</p>
 *
 * <p>This check reads every {@code item/generated} model and requires its texture to be related to
 * the model's own name: equal, one a prefix of the other, or inside a family that legitimately
 * shares one texture (the calling and binding books, chalk blanks, ritual dummies, the compass
 * animation frames, the debug items and a handful of single cases). Anything else is reported.</p>
 */
class ItemTextureOwnershipTest {

    private static final Path ITEM_MODELS = Paths.get("src/main/resources/assets/occultism/models/item");
    private static final Pattern LAYER = Pattern.compile("\"layer0\"\\s*:\\s*\"occultism:item/([a-z0-9_/]+)\"");

    /** Paths (relative to models/item, without .json) that legitimately share one texture. */
    private static final String[] SHARED_FAMILIES = {
            "book_of_calling_", "book_of_binding_", "chalk_", "debug_", "miner_",
            "true_sight_staff", "soul_gem", "fragile_soul_gem", "trinity_gem",
            "vitality_compass", "foliot_", "ritual_dummy/"
    };

    @Test
    void everyItemModelUsesItsOwnOrAFamilyTexture() throws IOException {
        List<String> offenders = new ArrayList<>();
        List<Path> models;
        try (Stream<Path> stream = Files.walk(ITEM_MODELS)) {
            models = new ArrayList<>();
            stream.filter(Files::isRegularFile).forEach(models::add);
        }

        for (Path model : models) {
            String json = new String(Files.readAllBytes(model), StandardCharsets.UTF_8);
            Matcher matcher = LAYER.matcher(json);
            if (!matcher.find()) {
                continue;
            }
            String relative = ITEM_MODELS.relativize(model).toString().replace('\\', '/');
            relative = relative.substring(0, relative.length() - ".json".length());
            String bareName = relative.contains("/") ? relative.substring(relative.lastIndexOf('/') + 1) : relative;
            String texture = matcher.group(1);
            String textureName = texture.substring(texture.lastIndexOf('/') + 1);

            if (textureName.equals(bareName) || bareName.startsWith(textureName) || textureName.startsWith(bareName)) {
                continue;
            }
            boolean family = false;
            for (String prefix : SHARED_FAMILIES) {
                if (relative.startsWith(prefix)) {
                    family = true;
                    break;
                }
            }
            if (!family) {
                offenders.add(relative + " -> " + texture);
            }
        }

        assertTrue(offenders.isEmpty(),
                "these item models render another item's texture: " + offenders);
    }

    @Test
    void demonicMeatUsesItsOwnTexture() throws IOException {
        String model = new String(Files.readAllBytes(ITEM_MODELS.resolve("demonic_meat.json")),
                StandardCharsets.UTF_8);
        assertTrue(model.contains("occultism:item/demonic_meat"),
                "demonic meat must not borrow another item's texture");
    }
}
