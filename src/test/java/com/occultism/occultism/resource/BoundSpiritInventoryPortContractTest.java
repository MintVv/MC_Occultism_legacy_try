package com.occultism.occultism.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.occultism.occultism.entity.EntityBoundSpirit;
import com.occultism.occultism.entity.EntityDjinni;
import com.occultism.occultism.spirit.SpiritTier;

import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

/** Contract coverage for the upstream-style single-slot inventory on bound spirits. */
class BoundSpiritInventoryPortContractTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        Bootstrap.register();
    }

    @Test
    void boundSpiritExposesOneCapabilitySlotAndKeepsItsContentsAcrossNbt() {
        EntityBoundSpirit source = new EntityDjinni(null);
        assertEquals(1, source.getSpiritInventory().getSlots());
        assertTrue(source.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null));

        IItemHandler capability = source.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        assertNotNull(capability);
        assertSame(source.getSpiritInventory(), capability);

        ItemStack input = new ItemStack(Items.IRON_INGOT, 12);
        assertTrue(source.getSpiritInventory().insertItem(0, input, false).isEmpty());
        assertEquals(12, source.getSpiritInventory().getStackInSlot(0).getCount());
        UUID owner = UUID.randomUUID();
        source.setTamed(true);
        source.setOwnerId(owner);

        NBTTagCompound serialized = source.getSpiritInventory().serializeNBT();
        EntityBoundSpirit restored = new EntityDjinni(null);
        restored.getSpiritInventory().deserializeNBT(serialized);

        assertEquals(Items.IRON_INGOT, restored.getSpiritInventory().getStackInSlot(0).getItem());
        assertEquals(12, restored.getSpiritInventory().getStackInSlot(0).getCount());
        assertEquals(SpiritTier.DJINNI, restored.getTier());
        assertTrue(source.isTamed());
        assertEquals(owner, source.getOwnerId());
    }

    @Test
    void boundSpiritInteractionAndGuiUseAnAppendOnlyServerEntry() throws Exception {
        String entity = readSource("src", "main", "java", "com", "occultism", "occultism", "entity",
                "EntityBoundSpirit.java");
        String handler = readSource("src", "main", "java", "com", "occultism", "occultism", "gui",
                "ModGuiHandler.java");
        String container = readSource("src", "main", "java", "com", "occultism", "occultism", "container",
                "ContainerBoundSpirit.java");
        String gui = readSource("src", "main", "java", "com", "occultism", "occultism", "client", "gui",
                "GuiBoundSpirit.java");
        String bowl = readSource("src", "main", "java", "com", "occultism", "occultism", "tileentity",
                "TileEntitySacrificialBowl.java");
        String debug = readSource("src", "main", "java", "com", "occultism", "occultism", "item",
                "ItemDebugSummonSpirit.java");

        assertTrue(entity.contains("player.isSneaking() && player.getHeldItem(hand).isEmpty()"));
        assertTrue(entity.contains("extends EntityTameable"));
        assertTrue(entity.contains("EntityAIFollowOwner"));
        assertTrue(entity.contains("ModGuiHandler.BOUND_SPIRIT"));
        assertTrue(entity.contains("canPlayerAccess"));
        assertTrue(entity.contains("super.writeEntityToNBT(compound)"));
        assertTrue(entity.contains("super.readEntityFromNBT(compound)"));
        assertTrue(entity.contains("compound.setTag(\"SpiritInventory\""));
        assertTrue(handler.contains("public static final int BOUND_SPIRIT = 13"));
        assertTrue(handler.contains("new ContainerBoundSpirit(player.inventory"));
        assertTrue(handler.contains("new GuiBoundSpirit(player.inventory"));
        assertTrue(handler.contains("canPlayerAccess(player)"));
        assertTrue(container.contains("new SlotItemHandler(spirit.getSpiritInventory(), 0, 152, 54)"));
        assertTrue(container.contains("canPlayerAccess(playerIn)"));
        assertTrue(gui.contains("spirit_tier.occultism."));
        assertTrue(bowl.contains("setSpiritOwner(castingPlayer)"));
        assertTrue(debug.contains("setSpiritOwner(player)"));
        assertTrue(EntityTameable.class.isAssignableFrom(EntityBoundSpirit.class));
    }

    private static String readSource(String... path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path[0], java.util.Arrays.copyOfRange(path, 1, path.length))),
                StandardCharsets.UTF_8);
    }
}
