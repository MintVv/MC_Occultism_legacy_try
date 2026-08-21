package com.occultism.occultism.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.occultism.occultism.spirit.MachineReference;
import com.occultism.occultism.spirit.DepositOrder;
import com.occultism.occultism.spirit.SpiritJobType;
import com.occultism.occultism.network.MessageRequestOrder;
import io.netty.buffer.Unpooled;
import net.minecraft.init.Items;
import net.minecraft.init.Bootstrap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;

class MachineManagementPortContractTest {

    private static final Path ASSET_ROOT = Paths.get("src", "main", "resources", "assets", "occultism");
    private static final Path JAVA_ROOT = Paths.get("src", "main", "java", "com", "occultism", "occultism");

    @BeforeAll
    static void bootstrapMinecraft() {
        Bootstrap.register();
    }

    @Test
    void manageMachineJobIsAppendOnlyAndUsesUpstreamId() {
        assertEquals(SpiritJobType.MANAGE_MACHINE, SpiritJobType.fromId("occultism:manage_machine"));
        assertTrue(SpiritJobType.MANAGE_MACHINE.isFoliotJob());
        assertTrue(SpiritJobType.MANAGE_MACHINE.isMachineManagementJob());
        assertEquals(SpiritJobType.MANAGE_MACHINE, SpiritJobType.values()[SpiritJobType.values().length - 1]);
    }

    @Test
    void machineReferenceRoundTripsPositionsFacesDimensionsAndName() {
        MachineReference reference = new MachineReference(new BlockPos(1, 2, 3), 7, EnumFacing.NORTH,
                new BlockPos(-4, 5, -6), 8, EnumFacing.WEST);
        reference.setInsertPosition(new BlockPos(10, 11, 12), 7, EnumFacing.SOUTH, "minecraft:furnace");
        reference.setExtractPosition(new BlockPos(13, 14, 15), 8, EnumFacing.EAST, "minecraft:chest");
        reference.setCustomName("test machine");

        MachineReference decoded = MachineReference.from(reference.writeToNBT(new NBTTagCompound()));
        assertEquals(new BlockPos(10, 11, 12), decoded.getInsertPosition());
        assertEquals(7, decoded.getInsertDimension());
        assertEquals(EnumFacing.SOUTH, decoded.getInsertFacing());
        assertEquals("minecraft:furnace", decoded.getInsertRegistryName());
        assertEquals(new BlockPos(13, 14, 15), decoded.getExtractPosition());
        assertEquals(8, decoded.getExtractDimension());
        assertEquals(EnumFacing.EAST, decoded.getExtractFacing());
        assertEquals("minecraft:chest", decoded.getExtractRegistryName());
        assertEquals("test machine", decoded.getCustomName());
    }

    @Test
    void summonRitualMatchesUpstreamMaterialsAndLocalJobRoute() throws IOException {
        JsonObject ritual = findRitual("summon_djinni_manage_machine");
        assertEquals("occultism:summon_spirit_with_job", ritual.get("ritual_type").getAsString());
        assertEquals("occultism:book_of_binding_bound_djinni", ritual.get("activation_item").getAsString());
        assertEquals("occultism:summon_djinni", ritual.get("pentacle_id").getAsString());
        assertEquals("occultism:manage_machine", ritual.get("spirit_job_type").getAsString());
        assertEquals("occultism:djinni", ritual.get("spirit_tier").getAsString());
        assertEquals(1200, ritual.get("duration_ticks").getAsInt());
        Set<String> ingredients = new HashSet<>();
        for (JsonElement element : ritual.getAsJsonArray("ingredients")) {
            ingredients.add(element.getAsString());
        }
        assertTrue(ingredients.contains("#c:storage_blocks/coal"));
        assertTrue(ingredients.contains("minecraft:gold_ingot"));
        assertTrue(ingredients.contains("minecraft:iron_ingot"));
        assertTrue(ingredients.contains("minecraft:furnace"));
        assertEquals("occultism:book_of_calling_djinni_manage_machine",
                ritual.getAsJsonObject("result").get("id").getAsString());
    }

