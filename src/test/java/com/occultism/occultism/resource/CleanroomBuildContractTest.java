package com.occultism.occultism.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

import com.occultism.occultism.Occultism;
import com.occultism.occultism.Tags;
import net.minecraftforge.fml.common.Mod;
import org.junit.jupiter.api.Test;

class CleanroomBuildContractTest {

    @Test
    void testsRunOnJava25() {
        assertEquals(25, Runtime.version().feature());
    }

    @Test
    void modRequiresCleanroom() {
        Mod metadata = Occultism.class.getAnnotation(Mod.class);
        assertTrue(metadata.dependencies().contains("required-after:cleanroom@[0.7.14-alpha,)"));
        assertEquals("occultism", metadata.modid());
    }

    @Test
    void generatedTagsMatchProjectMetadata() throws Exception {
        Properties properties = new Properties();
        try (var reader = Files.newBufferedReader(Paths.get("gradle.properties"), StandardCharsets.UTF_8)) {
            properties.load(reader);
        }
        assertEquals(properties.getProperty("mod_version"), Tags.VERSION);
        assertEquals(properties.getProperty("mod_id"), Tags.MOD_ID);
        assertEquals(properties.getProperty("mod_name"), Tags.MOD_NAME);
    }

    @Test
    void buildUsesNativeModernJavaRatherThanJava8Backporting() throws Exception {
        String build = Files.readString(Paths.get("build.gradle"), StandardCharsets.UTF_8);
        String dependencies = Files.readString(Paths.get("gradle/scripts/dependencies.gradle"), StandardCharsets.UTF_8);
        assertTrue(build.contains("id 'com.cleanroommc.cleanroomgradle'"));
        assertTrue(build.contains("options.release.set(25)"));
        assertFalse(build.contains("retrofuturagradle"));
        assertFalse(build.contains("jabel"));
        assertTrue(dependencies.contains("cleanroom.userdev("));
        assertFalse(dependencies.contains("rfg.deobf("));
    }
}
