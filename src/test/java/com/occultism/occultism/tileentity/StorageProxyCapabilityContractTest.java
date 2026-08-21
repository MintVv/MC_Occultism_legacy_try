package com.occultism.occultism.tileentity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.occultism.occultism.api.common.blockentity.IStorageController;
import com.occultism.occultism.api.common.blockentity.IStorageControllerProxy;
import com.occultism.occultism.api.common.data.GlobalBlockPos;
import com.occultism.occultism.container.ContainerStorageController;
import com.occultism.occultism.item.ItemStorageRemote;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

class StorageProxyCapabilityContractTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        Bootstrap.register();
    }

    @Test
    void globalPositionRoundTripsWithoutWorldLookup() {
        GlobalBlockPos position = new GlobalBlockPos(new BlockPos(-12, 64, 31), 7);
        NBTTagCompound encoded = position.serializeNBT();
        GlobalBlockPos decoded = GlobalBlockPos.from(encoded);

        assertEquals(position, decoded);
        assertEquals(7, decoded.getDimension());
        assertEquals(new BlockPos(-12, 64, 31), decoded.getPos());
        assertTrue(decoded.isPresent());
        assertFalse(decoded.isIn(null));

        GlobalBlockPos empty = GlobalBlockPos.from(new NBTTagCompound());
        assertFalse(empty.isPresent());
        assertNull(empty.getPos());
    }

    @Test
    void globalPositionReadsAndWritesTheUpstreamPackedSchema() {
        BlockPos target = new BlockPos(-12, 64, 31);
        NBTTagCompound upstream = new NBTTagCompound();
        upstream.setLong(GlobalBlockPos.TAG_UPSTREAM_POS, target.toLong());
        upstream.setString(GlobalBlockPos.TAG_UPSTREAM_DIMENSION, "minecraft:the_nether");

        GlobalBlockPos decoded = GlobalBlockPos.from(upstream);
        assertEquals(target, decoded.getPos());
        assertEquals(-1, decoded.getDimension());

        NBTTagCompound encoded = decoded.serializeNBT();
        assertEquals(target.toLong(), encoded.getLong(GlobalBlockPos.TAG_UPSTREAM_POS));
        assertEquals("minecraft:the_nether", encoded.getString(GlobalBlockPos.TAG_UPSTREAM_DIMENSION));
        // Legacy keys remain present for 1.12 saves and integrations.
        assertEquals(target.getX(), encoded.getInteger(GlobalBlockPos.TAG_X));
        assertEquals(-1, encoded.getInteger(GlobalBlockPos.TAG_DIMENSION));
    }

    @Test
    void globalPositionRejectsUnknownModernDimensionsInsteadOfFallingBackToOverworld() {
        NBTTagCompound upstream = new NBTTagCompound();
        upstream.setLong(GlobalBlockPos.TAG_UPSTREAM_POS, new BlockPos(1, 2, 3).toLong());
        upstream.setString(GlobalBlockPos.TAG_UPSTREAM_DIMENSION, "example:custom_realm");

        GlobalBlockPos decoded = GlobalBlockPos.from(upstream);
        assertFalse(decoded.isPresent());
        assertNull(decoded.getPos());
    }

    @Test
    void controllerApiUsesTheSameRealHandlerAndSimulationContract() {
        TileEntityStorageController controller = new TileEntityStorageController();
        IStorageController api = controller;
        ItemStack input = new ItemStack(Items.IRON_INGOT, 12);

        assertEquals(TileEntityStorageController.BASE_SLOT_COUNT, api.getMaxSlots());
        assertEquals(0, api.getUsedSlots());
        assertEquals(0, api.insertStack(input, true));
        assertEquals(0, api.getAvailableAmount(stack -> stack.getItem() == Items.IRON_INGOT));
        assertEquals(0, api.getUsedSlots());

        assertEquals(0, api.insertStack(input, false));
        assertEquals(1, api.getUsedSlots());
        assertNotNull(api.getStorageItemHandler());
        List<ItemStack> stacks = api.getStacks();
        assertEquals(1, stacks.size());
        assertEquals(12, stacks.get(0).getCount());

        ItemStack extracted = api.getItemStack(stack -> stack.getItem() == Items.IRON_INGOT, 5, false);
        assertEquals(5, extracted.getCount());
        assertEquals(7, api.getAvailableAmount(stack -> stack.getItem() == Items.IRON_INGOT));
    }

    @Test
    void controllerIsItsOwnLoadedProxy() {
        TileEntityStorageController controller = new TileEntityStorageController();
        IStorageControllerProxy proxy = controller;

        assertSame(controller, proxy.getLinkedStorageController());
        assertNotNull(proxy.getLinkedStorageControllerPosition());
        assertEquals(controller.getPos(), proxy.getLinkedStorageControllerPosition().getPos());
        GlobalBlockPos ignored = new GlobalBlockPos(new BlockPos(1, 2, 3), 4);
        proxy.setLinkedStorageControllerPosition(ignored);
        assertEquals(controller.getPos(), proxy.getLinkedStorageControllerPosition().getPos());
    }

    @Test
    void storageRemoteReadsNestedAndLegacyGlobalReferencesWithoutLoading() {
        ItemStorageRemote remote = new ItemStorageRemote();
        ItemStack stack = new ItemStack(remote);
        GlobalBlockPos reference = new GlobalBlockPos(new BlockPos(-8, 72, 19), 3);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setTag(ItemStorageRemote.TAG_LINKED_STORAGE_CONTROLLER, reference.serializeNBT());
        stack.setTagCompound(tag);

        assertEquals(reference, ItemStorageRemote.getLinkedControllerPosition(stack));
        assertNull(ItemStorageRemote.getStorageController(stack, null));

        NBTTagCompound legacy = new NBTTagCompound();
        legacy.setInteger("StorageControllerX", -8);
        legacy.setInteger("StorageControllerY", 72);
        legacy.setInteger("StorageControllerZ", 19);
        legacy.setInteger("StorageControllerDimension", 3);
        stack.setTagCompound(legacy);
        assertEquals(reference, ItemStorageRemote.getLinkedControllerPosition(stack));

        NBTTagCompound malformedNested = new NBTTagCompound();
        malformedNested.setLong(GlobalBlockPos.TAG_UPSTREAM_POS, reference.getPos().toLong());
        malformedNested.setString(GlobalBlockPos.TAG_UPSTREAM_DIMENSION, "example:missing");
        NBTTagCompound conflicting = legacy.copy();
        conflicting.setTag(ItemStorageRemote.TAG_LINKED_STORAGE_CONTROLLER, malformedNested);
        stack.setTagCompound(conflicting);
        assertNull(ItemStorageRemote.getLinkedControllerPosition(stack),
                "a malformed nested link must not fall back to conflicting legacy coordinates");

        NBTTagCompound conflictingNested = reference.serializeNBT();
        conflictingNested.setLong(GlobalBlockPos.TAG_UPSTREAM_POS, new BlockPos(1, 2, 3).toLong());
        stack.setTagCompound(new NBTTagCompound());
        stack.getTagCompound().setTag(ItemStorageRemote.TAG_LINKED_STORAGE_CONTROLLER, conflictingNested);
        assertNull(ItemStorageRemote.getLinkedControllerPosition(stack),
                "conflicting packed and legacy nested coordinates must be rejected");
    }

    @Test
    void wormholeKeepsReferenceButDoesNotResolveAnUnloadedTarget() {
        IStorageControllerProxy proxy = new TileEntityStableWormhole();
        GlobalBlockPos target = new GlobalBlockPos(new BlockPos(3, 70, -4), 2);

        assertNull(proxy.getLinkedStorageController());
        proxy.setLinkedStorageControllerPosition(target);
        assertEquals(target, proxy.getLinkedStorageControllerPosition());
        // No world is attached, so this must not force a dimension or chunk load.
        assertNull(proxy.getLinkedStorageController());
        assertEquals(target, proxy.getLinkedStorageControllerPosition());

        proxy.setLinkedStorageControllerPosition(null);
        assertNull(proxy.getLinkedStorageControllerPosition());
    }

    @Test
    void wormholeDropLinksRoundTripThroughNestedAndLegacyKeys() {
        TileEntityStableWormhole wormhole = new TileEntityStableWormhole();
        GlobalBlockPos target = new GlobalBlockPos(new BlockPos(-5, 66, 8), -1);
        wormhole.setLinkedStorageControllerPosition(target);

        ItemStack dropped = new ItemStack(Items.PAPER);
        TileEntityStableWormhole.writeLinkToStack(dropped, wormhole);
        assertTrue(TileEntityStableWormhole.hasLinkedController(dropped));
        assertEquals(target, TileEntityStableWormhole.getLinkedControllerPosition(dropped));
        assertTrue(dropped.getTagCompound().hasKey(TileEntityStableWormhole.TAG_LINKED_STORAGE_POSITION, 10));

        TileEntityStableWormhole restored = new TileEntityStableWormhole();
        restored.readLinkFromStack(dropped);
        assertEquals(target, restored.getLinkedStorageControllerPosition());
    }

    @Test
    void remoteContainerRejectsDetachedControllerBeforeCheckingLinks() {
        TileEntityStorageController controller = new TileEntityStorageController();
        ContainerStorageController container = new ContainerStorageController(new InventoryPlayer(null), controller,
                true);

        assertFalse(container.isStorageControllerLoaded());
        assertFalse(container.canInteractWith(null));
    }
}
