package com.occultism.occultism.ritual;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import com.occultism.occultism.entity.EntityBoundSpirit;
import com.occultism.occultism.entity.EntityFoliotJob;
import com.occultism.occultism.spirit.SpiritJobType;
import com.occultism.occultism.spirit.SpiritTier;
import com.occultism.occultism.tileentity.TileEntitySacrificialBowl;
import com.occultism.occultism.util.ItemNBTUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Drives the real bowl through each ritual type and asserts what actually gets spawned or
 * dropped.
 *
 * <p>The lifecycle tests cover the plumbing (interrupts, refunds, save/restore) using one
 * synthetic recipe. They never check the dispatch table, so a ritual type that silently falls
 * through to "drop the result item" - instead of summoning - would consume the whole offering
 * and not be noticed. That is what this exercises.</p>
 */
class RitualDispatchTest {

    private RitualTestWorld world;
    private TileEntitySacrificialBowl bowl;
    private java.lang.reflect.Field definitionsField;
    private Object originalDefinitions;

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
        definitionsField = RitualRecipeManager.class.getDeclaredField("definitions");
        definitionsField.setAccessible(true);
        originalDefinitions = definitionsField.get(null);
    }

    @AfterEach
    void restore() throws Exception {
        definitionsField.set(null, originalDefinitions);
    }

    @Test
    void aCraftRitualDropsItsResultItem() {
        RitualDefinition definition = definition("craft_dispatch", "craft", null, null, null, 20);
        start(definition);

        assertTrue(dropped(Items.DIAMOND) > 0,
                "A craft ritual must drop its result; spawned=" + world.spawned.size());
    }

    @Test
    void aJobRitualSpawnsAWorkingSpiritInsteadOfDroppingTheResult() {
        RitualDefinition definition = definition("job_dispatch", "summon_spirit_with_job",
                SpiritJobType.CLEANER, SpiritTier.FOLIOT, null, 20);
        start(definition);

        EntityFoliotJob spirit = firstOf(world.spawned, EntityFoliotJob.class);
        assertTrue(spirit != null, "A job ritual must spawn a Foliot job spirit");
        assertEquals(SpiritJobType.CLEANER, spirit.getJobType());
        assertEquals(0, dropped(Items.DIAMOND),
                "A summoning ritual must not also drop the display result item");
    }

    @Test
    void aTieredSummonSpawnsABoundSpiritOfThatTier() {
        RitualDefinition definition = definition("tier_dispatch", "summon", null, SpiritTier.AFRIT, null, 20);
        start(definition);

        EntityBoundSpirit spirit = firstOf(world.spawned, EntityBoundSpirit.class);
        assertTrue(spirit != null, "A tiered summon must spawn a bound spirit");
        assertEquals(SpiritTier.AFRIT, spirit.getTier());
        assertEquals(0, dropped(Items.DIAMOND));
    }

    @Test
    void anExplicitEntitySummonTakesPriorityOverTheTier() {
        // Upstream relies on this for the unbound Afrit/Marid rituals, whose spirit_tier is
        // metadata only. Entity instances are created through EntityList, which is empty in a
        // unit test, so assert the branch that must NOT run: with entity_to_summon present the
        // tier path has to stay silent even though the configured entity cannot be built here.
        RitualDefinition definition = definition("entity_dispatch", "summon", null, SpiritTier.AFRIT,
                new RitualEntityToSummon(new ResourceLocation("occultism", "afrit_wild"), 1), 20);
        start(definition);

        assertTrue(firstOf(world.spawned, EntityBoundSpirit.class) == null,
                "entity_to_summon must win over spirit_tier; spawned=" + describe(world.spawned));
    }

    @Test
    void aWildHuntNeverDropsItsDisplayResult() {
        // The three bosses are built through EntityList, so they cannot be counted here; what is
        // still verifiable is that the recipe result stays display-only for this ritual type.
        RitualDefinition definition = definition("hunt_dispatch", "summon_wild_hunt", null, null,
                new RitualEntityToSummon(new ResourceLocation("occultism", "afrit_wild"), 1), 20);
        start(definition);

        assertEquals(0, dropped(Items.DIAMOND), "The Wild Hunt recipe result is display only");
        assertTrue(firstOf(world.spawned, EntityBoundSpirit.class) == null,
                "A Wild Hunt must not fall through to the bound-spirit branch");
    }

    @Test
    void aNamingRitualCarriesTheBoundSpiritNameToItsResult() {
        startWithNamedActivation(definition("naming_dispatch", "craft_with_spirit_name", null, null, null, 20));

        ItemStack result = firstDrop(Items.DIAMOND);
        assertTrue(result != null, "The naming ritual must drop its result");
        assertEquals("Astaroth", ItemNBTUtil.getBoundSpiritName(result),
                "craft_with_spirit_name must copy the activation's bound spirit name onto the result");
    }

    @Test
    void aMinerSpiritRitualAlsoCarriesTheName() {
        startWithNamedActivation(definition("miner_dispatch", "craft_miner_spirit", null, null, null, 20));

        ItemStack result = firstDrop(Items.DIAMOND);
        assertTrue(result != null, "The miner spirit ritual must drop its result");
        assertEquals("Astaroth", ItemNBTUtil.getBoundSpiritName(result),
                "craft_miner_spirit must copy the bound spirit name as well");
    }

    @Test
    void anOrdinaryCraftRitualDoesNotCopyTheName() {
        // Only the naming ritual types may transfer the name; a plain craft must leave the
        // result untouched or every crafted item would inherit whatever the player held.
        startWithNamedActivation(definition("plain_dispatch", "craft", null, null, null, 20));

        ItemStack result = firstDrop(Items.DIAMOND);
        assertTrue(result != null, "The craft ritual must drop its result");
        assertEquals("", ItemNBTUtil.getBoundSpiritName(result),
                "A craft ritual must not copy the bound spirit name");
    }

    @Test
    void upgradingARitualSatchelKeepsItsContents() {
        // The upgrade consumes the tier-1 satchel as an ingredient, so the result has to inherit
        // its NBT while the bowl still holds it. Losing it would silently delete everything the
        // player stored in the satchel.
        NBTTagCompound stored = new NBTTagCompound();
        stored.setString("Marker", "keep-me");
        ItemStack source = new ItemStack(com.occultism.occultism.registry.ModItems.RITUAL_SATCHEL_T1);
        source.setTagCompound(stored.copy());
        // Outside the 9x9 foliot pattern but still inside the bowl scan radius.
        TileEntitySacrificialBowl satellite = world.installBowl(new BlockPos(6, 0, 6), false);
        satellite.setStack(source);

        start(resultDefinition("craft_upgrade_ritual_satchel",
                new ItemStack(com.occultism.occultism.registry.ModItems.RITUAL_SATCHEL_T2)));

        ItemStack result = firstDrop(com.occultism.occultism.registry.ModItems.RITUAL_SATCHEL_T2);
        assertTrue(result != null, "The satchel upgrade must drop the tier-2 satchel");
        assertTrue(result.hasTagCompound(), "The upgraded satchel must keep its NBT");
        assertEquals("keep-me", result.getTagCompound().getString("Marker"),
                "The upgraded satchel must inherit the source satchel's contents");
    }

    @Test
    void aRepairRitualReturnsTheActivationItemAtFullDurability() {
        ItemStack damaged = new ItemStack(Items.IRON_PICKAXE);
        damaged.setItemDamage(damaged.getMaxDamage() / 2);
        damaged.setTagCompound(new NBTTagCompound());
        damaged.getTagCompound().setString("Marker", "keep-me");

        startWithActivation(repairDefinition(), damaged);

        ItemStack result = firstDrop(Items.IRON_PICKAXE);
        assertTrue(result != null, "The repair ritual must give the item back");
        assertEquals(0, result.getItemDamage(), "The returned item must be fully repaired");
        assertEquals("keep-me", result.getTagCompound().getString("Marker"),
                "Repairing must keep the item's data, e.g. enchantments");
        assertTrue(bowl.getStack().isEmpty(), "The damaged original must be consumed");
    }

    @Test
    void aRepairRitualOnAnUndamagedItemStillReturnsIt() {
        // The repaired copy is dropped from createRepairResult; if that ever stops running the
        // bowl would clear the activation and eat the item outright.
        ItemStack pristine = new ItemStack(Items.IRON_PICKAXE);

        startWithActivation(repairDefinition(), pristine);

        ItemStack result = firstDrop(Items.IRON_PICKAXE);
        assertTrue(result != null, "Even an undamaged item must come back");
        assertEquals(0, result.getItemDamage());
    }

    // -- helpers -----------------------------------------------------------------------------

    private RitualDefinition repairDefinition() {
        ItemStack placeholder = new ItemStack(Items.DIAMOND);
        return new RitualDefinition("repair_dispatch", "repair_dispatch", null, "summon_foliot", "repair",
                new RitualIngredient(Items.IRON_PICKAXE, 1), new RitualResult(placeholder),
                new RitualResult(placeholder), 20, null, null, null, null, null, -1);
    }

    private void startWithActivation(RitualDefinition definition, ItemStack activation) {
        try {
            definitionsField.set(null, Arrays.asList(definition));
        } catch (IllegalAccessException exception) {
            throw new AssertionError(exception);
        }
        bowl.startRitual(definition, activation);
        for (int index = 0; index < 40; index++) {
            bowl.update();
        }
        assertFalse(bowl.isRitualActive(), "The ritual should have finished within the tick budget");
    }

    private RitualDefinition resultDefinition(String id, ItemStack result) {
        return new RitualDefinition(id, id, null, "summon_foliot", "craft",
                new RitualIngredient(Items.BOOK, 1), new RitualResult(result), new RitualResult(result), 20,
                null, null, null, null, null, -1);
    }

    private void startWithNamedActivation(RitualDefinition definition) {
        ItemStack activation = new ItemStack(Items.BOOK);
        activation.setTagCompound(new NBTTagCompound());
        ItemNBTUtil.setBoundSpiritName(activation, "Astaroth");
        try {
            definitionsField.set(null, Arrays.asList(definition));
        } catch (IllegalAccessException exception) {
            throw new AssertionError(exception);
        }
        bowl.startRitual(definition, activation);
        for (int index = 0; index < 40; index++) {
            bowl.update();
        }
        assertFalse(bowl.isRitualActive(), "The ritual should have finished within the tick budget");
    }

    private ItemStack firstDrop(net.minecraft.item.Item item) {
        for (EntityItem drop : world.drops) {
            if (drop.getItem().getItem() == item) {
                return drop.getItem();
            }
        }
        return null;
    }

    private RitualDefinition definition(String id, String ritualType, SpiritJobType job, SpiritTier tier,
            RitualEntityToSummon summon, int duration) {
        RitualResult result = new RitualResult(new ItemStack(Items.DIAMOND));
        return new RitualDefinition(id, id, null, "summon_foliot", ritualType,
                new RitualIngredient(Items.BOOK, 1), result, result, duration,
                null, null, summon, job, tier, -1);
    }

    private void start(RitualDefinition definition) {
        try {
            definitionsField.set(null, Arrays.asList(definition));
        } catch (IllegalAccessException exception) {
            throw new AssertionError(exception);
        }
        bowl.startRitual(definition, new ItemStack(Items.BOOK));
        for (int index = 0; index < 40; index++) {
            bowl.update();
        }
        assertFalse(bowl.isRitualActive(), "The ritual should have finished within the tick budget");
    }

    private static <T> T firstOf(List<Entity> entities, Class<T> type) {
        for (Entity entity : entities) {
            if (type.isInstance(entity)) {
                return type.cast(entity);
            }
        }
        return null;
    }

    private static String describe(List<Entity> entities) {
        StringBuilder builder = new StringBuilder();
        for (Entity entity : entities) {
            builder.append(entity.getClass().getSimpleName()).append(' ');
        }
        return builder.toString().trim();
    }

    private int dropped(net.minecraft.item.Item item) {
        return world.drops.stream().filter(drop -> drop.getItem().getItem() == item)
                .mapToInt(drop -> drop.getItem().getCount()).sum();
    }
}
