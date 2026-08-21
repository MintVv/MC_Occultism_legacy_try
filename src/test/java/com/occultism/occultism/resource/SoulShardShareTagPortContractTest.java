package com.occultism.occultism.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.occultism.occultism.item.ItemSoulShard;

import net.minecraft.init.Bootstrap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.init.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class SoulShardShareTagPortContractTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        Bootstrap.register();
    }

    @Test
    void shareTagKeepsOnlyCanonicalEntityIdAndDoesNotMutateServerData() {
        ItemSoulShard item = new ItemSoulShard(false);
        ItemStack stack = new ItemStack(item);
        NBTTagCompound entityData = new NBTTagCompound();
        entityData.setString("id", "minecraft:zombie");
        entityData.setString("CustomName", "Private Server Name");
        entityData.setLong("UUIDMost", 123L);
        entityData.setLong("UUIDLeast", 456L);
        ItemSoulShard.setStoredEntity(stack, entityData, "minecraft:zombie", "Ignored");

        NBTTagCompound before = stack.getTagCompound().copy();
        NBTTagCompound share = item.getNBTShareTag(stack);
        assertNotNull(share);
        assertEquals("minecraft:zombie", share.getCompoundTag("entityData").getString("id"));
        assertEquals(1, share.getCompoundTag("entityData").getKeySet().size());
        assertFalse(share.getCompoundTag("entityData").hasKey("CustomName"));
        assertFalse(share.getCompoundTag("entityData").hasKey("UUIDMost"));
        assertEquals(before, stack.getTagCompound(), "share filtering must not mutate server NBT");

        // Forge may call this method repeatedly while comparing/sending stacks.
        assertEquals(share, item.getNBTShareTag(stack));
        share.getCompoundTag("entityData").setString("id", "minecraft:pig");
        assertEquals("minecraft:zombie", item.getNBTShareTag(stack).getCompoundTag("entityData").getString("id"),
                "share tag must be an independent copy");
    }

    @Test
    void legacySoulShardSnapshotsAreFilteredWithoutDroppingLegacyDisplayFields() {
        ItemSoulShard item = new ItemSoulShard(false);
        ItemStack stack = new ItemStack(item);
        NBTTagCompound legacyEntity = new NBTTagCompound();
        legacyEntity.setString("id", "minecraft:pig");
        legacyEntity.setInteger("StateMarker", 42);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setTag("OccultismEntity", legacyEntity);
        tag.setString("EntityId", "minecraft:pig");
        tag.setString("EntityName", "Legacy Pig");
        stack.setTagCompound(tag);

        NBTTagCompound share = item.getNBTShareTag(stack);
        assertNotNull(share);
        assertEquals("minecraft:pig", share.getCompoundTag("OccultismEntity").getString("id"));
        assertEquals(1, share.getCompoundTag("OccultismEntity").getKeySet().size());
        assertEquals("minecraft:pig", share.getString("EntityId"));
        assertEquals("Legacy Pig", share.getString("EntityName"));
        assertEquals(42, stack.getTagCompound().getCompoundTag("OccultismEntity").getInteger("StateMarker"));
    }

    @Test
    void emptySoulShardHasNoShareTag() {
        ItemSoulShard item = new ItemSoulShard(false);
        ItemStack stack = new ItemStack(Items.PAPER);
        assertNull(item.getNBTShareTag(stack));
    }
}
