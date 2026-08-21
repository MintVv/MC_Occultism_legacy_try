package com.occultism.occultism.block;

import java.util.List;

import com.occultism.occultism.item.ItemSatchel;
import com.occultism.occultism.ritual.PentacleDetector;
import com.occultism.occultism.ritual.PentacleMatch;
import com.occultism.occultism.ritual.RitualDefinition;
import com.occultism.occultism.ritual.RitualDebugMessages;
import com.occultism.occultism.ritual.RitualRegistry;
import com.occultism.occultism.tileentity.TileEntitySacrificialBowl;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockGoldenSacrificialBowl extends Block {

    private static final AxisAlignedBB BOUNDING_BOX =
            new AxisAlignedBB(0.1875D, 0.0D, 0.1875D, 0.8125D, 0.5D, 0.8125D);
    private final int ritualDurationDivisor;

    public BlockGoldenSacrificialBowl() {
        this(1);
    }

    public BlockGoldenSacrificialBowl(int ritualDurationDivisor) {
        super(Material.ROCK);
        this.ritualDurationDivisor = Math.max(1, ritualDurationDivisor);
        this.setSoundType(SoundType.STONE);
        this.setHardness(2.0F);
        this.setResistance(10.0F);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
            EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) {
            TileEntitySacrificialBowl bowl = this.getOrCreateTileEntity(worldIn, pos, state);
            ItemStack heldStack = playerIn.getHeldItem(hand);

            if (bowl.isRitualActive()) {
                RitualDefinition activeRitual = RitualRegistry.getById(bowl.getActiveRitualId());
                ITextComponent ritualName = activeRitual == null
                        ? new TextComponentString(bowl.getActiveRitualId())
                        : activeRitual.createNameComponent();
                if (bowl.isWaitingForItemUse() && activeRitual != null) {
                    ItemStack itemToUse = activeRitual.getItemToUse().createDisplayStack();
                    playerIn.sendStatusMessage(new TextComponentTranslation("occultism.ritual.waiting_item_use",
                            ritualName, itemToUse.getDisplayName()), false);
                    return true;
                }
                playerIn.sendStatusMessage(new TextComponentTranslation("occultism.ritual.running", ritualName,
                        bowl.getRitualProgress(), bowl.getRitualDuration()), false);
                return true;
            }

            if (ItemSatchel.tryCleanPentacle(worldIn, pos, playerIn, heldStack)) {
                return true;
            }

            if (heldStack.isEmpty()) {
                this.reportPentacle(worldIn, pos, playerIn);
                return true;
            }

            List<PentacleMatch> matches = PentacleDetector.findMatchesAt(worldIn, pos);
            if (matches.isEmpty()) {
                playerIn.sendStatusMessage(new TextComponentTranslation("occultism.pentacle_debug.none"), false);
                return true;
            }

            RitualDefinition activationMatch = RitualRegistry.findMatching(matches, heldStack);
            if (activationMatch == null) {
                playerIn.sendStatusMessage(new TextComponentTranslation("occultism.ritual.wrong_activation",
                        heldStack.getDisplayName()), false);
                this.reportPentacle(worldIn, pos, playerIn);
                RitualDebugMessages.sendPentacleRituals(playerIn, matches.get(0).getDefinition());
                return true;
            }

            RitualDefinition ritual = this.findStartableRitual(worldIn, pos, bowl, matches, heldStack);
            if (ritual == null) {
                playerIn.sendStatusMessage(new TextComponentTranslation("occultism.ritual.missing_ingredients",
                        activationMatch.createNameComponent()), false);
                RitualDebugMessages.sendActivationMatches(playerIn, matches.get(0).getDefinition(), heldStack);
                return true;
            }

            if (ritual.requiresSacrifice() && !bowl.hasMatchingSacrifice(worldIn, pos, ritual)) {
                playerIn.sendStatusMessage(new TextComponentTranslation("occultism.ritual.missing_sacrifice",
                        ritual.createNameComponent(), ritual.getSacrifice().getTag()), false);
                return true;
            }

            ItemStack activationStack = heldStack.copy();
            activationStack.setCount(1);
            if (!playerIn.capabilities.isCreativeMode) {
                heldStack.shrink(1);
            }
            if (!bowl.consumeSacrifice(worldIn, pos, ritual)) {
                playerIn.sendStatusMessage(new TextComponentTranslation("occultism.ritual.missing_sacrifice",
                        ritual.createNameComponent(), ritual.getSacrifice().getTag()), false);
                return true;
            }
            bowl.startRitual(ritual, activationStack, this.ritualDurationDivisor, playerIn);
            playerIn.sendStatusMessage(
                    new TextComponentTranslation("occultism.ritual.started", ritual.createNameComponent()), false);
            worldIn.playSound(null, pos, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 0.6F, 0.8F);
        }
        return true;
    }

    private RitualDefinition findStartableRitual(World world, BlockPos pos, TileEntitySacrificialBowl bowl,
            List<PentacleMatch> matches, ItemStack activationStack) {
        for (PentacleMatch match : matches) {
            for (RitualDefinition definition : RitualRegistry.getDefinitions()) {
                if (definition.matchesActivation(match, activationStack)
                        && bowl.canStartRitual(world, pos, definition, activationStack)) {
                    return definition;
                }
            }
        }
        return null;
    }

    private void reportPentacle(World world, BlockPos pos, EntityPlayer player) {
        List<PentacleMatch> matches = PentacleDetector.findMatchesAt(world, pos);
        if (matches.isEmpty()) {
            player.sendStatusMessage(new TextComponentTranslation("occultism.pentacle_debug.none"), false);
        } else {
            PentacleMatch match = matches.get(0);
            player.sendStatusMessage(new TextComponentTranslation("occultism.pentacle_debug.found",
                    match.getDefinition().getDisplayName(), match.getDefinition().getId(), match.getRequiredGlyphs(),
                    match.getCenter().getX(), match.getCenter().getY(), match.getCenter().getZ()), false);
            RitualDebugMessages.sendPentacleRituals(player, match.getDefinition());
        }
    }

    private TileEntitySacrificialBowl getOrCreateTileEntity(World world, BlockPos pos, IBlockState state) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof TileEntitySacrificialBowl) {
            return (TileEntitySacrificialBowl) tileEntity;
        }
        TileEntitySacrificialBowl bowl = (TileEntitySacrificialBowl) this.createTileEntity(world, state);
        world.setTileEntity(pos, bowl);
        return bowl;
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntitySacrificialBowl();
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity instanceof TileEntitySacrificialBowl) {
            ItemStack stack = ((TileEntitySacrificialBowl) tileEntity).getStack();
            if (!stack.isEmpty()) {
                InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), stack);
            }
        }
        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return BOUNDING_BOX;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }
}
