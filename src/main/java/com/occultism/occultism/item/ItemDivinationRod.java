package com.occultism.occultism.item;

import java.util.List;

import javax.annotation.Nullable;

import com.occultism.occultism.Tags;
import com.occultism.occultism.registry.ModBlocks;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemDivinationRod extends Item {

    private static final String TAG_TARGET_BLOCK = "DivinationTargetBlock";
    private static final String TAG_TARGET_META = "DivinationTargetMeta";
    private static final String TAG_LAST_X = "DivinationLastX";
    private static final String TAG_LAST_Y = "DivinationLastY";
    private static final String TAG_LAST_Z = "DivinationLastZ";
    private static final String TAG_DISTANCE = "DivinationDistance";
    private static final int META_ANY = -1;
    private static final int SCAN_DURATION_TICKS = 40;
    private static final int SCAN_RADIUS = 64;
    private static final int SCAN_RADIUS_SQ = SCAN_RADIUS * SCAN_RADIUS;
    private static final float NOT_FOUND = 7.0F;
    private static final float SEARCHING = 8.0F;

    private final boolean trueSightStaff;

    public ItemDivinationRod(boolean trueSightStaff) {
        this.trueSightStaff = trueSightStaff;
        this.setMaxStackSize(1);
        this.addPropertyOverride(new ResourceLocation(Tags.MOD_ID, "distance"), new IItemPropertyGetter() {
            @Override
            public float apply(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn) {
                return getDistance(stack);
            }
        });
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand,
            EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!player.isSneaking()) {
            return EnumActionResult.PASS;
        }

        ItemStack stack = player.getHeldItem(hand);
        IBlockState state = worldIn.getBlockState(pos);
        if (state.getBlock().isAir(state, worldIn, pos)) {
            return EnumActionResult.PASS;
        }

        TargetBlock target = this.getLinkedTargetForState(state, player.capabilities.isCreativeMode);
        if (target == null) {
            if (!worldIn.isRemote) {
                player.sendStatusMessage(new net.minecraft.util.text.TextComponentTranslation(
                        this.getTranslationKey() + ".message.no_link_found"), true);
            }
            return EnumActionResult.SUCCESS;
        }

        if (!worldIn.isRemote) {
            setTarget(stack, target);
            clearLastResult(stack);
            setDistance(stack, NOT_FOUND);
            player.sendStatusMessage(new net.minecraft.util.text.TextComponentTranslation(
                    this.getTranslationKey() + ".message.linked_block", getTargetDisplayName(target)), true);
            worldIn.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.BLOCK_NOTE_HARP,
                    SoundCategory.PLAYERS, 0.8F, 1.2F);
        }
        return EnumActionResult.SUCCESS;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);
        TargetBlock target = getTarget(stack);
        if (target == null) {
            if (!worldIn.isRemote) {
                playerIn.sendStatusMessage(new net.minecraft.util.text.TextComponentTranslation(
                        this.getTranslationKey() + ".message.no_linked_block"), true);
            }
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }

        setDistance(stack, SEARCHING);
        playerIn.setActiveHand(handIn);
        worldIn.playSound(null, playerIn.posX, playerIn.posY, playerIn.posZ, SoundEvents.BLOCK_NOTE_HARP,
                SoundCategory.PLAYERS, 0.7F, 1.0F);
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase entityLiving) {
        if (!worldIn.isRemote && entityLiving instanceof EntityPlayer) {
            this.finishScan(stack, worldIn, (EntityPlayer) entityLiving);
        }
        return stack;
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World worldIn, EntityLivingBase entityLiving, int timeLeft) {
        if (!worldIn.isRemote && timeLeft > 0) {
            restoreDistanceFromLastResult(stack, entityLiving);
        }
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return SCAN_DURATION_TICKS;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || oldStack.getItem() != newStack.getItem();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        TargetBlock target = getTarget(stack);
        if (target == null) {
            tooltip.add(I18n.format(this.getTranslationKey() + ".tooltip.no_linked_block"));
        } else {
            tooltip.add(I18n.format(this.getTranslationKey() + ".tooltip.linked_block",
                    getTargetDisplayName(target)));
        }
    }

    private void finishScan(ItemStack stack, World world, EntityPlayer player) {
        TargetBlock target = getTarget(stack);
        if (target == null) {
            setDistance(stack, NOT_FOUND);
            return;
        }

        BlockPos result = findNearest(world, player, target);
        if (result == null) {
            clearLastResult(stack);
            setDistance(stack, NOT_FOUND);
            player.sendStatusMessage(new net.minecraft.util.text.TextComponentTranslation(
                    this.getTranslationKey() + ".message.not_found"), true);
            return;
        }

        setLastResult(stack, result);
        setDistance(stack, getDistance(player, result));
        player.getCooldownTracker().setCooldown(this, 40);
        player.sendStatusMessage(new net.minecraft.util.text.TextComponentTranslation(
                this.getTranslationKey() + ".message.found_block", getTargetDisplayName(target),
                result.getX(), result.getY(), result.getZ()), true);
        world.playSound(null, result, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.PLAYERS, 0.7F, 1.4F);
        spawnResultParticles(world, result);
    }

    private TargetBlock getLinkedTargetForState(IBlockState state, boolean creativePlayer) {
        Block block = state.getBlock();
        int meta = block.getMetaFromState(state);

        if (block == Blocks.STONE && (meta == 5 || meta == 6)
                || block == ModBlocks.OTHERSTONE || block == ModBlocks.OTHERSTONE_NATURAL) {
            return new TargetBlock(ModBlocks.OTHERSTONE_NATURAL, META_ANY);
        }
        if (block == Blocks.STONE && (meta == 3 || meta == 4)
                || block == ModBlocks.OTHERROCK || block == ModBlocks.OTHERROCK_NATURAL) {
            return new TargetBlock(ModBlocks.OTHERROCK_NATURAL, META_ANY);
        }
        if (block == Blocks.LOG && (meta & 3) == 0
                || block == ModBlocks.OTHERWORLD_LOG || block == ModBlocks.OTHERWORLD_LOG_NATURAL
                || block == ModBlocks.OTHERWORLD_WOOD) {
            return new TargetBlock(ModBlocks.OTHERWORLD_LOG_NATURAL, META_ANY);
        }
        if (block == ModBlocks.STRIPPED_OTHERWORLD_LOG || block == ModBlocks.STRIPPED_OTHERWORLD_LOG_NATURAL) {
            return new TargetBlock(ModBlocks.STRIPPED_OTHERWORLD_LOG_NATURAL, META_ANY);
        }
        if (block == Blocks.LEAVES && (meta & 3) == 0
                || block == ModBlocks.OTHERWORLD_LEAVES || block == ModBlocks.OTHERWORLD_LEAVES_NATURAL) {
            return new TargetBlock(ModBlocks.OTHERWORLD_LEAVES_NATURAL, META_ANY);
        }
        if (block == Blocks.NETHERRACK || block == ModBlocks.IESNIUM_ORE || block == ModBlocks.IESNIUM_ORE_NATURAL) {
            return new TargetBlock(ModBlocks.IESNIUM_ORE_NATURAL, META_ANY);
        }
        if (block == Blocks.YELLOW_FLOWER || block == Blocks.RED_FLOWER || block == ModBlocks.OTHERFLOWER
                || block == ModBlocks.OTHERFLOWER_NATURAL) {
            return new TargetBlock(ModBlocks.OTHERFLOWER_NATURAL, META_ANY);
        }
        if (block == Blocks.END_STONE || block == ModBlocks.OTHERGLASS_NATURAL) {
            return new TargetBlock(ModBlocks.OTHERGLASS_NATURAL, META_ANY);
        }

        if (this.trueSightStaff || creativePlayer) {
            return new TargetBlock(block, meta);
        }
        return null;
    }

    @Nullable
    private static BlockPos findNearest(World world, EntityPlayer player, TargetBlock target) {
        BlockPos origin = new BlockPos(player);
        BlockPos best = null;
        double bestDistanceSq = Double.MAX_VALUE;
        int minX = origin.getX() - SCAN_RADIUS;
        int maxX = origin.getX() + SCAN_RADIUS;
        int minY = Math.max(0, origin.getY() - SCAN_RADIUS);
        int maxY = Math.min(world.getHeight() - 1, origin.getY() + SCAN_RADIUS);
        int minZ = origin.getZ() - SCAN_RADIUS;
        int maxZ = origin.getZ() + SCAN_RADIUS;
        BlockPos.PooledMutableBlockPos scanPos = BlockPos.PooledMutableBlockPos.retain();

        try {
            for (int x = minX; x <= maxX; x++) {
                int dx = x - origin.getX();
                int dxSq = dx * dx;
                for (int z = minZ; z <= maxZ; z++) {
                    int dz = z - origin.getZ();
                    int columnDistanceSq = dxSq + dz * dz;
                    if (columnDistanceSq > SCAN_RADIUS_SQ) {
                        continue;
                    }
                    scanPos.setPos(x, origin.getY(), z);
                    if (!world.isBlockLoaded(scanPos)) {
                        continue;
                    }
                    for (int y = minY; y <= maxY; y++) {
                        int dy = y - origin.getY();
                        if (columnDistanceSq + dy * dy > SCAN_RADIUS_SQ) {
                            continue;
                        }
                        scanPos.setPos(x, y, z);
                        IBlockState state = world.getBlockState(scanPos);
                        if (!target.matches(state)) {
                            continue;
                        }
                        double distanceSq = player.getDistanceSq(scanPos);
                        if (distanceSq < bestDistanceSq) {
                            bestDistanceSq = distanceSq;
                            best = new BlockPos(x, y, z);
                        }
                    }
                }
            }
        } finally {
            scanPos.release();
        }
        return best;
    }

    private static void spawnResultParticles(World world, BlockPos pos) {
        if (!(world instanceof WorldServer)) {
            return;
        }
        WorldServer server = (WorldServer) world;
        server.spawnParticle(EnumParticleTypes.SPELL_WITCH, pos.getX() + 0.5D, pos.getY() + 0.5D,
                pos.getZ() + 0.5D, 24, 0.55D, 0.55D, 0.55D, 0.0D);
        server.spawnParticle(EnumParticleTypes.PORTAL, pos.getX() + 0.5D, pos.getY() + 0.5D,
                pos.getZ() + 0.5D, 18, 0.45D, 0.45D, 0.45D, 0.05D);
    }

    private static float getDistance(EntityLivingBase entity, BlockPos result) {
        double dx = result.getX() + 0.5D - entity.posX;
        double dz = result.getZ() + 0.5D - entity.posZ;
        double distance = Math.sqrt(dx * dx + dz * dz);
        if (distance < 6.0D) {
            return 0.0F;
        }
        if (distance < 15.0D) {
            return 1.0F;
        }
        if (distance < 25.0D) {
            return 2.0F;
        }
        if (distance < 35.0D) {
            return 3.0F;
        }
        if (distance < 45.0D) {
            return 4.0F;
        }
        if (distance < 65.0D) {
            return 5.0F;
        }
        return 6.0F;
    }

    private static void restoreDistanceFromLastResult(ItemStack stack, EntityLivingBase entity) {
        BlockPos lastResult = getLastResult(stack);
        setDistance(stack, lastResult == null ? NOT_FOUND : getDistance(entity, lastResult));
    }

    private static void setTarget(ItemStack stack, TargetBlock target) {
        NBTTagCompound tag = getOrCreateTag(stack);
        ResourceLocation id = Block.REGISTRY.getNameForObject(target.block);
        tag.setString(TAG_TARGET_BLOCK, id == null ? "" : id.toString());
        tag.setInteger(TAG_TARGET_META, target.meta);
        stack.setTagCompound(tag);
    }

    @Nullable
    private static TargetBlock getTarget(ItemStack stack) {
        if (!stack.hasTagCompound()) {
            return null;
        }
        NBTTagCompound tag = stack.getTagCompound();
        if (!tag.hasKey(TAG_TARGET_BLOCK)) {
            return null;
        }
        Block block = Block.REGISTRY.getObject(new ResourceLocation(tag.getString(TAG_TARGET_BLOCK)));
        if (block == null || block == Blocks.AIR) {
            return null;
        }
        return new TargetBlock(block, tag.hasKey(TAG_TARGET_META) ? tag.getInteger(TAG_TARGET_META) : META_ANY);
    }

    private static void setLastResult(ItemStack stack, BlockPos pos) {
        NBTTagCompound tag = getOrCreateTag(stack);
        tag.setInteger(TAG_LAST_X, pos.getX());
        tag.setInteger(TAG_LAST_Y, pos.getY());
        tag.setInteger(TAG_LAST_Z, pos.getZ());
        stack.setTagCompound(tag);
    }

    private static void clearLastResult(ItemStack stack) {
        if (!stack.hasTagCompound()) {
            return;
        }
        NBTTagCompound tag = stack.getTagCompound();
        tag.removeTag(TAG_LAST_X);
        tag.removeTag(TAG_LAST_Y);
        tag.removeTag(TAG_LAST_Z);
        if (tag.isEmpty()) {
            stack.setTagCompound(null);
        }
    }

    @Nullable
    private static BlockPos getLastResult(ItemStack stack) {
        if (!stack.hasTagCompound()) {
            return null;
        }
        NBTTagCompound tag = stack.getTagCompound();
        if (!tag.hasKey(TAG_LAST_X) || !tag.hasKey(TAG_LAST_Y) || !tag.hasKey(TAG_LAST_Z)) {
            return null;
        }
        return new BlockPos(tag.getInteger(TAG_LAST_X), tag.getInteger(TAG_LAST_Y), tag.getInteger(TAG_LAST_Z));
    }

    private static void setDistance(ItemStack stack, float distance) {
        NBTTagCompound tag = getOrCreateTag(stack);
        tag.setFloat(TAG_DISTANCE, distance);
        stack.setTagCompound(tag);
    }

    private static float getDistance(ItemStack stack) {
        return stack.hasTagCompound() && stack.getTagCompound().hasKey(TAG_DISTANCE)
                ? stack.getTagCompound().getFloat(TAG_DISTANCE) : NOT_FOUND;
    }

    private static NBTTagCompound getOrCreateTag(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        return tag == null ? new NBTTagCompound() : tag;
    }

    private static String getTargetDisplayName(TargetBlock target) {
        Item item = Item.getItemFromBlock(target.block);
        if (item == null || item == Item.getItemFromBlock(Blocks.AIR)) {
            return target.block.getLocalizedName();
        }
        int displayMeta = target.meta == META_ANY ? 0 : target.meta;
        return new ItemStack(item, 1, displayMeta).getDisplayName();
    }

    private static final class TargetBlock {
        private final Block block;
        private final int meta;

        private TargetBlock(Block block, int meta) {
            this.block = block;
            this.meta = meta;
        }

        private boolean matches(IBlockState state) {
            if (state.getBlock() != this.block) {
                return false;
            }
            return this.meta == META_ANY || this.block.getMetaFromState(state) == this.meta;
        }
    }
}
