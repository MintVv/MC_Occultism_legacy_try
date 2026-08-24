package com.occultism.occultism.resource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.occultism.occultism.entity.EntityDevilFamiliar;

import net.minecraft.init.Bootstrap;
import org.junit.jupiter.api.Test;

class DevilUpgradePortContractTest {

    private static final Path ENTITY = Paths.get("src", "main", "java", "com", "occultism", "occultism",
            "entity", "EntityDevilFamiliar.java");

    @Test
    void devilAcceptsExactlyOneBlacksmithUpgradeAndPersistsIt() {
        Bootstrap.register();

        EntityDevilFamiliar devil = new EntityDevilFamiliar(null);
        assertFalse(devil.hasBlacksmithUpgrade());
        assertTrue(devil.canBlacksmithUpgrade());

        devil.blacksmithUpgrade();
        assertTrue(devil.hasBlacksmithUpgrade());
        assertFalse(devil.canBlacksmithUpgrade());

        devil.blacksmithUpgrade();
        assertTrue(devil.hasBlacksmithUpgrade());

    }

    @Test
    void devilUsesTheUpstreamSinInteractionContractFor1_12() throws IOException {
        String entity = new String(Files.readAllBytes(ENTITY), StandardCharsets.UTF_8);
        assertTrue(entity.contains("implements IBlacksmithUpgradable"));
        assertTrue(entity.contains("private static final long SIN_INTERVAL = 20L * 60L * 33L;"));
        assertTrue(entity.contains("stack.getItem() == Items.GOLDEN_APPLE"));
        assertTrue(entity.contains("stack.getItemDamage() == 0"),
                "The 1.12 port must not consume an already enchanted golden apple");
        assertTrue(entity.contains("new ItemStack(Items.GOLDEN_APPLE, 1, 1)"),
                "1.12 stores the enchanted golden apple variant as metadata 1");
        assertTrue(entity.contains("compound.setBoolean(\"hasBlacksmithUpgrade\", this.hasBlacksmithUpgrade());"));
        assertTrue(entity.contains("compound.setLong(\"sinLastTime\", this.sinLastTime);"));
        assertTrue(entity.contains("dialog.occultism.devil.no_upgrade"));
        assertTrue(entity.contains("dialog.occultism.devil.sin_on_cooldown"));
    }
}