    @Test
    void bookAndEntityExposeThreeConfigurationModesAndCapabilityLoop() throws IOException {
        String book = readJava("item", "ItemBookOfCallingFoliotJob.java");
        String entity = readJava("entity", "EntityFoliotJob.java");
        assertTrue(book.contains("SET_STORAGE_CONTROLLER"));
        assertTrue(book.contains("SET_MANAGED_MACHINE"));
        assertTrue(book.contains("configureManageMachinePosition"));
        assertTrue(book.contains("setStorageControllerPos"));
        assertTrue(book.contains("setManagedMachine"));
        assertTrue(entity.contains("updateManageMachine"));
        assertTrue(entity.contains("CapabilityItemHandler.ITEM_HANDLER_CAPABILITY"));
        assertTrue(entity.contains("transferItems(output, storage"));
        assertTrue(entity.contains("transferMatchingItems(storage, machine")
                || entity.contains("transferItems(storage, machine"));
    }

    @Test
    void depositOrderMatchesAndRoundTripsRemainingAmount() {
        ItemStack filter = new ItemStack(Items.IRON_INGOT, 32);
        DepositOrder order = new DepositOrder(filter, 12);
        assertTrue(order.matches(new ItemStack(Items.IRON_INGOT, 1)));
        assertEquals(12, order.getAmount());
        assertEquals(5, order.take(5));
        assertEquals(7, order.getAmount());

        DepositOrder decoded = DepositOrder.from(order.writeToNBT(new NBTTagCompound()));
        assertTrue(decoded != null);
        assertEquals(7, decoded.getAmount());
        assertTrue(decoded.matches(new ItemStack(Items.IRON_INGOT, 3)));
    }

    @Test
    void depositOrderTracksBoundedRetriesAndCompletionSafely() {
        DepositOrder order = new DepositOrder(new ItemStack(Items.IRON_INGOT), 4);
        assertEquals(DepositOrder.Status.PENDING, order.getStatus());
        for (int attempt = 0; attempt < DepositOrder.DEFAULT_MAX_RETRIES - 1; attempt++) {
            assertTrue(order.recordFailure());
        }
        assertTrue(order.isComplete() == false);
        assertTrue(!order.recordFailure());
        assertTrue(order.isExhausted());
        assertTrue(order.isComplete());

        DepositOrder successful = new DepositOrder(new ItemStack(Items.GOLD_INGOT), 4);
        successful.recordSuccess(3);
        assertEquals(1, successful.getAmount());
        assertEquals(DepositOrder.Status.PENDING, successful.getStatus());
        successful.recordSuccess(1);
        assertEquals(DepositOrder.Status.COMPLETE, successful.getStatus());
        assertTrue(DepositOrder.from(successful.writeToNBT(new NBTTagCompound())) == null);
    }

    @Test
    void machineReferenceRequiresLoadedSameDimensionAndBothFaces() {
        MachineReference reference = new MachineReference(new BlockPos(1, 2, 3), 7, EnumFacing.NORTH,
                null, 7, EnumFacing.SOUTH);
        assertTrue(reference.isConfigured());
        assertTrue(!reference.isStrictlyConfigured());
        assertTrue(reference.hasInsertEndpoint());
        assertTrue(!reference.hasExtractEndpoint());
    }

