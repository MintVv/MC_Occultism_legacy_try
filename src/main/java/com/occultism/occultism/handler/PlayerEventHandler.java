package com.occultism.occultism.handler;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.occultism.occultism.Tags;
import com.occultism.occultism.advancement.FamiliarTrigger;
import com.occultism.occultism.block.IOtherworldBlock;
import com.occultism.occultism.block.OtherworldBlockTier;
import com.occultism.occultism.entity.EntityBoundSpirit;
import com.occultism.occultism.entity.EntityFoliotJob;
import com.occultism.occultism.entity.EntityMercyGoat;
import com.occultism.occultism.entity.EntityPossessedHoglin;
import com.occultism.occultism.entity.IFamiliar;
import com.occultism.occultism.item.ItemBookOfCallingFoliotJob;
import com.occultism.occultism.item.ItemButcherKnife;
import com.occultism.occultism.item.ItemDemonDreamFood;
import com.occultism.occultism.item.ItemFamiliarRing;
import com.occultism.occultism.item.ItemMagicLamp;
import com.occultism.occultism.item.ItemOtherworldGoggles;
import com.occultism.occultism.item.ItemSoulGem;
import com.occultism.occultism.item.ItemSoulShard;
import com.occultism.occultism.item.ItemVitalityCompass;
import com.occultism.occultism.registry.ModBlocks;
import com.occultism.occultism.registry.ModEnchantments;
import com.occultism.occultism.registry.ModItems;
import com.occultism.occultism.registry.ModAdvancements;
import com.occultism.occultism.registry.ModPotions;
import com.occultism.occultism.ritual.RitualDefinition;
import com.occultism.occultism.ritual.RitualRegistry;
import com.occultism.occultism.tileentity.TileEntitySacrificialBowl;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.BlockLog;
import net.minecraft.block.Block;
import net.minecraft.block.BlockJukebox;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityWitherSkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityDonkey;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityLlama;
import net.minecraft.entity.passive.EntityMule;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemRecord;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class PlayerEventHandler {

    private static final double RITUAL_ITEM_USE_RANGE_SQ = 16.0D * 16.0D;
    private static final int OTHERWORLD_REVEAL_RANGE = 10;
    private static final float SHATTERED_SOUL_CHANCE_PER_LEVEL = 0.10F;
    private static final Set<BlockPos> OTHERWORLD_REVEALED = new HashSet<>();
    private static int otherworldRevealDimension = Integer.MIN_VALUE;

    private PlayerEventHandler() {
    }

    @SubscribeEvent
    public static void onRightClickEntity(PlayerInteractEvent.EntityInteract event) {
        ItemStack held = event.getItemStack();
        if (!held.isEmpty() && held.getItem() instanceof ItemMagicLamp) {
            EnumActionResult result = ((ItemMagicLamp) held.getItem()).tryCapture(held, event.getEntityPlayer(),
                    event.getTarget(), event.getHand());
            if (result != EnumActionResult.PASS) {
                event.setCancellationResult(result);
                event.setCanceled(true);
            }
            return;
        }
        if (!held.isEmpty() && held.getItem() instanceof ItemFamiliarRing) {
            EnumActionResult result = ((ItemFamiliarRing) held.getItem()).tryCapture(held, event.getEntityPlayer(),
                    event.getTarget(), event.getHand());
            if (result != EnumActionResult.PASS) {
                event.setCancellationResult(result);
                event.setCanceled(true);
            }
            return;
        }
        if (!held.isEmpty() && held.getItem() instanceof ItemBookOfCallingFoliotJob) {
            EnumActionResult result = ((ItemBookOfCallingFoliotJob) held.getItem()).tryInteractWithSpirit(held,
                    event.getEntityPlayer(), event.getTarget(), event.getHand());
            if (result != EnumActionResult.PASS) {
                event.setCancellationResult(result);
                event.setCanceled(true);
            }
            return;
        }
        if (!held.isEmpty() && held.getItem() instanceof ItemDemonDreamFood) {
            if (((ItemDemonDreamFood) held.getItem()).tryHealSpirit(held, event.getEntityPlayer(), event.getTarget(),
                    event.getHand())) {
                event.setCancellationResult(EnumActionResult.SUCCESS);
                event.setCanceled(true);
            }
            return;
        }
        if (!held.isEmpty() && held.getItem() instanceof ItemVitalityCompass) {
            EnumActionResult result = ((ItemVitalityCompass) held.getItem()).tryLink(held, event.getEntityPlayer(),
                    event.getTarget(), event.getHand());
            if (result != EnumActionResult.PASS) {
                event.setCancellationResult(result);
                event.setCanceled(true);
            }
            return;
        }
        if (held.isEmpty() || !(held.getItem() instanceof ItemSoulGem)) {
            return;
        }

        EnumActionResult result = ((ItemSoulGem) held.getItem()).tryCapture(held, event.getEntityPlayer(),
                event.getTarget(), event.getHand());
        if (result == EnumActionResult.PASS) {
            return;
        }
        event.setCancellationResult(result);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onRightClickEntitySpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        ItemStack held = event.getItemStack();
        if (!held.isEmpty() && held.getItem() instanceof ItemMagicLamp) {
            Entity target = event.getTarget();
            EnumActionResult result = ((ItemMagicLamp) held.getItem()).tryCapture(held, event.getEntityPlayer(),
                    target, event.getHand());
            if (result != EnumActionResult.PASS) {
                event.setCancellationResult(result);
                event.setCanceled(true);
            }
            return;
        }
        if (!held.isEmpty() && held.getItem() instanceof ItemFamiliarRing) {
            Entity target = event.getTarget();
            EnumActionResult result = ((ItemFamiliarRing) held.getItem()).tryCapture(held, event.getEntityPlayer(),
                    target, event.getHand());
            if (result != EnumActionResult.PASS) {
                event.setCancellationResult(result);
                event.setCanceled(true);
            }
            return;
        }
        if (!held.isEmpty() && held.getItem() instanceof ItemBookOfCallingFoliotJob) {
            Entity target = event.getTarget();
            EnumActionResult result = ((ItemBookOfCallingFoliotJob) held.getItem()).tryInteractWithSpirit(held,
                    event.getEntityPlayer(), target, event.getHand());
            if (result != EnumActionResult.PASS) {
                event.setCancellationResult(result);
                event.setCanceled(true);
            }
            return;
        }
        if (!held.isEmpty() && held.getItem() instanceof ItemDemonDreamFood) {
            Entity target = event.getTarget();
            if (((ItemDemonDreamFood) held.getItem()).tryHealSpirit(held, event.getEntityPlayer(), target,
                    event.getHand())) {
                event.setCancellationResult(EnumActionResult.SUCCESS);
                event.setCanceled(true);
            }
            return;
        }
        if (!held.isEmpty() && held.getItem() instanceof ItemVitalityCompass) {
            Entity target = event.getTarget();
            EnumActionResult result = ((ItemVitalityCompass) held.getItem()).tryLink(held, event.getEntityPlayer(),
                    target, event.getHand());
            if (result != EnumActionResult.PASS) {
                event.setCancellationResult(result);
                event.setCanceled(true);
            }
            return;
        }
        if (held.isEmpty() || !(held.getItem() instanceof ItemSoulGem)) {
            return;
        }

        Entity target = event.getTarget();
        EnumActionResult result = ((ItemSoulGem) held.getItem()).tryCapture(held, event.getEntityPlayer(), target,
                event.getHand());
        if (result == EnumActionResult.PASS) {
            return;
        }
        event.setCancellationResult(result);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        ItemStack held = event.getItemStack();
        dancingFamiliars(event, held);
        if (tryStripOtherworldLog(event, held)) {
            return;
        }
        if (held.isEmpty() || held.getItem() != Items.FLINT_AND_STEEL) {
            return;
        }

        World world = event.getWorld();
        BlockPos targetPos = world.getBlockState(event.getPos()).getBlock().isReplaceable(world, event.getPos())
                ? event.getPos()
                : event.getPos().offset(event.getFace());
        if (!world.isAirBlock(targetPos) && !world.getBlockState(targetPos).getBlock().isReplaceable(world, targetPos)) {
            return;
        }

        List<EntityItem> datura = world.getEntitiesWithinAABB(EntityItem.class,
                new AxisAlignedBB(event.getPos()).grow(1.0D),
                item -> !item.isDead && !item.getItem().isEmpty() && item.getItem().getItem() == ModItems.DATURA);
        if (datura.isEmpty()) {
            return;
        }

        event.setCanceled(true);
        event.setCancellationResult(EnumActionResult.SUCCESS);
        if (world.isRemote) {
            return;
        }

        datura.forEach(EntityItem::setDead);
        world.setBlockState(targetPos, ModBlocks.SPIRIT_FIRE.getDefaultState(), 11);
        world.playSound(null, targetPos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0F,
                world.rand.nextFloat() * 0.4F + 0.8F);

        if (!event.getEntityPlayer().capabilities.isCreativeMode) {
            held.damageItem(1, event.getEntityPlayer());
        }
        event.getEntityPlayer().swingArm(event.getHand() == null ? EnumHand.MAIN_HAND : event.getHand());
    }

    private static void dancingFamiliars(PlayerInteractEvent.RightClickBlock event, ItemStack held) {
        if (event.getWorld().isRemote || held.isEmpty() || !(held.getItem() instanceof ItemRecord)) {
            return;
        }
        IBlockState state = event.getWorld().getBlockState(event.getPos());
        if (state.getBlock() != Blocks.JUKEBOX || !state.getPropertyKeys().contains(BlockJukebox.HAS_RECORD)
                || state.getValue(BlockJukebox.HAS_RECORD)) {
            return;
        }
        EntityPlayer player = event.getEntityPlayer();
        boolean hasFamiliar = !event.getWorld().getEntitiesWithinAABB(Entity.class,
                new AxisAlignedBB(event.getPos()).grow(3.0D),
                entity -> entity instanceof IFamiliar
                        && ((IFamiliar) entity).getFamiliarOwner() == player).isEmpty();
        if (hasFamiliar) {
            ModAdvancements.FAMILIAR.trigger(player, FamiliarTrigger.Type.PARTY);
        }
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        ItemStack held = event.getItemStack();
        if (held.isEmpty() || event.getWorld().isRemote) {
            return;
        }

        TileEntitySacrificialBowl bowl = findWaitingItemUseBowl(event.getWorld(), event.getEntityPlayer(), held);
        if (bowl == null || !bowl.provideItemUse(held)) {
            return;
        }

        RitualDefinition ritual = RitualRegistry.getById(bowl.getActiveRitualId());
        ITextComponent ritualName = ritual == null
                ? new TextComponentString(bowl.getActiveRitualId())
                : ritual.createNameComponent();
        event.getEntityPlayer().sendStatusMessage(new TextComponentTranslation("occultism.ritual.item_used",
                held.getDisplayName(), ritualName), false);
    }

    private static TileEntitySacrificialBowl findWaitingItemUseBowl(World world,
            net.minecraft.entity.player.EntityPlayer player, ItemStack held) {
        TileEntitySacrificialBowl nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (TileEntity tileEntity : world.loadedTileEntityList) {
            if (!(tileEntity instanceof TileEntitySacrificialBowl)) {
                continue;
            }
            TileEntitySacrificialBowl bowl = (TileEntitySacrificialBowl) tileEntity;
            if (!bowl.canProvideItemUse(held)) {
                continue;
            }
            double distance = bowl.getPos().distanceSq(player.posX, player.posY, player.posZ);
            if (distance > RITUAL_ITEM_USE_RANGE_SQ || distance >= nearestDistance) {
                continue;
            }
            nearest = bowl;
            nearestDistance = distance;
        }
        return nearest;
    }

    private static boolean tryStripOtherworldLog(PlayerInteractEvent.RightClickBlock event, ItemStack held) {
        if (held.isEmpty() || !isAxe(held.getItem())) {
            return false;
        }

        World world = event.getWorld();
        IBlockState state = world.getBlockState(event.getPos());
        Block block = state.getBlock();
        Block stripped = null;
        if (block == ModBlocks.OTHERWORLD_LOG) {
            stripped = ModBlocks.STRIPPED_OTHERWORLD_LOG;
        } else if (block == ModBlocks.OTHERWORLD_LOG_NATURAL) {
            stripped = ModBlocks.STRIPPED_OTHERWORLD_LOG_NATURAL;
        } else if (block == ModBlocks.OTHERWORLD_WOOD) {
            stripped = ModBlocks.STRIPPED_OTHERWORLD_WOOD;
        }
        if (stripped == null) {
            return false;
        }

        event.setCanceled(true);
        event.setCancellationResult(EnumActionResult.SUCCESS);
        if (!world.isRemote) {
            IBlockState strippedState = stripped.getDefaultState();
            if (state.getPropertyKeys().contains(BlockLog.LOG_AXIS)
                    && strippedState.getPropertyKeys().contains(BlockLog.LOG_AXIS)) {
                strippedState = strippedState.withProperty(BlockLog.LOG_AXIS, state.getValue(BlockLog.LOG_AXIS));
            }
            if (state.getPropertyKeys().contains(IOtherworldBlock.UNCOVERED)
                    && strippedState.getPropertyKeys().contains(IOtherworldBlock.UNCOVERED)) {
                strippedState = strippedState.withProperty(IOtherworldBlock.UNCOVERED,
                        state.getValue(IOtherworldBlock.UNCOVERED));
            }
            world.setBlockState(event.getPos(), strippedState, 11);
            world.playSound(null, event.getPos(), SoundEvents.BLOCK_WOOD_PLACE, SoundCategory.BLOCKS, 1.0F, 1.0F);
            if (!event.getEntityPlayer().capabilities.isCreativeMode) {
                held.damageItem(1, event.getEntityPlayer());
            }
        }
        event.getEntityPlayer().swingArm(event.getHand() == null ? EnumHand.MAIN_HAND : event.getHand());
        return true;
    }

    private static boolean isAxe(Item item) {
        return item == Items.WOODEN_AXE || item == Items.STONE_AXE || item == Items.IRON_AXE
                || item == Items.GOLDEN_AXE || item == Items.DIAMOND_AXE;
    }

    @SubscribeEvent
    public static void onHarvestDrops(BlockEvent.HarvestDropsEvent event) {
        if (event.getWorld().isRemote || event.isSilkTouching()) {
            return;
        }
        if (!isGrassForDaturaSeed(event.getState())) {
            return;
        }
        if (event.getWorld().rand.nextFloat() < 0.02F) {
            event.getDrops().add(new ItemStack(ModItems.DATURA_SEEDS));
        }
    }

    private static boolean isGrassForDaturaSeed(IBlockState state) {
        if (state.getBlock() == Blocks.TALLGRASS) {
            BlockTallGrass.EnumType type = state.getValue(BlockTallGrass.TYPE);
            return type == BlockTallGrass.EnumType.GRASS || type == BlockTallGrass.EnumType.FERN;
        }
        if (state.getBlock() == Blocks.DOUBLE_PLANT
                && state.getValue(BlockDoublePlant.HALF) == BlockDoublePlant.EnumBlockHalf.LOWER) {
            BlockDoublePlant.EnumPlantType type = state.getValue(BlockDoublePlant.VARIANT);
            return type == BlockDoublePlant.EnumPlantType.GRASS || type == BlockDoublePlant.EnumPlantType.FERN;
        }
        return false;
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player == null || !event.player.world.isRemote
                || event.player.ticksExisted % 10 != 0) {
            return;
        }
        OtherworldBlockTier revealTier = getPlayerRevealTier(event.player);
        if (revealTier == OtherworldBlockTier.NONE) {
            restoreOtherworldReveal(event.player.world);
            return;
        }
        updateOtherworldReveal(event.player, revealTier);
    }

    private static OtherworldBlockTier getPlayerRevealTier(EntityPlayer player) {
        OtherworldBlockTier tier = OtherworldBlockTier.NONE;
        if (player.isPotionActive(ModPotions.THIRD_EYE)) {
            tier = OtherworldBlockTier.ONE;
        }
        if (ItemOtherworldGoggles.isGoggles(player.getItemStackFromSlot(EntityEquipmentSlot.HEAD))
                || IOtherworldBlock.isTrueSightStaff(player.getHeldItemMainhand())
                || IOtherworldBlock.isTrueSightStaff(player.getHeldItemOffhand())) {
            tier = OtherworldBlockTier.max(tier, OtherworldBlockTier.TWO);
        }
        return tier;
    }

    private static void updateOtherworldReveal(EntityPlayer player, OtherworldBlockTier revealTier) {
        World world = player.world;
        int dimension = world.provider.getDimension();
        if (otherworldRevealDimension != dimension) {
            OTHERWORLD_REVEALED.clear();
            otherworldRevealDimension = dimension;
        }
        BlockPos origin = player.getPosition();
        int range = OTHERWORLD_REVEAL_RANGE;
        Set<BlockPos> visibleNow = new HashSet<>();
        for (BlockPos pos : BlockPos.getAllInBox(origin.add(-range, -range, -range),
                origin.add(range, range, range))) {
            IBlockState state = world.getBlockState(pos);
            if (!isOtherworldVisibleState(state, revealTier)) {
                continue;
            }
            BlockPos immutable = pos.toImmutable();
            visibleNow.add(immutable);
            if (!state.getValue(IOtherworldBlock.UNCOVERED)) {
                world.setBlockState(immutable, state.withProperty(IOtherworldBlock.UNCOVERED, Boolean.TRUE), 2);
                OTHERWORLD_REVEALED.add(immutable);
            }
            if (world.rand.nextInt(24) != 0) {
                continue;
            }
            world.spawnParticle(EnumParticleTypes.SPELL_WITCH, pos.getX() + 0.5D, pos.getY() + 0.75D,
                    pos.getZ() + 0.5D, 0.0D, 0.02D, 0.0D);
        }
        Iterator<BlockPos> iterator = OTHERWORLD_REVEALED.iterator();
        while (iterator.hasNext()) {
            BlockPos pos = iterator.next();
            if (visibleNow.contains(pos)) {
                continue;
            }
            setOtherworldBlockCovered(world, pos);
            iterator.remove();
        }
    }

    private static void restoreOtherworldReveal(World world) {
        if (OTHERWORLD_REVEALED.isEmpty()) {
            return;
        }
        Iterator<BlockPos> iterator = OTHERWORLD_REVEALED.iterator();
        while (iterator.hasNext()) {
            setOtherworldBlockCovered(world, iterator.next());
            iterator.remove();
        }
    }

    private static void setOtherworldBlockCovered(World world, BlockPos pos) {
        if (!world.isBlockLoaded(pos)) {
            return;
        }
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof IOtherworldBlock
                && state.getPropertyKeys().contains(IOtherworldBlock.UNCOVERED)
                && state.getValue(IOtherworldBlock.UNCOVERED)) {
            world.setBlockState(pos, state.withProperty(IOtherworldBlock.UNCOVERED, Boolean.FALSE), 2);
        }
    }

    private static boolean isOtherworldVisibleState(IBlockState state, OtherworldBlockTier revealTier) {
        if (!(state.getBlock() instanceof IOtherworldBlock)
                || !state.getPropertyKeys().contains(IOtherworldBlock.UNCOVERED)) {
            return false;
        }
        return revealTier.canReveal(((IOtherworldBlock) state.getBlock()).getTier());
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (event.getEntityLiving().world.isRemote) {
            return;
        }
        Entity source = event.getSource().getTrueSource();
        if (source instanceof EntityLivingBase) {
            addShatteredSoulDrop(event, ((EntityLivingBase) source).getHeldItemMainhand());
        }
        if (!(source instanceof EntityPlayer)) {
            return;
        }
        EntityPlayer player = (EntityPlayer) source;
        ItemStack weapon = player.getHeldItemMainhand();
        if (weapon.isEmpty() || !(weapon.getItem() instanceof ItemButcherKnife)) {
            return;
        }

        int tallowCount = getTallowCount(event.getEntityLiving());
        if (tallowCount > 0) {
            addDrop(event, new ItemStack(ModItems.TALLOW, tallowCount));
        }

        if (((ItemButcherKnife) weapon.getItem()).isIesnium()) {
            ItemStack head = getIesniumHeadDrop(event);
            if (!head.isEmpty()) {
                addDrop(event, head);
            }
        }
    }

    private static void addShatteredSoulDrop(LivingDropsEvent event, ItemStack weapon) {
        if (weapon.isEmpty() || isShatteredSoulDenied(event.getEntityLiving())) {
            return;
        }
        int level = EnchantmentHelper.getEnchantmentLevel(ModEnchantments.FRACTURE_SOUL, weapon);
        if (level <= 0) {
            return;
        }
        float chance = Math.min(1.0F, level * SHATTERED_SOUL_CHANCE_PER_LEVEL);
        if (event.getEntityLiving().world.rand.nextFloat() >= chance) {
            return;
        }

        ItemStack shard = new ItemStack(ModItems.SOUL_SHATTERED);
        if (ItemSoulShard.storeEntity(shard, event.getEntityLiving())) {
            addDrop(event, shard);
        }
    }

    private static boolean isShatteredSoulDenied(EntityLivingBase entity) {
        return entity instanceof EntityPlayer || entity instanceof IFamiliar || entity instanceof EntityFoliotJob
                || entity instanceof EntityBoundSpirit || entity instanceof EntityDragon || entity instanceof EntityWither
                || !entity.isNonBoss();
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntityLiving().world.isRemote || !(event.getEntityLiving() instanceof EntityFoliotJob)
                && !(event.getEntityLiving() instanceof EntityBoundSpirit)
                || !(event.getSource().getTrueSource() instanceof EntityPlayer)) {
            return;
        }
        EntityPlayer player = (EntityPlayer) event.getSource().getTrueSource();
        ItemStack weapon = player.getHeldItemMainhand();
        if (!weapon.isEmpty() && weapon.getItem() instanceof ItemButcherKnife
                && ((ItemButcherKnife) weapon.getItem()).isIesnium()) {
            event.setAmount(event.getAmount() * 3.0F);
        }
    }

    private static int getTallowCount(net.minecraft.entity.EntityLivingBase entity) {
        if (entity instanceof EntityPossessedHoglin) {
            return 4;
        }
        if (entity instanceof EntityCow) {
            return 4;
        }
        if (entity instanceof EntityHorse || entity instanceof EntityDonkey || entity instanceof EntityMule
                || entity instanceof EntityLlama) {
            return 3;
        }
        if (entity instanceof EntityMercyGoat) {
            return 2;
        }
        if (entity instanceof EntityPig || entity instanceof EntitySheep) {
            return 2;
        }
        return 0;
    }

    private static ItemStack getIesniumHeadDrop(LivingDropsEvent event) {
        Entity entity = event.getEntity();
        float chance = 0.0F;
        int skullMeta = -1;
        if (entity instanceof EntityPlayer) {
            ItemStack skull = new ItemStack(Items.SKULL, 1, 3);
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("SkullOwner", ((EntityPlayer) entity).getName());
            skull.setTagCompound(tag);
            return skull;
        }
        if (entity instanceof EntityZombie) {
            chance = 0.25F;
            skullMeta = 2;
        } else if (entity instanceof EntityCreeper) {
            chance = 0.25F;
            skullMeta = 4;
        } else if (entity instanceof EntityWitherSkeleton) {
            chance = 0.15F;
            skullMeta = 1;
        } else if (entity instanceof EntitySkeleton) {
            chance = 0.25F;
            skullMeta = 0;
        } else if (entity instanceof EntityDragon) {
            chance = 0.9F;
            skullMeta = 5;
        }
        if (skullMeta < 0 || event.getEntityLiving().world.rand.nextFloat() >= chance) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(Items.SKULL, 1, skullMeta);
    }

    private static void addDrop(LivingDropsEvent event, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        event.getDrops().add(new EntityItem(event.getEntityLiving().world,
                event.getEntityLiving().posX, event.getEntityLiving().posY, event.getEntityLiving().posZ, stack));
    }
}
