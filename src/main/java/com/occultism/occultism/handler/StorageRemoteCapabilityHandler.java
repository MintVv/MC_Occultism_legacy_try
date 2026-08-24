package com.occultism.occultism.handler;

import com.occultism.occultism.Tags;
import com.occultism.occultism.item.ItemStorageRemote;
import com.occultism.occultism.item.StorageRemoteCapabilityProvider;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Attaches the loaded-only item-handler proxy to Storage Remote stacks. */
@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class StorageRemoteCapabilityHandler {

    private static final ResourceLocation KEY = new ResourceLocation(Tags.MOD_ID, "storage_remote_proxy");

    private StorageRemoteCapabilityHandler() {
    }

    @SubscribeEvent
    public static void attach(AttachCapabilitiesEvent<ItemStack> event) {
        ItemStack stack = event.getObject();
        if (stack != null && !stack.isEmpty() && stack.getItem() instanceof ItemStorageRemote) {
            event.addCapability(KEY, new StorageRemoteCapabilityProvider(stack));
        }
    }
}
