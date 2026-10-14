package com.occultism.occultism.resource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

/** Static resource and renderer contracts for the dedicated high-tier spirit visuals. */
class HighTierSpiritRendererPortContractTest {

    @Test
    void dedicatedEntitiesUseTierSpecificRenderers() throws Exception {
        String renderers = readSource("src", "main", "java", "com", "occultism", "occultism",
                "client", "render", "ModEntityRenderers.java");

        assertTrue(renderers.contains("new RenderHighTierBoundSpirit<EntityDjinni>"));
        assertTrue(renderers.contains("new RenderHighTierBoundSpirit<EntityAfrit>"));
        assertTrue(renderers.contains("new RenderHighTierBoundSpirit<EntityMarid>"));
        assertTrue(renderers.contains("new RenderHighTierJob<EntityDjinniJob>"));
        assertTrue(renderers.contains("new RenderHighTierJob<EntityAfritJob>"));
        assertTrue(renderers.contains("new RenderHighTierJob<EntityMaridJob>"));
    }

    @Test
    void highTierModelUsesOneRootForHeadBodyAndTail() throws Exception {
        String model = readSource("src", "main", "java", "com", "occultism", "occultism",
                "client", "model", "ModelHighTierSpirit.java");

        assertTrue(model.contains("private final ModelRenderer root"));
        assertTrue(model.contains("this.root.addChild(this.bipedHead)"));
        assertTrue(model.contains("this.root.addChild(this.bipedBody)"));
        assertTrue(model.contains("this.root.addChild(this.tailBase)"));
        assertTrue(model.contains("this.tailBase.addChild(this.tailMiddle)"));
        assertTrue(model.contains("this.tailMiddle.addChild(this.tailTip)"));
        assertFalse(model.contains("this.bipedHead.render(scale);"));
        assertTrue(model.contains("this.root.render(scale);"));
    }

    @Test
    void highTierRenderersDoNotFallBackToFoliotModels() throws Exception {
        String bound = readSource("src", "main", "java", "com", "occultism", "occultism",
                "client", "render", "RenderHighTierBoundSpirit.java");
        String job = readSource("src", "main", "java", "com", "occultism", "occultism",
                "client", "render", "RenderHighTierJob.java");

        assertTrue(bound.contains("new ModelHighTierSpirit(tier)"));
        assertTrue(job.contains("new ModelHighTierSpirit(tier)"));
        assertFalse(bound.contains("ModelFoliotJob"));
        assertFalse(job.contains("ModelFoliotJob"));
    }

    @Test
    void upstreamHighTierTexturesArePresentForBoundAndJobEntities() {
        assertTrue(Files.exists(Paths.get("src", "main", "resources", "assets", "occultism",
                "textures", "entity", "djinni.png")));
        assertTrue(Files.exists(Paths.get("src", "main", "resources", "assets", "occultism",
                "textures", "entity", "afrit.png")));
        assertTrue(Files.exists(Paths.get("src", "main", "resources", "assets", "occultism",
                "textures", "entity", "marid.png")));
        assertTrue(Files.exists(Paths.get("src", "main", "resources", "assets", "occultism",
                "textures", "entity", "djinni_worker.png")));
        assertTrue(Files.exists(Paths.get("src", "main", "resources", "assets", "occultism",
                "textures", "entity", "afrit_worker.png")));
        assertTrue(Files.exists(Paths.get("src", "main", "resources", "assets", "occultism",
                "textures", "entity", "marid_worker.png")));
    }

    private static String readSource(String... path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path[0],
                java.util.Arrays.copyOfRange(path, 1, path.length))), StandardCharsets.UTF_8);
    }
}
