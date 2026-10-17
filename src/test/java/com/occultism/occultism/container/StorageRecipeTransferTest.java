package com.occultism.occultism.container;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.mojang.authlib.GameProfile;
import com.occultism.occultism.api.common.data.GlobalBlockPos;
import com.occultism.occultism.item.ItemStorageRemote;
import com.occultism.occultism.registry.ModBlocks;
import com.occultism.occultism.tileentity.TileEntityStorageController;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.profiler.Profiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.crafting.IngredientNBT;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistryModifiable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StorageRecipeTransferTest {
    private final List<ResourceLocation> registered = new ArrayList<>();
    private TestWorld world;
    private TestPlayer player;
    private TileEntityStorageController controller;
    private ContainerStorageController container;

    @BeforeAll
    static void bootstrap() {
        Bootstrap.register();
    }

    @BeforeEach
    void setup() {
        world = new TestWorld(false);
        controller = new TileEntityStorageController();
        controller.setStorageSlotCount(8);
        controller.setWorld(world);
        controller.setPos(BlockPos.ORIGIN);
        world.controller = controller;
        player = new TestPlayer(world);
        player.setPosition(0.5, 0.5, 0.5);
        container = new ContainerStorageController(player.inventory, controller);
    }

    @AfterEach
    void removeRecipes() {
        IForgeRegistryModifiable<IRecipe> registry = (IForgeRegistryModifiable<IRecipe>) ForgeRegistries.RECIPES;
        for (ResourceLocation id : registered) {
            registry.remove(id);
        }
    }

    @Test
    void shapedDimensionsExpandToThreeColumnsAndKeepInternalHoles() {
        Ingredient iron = Ingredient.fromItem(Items.IRON_INGOT);
        Ingredient gold = Ingredient.fromItem(Items.GOLD_INGOT);
        NonNullList<Ingredient> matrix = StorageRecipeLayout.getMatrixIngredients(new ShapedRecipes("",
                2, 2, ingredients(iron, Ingredient.EMPTY, gold, iron), new ItemStack(Items.DIAMOND)));
        assertSame(iron, matrix.get(0));
        assertSame(Ingredient.EMPTY, matrix.get(1));
        assertSame(Ingredient.EMPTY, matrix.get(2));
        assertSame(gold, matrix.get(3));
        assertSame(iron, matrix.get(4));
        assertSame(Ingredient.EMPTY, matrix.get(5));
        assertSame(Ingredient.EMPTY, matrix.get(8));
        assertNull(StorageRecipeLayout.getMatrixIngredients(new ShapedRecipes("", 4, 1,
                ingredients(iron, iron, iron, iron), new ItemStack(Items.DIAMOND))));
        assertNull(StorageRecipeLayout.getMatrixIngredients(new ShapedRecipes("", 2, 2,
                ingredients(iron), new ItemStack(Items.DIAMOND))));
        assertNull(StorageRecipeLayout.getMatrixIngredients(new ShapelessRecipes("",
                new ItemStack(Items.DIAMOND), ingredients())));
    }

    @Test
    void registeredIngredientsUseAlternativesPastTheOldFiveItemLimitAndConsumeOne() {
        ResourceLocation id = register(shapeless(
                Ingredient.fromItem(Items.IRON_INGOT), Ingredient.fromItem(Items.GOLD_INGOT),
                Ingredient.fromItem(Items.IRON_INGOT), Ingredient.fromItem(Items.GOLD_INGOT),
                Ingredient.fromItem(Items.IRON_INGOT), Ingredient.fromItem(Items.GOLD_INGOT)));
        player.inventory.setInventorySlotContents(0, new ItemStack(Items.IRON_INGOT, 20));
        controller.setInventorySlotContents(0, new ItemStack(Items.GOLD_INGOT, 60));
        assertTrue(container.setRecipeById(id, player));
        assertEquals(Items.IRON_INGOT, matrix(0).getItem(), "player inventory takes priority over storage");
        assertEquals(Items.GOLD_INGOT, matrix(1).getItem());
        assertEquals(Items.IRON_INGOT, matrix(2).getItem());
        assertEquals(1, matrix(0).getCount());
        assertEquals(17, player.inventory.getStackInSlot(0).getCount());
        assertEquals(57, controller.getAvailableAmount(stack -> stack.getItem() == Items.GOLD_INGOT));
        assertEquals(Items.DIAMOND, container.getCraftResultInventory().getStackInSlot(0).getItem());
        assertEquals(Items.IRON_INGOT, controller.getCraftingMatrixSlot(0).getItem());
        assertFalse(container.isRecipeLocked());
    }

    @Test
    void storageRefillsMostAbundantMatchingVariantWithoutUsingTheSearchFilter() {
        ResourceLocation id = register(shapeless(Ingredient.fromItems(Items.GOLD_INGOT, Items.IRON_INGOT)));
        controller.setInventorySlotContents(0, new ItemStack(Items.GOLD_INGOT, 5));
        controller.setInventorySlotContents(1, new ItemStack(Items.IRON_INGOT, 30));
        controller.setSearchQuery("nothing_should_match_this_query");
        assertTrue(container.setRecipeById(id, player));
        assertEquals(Items.IRON_INGOT, matrix(0).getItem());
        assertEquals(29, controller.getAvailableAmount(stack -> stack.getItem() == Items.IRON_INGOT));
        assertEquals(5, controller.getAvailableAmount(stack -> stack.getItem() == Items.GOLD_INGOT));
    }

    @Test
    void shapedTransferPlacesEachCostOnceAndMissingCostsDoNotMaterialize() {
        ResourceLocation id = register(new ShapedRecipes("", 1, 3,
                ingredients(Ingredient.fromItem(Items.IRON_INGOT), Ingredient.fromItem(Items.GOLD_INGOT),
                        Ingredient.fromItem(Items.IRON_INGOT)), new ItemStack(Items.DIAMOND)));
        controller.setInventorySlotContents(0, new ItemStack(Items.IRON_INGOT, 2));
        assertTrue(container.setRecipeById(id, player));
        assertEquals(Items.IRON_INGOT, matrix(0).getItem());
        assertTrue(matrix(3).isEmpty());
        assertEquals(Items.IRON_INGOT, matrix(6).getItem());
        assertEquals(0, controller.getAvailableAmount(stack -> stack.getItem() == Items.IRON_INGOT));
        assertTrue(container.getCraftResultInventory().getStackInSlot(0).isEmpty());
    }

    @Test
    void nbtIngredientPredicateIsPreservedInsteadOfFlatteningItemExamples() {
        ItemStack template = new ItemStack(Items.IRON_INGOT);
        template.setTagCompound(new NBTTagCompound());
        template.getTagCompound().setString("binding", "correct");
        ResourceLocation id = register(shapeless(new ExactNbtIngredient(template)));
        player.inventory.setInventorySlotContents(0, new ItemStack(Items.IRON_INGOT, 4));
        ItemStack tagged = template.copy();
        tagged.setCount(8);
        controller.setInventorySlotContents(0, tagged);
        assertTrue(container.setRecipeById(id, player));
        assertEquals("correct", matrix(0).getTagCompound().getString("binding"));
        assertEquals(4, player.inventory.getStackInSlot(0).getCount());
        assertEquals(7, controller.getStackInSlot(0).getCount());
    }

    @Test
    void unknownOversizedAndLockedRecipesLeaveExistingMatrixUntouched() throws Exception {
        container.getCraftMatrix().setInventorySlotContents(0, new ItemStack(Items.STICK, 3));
        assertFalse(container.setRecipeById(new ResourceLocation("occultism_test:not_registered"), player));
        ResourceLocation tooLarge = register(new ShapedRecipes("", 4, 1,
                ingredients(Ingredient.fromItem(Items.IRON_INGOT), Ingredient.fromItem(Items.IRON_INGOT),
                        Ingredient.fromItem(Items.IRON_INGOT), Ingredient.fromItem(Items.IRON_INGOT)),
                new ItemStack(Items.DIAMOND)));
        assertFalse(container.setRecipeById(tooLarge, player));
        ResourceLocation valid = register(shapeless(Ingredient.fromItem(Items.IRON_INGOT)));
        Field lock = ContainerStorageController.class.getDeclaredField("recipeLocked");
        lock.setAccessible(true);
        lock.setBoolean(container, true);
        try {
            assertFalse(container.setRecipeById(valid, player));
        } finally {
            lock.setBoolean(container, false);
        }
        assertEquals(3, matrix(0).getCount());
        assertEquals(Items.STICK, matrix(0).getItem());
        assertEquals(0, controller.getUsedSlots());
    }

    @Test
    void detachedUnloadedOutOfRangeAndClientContainersCannotApplyRecipeIds() {
        ResourceLocation id = register(shapeless(Ingredient.fromItem(Items.IRON_INGOT)));
        player.inventory.setInventorySlotContents(0, new ItemStack(Items.IRON_INGOT, 4));
        world.loaded = false;
        assertFalse(container.setRecipeById(id, player));
        world.loaded = true;
        world.controller = new TileEntityStorageController();
        assertFalse(container.setRecipeById(id, player));
        world.controller = controller;
        player.setPosition(30, 0, 0);
        assertFalse(container.setRecipeById(id, player));
        player.setPosition(0.5, 0.5, 0.5);
        TestWorld client = new TestWorld(true);
        controller.setWorld(client);
        client.controller = controller;
        assertFalse(container.setRecipeById(id, player));
        assertEquals(4, player.inventory.getStackInSlot(0).getCount());
        assertTrue(matrix(0).isEmpty());
    }

    @Test
    void mainInventoryTransferNeverConsumesArmorOffhandOrTheLockedRemote() {
        ItemStorageRemote remoteItem = new ItemStorageRemote();
        ItemStack remote = new ItemStack(remoteItem);
        ItemStorageRemote.setLinkedController(remote, new GlobalBlockPos(BlockPos.ORIGIN, 0));
        player.inventory.setInventorySlotContents(0, remote);
        player.inventory.armorInventory.set(0, new ItemStack(Items.IRON_INGOT));
        player.inventory.offHandInventory.set(0, new ItemStack(Items.GOLD_INGOT));
        ResourceLocation id = register(shapeless(Ingredient.fromItems(remoteItem, Items.IRON_INGOT, Items.GOLD_INGOT)));
        container = new ContainerStorageController(player.inventory, controller, 0);
        assertTrue(container.setRecipeById(id, player));
        assertTrue(matrix(0).isEmpty());
        assertSame(remote, player.inventory.getStackInSlot(0));
        assertEquals(1, player.inventory.armorInventory.get(0).getCount());
        assertEquals(1, player.inventory.offHandInventory.get(0).getCount());
        assertTrue(ItemStorageRemote.getCraftingMatrix(remote).get(0).isEmpty());
    }

    private ItemStack matrix(int slot) {
        return container.getCraftMatrix().getStackInSlot(slot);
    }

    private ResourceLocation register(IRecipe recipe) {
        ResourceLocation id = new ResourceLocation("occultism_test", "recipe_transfer_" + registered.size());
        recipe.setRegistryName(id);
        ForgeRegistries.RECIPES.register(recipe);
        registered.add(id);
        return id;
    }

    private static ShapelessRecipes shapeless(Ingredient... ingredients) {
        return new ShapelessRecipes("", new ItemStack(Items.DIAMOND), ingredients(ingredients));
    }

    private static NonNullList<Ingredient> ingredients(Ingredient... ingredients) {
        NonNullList<Ingredient> result = NonNullList.create();
        java.util.Collections.addAll(result, ingredients);
        return result;
    }

    private static final class ExactNbtIngredient extends IngredientNBT {
        private ExactNbtIngredient(ItemStack stack) { super(stack); }
    }

    private static final class TestPlayer extends EntityPlayer {
        private TestPlayer(World world) {
            super(world, new GameProfile(UUID.randomUUID(), "storage_recipe_test"));
        }
        @Override
        public boolean isSpectator() { return false; }
        @Override
        public boolean isCreative() { return false; }
    }

    private static final class TestWorld extends World {
        private TileEntityStorageController controller;
        private boolean loaded = true;

        private TestWorld(boolean client) {
            super(null, new WorldInfo(new NBTTagCompound()), new WorldProviderSurface(), new Profiler(), client);
            this.getWorldInfo().setDifficulty(EnumDifficulty.NORMAL);
        }
        @Override
        protected IChunkProvider createChunkProvider() { return null; }
        @Override
        protected boolean isChunkLoaded(int x, int z, boolean allowEmpty) { return loaded; }
        @Override
        public boolean isBlockLoaded(BlockPos pos) { return loaded; }
        @Override
        public TileEntity getTileEntity(BlockPos pos) { return BlockPos.ORIGIN.equals(pos) ? controller : null; }
        @Override
        public IBlockState getBlockState(BlockPos pos) {
            if (!loaded) {
                throw new AssertionError("Unexpected unloaded-block access");
            }
            return BlockPos.ORIGIN.equals(pos) ? ModBlocks.STORAGE_CONTROLLER.getDefaultState()
                    : Blocks.AIR.getDefaultState();
        }
        @Override
        public BlockPos getSpawnPoint() { return BlockPos.ORIGIN; }
        @Override
        public void markChunkDirty(BlockPos pos, TileEntity tile) { }
        @Override
        public void updateComparatorOutputLevel(BlockPos pos, Block block) { }
    }
}
