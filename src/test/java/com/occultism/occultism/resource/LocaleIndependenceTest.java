package com.occultism.occultism.resource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * Strings that become identifiers must not depend on the player's locale.
 *
 * <p>Java's {@code String.format} and {@code toLowerCase()} use the default locale. That is fine
 * for prose, but the vitality compass built its model names with
 * {@code String.format("compass_%02d", i)}: in a locale whose digits are not ASCII the result is
 * a model name that does not exist, so the item silently renders as a missing model. Likewise a
 * case conversion without a locale turns {@code I} into a dotless {@code ı} under a Turkish
 * locale and produces a translation key that is not in the language file.</p>
 *
 * <p>The suite runs under the default locale, so none of this shows up in a test run. This scans
 * the sources instead. Display-only formatting may opt out through {@link #ALLOWED_FORMATTING}.</p>
 */
class LocaleIndependenceTest {

    private static final Path JAVA = Paths.get("src/main/java");

    /** Formatting whose output is shown to the player, where the locale is the right choice. */
    private static final Set<String> ALLOWED_FORMATTING = new LinkedHashSet<>(Arrays.asList(
            // JEI tooltip: a decimal separator matching the player's locale is desirable there.
            "DimensionalMineshaftRecipeCategory.java"));

    private static final Pattern FORMAT = Pattern.compile("\\bString\\.format\\s*\\(");
    private static final Pattern CASE = Pattern.compile("\\.to(?:Lower|Upper)Case\\s*\\(\\s*\\)");

    @Test
    void identifierStringsDoNotDependOnTheDefaultLocale() throws IOException {
        Set<String> problems = new LinkedHashSet<>();
        for (Path file : javaFiles()) {
            String name = file.getFileName().toString();
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            for (int index = 0; index < lines.size(); index++) {
                String line = lines.get(index);
                if (FORMAT.matcher(line).find() && !line.contains("Locale")) {
                    if (!ALLOWED_FORMATTING.contains(name)) {
                        problems.add(name + ":" + (index + 1) + " String.format without an explicit Locale");
                    }
                }
                if (CASE.matcher(line).find()) {
                    problems.add(name + ":" + (index + 1) + " case conversion without an explicit Locale");
                }
            }
        }
        assertTrue(problems.isEmpty(),
                "Locale-dependent strings that can become identifiers or keys: " + problems);
    }

    @Test
    void theScanActuallySeesTheSources() throws IOException {
        // Guards against the filter silently matching nothing and the test above passing forever.
        List<Path> files = javaFiles();
        assertFalse(files.isEmpty(), "No Java sources found under " + JAVA);
        boolean sawFormat = false;
        for (Path file : files) {
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                if (FORMAT.matcher(line).find()) {
                    sawFormat = true;
                }
            }
        }
        assertTrue(sawFormat, "Expected at least one String.format call in the sources");
    }

    private static List<Path> javaFiles() throws IOException {
        List<Path> files = new ArrayList<>();
        try (Stream<Path> stream = Files.walk(JAVA)) {
            stream.filter(path -> path.toString().endsWith(".java")).forEach(files::add);
        }
        return files;
    }
}
