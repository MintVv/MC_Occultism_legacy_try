package com.occultism.occultism.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.occultism.occultism.item.ItemBookOfCallingFoliotJob;
import com.occultism.occultism.item.BookOfCallingMode;
import com.occultism.occultism.registry.ModItems;
import com.occultism.occultism.spirit.MachineReference;
import com.occultism.occultism.spirit.SpiritJobType;
import com.occultism.occultism.spirit.SpiritTier;
import com.occultism.occultism.util.ItemNBTUtil;

import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.ItemStackHandler;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ManageMachinePortContractTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        Bootstrap.register();
    }

    @Test
    void jobIdIsAppendOnlyAndMachineBookExposesThreeModes() {
        assertEquals("occultism:manage_machine", SpiritJobType.MANAGE_MACHINE.getId().toString());
        ItemBookOfCallingFoliotJob book = (ItemBookOfCallingFoliotJob) ModItems.BOOK_OF_CALLING_DJINNI_MANAGE_MACHINE;
        assertEquals(SpiritJobType.MANAGE_MACHINE, book.getJobType());
        assertEquals(SpiritTier.DJINNI, book.getSpiritTier());
        assertEquals(3, book.getAvailableModes().length);
        assertEquals(BookOfCallingMode.SET_STORAGE_CONTROLLER, book.getAvailableModes()[0]);
        assertEquals(BookOfCallingMode.SET_MANAGED_MACHINE, book.getAvailableModes()[1]);
        assertEquals(BookOfCallingMode.SET_EXTRACT, book.getAvailableModes()[2]);
    }

    @Test
    void machineReferenceAndBookNbtRoundTripPositionsDimensionsAndFaces() {
        ItemStack book = new ItemStack(ModItems.BOOK_OF_CALLING_DJINNI_MANAGE_MACHINE);
        BlockPos controller = new BlockPos(2, 3, 4);
        ItemNBTUtil.setStorageControllerPosition(book, controller, 7);
        MachineReference reference = new MachineReference(new BlockPos(10, 11, 12), 7, EnumFacing.NORTH,
                new BlockPos(20, 21, 22), 7, EnumFacing.WEST);
        reference.setCustomName("test machine");
        ItemNBTUtil.setManagedMachine(book, reference);

        assertEquals(controller, ItemNBTUtil.getStorageControllerPosition(book));
        assertEquals(7, ItemNBTUtil.getStorageControllerDimension(book));
        MachineReference decoded = ItemNBTUtil.getManagedMachine(book);
        assertNotNull(decoded);
        assertEquals(new BlockPos(10, 11, 12), decoded.getInsertPosition());
        assertEquals(EnumFacing.NORTH, decoded.getInsertFacing());
        assertEquals(new BlockPos(20, 21, 22), decoded.getExtractPosition());
        assertEquals(EnumFacing.WEST, decoded.getExtractFacing());
        assertEquals("test machine", decoded.getCustomName());
    }

    @Test
    void entityMachineConfigurationPersistsAndCapabilityTransferIsLossless() {
        EntityFoliotJob entity = new EntityFoliotJob(null, SpiritJobType.MANAGE_MACHINE, SpiritTier.DJINNI);
        entity.setStorageControllerPos(new BlockPos(1, 2, 3), 7);
        MachineReference reference = new MachineReference(new BlockPos(4, 5, 6), 7, EnumFacing.UP,
                new BlockPos(8, 9, 10), 7, EnumFacing.DOWN);
        entity.setManagedMachine(reference);
        NBTTagCompound encoded = new NBTTagCompound();
        entity.writeEntityToNBT(encoded);
        EntityFoliotJob decoded = new EntityFoliotJob(null);
        decoded.readEntityFromNBT(encoded);
        assertEquals(SpiritJobType.MANAGE_MACHINE, decoded.getJobType());
        assertEquals(new BlockPos(1, 2, 3), decoded.getStorageControllerPos());
        assertEquals(new BlockPos(4, 5, 6), decoded.getManagedMachine().getInsertPosition());

        ItemStackHandler source = new ItemStackHandler(1);
        ItemStackHandler target = new ItemStackHandler(1);
        source.insertItem(0, new ItemStack(Items.DIAMOND, 8), false);
        assertTrue(EntityFoliotJob.transferItemHandlers(source, target, 4));
        assertEquals(4, source.getStackInSlot(0).getCount());
        assertEquals(4, target.getStackInSlot(0).getCount());
    }

    @Test
    void ritualResourceKeepsUpstreamIdentityAndDjinniMaterials() throws Exception {
        String source = new String(Files.readAllBytes(Paths.get("src", "main", "resources", "assets", "occultism",
                "rituals", "rituals.json")), StandardCharsets.UTF_8);
        assertTrue(source.contains("\"id\": \"summon_djinni_manage_machine\""));
        assertTrue(source.contains("\"spirit_job_type\": \"occultism:manage_machine\""));
        assertTrue(source.contains("\"spirit_tier\": \"occultism:djinni\""));
        assertTrue(source.contains("\"duration_ticks\": 1200"));
        assertTrue(source.contains("\"#c:storage_blocks/coal\""));
        assertTrue(source.contains("book_of_calling_djinni_manage_machine"));
    }
}
