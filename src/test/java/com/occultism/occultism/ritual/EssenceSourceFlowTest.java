package com.occultism.occultism.ritual;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.occultism.occultism.entity.EntityAfritWild;
import com.occultism.occultism.entity.EntityMaridWild;
import com.occultism.occultism.entity.EntityPossessedBee;
import com.occultism.occultism.handler.RitualSacrificeHandler;
import com.occultism.occultism.registry.ModBlocks;
import com.occultism.occultism.registry.ModItems;
import com.occultism.occultism.registry.ModOreDictionary;
import com.occultism.occultism.tileentity.TileEntitySacrificialBowl;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityGuardian;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.LootTableManager;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EssenceSourceFlowTest {
    private static final Path ASSETS = Path.of("src/main/resources/assets/occultism");
    private RitualTestWorld world;
    private TestPlayer player;
    private TileEntitySacrificialBowl bowl;
    private RitualDefinition ritual;
    private final List<TileEntitySacrificialBowl> offerings = new ArrayList<>();
    private Field definitions;
    private Object originalDefinitions;

    @BeforeAll
    static void bootstrap() throws Exception {
        RitualBundleLoadTest.bootstrap();
        ModOreDictionary.register();
        if (TileEntity.getKey(TileEntitySacrificialBowl.class) == null) {
            TileEntity.register("occultism:sacrificial_bowl", TileEntitySacrificialBowl.class);
        }
        register("possessed_bee", EntityPossessedBee.class);
        register("afrit_wild", EntityAfritWild.class);
        register("marid_wild", EntityMaridWild.class);
    }

    private static void register(String id, Class<? extends Entity> type) {
        ResourceLocation key = new ResourceLocation("occultism", id);
        if (!ForgeRegistries.ENTITIES.containsKey(key)) {
            ForgeRegistries.ENTITIES.register(new EntityEntry(type, id).setRegistryName(key));
        }
    }

    @BeforeEach
    void load() throws Exception {
        definitions = RitualRecipeManager.class.getDeclaredField("definitions");
        definitions.setAccessible(true);
        originalDefinitions = definitions.get(null);
        RitualRecipeManager.load();
        world = new RitualTestWorld(false);
        player = new TestPlayer(world);
        world.playerEntities.add(player);
    }

    @AfterEach
    void restore() throws Exception {
        RitualSacrificeHandler.onWorldUnload(new WorldEvent.Unload(world));
        definitions.set(null, originalDefinitions);
    }

    @Test
    void beeSourceUsesChickenAndDoesNotRequireItsOwnHoneyProgression() {
        RitualDefinition bee = RitualRecipeManager.getById("possess_bee");
        assertEquals("c:chickens", bee.getSacrifice().getTag());
        Set<net.minecraft.item.Item> blocked = Set.of(ModItems.CURSED_HONEY,
                net.minecraft.item.Item.getItemFromBlock(ModBlocks.BEE_NEST), ModItems.CHALK_ORANGE,
                ModItems.AFRIT_ESSENCE, ModItems.MARID_ESSENCE, ModItems.DEMONIC_MEAT);
        for (RitualIngredient cost : bee.getAdditionalIngredients()) {
            assertFalse(blocked.contains(cost.createDisplayStack().getItem()));
        }
        assertEquals("possess_djinni", bee.getPentacleId());
    }

    @Test
    void chickenSacrificeSummonsBeeAndItsActualDropCraftsOrangeChalk() throws Exception {
        prepare("possess_bee");
        activate();
        assertTrue(bowl.isWaitingForSacrifice());
        tick(25);
        assertEquals(0, bowl.getRitualProgress());
        assertTrue(world.drops.isEmpty());
        sacrifice(new EntityCow(world));
        assertTrue(bowl.isWaitingForSacrifice());
        sacrifice(new EntityChicken(world));
        assertFalse(bowl.isWaitingForSacrifice());
        EntityPossessedBee bee = finish(EntityPossessedBee.class);
        Method drop = EntityPossessedBee.class.getDeclaredMethod("dropFewItems", boolean.class, int.class);
        drop.setAccessible(true);
        drop.invoke(bee, true, 0);
        List<ItemStack> honey = world.drops.stream().map(entity -> entity.getItem())
                .filter(stack -> stack.getItem() == ModItems.CURSED_HONEY).toList();
        assertEquals(1, honey.size());
        assertEquals(1, honey.getFirst().getCount());
        craftChalk("orange", honey.getFirst());
    }

    @Test
    void cowSacrificeSummonsAfritWithEntourageWithoutFreeEssence() throws Exception {
        prepare("summon_unbound_afrit");
        activate();
        tick(30);
        assertTrue(bowl.isWaitingForSacrifice());
        assertEquals(0, bowl.getRitualProgress());
        sacrifice(new EntityChicken(world));
        assertTrue(bowl.isWaitingForSacrifice());
        sacrifice(new EntityCow(world));
        EntityAfritWild afrit = finish(EntityAfritWild.class);
        assertEntourage(EntityBlaze.class);
        ItemStack essence = rolledEssence(afrit, "afrit_wild", ModItems.AFRIT_ESSENCE);
        craftChalk("red", essence);
    }

    @Test
    void eyeUseSummonsMaridWithEntourageAndLootCraftsBlueChalk() throws Exception {
        prepare("summon_unbound_marid");
        activate();
        tick(30);
        assertTrue(bowl.isWaitingForItemUse());
        assertEquals(0, bowl.getRitualProgress());
        assertFalse(bowl.provideItemUse(new ItemStack(Items.ENDER_PEARL)));
        assertTrue(bowl.provideItemUse(new ItemStack(Items.ENDER_EYE)));
        EntityMaridWild marid = finish(EntityMaridWild.class);
        assertEntourage(EntityGuardian.class);
        ItemStack essence = rolledEssence(marid, "marid_wild", ModItems.MARID_ESSENCE);
        craftChalk("blue", essence);
    }

    @Test
    void eachSourcePentacleUsesOnlyEarlierChalkColors() {
        for (String id : List.of("possess_bee", "summon_unbound_afrit", "summon_unbound_marid")) {
            PentacleDefinition pentacle = PentacleDetector.getDefinitionById(
                    RitualRecipeManager.getById(id).getPentacleId());
            String pattern = String.join("", pentacle.getPattern());
            assertFalse(pattern.contains("U"), id + " must not need blue chalk before Marid essence");
            assertFalse(pattern.contains("C"), id + " must not need eldritch cyan chalk");
            if (!id.equals("summon_unbound_marid")) {
                assertFalse(pattern.contains("R"), id + " must not need red chalk before Afrit essence");
            }
            if (id.equals("possess_bee")) {
                assertFalse(pattern.contains("O"), "First honey must precede orange chalk");
                assertFalse(pattern.contains("P"), "First honey must precede pink chalk and demonic meat");
            }
        }
    }

    @Test
    void registeredHostileEntitiesConstructWithTheirAttackSpeedAttributes() {
        for (String id : List.of("afrit_wild", "marid_wild")) {
            EntityLivingBase entity = (EntityLivingBase) net.minecraft.entity.EntityList.createEntityByIDFromName(
                    new ResourceLocation("occultism", id), world);
            assertNotNull(entity, id);
            assertNotNull(entity.getEntityAttribute(net.minecraft.entity.SharedMonsterAttributes.ATTACK_SPEED));
            assertEquals(8.0D, entity.getEntityAttribute(
                    net.minecraft.entity.SharedMonsterAttributes.ATTACK_SPEED).getBaseValue());
        }
    }

    @Test
    void canceledChickenDeathLeavesRealBeeRitualWaitingAndMaterialsUntouched() {
        prepare("possess_bee");
        activate();
        EntityChicken chicken = new EntityChicken(world);
        chicken.setPosition(1, 0, 1);
        chicken.setHealth(0);
        LivingDeathEvent canceled = new LivingDeathEvent(chicken, DamageSource.causePlayerDamage(player)) {
            @Override
            public boolean isCancelable() { return true; }
        };
        RitualSacrificeHandler.onLivingDeath(canceled);
        canceled.setCanceled(true);
        RitualSacrificeHandler.onWorldTick(new TickEvent.WorldTickEvent(Side.SERVER, TickEvent.Phase.END, world));
        tick(ritual.getDurationTicks());
        assertTrue(bowl.isWaitingForSacrifice());
        assertEquals(0, bowl.getRitualProgress());
        assertTrue(offerings.stream().allMatch(offering -> !offering.isEmpty()));
        assertTrue(world.spawned.isEmpty());
    }

    @Test
    void maridWaitingForEyeSurvivesSaveAndFinishesOnlyOnce() {
        prepare("summon_unbound_marid");
        activate();
        tick(30);
        NBTTagCompound saved = bowl.writeToNBT(new NBTTagCompound());
        bowl = world.installBowl(BlockPos.ORIGIN, true);
        bowl.readFromNBT(saved);
        assertTrue(bowl.isWaitingForItemUse());
        assertEquals(0, bowl.getRitualProgress());
        assertTrue(bowl.provideItemUse(new ItemStack(Items.ENDER_EYE)));
        finish(EntityMaridWild.class);
        int spawned = world.spawned.size();
        tick(ritual.getDurationTicks());
        assertEquals(spawned, world.spawned.size());
        assertTrue(world.drops.isEmpty());
    }

    private void prepare(String id) {
        ritual = RitualRecipeManager.getById(id);
        assertNotNull(ritual);
        PentacleDefinition pentacle = PentacleDetector.getDefinitionById(ritual.getPentacleId());
        for (PentacleDetector.PentacleSymbol symbol : PentacleDetector.getRequiredSymbols(pentacle, BlockPos.ORIGIN)) {
            char mark = symbol.getSymbol();
            Block block = switch (mark) {
                case 'W' -> ModBlocks.CHALK_GLYPH_WHITE;
                case 'S' -> ModBlocks.CHALK_GLYPH_LIGHT_GRAY;
                case 'G' -> ModBlocks.CHALK_GLYPH_GRAY;
                case 'K' -> ModBlocks.CHALK_GLYPH_BLACK;
                case 'Y' -> ModBlocks.CHALK_GLYPH_YELLOW;
                case 'L' -> ModBlocks.CHALK_GLYPH_LIME;
                case 'O' -> ModBlocks.CHALK_GLYPH_ORANGE;
                case 'R' -> ModBlocks.CHALK_GLYPH_RED;
                case '1' -> ModBlocks.LARGE_CANDLE;
                case '3' -> ModBlocks.SPIRIT_ATTUNED_CRYSTAL;
                case '2', '4' -> Blocks.SKULL;
                default -> throw new AssertionError("Unexpected symbol " + mark);
            };
            world.states.put(symbol.getPos(), block.getDefaultState());
            if (block == Blocks.SKULL) {
                TileEntitySkull skull = new TileEntitySkull();
                skull.setWorld(world);
                skull.setPos(symbol.getPos());
                skull.setType(mark == '4' ? 1 : 0);
                world.tiles.put(symbol.getPos(), skull);
            }
        }
        bowl = world.installBowl(BlockPos.ORIGIN, true);
        int cost = 0;
        for (int x = -8; x <= 8 && cost < ritual.getAdditionalIngredients().size(); x++) {
            for (int z = -8; z <= 8 && cost < ritual.getAdditionalIngredients().size(); z++) {
                BlockPos pos = new BlockPos(x, 0, z);
                if (world.getBlockState(pos).getBlock() != Blocks.AIR) {
                    continue;
                }
                TileEntitySacrificialBowl offering = world.installBowl(pos, false);
                ItemStack stack = ritual.getAdditionalIngredients().get(cost++).createDisplayStack();
                assertFalse(stack.isEmpty());
                offering.setStack(stack);
                offerings.add(offering);
            }
        }
        assertEquals(ritual.getAdditionalIngredients().size(), cost);
        player.setHeldItem(EnumHand.MAIN_HAND, ritual.getActivationIngredient().createDisplayStack());
        assertTrue(PentacleDetector.matches(world, BlockPos.ORIGIN, pentacle));
    }

    private void activate() {
        ModBlocks.GOLDEN_SACRIFICIAL_BOWL.onBlockActivated(world, BlockPos.ORIGIN,
                world.getBlockState(BlockPos.ORIGIN), player, EnumHand.MAIN_HAND, EnumFacing.UP, 0.5F, 0.5F, 0.5F);
        assertEquals(ritual.getId(), bowl.getActiveRitualId());
        assertTrue(player.getHeldItemMainhand().isEmpty());
    }

    private void sacrifice(EntityLivingBase victim) {
        victim.setPosition(1, 0, 1);
        victim.setHealth(0);
        RitualSacrificeHandler.onLivingDeath(new LivingDeathEvent(victim, DamageSource.causePlayerDamage(player)));
        RitualSacrificeHandler.onWorldTick(new TickEvent.WorldTickEvent(Side.SERVER, TickEvent.Phase.END, world));
    }

    private <T extends Entity> T finish(Class<T> type) {
        tick(ritual.getDurationTicks() - 1);
        assertTrue(world.spawned.isEmpty());
        tick(1);
        assertFalse(bowl.isRitualActive());
        assertTrue(bowl.isEmpty());
        assertTrue(offerings.stream().allMatch(TileEntitySacrificialBowl::isEmpty));
        List<T> results = world.spawned.stream().filter(type::isInstance).map(type::cast).toList();
        assertEquals(1, results.size());
        assertTrue(world.drops.isEmpty(), "Summon recipe result is display-only, not free essence");
        return results.getFirst();
    }

    private void assertEntourage(Class<? extends Entity> type) {
        long count = world.spawned.stream().filter(type::isInstance).count();
        assertTrue(count >= 3 && count <= 8, "Entourage count: " + count);
    }

    private ItemStack rolledEssence(EntityLiving entity, String id, net.minecraft.item.Item expected) throws Exception {
        Method location = entity.getClass().getDeclaredMethod("getLootTable");
        location.setAccessible(true);
        ResourceLocation key = (ResourceLocation) location.invoke(entity);
        assertEquals(new ResourceLocation("occultism", "entities/" + id), key);
        Field gsonField = LootTableManager.class.getDeclaredField("GSON_INSTANCE");
        gsonField.setAccessible(true);
        LootTable table = ForgeHooks.loadLootTable((Gson) gsonField.get(null), key,
                Files.readString(ASSETS.resolve("loot_tables/entities/" + id + ".json")), true, null);
        ItemStack found = ItemStack.EMPTY;
        for (int seed = 0; seed < 128; seed++) {
            LootContext context = new LootContext(0, null, null, entity, player, DamageSource.causePlayerDamage(player)) {
                @Override
                public int getLootingModifier() { return 0; }
            };
            for (ItemStack stack : table.generateLootForPools(new Random(seed), context)) {
                assertEquals(expected, stack.getItem());
                assertEquals(1, stack.getCount());
                found = stack;
            }
        }
        assertFalse(found.isEmpty(), "Loot table never yields its essence");
        return found;
    }

    private void craftChalk(String color, ItemStack source) throws Exception {
        JsonObject json = new JsonParser().parse(Files.readString(ASSETS.resolve(
                "recipes/chalk_" + color + "_impure.json"))).getAsJsonObject();
        InventoryCrafting grid = new InventoryCrafting(new Container() {
            @Override
            public boolean canInteractWith(EntityPlayer player) { return true; }
        }, 3, 3);
        int slot = 0;
        boolean usedSource = false;
        for (JsonElement element : json.getAsJsonArray("ingredients")) {
            JsonObject cost = element.getAsJsonObject();
            ResourceLocation id = new ResourceLocation(cost.get("item").getAsString());
            ItemStack input = new ItemStack(ForgeRegistries.ITEMS.getValue(id), 1,
                    cost.has("data") ? cost.get("data").getAsInt() : 0);
            if (input.getItem() == source.getItem()) {
                input = source.copy();
                usedSource = true;
            }
            grid.setInventorySlotContents(slot++, input);
        }
        assertTrue(usedSource);
        var recipe = CraftingHelper.getRecipe(json, new JsonContext("occultism"));
        assertTrue(recipe.matches(grid, world));
        ItemStack impure = recipe.getCraftingResult(grid);
        ItemStack purified = SpiritFireRecipeRegistry.getResult(impure);
        assertEquals("occultism:chalk_" + color, purified.getItem().getRegistryName().toString());
        assertEquals(1, purified.getCount());
    }

    private void tick(int count) {
        for (int i = 0; i < count; i++) {
            world.setTotalWorldTime(world.getTotalWorldTime() + 1);
            bowl.update();
        }
    }

    private static final class TestPlayer extends EntityPlayer {
        private TestPlayer(World world) {
            super(world, new GameProfile(UUID.randomUUID(), "essence_test"));
        }

        @Override
        public boolean isSpectator() { return false; }

        @Override
        public boolean isCreative() { return false; }
    }
}
