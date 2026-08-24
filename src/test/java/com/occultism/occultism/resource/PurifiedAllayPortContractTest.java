package com.occultism.occultism.resource;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.occultism.occultism.entity.EntityPurifiedAllay;
import com.occultism.occultism.entity.IFamiliar;

import net.minecraft.entity.monster.EntityVex;
import net.minecraft.init.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class PurifiedAllayPortContractTest {

    private static final Path ROOT = Paths.get("src", "main");
    private static final Path JAVA_ROOT = ROOT.resolve("java").resolve("com").resolve("occultism")
            .resolve("occultism");
    private static final Path ASSET_ROOT = ROOT.resolve("resources").resolve("assets").resolve("occultism");

    @BeforeAll
    static void bootstrapMinecraft() {
        Bootstrap.register();
    }

    @Test
    void entityUsesTheVexBaseAndSharedFamiliarOwnershipContract() {
        EntityPurifiedAllay allay = new EntityPurifiedAllay(null);
        assertTrue(allay instanceof EntityVex);
        assertTrue(allay instanceof IFamiliar);
    }

    @Test
    void ownerIsPersistedAndControlsFollowingAndHeldItemRetrieval() throws IOException {
        String entity = readUtf8(JAVA_ROOT.resolve("entity").resolve("EntityPurifiedAllay.java"));
        assertTrue(entity.contains("implements IFamiliar"));
        assertTrue(entity.contains("OccultismFamiliarOwner"));
        assertTrue(entity.contains("getFamiliarOwner() != player"));
        assertTrue(entity.contains("getFollowTarget()"));
        assertTrue(entity.contains("this.familiarOwnerId != null"));
        assertTrue(entity.contains("returnHeldItemToPlayer(followTarget)"));
    }

    @Test
    void resurrectionRitualBindsTheCreatedEntityToTheCastingPlayer() throws IOException {
        String bowl = readUtf8(JAVA_ROOT.resolve("tileentity").resolve("TileEntitySacrificialBowl.java"));
        assertTrue(bowl.contains("\"resurrect_allay\".equals(definition.getId())"));
        assertTrue(bowl.contains("((IFamiliar) entity).setFamiliarOwner(castingPlayer);"));
        String rituals = readUtf8(ASSET_ROOT.resolve("rituals").resolve("rituals.json"));
        assertTrue(rituals.contains("\"entity_to_summon\": \"occultism:purified_allay\""));
        String english = readUtf8(ASSET_ROOT.resolve("lang").resolve("en_us.lang"));
        String chinese = readUtf8(ASSET_ROOT.resolve("lang").resolve("zh_cn.lang"));
        assertTrue(english.contains("bound to the caster"));
        assertTrue(chinese.contains("绑定施法者"));
    }

    private static String readUtf8(Path path) throws IOException {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}