    @Test
    void machineManagerUsesCapacityOrdersAndSameDimensionBoundary() throws IOException {
        String entity = readJava("entity", "EntityFoliotJob.java");
        assertTrue(entity.contains("generateDepositOrders(storage, machine)"));
        assertTrue(entity.contains("machine.getSlotLimit(machineSlot)"));
        assertTrue(entity.contains("isManageMachineSameDimension()"));
        assertTrue(entity.contains("managedMachine.getInsertDimension() != activeDimension"));
        assertTrue(entity.contains("CurrentDepositOrder"));
        assertTrue(entity.contains("DepositOrders"));
        assertTrue(entity.contains("NAVIGATING_TO_MACHINE_OUTPUT"));
        assertTrue(entity.contains("isManageMachineEndpointsLoaded"));
        assertTrue(entity.contains("getMachineHandler(true)"));
        assertTrue(entity.contains("recordFailure"));
    }

    @Test
    void machineOrderPacketRoundTripsAndBoundsClientPayload() {
        MachineReference reference = new MachineReference(new BlockPos(4, 5, 6), 7, EnumFacing.UP,
                new BlockPos(8, 9, 10), 7, EnumFacing.DOWN);
        reference.setInsertPosition(reference.getInsertPosition(), 7, EnumFacing.NORTH, "minecraft:furnace");
        reference.setExtractPosition(reference.getExtractPosition(), 7, EnumFacing.SOUTH, "minecraft:chest");
        MessageRequestOrder message = new MessageRequestOrder(new BlockPos(1, 2, 3), 7, reference,
                new ItemStack(Items.IRON_INGOT, 3));
        assertTrue(message.isValid());

        io.netty.buffer.ByteBuf buffer = Unpooled.buffer();
        message.toBytes(buffer);
        MessageRequestOrder decoded = new MessageRequestOrder();
        decoded.fromBytes(buffer);
        assertTrue(decoded.isValid());
        assertEquals(new BlockPos(1, 2, 3), decoded.getStorageControllerPosition());
        assertEquals(7, decoded.getStorageControllerDimension());
        assertEquals(reference.getInsertPosition(), decoded.getTargetMachine().getInsertPosition());
        assertEquals(3, decoded.getStack().getCount());

        assertTrue(!new MessageRequestOrder(new BlockPos(1, 2, 3), 7, reference, ItemStack.EMPTY).isValid());
    }

    @Test
    void machineResourcesAreBilingualAndDummyIsRegistered() throws IOException {
        String items = readJava("registry", "ModItems.java");
        assertTrue(items.contains("BOOK_OF_CALLING_DJINNI_MANAGE_MACHINE"));
        assertTrue(items.contains("RITUAL_DUMMY_SUMMON_DJINNI_MANAGE_MACHINE"));
        assertTrue(Files.exists(ASSET_ROOT.resolve("models/item/book_of_calling_djinni_manage_machine.json")));
        assertTrue(Files.exists(ASSET_ROOT.resolve("models/item/ritual_dummy/summon_djinni_manage_machine.json")));
        String english = readUtf8(ASSET_ROOT.resolve("lang/en_us.lang"));
        String chinese = readUtf8(ASSET_ROOT.resolve("lang/zh_cn.lang"));
        assertTrue(english.contains("item.occultism.book_of_calling_djinni_manage_machine.name="));
        assertTrue(english.contains("item.occultism.ritual_dummy.summon_djinni_manage_machine.tooltip="));
        assertTrue(chinese.contains("item.occultism.book_of_calling_djinni_manage_machine.name="));
        assertTrue(chinese.contains("item.occultism.ritual_dummy.summon_djinni_manage_machine.tooltip="));
    }

    private static JsonObject findRitual(String id) throws IOException {
        JsonArray rituals = new JsonParser().parse(readUtf8(ASSET_ROOT.resolve("rituals/rituals.json"))).getAsJsonArray();
        for (JsonElement element : rituals) {
            JsonObject ritual = element.getAsJsonObject();
            if (id.equals(ritual.get("id").getAsString())) {
                return ritual;
            }
        }
        throw new AssertionError("Missing ritual definition: " + id);
    }

    private static String readJava(String directory, String file) throws IOException {
        return readUtf8(JAVA_ROOT.resolve(directory).resolve(file));
    }

    private static String readUtf8(Path path) throws IOException {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}
