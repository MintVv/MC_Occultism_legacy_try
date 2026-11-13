package com.occultism.occultism.ritual;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.mojang.authlib.GameProfile;
import com.occultism.occultism.config.ModConfig;
import com.occultism.occultism.entity.EntityFoliotJob;
import com.occultism.occultism.item.ItemBookOfCallingFoliotJob;
import com.occultism.occultism.registry.ModBlocks;
import com.occultism.occultism.registry.ModItems;
import com.occultism.occultism.registry.ModOreDictionary;
import com.occultism.occultism.spirit.SpiritJobType;
import com.occultism.occultism.spirit.SpiritTier;
import com.occultism.occultism.tileentity.TileEntitySacrificialBowl;
import com.occultism.occultism.util.ItemNBTUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FoliotCrusherFlowTest {
    private CrusherWorld world;
    private TestPlayer player;
    private TileEntitySacrificialBowl bowl;
    private final List<TileEntitySacrificialBowl> offerings = new ArrayList<>();
    private RitualDefinition ritual;
    private Object originalDefinitions;
    private Field definitions;
    private double oldTimeMultiplier;
    private double oldOutputMultiplier;
    private int oldOperationCount;

    @BeforeAll
    static void bootstrap() throws Exception {
        RitualBundleLoadTest.bootstrap();
        ModOreDictionary.register();
        if (TileEntity.getKey(TileEntitySacrificialBowl.class) == null) {
            TileEntity.register("occultism:sacrificial_bowl", TileEntitySacrificialBowl.class);
        }
    }

    @BeforeEach
    void setup() throws Exception {
        definitions = RitualRecipeManager.class.getDeclaredField("definitions");
        definitions.setAccessible(true);
        originalDefinitions = definitions.get(null);
        RitualRecipeManager.load();
        ritual = RitualRecipeManager.getById("summon_foliot_crusher");
        assertNotNull(ritual);
        world = new CrusherWorld();
        world.placeFoliotPentacle(BlockPos.ORIGIN);
        bowl = world.installBowl(BlockPos.ORIGIN, true);
        Item[] metals = {Items.IRON_INGOT, Items.GOLD_INGOT, ModItems.COPPER_INGOT, ModItems.SILVER_INGOT};
        for (int i = 0; i < metals.length; i++) {
            TileEntitySacrificialBowl offering = world.installBowl(new BlockPos(6, 0, i - 2), false);
            offering.setStack(new ItemStack(metals[i]));
            offerings.add(offering);
        }
        player = new TestPlayer(world);
        world.playerEntities.add(player);
        ItemStack activation = new ItemStack(ModItems.BOOK_OF_BINDING_BOUND_FOLIOT);
        ItemNBTUtil.setBoundSpiritName(activation, "Astaroth");
        player.setHeldItem(EnumHand.MAIN_HAND, activation);
        var settings = ModConfig.SPIRIT_JOBS.crusherFoliot;
        oldTimeMultiplier = settings.timeMultiplier;
        oldOutputMultiplier = settings.outputMultiplier;
        oldOperationCount = settings.operationCount;
        settings.timeMultiplier = 2.0D;
        settings.outputMultiplier = 1.0D;
        settings.operationCount = 1;
    }

    @AfterEach
    void restore() throws Exception {
        definitions.set(null, originalDefinitions);
        var settings = ModConfig.SPIRIT_JOBS.crusherFoliot;
        settings.timeMultiplier = oldTimeMultiplier;
        settings.outputMultiplier = oldOutputMultiplier;
        settings.operationCount = oldOperationCount;
    }

    @Test
    void rightClickConsumesTheRealOfferingsAndCreatesOneNamedLinkedCrusher() throws Exception {
        EntityFoliotJob spirit = summon();
        ItemStack book = onlyCallingBook();
        assertEquals(SpiritJobType.CRUSHER, spirit.getJobType());
        assertEquals(SpiritTier.FOLIOT, spirit.getTier());
        assertEquals("Astaroth", spirit.getCustomNameTag());
        assertEquals(spirit.getUniqueID(), ItemNBTUtil.getSpiritEntityUUID(book));
        assertEquals("Astaroth", ItemNBTUtil.getBoundSpiritName(book));
        ItemBookOfCallingFoliotJob item = (ItemBookOfCallingFoliotJob) book.getItem();
        assertEquals(SpiritJobType.CRUSHER, item.getJobType());
        assertEquals(SpiritTier.FOLIOT, item.getSpiritTier());
        assertFalse(ItemBookOfCallingFoliotJob.hasStoredSpirit(book));
        assertTrue(player.getHeldItemMainhand().isEmpty());
        assertTrue(bowl.isEmpty());
        assertTrue(offerings.stream().allMatch(TileEntitySacrificialBowl::isEmpty));
        assertEquals(1, spirits().size());
        assertEquals(1, world.drops.size());
        tickBowl(ritual.getDurationTicks());
        assertEquals(1, spirits().size());
        assertEquals(1, world.drops.size());
    }

    @Test
    void missingCopperCannotConsumeTheBookOrStartAnotherRitual() {
        offerings.get(2).removeStack();
        activate();
        assertFalse(bowl.isRitualActive());
        assertEquals(ModItems.BOOK_OF_BINDING_BOUND_FOLIOT, player.getHeldItemMainhand().getItem());
        assertEquals(1, player.getHeldItemMainhand().getCount());
        assertEquals(1, offerings.get(0).getStack().getCount());
        assertEquals(1, offerings.get(1).getStack().getCount());
        assertEquals(1, offerings.get(3).getStack().getCount());
        assertTrue(world.spawned.isEmpty());
    }

    @Test
    void realRitualCanResumeAfterConsumingOneMetalWithoutDuplicatingIt() throws Exception {
        activate();
        int interval = ritual.getDurationTicks() / (ritual.getAdditionalIngredients().size() + 1);
        tickBowl(interval);
        assertEquals(1, offerings.stream().filter(TileEntitySacrificialBowl::isEmpty).count());
        NBTTagCompound saved = bowl.writeToNBT(new NBTTagCompound());
        assertEquals(1, saved.getInteger("ConsumedAdditionalIngredients"));
        bowl = world.installBowl(BlockPos.ORIGIN, true);
        bowl.readFromNBT(saved);
        tickBowl(ritual.getDurationTicks() - interval);
        assertFalse(bowl.isRitualActive());
        assertTrue(offerings.stream().allMatch(TileEntitySacrificialBowl::isEmpty));
        assertEquals(1, spirits().size());
        assertEquals(spirits().getFirst().getUniqueID(), ItemNBTUtil.getSpiritEntityUUID(onlyCallingBook()));
    }

    @Test
    void summonedCrusherProducesExactIronGoldAndObsidianDust() throws Exception {
        EntityFoliotJob spirit = summon();
        assertCrush(spirit, new ItemStack(Items.IRON_INGOT, 2), ModItems.IRON_DUST, 2);
        assertCrush(spirit, new ItemStack(Items.GOLD_INGOT, 3), ModItems.GOLD_DUST, 3);
        assertCrush(spirit, new ItemStack(Blocks.OBSIDIAN, 2), ModItems.OBSIDIAN_DUST, 2);
    }

    @Test
    void pickupConsumesOnlyOneInputAndTheEntireProcessingTimeIsRequired() throws Exception {
        EntityFoliotJob spirit = summon();
        EntityItem input = new EntityItem(world, spirit.posX, spirit.posY, spirit.posZ,
                new ItemStack(Items.IRON_INGOT, 3));
        invoke(spirit, "pickupOne", new Class<?>[] {EntityItem.class}, input);
        assertEquals(2, input.getItem().getCount());
        assertEquals(1, spirit.getHeldItemMainhand().getCount());
        int drops = world.drops.size();
        tickJob(spirit, 199);
        assertEquals(drops, world.drops.size());
        assertEquals(1, spirit.getHeldItemMainhand().getCount());
        tickJob(spirit, 1);
        assertTrue(spirit.getHeldItemMainhand().isEmpty());
        assertEquals(1, dropped(ModItems.IRON_DUST));
    }

    @Test
    void invalidInputIsReturnedInsteadOfBeingConsumed() throws Exception {
        EntityFoliotJob spirit = summon();
        EntityItem input = new EntityItem(world, 0, 1, 0, new ItemStack(Items.STICK, 3));
        invoke(spirit, "pickupOne", new Class<?>[] {EntityItem.class}, input);
        assertTrue(spirit.getHeldItemMainhand().isEmpty());
        assertEquals(3, input.getItem().getCount());
        spirit.setHeldItem(EnumHand.MAIN_HAND, new ItemStack(Items.STICK, 3));
        tickJob(spirit, 1);
        assertTrue(spirit.getHeldItemMainhand().isEmpty());
        assertEquals(3, dropped(Items.STICK));
        assertEquals(0, dropped(ModItems.IRON_DUST));
    }

    @Test
    void savedCrusherRetainsInputAndProgressAndOnlyProducesOnce() throws Exception {
        EntityFoliotJob spirit = summon();
        spirit.setHeldItem(EnumHand.MAIN_HAND, new ItemStack(Items.IRON_INGOT));
        tickJob(spirit, 73);
        NBTTagCompound saved = spirit.writeToNBT(new NBTTagCompound());
        assertEquals(73, saved.getInteger("WorkTimer"));
        EntityFoliotJob restored = new EntityFoliotJob(world);
        restored.readFromNBT(saved);
        assertEquals(spirit.getUniqueID(), restored.getUniqueID());
        assertEquals(ItemNBTUtil.getSpiritEntityUUID(onlyCallingBook()), restored.getUniqueID());
        assertEquals("Astaroth", restored.getCustomNameTag());
        assertEquals(SpiritJobType.CRUSHER, restored.getJobType());
        assertEquals(Items.IRON_INGOT, restored.getHeldItemMainhand().getItem());
        tickJob(restored, 126);
        assertEquals(0, dropped(ModItems.IRON_DUST));
        tickJob(restored, 1);
        assertEquals(1, dropped(ModItems.IRON_DUST));
        assertTrue(restored.getHeldItemMainhand().isEmpty());
        tickJob(restored, 300);
        assertEquals(1, dropped(ModItems.IRON_DUST));
    }

    @Test
    void rejectedSpiritSpawnConsumesNoExtraCostsAndProducesNoUnlinkedBook() {
        world.rejectSpirits = true;
        activate();
        tickBowl(ritual.getDurationTicks() + 20);
        assertFalse(bowl.isRitualActive());
        assertTrue(player.getHeldItemMainhand().isEmpty());
        assertTrue(offerings.stream().allMatch(TileEntitySacrificialBowl::isEmpty));
        assertTrue(spirits().isEmpty());
        assertTrue(world.drops.isEmpty());
    }

    @Test
    void changingInputRecipeCannotReuseAlmostFinishedProgress() throws Exception {
        EntityFoliotJob spirit = summon();
        spirit.setHeldItem(EnumHand.MAIN_HAND, new ItemStack(Items.IRON_INGOT));
        tickJob(spirit, 199);
        spirit.setHeldItem(EnumHand.MAIN_HAND, new ItemStack(Items.GOLD_INGOT));
        tickJob(spirit, 199);
        assertEquals(0, dropped(ModItems.IRON_DUST));
        assertEquals(0, dropped(ModItems.GOLD_DUST));
        tickJob(spirit, 1);
        assertEquals(1, dropped(ModItems.GOLD_DUST));
        assertTrue(spirit.getHeldItemMainhand().isEmpty());
    }

    @Test
    void removingAnUnfinishedInputCannotProduceADustLater() throws Exception {
        EntityFoliotJob spirit = summon();
        spirit.setHeldItem(EnumHand.MAIN_HAND, new ItemStack(Items.IRON_INGOT));
        tickJob(spirit, 199);
        spirit.setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);
        tickJob(spirit, 300);
        assertEquals(0, dropped(ModItems.IRON_DUST));
        spirit.setHeldItem(EnumHand.MAIN_HAND, new ItemStack(Items.IRON_INGOT));
        tickJob(spirit, 199);
        assertEquals(0, dropped(ModItems.IRON_DUST));
        tickJob(spirit, 1);
        assertEquals(1, dropped(ModItems.IRON_DUST));
    }

    @Test
    void batchLimitConsumesOnlyTheAvailableInputs() throws Exception {
        EntityFoliotJob spirit = summon();
        ModConfig.SPIRIT_JOBS.crusherFoliot.operationCount = 4;
        EntityItem input = new EntityItem(world, 0, 1, 0, new ItemStack(Items.IRON_INGOT, 6));
        invoke(spirit, "pickupOne", new Class<?>[] {EntityItem.class}, input);
        assertEquals(2, input.getItem().getCount());
        tickJob(spirit, 200);
        assertEquals(4, dropped(ModItems.IRON_DUST));
        assertTrue(spirit.getHeldItemMainhand().isEmpty());
        invoke(spirit, "pickupOne", new Class<?>[] {EntityItem.class}, input);
        assertTrue(input.isDead);
        tickJob(spirit, 200);
        assertEquals(6, dropped(ModItems.IRON_DUST));
        assertTrue(spirit.getHeldItemMainhand().isEmpty());
    }

    @Test
    void fullDepositInventoryFallsBackToADropWithoutLossOrDuplication() throws Exception {
        EntityFoliotJob spirit = summon();
        net.minecraft.tileentity.TileEntityChest chest = new net.minecraft.tileentity.TileEntityChest();
        BlockPos target = new BlockPos(7, 0, 0);
        chest.setWorld(world);
        chest.setPos(target);
        world.tiles.put(target, chest);
        world.states.put(target, Blocks.CHEST.getDefaultState());
        spirit.setDepositPos(target);
        spirit.setHeldItem(EnumHand.MAIN_HAND, new ItemStack(Items.IRON_INGOT));
        tickJob(spirit, 200);
        assertEquals(ModItems.IRON_DUST, chest.getStackInSlot(0).getItem());
        assertEquals(1, chest.getStackInSlot(0).getCount());
        assertEquals(0, dropped(ModItems.IRON_DUST));
        for (int slot = 0; slot < chest.getSizeInventory(); slot++) {
            chest.setInventorySlotContents(slot, new ItemStack(Items.STICK, 64));
        }
        spirit.setHeldItem(EnumHand.MAIN_HAND, new ItemStack(Items.GOLD_INGOT));
        tickJob(spirit, 200);
        assertEquals(1, dropped(ModItems.GOLD_DUST));
        assertTrue(spirit.getHeldItemMainhand().isEmpty());
        for (int slot = 0; slot < chest.getSizeInventory(); slot++) {
            assertEquals(Items.STICK, chest.getStackInSlot(slot).getItem());
            assertEquals(64, chest.getStackInSlot(slot).getCount());
        }
    }

    private EntityFoliotJob summon() throws Exception {
        activate();
        assertTrue(bowl.isRitualActive());
        assertEquals("summon_foliot_crusher", bowl.getActiveRitualId());
        assertEquals(ritual.getDurationTicks(), bowl.getRitualDuration());
        tickBowl(ritual.getDurationTicks() - 1);
        assertTrue(spirits().isEmpty());
        tickBowl(1);
        assertFalse(bowl.isRitualActive());
        assertEquals(1, spirits().size());
        return spirits().getFirst();
    }

    private void activate() {
        assertTrue(ModBlocks.GOLDEN_SACRIFICIAL_BOWL.onBlockActivated(world, BlockPos.ORIGIN,
                world.getBlockState(BlockPos.ORIGIN), player, EnumHand.MAIN_HAND, EnumFacing.UP, 0.5F, 0.5F, 0.5F));
    }

    private void tickBowl(int count) {
        for (int i = 0; i < count; i++) {
            world.setTotalWorldTime(world.getTotalWorldTime() + 1);
            bowl.update();
        }
    }

    private void tickJob(EntityFoliotJob spirit, int count) throws Exception {
        for (int i = 0; i < count; i++) {
            world.setTotalWorldTime(world.getTotalWorldTime() + 1);
            spirit.ticksExisted++;
            invoke(spirit, "updateJob", new Class<?>[0]);
        }
    }

    private void assertCrush(EntityFoliotJob spirit, ItemStack input, Item output, int count) throws Exception {
        int previous = dropped(output);
        spirit.setHeldItem(EnumHand.MAIN_HAND, input);
        tickJob(spirit, 200 * input.getCount());
        assertEquals(previous + count, dropped(output));
        assertTrue(spirit.getHeldItemMainhand().isEmpty());
    }

    private List<EntityFoliotJob> spirits() {
        return world.spawned.stream().filter(EntityFoliotJob.class::isInstance)
                .map(EntityFoliotJob.class::cast).toList();
    }

    private ItemStack onlyCallingBook() {
        List<ItemStack> books = world.drops.stream().map(EntityItem::getItem)
                .filter(stack -> stack.getItem() == ModItems.BOOK_OF_CALLING_FOLIOT_CRUSHER).toList();
        assertEquals(1, books.size());
        return books.getFirst();
    }

    private int dropped(Item item) {
        return world.drops.stream().map(EntityItem::getItem).filter(stack -> stack.getItem() == item)
                .mapToInt(ItemStack::getCount).sum();
    }

    private static Object invoke(EntityFoliotJob spirit, String name, Class<?>[] types, Object... arguments)
            throws Exception {
        Method method = EntityFoliotJob.class.getDeclaredMethod(name, types);
        method.setAccessible(true);
        return method.invoke(spirit, arguments);
    }

    private static final class CrusherWorld extends RitualTestWorld {
        private boolean rejectSpirits;

        private CrusherWorld() {
            super(false);
        }

        @Override
        public boolean spawnEntity(Entity entity) {
            return !(rejectSpirits && entity instanceof EntityFoliotJob) && super.spawnEntity(entity);
        }

        @Override
        public <T extends Entity> List<T> getEntitiesWithinAABB(Class<? extends T> type, AxisAlignedBB bounds) {
            List<T> matches = new ArrayList<>();
            for (EntityPlayer entity : playerEntities) {
                if (type.isInstance(entity) && bounds.intersects(entity.getEntityBoundingBox())) {
                    matches.add(type.cast(entity));
                }
            }
            return matches;
        }
    }

    private static final class TestPlayer extends EntityPlayer {
        private TestPlayer(World world) {
            super(world, new GameProfile(UUID.randomUUID(), "crusher_test"));
        }

        @Override
        public boolean isSpectator() { return false; }

        @Override
        public boolean isCreative() { return false; }
    }
}
