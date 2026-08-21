package com.occultism.occultism.tileentity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import com.occultism.occultism.network.MessageSetManagedMachine;
import com.occultism.occultism.spirit.MachineReference;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.init.Bootstrap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class LinkedMachineRegistrationContractTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        Bootstrap.register();
    }

    @Test
    void controllerLinksDeduplicatesAndRoundTripsMachineReferences() {
        TileEntityStorageController controller = new TileEntityStorageController();
        MachineReference reference = reference(EnumFacing.NORTH, EnumFacing.SOUTH);

        assertTrue(controller.linkMachine(reference));
        assertFalse(controller.linkMachine(reference));
        assertTrue(controller.isMachineLinked(reference));
        assertEquals(1, controller.getLinkedMachineCount());

        NBTTagCompound encoded = new NBTTagCompound();
        controller.writeLinkedMachines(encoded);
        assertTrue(encoded.hasKey(TileEntityStorageController.TAG_LINKED_MACHINES, 9));
        TileEntityStorageController restored = new TileEntityStorageController();
        restored.readLinkedMachines(encoded);
        assertEquals(1, restored.getLinkedMachineCount());
        MachineReference decoded = restored.getLinkedMachines().get(0);
        assertEquals(reference.getInsertPosition(), decoded.getInsertPosition());
        assertEquals(reference.getExtractPosition(), decoded.getExtractPosition());
        assertEquals(EnumFacing.NORTH, decoded.getInsertFacing());
        assertEquals(EnumFacing.SOUTH, decoded.getExtractFacing());
        assertEquals("machine", decoded.getCustomName());

        assertTrue(restored.unlinkMachine(reference));
        assertFalse(restored.isMachineLinked(reference));
        assertEquals(0, restored.getLinkedMachineCount());
    }

    @Test
    void controllerBoundsMalformedRegistrationsAndUsesDefensiveCopies() {
        TileEntityStorageController controller = new TileEntityStorageController();
        MachineReference partial = new MachineReference(new BlockPos(1, 2, 3), 0, EnumFacing.UP,
                null, 0, EnumFacing.DOWN);
        assertFalse(controller.linkMachine(partial));

        MachineReference reference = reference(EnumFacing.UP, EnumFacing.DOWN);
        assertTrue(controller.linkMachine(reference));
        MachineReference copy = controller.getLinkedMachines().get(0);
        copy.setCustomName("mutated");
        assertEquals("machine", controller.getLinkedMachines().get(0).getCustomName());

        NBTTagCompound malformed = new NBTTagCompound();
        NBTTagCompound entries = new NBTTagCompound();
        entries.setString("bad", "payload");
        net.minecraft.nbt.NBTTagList list = new net.minecraft.nbt.NBTTagList();
        list.appendTag(entries);
        malformed.setTag(TileEntityStorageController.TAG_LINKED_MACHINES, list);
        TileEntityStorageController decoded = new TileEntityStorageController();
        decoded.readFromNBT(malformed);
        assertEquals(0, decoded.getLinkedMachineCount());
    }

    @Test
    void managedMachinePacketRoundTripsAndRejectsPartialReferences() {
        MachineReference reference = reference(EnumFacing.WEST, EnumFacing.EAST);
        MessageSetManagedMachine packet = new MessageSetManagedMachine(reference);
        assertTrue(packet.isValid());
        ByteBuf buffer = Unpooled.buffer();
        try {
            packet.toBytes(buffer);
            MessageSetManagedMachine decoded = new MessageSetManagedMachine();
            decoded.fromBytes(buffer);
            assertTrue(decoded.isValid());
            assertNotNull(decoded.getManagedMachine());
            assertEquals(EnumFacing.WEST, decoded.getManagedMachine().getInsertFacing());
            assertEquals("machine", decoded.getManagedMachine().getCustomName());
        } finally {
            buffer.release();
        }

        MachineReference partial = new MachineReference(new BlockPos(1, 2, 3), 0, EnumFacing.UP,
                null, 0, EnumFacing.DOWN);
        assertFalse(new MessageSetManagedMachine(partial).isValid());
    }

    @Test
    void managerRouteIsBoundToReferenceAndSpiritId() {
        TileEntityStorageController controller = new TileEntityStorageController();
        MachineReference reference = reference(EnumFacing.UP, EnumFacing.DOWN);
        UUID spiritId = UUID.randomUUID();
        assertTrue(controller.registerMachineManager(reference, spiritId));
        assertEquals(1, controller.getDepositOrderRouteCount());
        // A detached controller cannot dispatch into a world, but the route is
        // still represented until the manager unregisters it.
        assertFalse(controller.addDepositOrder(reference, new net.minecraft.item.ItemStack(
                net.minecraft.init.Items.IRON_INGOT), 1));
        assertFalse(controller.unregisterMachineManager(reference, UUID.randomUUID()));
        assertEquals(1, controller.getDepositOrderRouteCount());
        assertTrue(controller.unregisterMachineManager(reference, spiritId));
        assertEquals(0, controller.getDepositOrderRouteCount());
    }

    private static MachineReference reference(EnumFacing insertFacing, EnumFacing extractFacing) {
        MachineReference reference = new MachineReference(new BlockPos(10, 11, 12), 0, insertFacing,
                new BlockPos(13, 14, 15), 0, extractFacing);
        reference.setInsertPosition(reference.getInsertPosition(), 0, insertFacing, "minecraft:furnace");
        reference.setExtractPosition(reference.getExtractPosition(), 0, extractFacing, "minecraft:chest");
        reference.setCustomName("machine");
        return reference;
    }
}
