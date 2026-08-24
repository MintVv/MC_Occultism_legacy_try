package com.occultism.occultism.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.occultism.occultism.api.common.blockentity.IStorageControllerProxy;
import com.occultism.occultism.api.common.data.GlobalBlockPos;
import com.occultism.occultism.handler.StorageRemoteCapabilityHandler;
import com.occultism.occultism.item.ItemStorageRemote;
import com.occultism.occultism.item.StorageRemoteCapabilityProvider;
import com.occultism.occultism.registry.ModItems;

import net.minecraft.init.Bootstrap;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.items.CapabilityItemHandler;

/** Contract coverage for the loaded-only Storage Remote ItemStack proxy. */
class StorageRemoteCapabilityPortContractTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        Bootstrap.register();
    }

    @Test
    void remoteStackAttachesAnItemHandlerProxyWithoutResolvingAnUnloadedTarget() {
        ItemStack stack = new ItemStack(ModItems.STORAGE_REMOTE);
        StorageRemoteCapabilityProvider provider = new StorageRemoteCapabilityProvider(stack);
        IStorageControllerProxy proxy = provider;
        GlobalBlockPos target = new GlobalBlockPos(new BlockPos(-4, 70, 11), 3);

        assertFalse(provider.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null));
        assertNull(provider.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null));

        proxy.setLinkedStorageControllerPosition(target);
        assertEquals(target, proxy.getLinkedStorageControllerPosition());
        assertFalse(provider.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null));
        assertNull(proxy.getLinkedStorageController());
    }

    @Test
    void proxySetterKeepsLegacyAndNestedLinkSchemasInSyncAndCanClearThem() {
        ItemStack stack = new ItemStack(ModItems.STORAGE_REMOTE);
        StorageRemoteCapabilityProvider provider = new StorageRemoteCapabilityProvider(stack);
        GlobalBlockPos target = new GlobalBlockPos(new BlockPos(1, 64, 2), -1);

        provider.setLinkedStorageControllerPosition(target);
        NBTTagCompound tag = stack.getTagCompound();
        assertTrue(tag.hasKey(ItemStorageRemote.TAG_LINKED_STORAGE_CONTROLLER, 10));
        assertEquals(target.getPos().getX(), tag.getInteger("StorageControllerX"));
        assertEquals(target.getDimension(), tag.getInteger("StorageControllerDimension"));

        provider.setLinkedStorageControllerPosition(null);
        assertNull(ItemStorageRemote.getLinkedControllerPosition(stack));
        assertTrue(stack.getTagCompound() == null || stack.getTagCompound().isEmpty());
    }

    @Test
    void remoteTerminalMatrixAndOrderRoundTripThroughItemNbt() {
        ItemStack stack = new ItemStack(ModItems.STORAGE_REMOTE);
        List<ItemStack> matrix = Arrays.asList(
                new ItemStack(Items.DIAMOND, 300), ItemStack.EMPTY, new ItemStack(Items.EMERALD, 2),
                ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY,
                ItemStack.EMPTY);

        ItemStorageRemote.setCraftingMatrix(stack, matrix);
        ItemStorageRemote.setOrderStack(stack, new ItemStack(Items.ENDER_PEARL, 7));

        List<ItemStack> decoded = ItemStorageRemote.getCraftingMatrix(stack);
        assertEquals(9, decoded.size());
        assertEquals(300, decoded.get(0).getCount());
        assertEquals(Items.EMERALD, decoded.get(2).getItem());
        assertEquals(7, ItemStorageRemote.getOrderStack(stack).getCount());

        ItemStorageRemote.setOrderStack(stack, ItemStack.EMPTY);
        assertTrue(ItemStorageRemote.getOrderStack(stack).isEmpty());
    }

    @Test
    void forgeItemStackAttachmentIsRestrictedToStorageRemoteItems() {
        ItemStack remote = new ItemStack(ModItems.STORAGE_REMOTE);
        AttachCapabilitiesEvent<ItemStack> remoteEvent = new AttachCapabilitiesEvent<>(ItemStack.class, remote);
        StorageRemoteCapabilityHandler.attach(remoteEvent);
        assertEquals(1, remoteEvent.getCapabilities().size());
        assertTrue(remoteEvent.getCapabilities().values().iterator().next()
                instanceof StorageRemoteCapabilityProvider);

        ItemStack ordinary = new ItemStack(net.minecraft.init.Items.PAPER);
        AttachCapabilitiesEvent<ItemStack> ordinaryEvent = new AttachCapabilitiesEvent<>(ItemStack.class, ordinary);
        StorageRemoteCapabilityHandler.attach(ordinaryEvent);
        assertTrue(ordinaryEvent.getCapabilities().isEmpty());
    }

    @Test
    void sourceRegistersItemStackAttachmentAndAllowsNullWorldLoadedLookup() throws Exception {
        String handler = readSource("src", "main", "java", "com", "occultism", "occultism", "handler",
                "StorageRemoteCapabilityHandler.java");
        String provider = readSource("src", "main", "java", "com", "occultism", "occultism", "item",
                "StorageRemoteCapabilityProvider.java");
        String remote = readSource("src", "main", "java", "com", "occultism", "occultism", "item",
                "ItemStorageRemote.java");

        assertTrue(handler.contains("AttachCapabilitiesEvent<ItemStack>"));
        assertTrue(handler.contains("new StorageRemoteCapabilityProvider(stack)"));
        assertTrue(provider.contains("implements IStorageControllerProxy"));
        assertTrue(provider.contains("CapabilityItemHandler.ITEM_HANDLER_CAPABILITY"));
        assertTrue(provider.contains("getStorageController(this.remote, null)"));
        assertTrue(remote.contains("world != null && world.provider.getDimension()"));
        assertTrue(remote.contains("DimensionManager.getWorld(reference.getDimension())"));
        assertTrue(remote.contains("public static void clearLinkedController"));

        String english = readSource("src", "main", "resources", "assets", "occultism", "lang", "en_us.lang");
        String chinese = readSource("src", "main", "resources", "assets", "occultism", "lang", "zh_cn.lang");
        assertTrue(english.contains("exposes the controller's Forge item-handler capability"));
        assertTrue(chinese.contains("暴露控制器的 Forge 物品处理器能力"));
        assertTrue(english.contains("starts with 128 active slots"));
        assertTrue(chinese.contains("从 128 个活动槽位开始"));
        assertFalse(english.contains("exposes a 54-slot inventory through its GUI"));
        assertFalse(chinese.contains("通过 GUI 提供 54 格库存"));
    }

    private static String readSource(String... path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path[0], java.util.Arrays.copyOfRange(path, 1, path.length))),
                StandardCharsets.UTF_8);
    }
}
