package com.occultism.occultism.ritual;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;

import com.occultism.occultism.registry.ModBlocks;
import com.occultism.occultism.tileentity.TileEntitySacrificialBowl;
import net.minecraft.init.Blocks;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RitualLifecycleTest {
    private RitualTestWorld world;
    private TileEntitySacrificialBowl bowl;
    private Field recipes;
    private Object originalRecipes;
    private RitualDefinition definition;

    @BeforeAll
    static void bootstrap() {
        Bootstrap.register();
        if (TileEntity.getKey(TileEntitySacrificialBowl.class) == null) {
            TileEntity.register("occultism:sacrificial_bowl", TileEntitySacrificialBowl.class);
        }
    }

    @BeforeEach
    void setup() throws Exception {
        world = new RitualTestWorld(false);
        world.placeFoliotPentacle(BlockPos.ORIGIN);
        bowl = world.installBowl(BlockPos.ORIGIN, true);
        recipes = RitualRecipeManager.class.getDeclaredField("definitions");
        recipes.setAccessible(true);
        originalRecipes = recipes.get(null);
        configure(false, false, 30);
    }

    @AfterEach
    void restore() throws Exception {
        recipes.set(null, originalRecipes);
    }

    @Test
    void brokenPentacleImmediatelyInterruptsAndReturnsOnlyTheActivationWithItsNbt() {
        ItemStack activation = new ItemStack(Items.BOOK);
        activation.setTagCompound(new NBTTagCompound());
        activation.getTagCompound().setString("boundName", "preserved");
        bowl.startRitual(definition, activation);
        world.states.put(glyph(), Blocks.AIR.getDefaultState());
        bowl.update();
        assertFalse(bowl.isRitualActive());
        assertEquals(1, dropped(Items.BOOK));
        assertEquals(0, dropped(Items.DIAMOND));
        assertEquals("preserved", world.drops.get(0).getItem().getTagCompound().getString("boundName"));
        bowl.update();
        assertEquals(1, dropped(Items.BOOK), "interrupting twice must not duplicate activation");
    }

    @Test
    void missingMaterialsInterruptBeforeWaitingForSacrifice() throws Exception {
        configure(true, false, 30, new RitualIngredient(Items.REDSTONE, 1));
        start();
        assertTrue(bowl.isWaitingForSacrifice());
        bowl.update();
        assertFalse(bowl.isRitualActive());
        assertEquals(1, dropped(Items.BOOK));
        assertEquals(0, dropped(Items.DIAMOND));
    }

    @Test
    void brokenPentacleAlsoInterruptsWhileWaitingForItemUse() throws Exception {
        configure(false, true, 30);
        start();
        assertTrue(bowl.isWaitingForItemUse());
        world.states.put(glyph(), Blocks.AIR.getDefaultState());
        bowl.update();
        assertFalse(bowl.isRitualActive());
        assertEquals(1, dropped(Items.BOOK));
    }

    @Test
    void alreadyConsumedIngredientsAreNotRequiredAgainAfterSaving() throws Exception {
        configure(false, false, 30, new RitualIngredient(Items.REDSTONE, 1),
                new RitualIngredient(Items.GLOWSTONE_DUST, 1));
        TileEntitySacrificialBowl first = material(new BlockPos(6, 0, 0), Items.REDSTONE, 1);
        material(new BlockPos(6, 0, 1), Items.GLOWSTONE_DUST, 1);
        start();
        tick(10);
        assertTrue(bowl.isRitualActive());
        assertTrue(first.isEmpty());
        NBTTagCompound saved = bowl.writeToNBT(new NBTTagCompound());
        assertEquals(1, saved.getInteger("ConsumedAdditionalIngredients"));
        TileEntitySacrificialBowl loaded = world.installBowl(BlockPos.ORIGIN, true);
        loaded.readFromNBT(saved);
        loaded.update();
        assertTrue(loaded.isRitualActive(), "reload must validate only the unconsumed suffix");
        assertEquals(11, loaded.getRitualProgress());
        assertEquals(0, dropped(Items.BOOK));
    }

    @Test
    void losingARemainingMaterialDoesNotRefundAlreadyConsumedCostsOrCreateAResult() throws Exception {
        configure(false, false, 30, new RitualIngredient(Items.REDSTONE, 1),
                new RitualIngredient(Items.GLOWSTONE_DUST, 1));
        TileEntitySacrificialBowl first = material(new BlockPos(6, 0, 0), Items.REDSTONE, 1);
        TileEntitySacrificialBowl second = material(new BlockPos(6, 0, 1), Items.GLOWSTONE_DUST, 1);
        start();
        tick(10);
        assertTrue(first.isEmpty());
        second.removeStack();
        bowl.update();
        assertFalse(bowl.isRitualActive());
        assertEquals(1, dropped(Items.BOOK));
        assertEquals(0, dropped(Items.REDSTONE));
        assertEquals(0, dropped(Items.DIAMOND));
    }

    @Test
    void oneTickAcceleratedRitualStillConsumesAllThreeMaterials() throws Exception {
        configure(false, false, 30, new RitualIngredient(Items.REDSTONE, 1),
                new RitualIngredient(Items.REDSTONE, 1), new RitualIngredient(Items.REDSTONE, 1));
        TileEntitySacrificialBowl materials = material(new BlockPos(6, 0, 0), Items.REDSTONE, 3);
        bowl.startRitual(definition, new ItemStack(Items.BOOK), 100);
        assertEquals(1, bowl.getRitualDuration());
        bowl.update();
        assertFalse(bowl.isRitualActive());
        assertTrue(materials.isEmpty(), "no accelerated result may bypass an ingredient");
        assertEquals(1, dropped(Items.DIAMOND));
        assertEquals(0, dropped(Items.BOOK));
    }

    @Test
    void ordinaryCompletionConsumesCostsAndCannotRepeatItsResult() throws Exception {
        configure(false, false, 3, new RitualIngredient(Items.REDSTONE, 1));
        TileEntitySacrificialBowl materials = material(new BlockPos(6, 0, 0), Items.REDSTONE, 1);
        start();
        tick(5);
        assertFalse(bowl.isRitualActive());
        assertTrue(materials.isEmpty());
        assertEquals(1, dropped(Items.DIAMOND));
        assertEquals(0, dropped(Items.BOOK));
    }

    @Test
    void removedDefinitionsOrUnknownPentaclesReturnTheActivationRatherThanSilentlyLosingIt() throws Exception {
        start();
        assertEquals(1, RitualRecipeManager.removeById(definition.getId()));
        assertTrue(RitualRecipeManager.getDefinitions().isEmpty());
        bowl.update();
        assertFalse(bowl.isRitualActive());
        assertEquals(1, dropped(Items.BOOK));
        definition = new RitualDefinition("unknown_pentacle", "Unknown", "missing_pentacle",
                new RitualIngredient(Items.BOOK, 1), new RitualResult(new ItemStack(Items.DIAMOND)), 30);
        recipes.set(null, Collections.singletonList(definition));
        start();
        bowl.update();
        assertFalse(bowl.isRitualActive());
        assertEquals(2, dropped(Items.BOOK));
        assertEquals(0, dropped(Items.DIAMOND));
    }

    @Test
    void clearingAllRecipesAlsoInterruptsSavedAndWaitingRitualsWithoutRestoringDefaults() throws Exception {
        configure(true, false, 30);
        start();
        NBTTagCompound saved = bowl.writeToNBT(new NBTTagCompound());
        assertEquals(1, RitualRecipeManager.removeAll());
        bowl.readFromNBT(saved);
        bowl.update();
        assertFalse(bowl.isRitualActive());
        assertTrue(RitualRecipeManager.getDefinitions().isEmpty());
        assertEquals(1, dropped(Items.BOOK));
        assertEquals(0, dropped(Items.DIAMOND));
        bowl.update();
        assertEquals(1, dropped(Items.BOOK));
    }

    @Test
    void removingOrReplacingActivationNeverCreatesADuplicateOfTheOriginal() {
        start();
        assertEquals(Items.BOOK, bowl.removeStack().getItem());
        bowl.update();
        assertFalse(bowl.isRitualActive());
        assertTrue(world.drops.isEmpty());
        start();
        bowl.setStack(new ItemStack(Items.GOLD_INGOT));
        bowl.update();
        assertFalse(bowl.isRitualActive());
        assertEquals(1, dropped(Items.GOLD_INGOT));
        assertEquals(0, dropped(Items.BOOK));
        assertEquals(0, dropped(Items.DIAMOND));
    }

    @Test
    void unloadedPatternCellsFailWithoutLoadingThemAndHigherChalkSubstitutesStillMatch() {
        world.states.put(glyph(), ModBlocks.CHALK_GLYPH_GRAY.getDefaultState());
        assertTrue(PentacleDetector.matches(world, BlockPos.ORIGIN,
                PentacleDetector.getDefinitionById("summon_foliot")));
        start();
        world.unloaded.add(glyph());
        // The fixture throws if getBlockState is ever called on an unloaded cell.
        assertDoesNotThrow(bowl::update);
        assertFalse(bowl.isRitualActive());
        assertEquals(1, dropped(Items.BOOK));
    }

    @Test
    void malformedConsumedCountIsRejectedAndMatchingNeverMutatesTheProvidedStacks() throws Exception {
        configure(false, false, 30, new RitualIngredient(Items.REDSTONE, 2));
        ItemStack input = new ItemStack(Items.REDSTONE, 3);
        assertTrue(definition.matchesRemainingIngredients(Collections.singletonList(input), 0));
        assertEquals(3, input.getCount());
        assertTrue(definition.matchesRemainingIngredients(Collections.emptyList(), 1));
        assertFalse(definition.matchesRemainingIngredients(Collections.singletonList(input), -1));
        assertFalse(definition.matchesRemainingIngredients(Collections.singletonList(input), 2));
        material(new BlockPos(6, 0, 0), Items.REDSTONE, 2);
        start();
        NBTTagCompound saved = bowl.writeToNBT(new NBTTagCompound());
        saved.setInteger("ConsumedAdditionalIngredients", 99);
        bowl.readFromNBT(saved);
        bowl.update();
        assertFalse(bowl.isRitualActive());
        assertEquals(1, dropped(Items.BOOK));
        assertEquals(0, dropped(Items.DIAMOND));
    }

    @Test
    void actualBowlConsumptionReservesTheExactIngredientThroughSavingAndCompletion() throws Exception {
        configure(false, false, 30, new RitualIngredient(Arrays.asList(
                new RitualIngredient(Items.IRON_INGOT, 1), new RitualIngredient(Items.GOLD_INGOT, 1))),
                new RitualIngredient(Items.IRON_INGOT, 1));
        TileEntitySacrificialBowl iron = material(new BlockPos(6, 0, 0), Items.IRON_INGOT, 1);
        TileEntitySacrificialBowl gold = material(new BlockPos(6, 0, 1), Items.GOLD_INGOT, 1);
        assertTrue(bowl.canStartRitual(world, BlockPos.ORIGIN, definition, new ItemStack(Items.BOOK)));
        start();
        tick(10);
        assertTrue(bowl.isRitualActive());
        assertTrue(gold.isEmpty(), "the broad first cost must consume gold, reserving iron");
        assertEquals(1, iron.getStack().getCount());
        NBTTagCompound saved = bowl.writeToNBT(new NBTTagCompound());
        bowl.readFromNBT(saved);
        tick(20);
        assertFalse(bowl.isRitualActive());
        assertTrue(iron.isEmpty());
        assertEquals(1, dropped(Items.DIAMOND));
        assertEquals(0, dropped(Items.BOOK));
    }

    private void configure(boolean sacrifice, boolean itemUse, int duration, RitualIngredient... ingredients)
            throws Exception {
        RitualResult result = new RitualResult(new ItemStack(Items.DIAMOND));
        definition = new RitualDefinition("lifecycle_test", "Lifecycle", "summon_foliot", "craft",
                new RitualIngredient(Items.BOOK, 1), result, result, duration,
                sacrifice ? new RitualSacrifice("c:cows", "Cow") : null,
                itemUse ? new RitualIngredient(Items.STICK, 1) : null, ingredients);
        recipes.set(null, Arrays.asList(definition));
    }

    private void start() {
        bowl.startRitual(definition, new ItemStack(Items.BOOK));
    }

    private TileEntitySacrificialBowl material(BlockPos pos, Item item, int count) {
        TileEntitySacrificialBowl result = world.installBowl(pos, false);
        result.setStack(new ItemStack(item, count));
        return result;
    }

    private void tick(int count) {
        for (int index = 0; index < count; index++) {
            bowl.update();
        }
    }

    private int dropped(Item item) {
        return world.drops.stream().filter(drop -> drop.getItem().getItem() == item)
                .mapToInt(drop -> drop.getItem().getCount()).sum();
    }

    private BlockPos glyph() {
        return PentacleDetector.getRequiredSymbols(PentacleDetector.getDefinitionById("summon_foliot"),
                BlockPos.ORIGIN).stream().filter(symbol -> symbol.getSymbol() == 'W')
                .findFirst().get().getPos();
    }
}
