package com.occultism.occultism.item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.occultism.occultism.Occultism;
import com.occultism.occultism.block.BlockChalkGlyph;
import com.occultism.occultism.gui.ModGuiHandler;
import com.occultism.occultism.registry.ModBlocks;
import com.occultism.occultism.registry.ModItems;
import com.occultism.occultism.ritual.PentacleDetector;
import com.occultism.occultism.ritual.PentacleDefinition;
import com.occultism.occultism.ritual.PentacleMatch;
import com.occultism.occultism.ritual.RitualDefinition;
import com.occultism.occultism.ritual.RitualDebugMessages;
import com.occultism.occultism.ritual.RitualIngredient;
import com.occultism.occultism.ritual.RitualRegistry;
import com.occultism.occultism.tileentity.TileEntitySacrificialBowl;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSkull;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class ItemDictionaryOfSpirits extends Item {

    private static final Map<UUID, Integer> DEBUG_PENTACLE_INDEX = new HashMap<>();

    @Override
    public net.minecraft.util.ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn,
            EnumHand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);
        if (playerIn.isSneaking()) {
            return new net.minecraft.util.ActionResult<>(EnumActionResult.PASS, stack);
        }
        if (!worldIn.isRemote) {
            playerIn.openGui(Occultism.INSTANCE, ModGuiHandler.DICTIONARY_OF_SPIRITS, worldIn, 0, 0, 0);
        }
        return new net.minecraft.util.ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand,
            EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        if (!player.canPlayerEdit(pos, facing, stack)) {
            return EnumActionResult.FAIL;
        }

        if (!worldIn.isRemote) {
            if (player.isSneaking() && facing == EnumFacing.UP && this.canPlaceDebugPentacle(player)) {
                BlockPos center = pos.up();
                int debugIndex = this.getDebugPentacleIndex(player);
                List<PentacleDefinition> definitions = PentacleDetector.getDefinitions();
                PentacleDefinition definition = definitions.get(debugIndex % definitions.size());
                this.advanceDebugPentacleIndex(player, debugIndex, definitions.size());
                int placed = this.placeDebugPentacle(worldIn, center, definition);
                RitualDefinition ritual = RitualRegistry.getFirstForPentacle(definition.getId());
                int placedBowls = ritual == null ? 0 : this.placeDebugSacrificialBowls(worldIn, center, ritual);
                player.sendStatusMessage(new TextComponentTranslation("occultism.pentacle_debug.placed",
                        debugIndex % definitions.size() + 1, definitions.size(), definition.getDisplayName(),
                        definition.getId(), placed, center.getX(), center.getY(), center.getZ()), false);
                if (ritual != null) {
                    player.sendStatusMessage(new TextComponentTranslation("occultism.pentacle_debug.placed_ritual",
                            ritual.getDisplayName(), placedBowls,
                            ritual.getActivationIngredient().createDisplayStack().getDisplayName()), false);
                }
                RitualDebugMessages.sendPentacleRituals(player, definition);
                return EnumActionResult.SUCCESS;
            }

            List<PentacleMatch> matches = PentacleDetector.findMatches(worldIn, pos);
            if (matches.isEmpty()) {
                player.sendStatusMessage(new TextComponentTranslation("occultism.pentacle_debug.none"), false);
            } else {
                PentacleMatch match = matches.get(0);
                player.sendStatusMessage(new TextComponentTranslation("occultism.pentacle_debug.found",
                        match.getDefinition().getDisplayName(), match.getDefinition().getId(),
                        match.getRequiredGlyphs(), match.getCenter().getX(), match.getCenter().getY(),
                        match.getCenter().getZ()), false);
                RitualDebugMessages.sendPentacleRituals(player, match.getDefinition());
            }
        }

        return EnumActionResult.SUCCESS;
    }

    private boolean canPlaceDebugPentacle(EntityPlayer player) {
        return player instanceof EntityPlayerMP && player.capabilities.isCreativeMode
                && player.canUseCommand(2, "occultism");
    }

    private int getDebugPentacleIndex(EntityPlayer player) {
        return DEBUG_PENTACLE_INDEX.containsKey(player.getUniqueID())
                ? DEBUG_PENTACLE_INDEX.get(player.getUniqueID()) : 0;
    }

    private void advanceDebugPentacleIndex(EntityPlayer player, int index, int definitionCount) {
        DEBUG_PENTACLE_INDEX.put(player.getUniqueID(), (index + 1) % definitionCount);
    }

    private int placeDebugPentacle(World world, BlockPos center, PentacleDefinition definition) {
        int placed = 0;
        this.clearDebugGlyphs(world, center, 10);
        if (world.isAirBlock(center) || world.getBlockState(center).getBlock().isReplaceable(world, center)
                || world.getBlockState(center).getBlock() == ModBlocks.GOLDEN_SACRIFICIAL_BOWL) {
            world.setBlockState(center, ModBlocks.GOLDEN_SACRIFICIAL_BOWL.getDefaultState(), 3);
        }
        for (int z = 0; z < definition.getHeight(); z++) {
            String row = definition.getPattern()[z];
            for (int x = 0; x < row.length(); x++) {
                char symbol = row.charAt(x);
                BlockPos target = center.add(x - definition.getCenterX(), 0, z - definition.getCenterZ());
                if (target.equals(center)) {
                    continue;
                }

                BlockChalkGlyph glyph = this.getGlyphForSymbol(symbol);
                if (glyph != null) {
                    if (this.placeDebugGlyph(world, target, glyph)) {
                        placed++;
                    }
                    continue;
                }

                if (this.placeDebugRequirement(world, target, symbol)) {
                    placed++;
                }
            }
        }
        return placed;
    }

    private boolean placeDebugGlyph(World world, BlockPos target, BlockChalkGlyph glyph) {
        if (!this.canReplaceDebugPentacleBlock(world, target)) {
            return false;
        }
        if (!glyph.canPlaceBlockAt(world, target)) {
            return false;
        }
        world.setBlockState(target, glyph.getDefaultState().withProperty(BlockChalkGlyph.SIGN,
                glyph.getRandomSign(world.rand)), 3);
        return true;
    }

    private boolean placeDebugRequirement(World world, BlockPos target, char symbol) {
        IBlockState state = this.getRequirementState(symbol);
        if (state == null || !this.canReplaceDebugPentacleBlock(world, target)) {
            return false;
        }
        if (!state.getBlock().canPlaceBlockAt(world, target)) {
            return false;
        }
        world.setBlockState(target, state, 3);
        if (symbol == '2' || symbol == '4') {
            this.configureSkull(world, target, symbol == '4' ? 1 : 0);
        }
        return true;
    }

    private IBlockState getRequirementState(char symbol) {
        switch (symbol) {
            case '1':
                return ModBlocks.LARGE_CANDLE.getDefaultState();
            case '2':
            case '4':
                return Blocks.SKULL.getDefaultState().withProperty(BlockSkull.FACING, EnumFacing.UP);
            case '3':
                return ModBlocks.SPIRIT_ATTUNED_CRYSTAL.getDefaultState();
            default:
                return null;
        }
    }

    private void configureSkull(World world, BlockPos target, int skullType) {
        TileEntity tileEntity = world.getTileEntity(target);
        if (tileEntity instanceof TileEntitySkull) {
            TileEntitySkull skull = (TileEntitySkull) tileEntity;
            skull.setType(skullType);
            skull.setSkullRotation(world.rand.nextInt(16));
            skull.markDirty();
        }
    }

    private boolean canReplaceDebugPentacleBlock(World world, BlockPos target) {
        IBlockState current = world.getBlockState(target);
        Block block = current.getBlock();
        return block instanceof BlockChalkGlyph || this.isDebugRequirementBlock(world, target)
                || world.isAirBlock(target) || block.isReplaceable(world, target);
    }

    private boolean isDebugRequirementBlock(World world, BlockPos target) {
        Block block = world.getBlockState(target).getBlock();
        if (block == ModBlocks.LARGE_CANDLE || block == ModBlocks.SPIRIT_ATTUNED_CRYSTAL || block == Blocks.SKULL) {
            return true;
        }
        return false;
    }

    private int placeDebugSacrificialBowls(World world, BlockPos center, RitualDefinition ritual) {
        int placed = 0;
        for (RitualIngredient ingredient : ritual.getAdditionalIngredients()) {
            BlockPos bowlPos = this.findDebugBowlPosition(world, center);
            if (bowlPos == null) {
                break;
            }
            world.setBlockState(bowlPos, ModBlocks.SACRIFICIAL_BOWL.getDefaultState(), 3);
            if (world.getTileEntity(bowlPos) instanceof TileEntitySacrificialBowl) {
                ((TileEntitySacrificialBowl) world.getTileEntity(bowlPos)).setStack(ingredient.createDisplayStack());
                placed++;
            }
        }
        return placed;
    }

    private BlockPos findDebugBowlPosition(World world, BlockPos center) {
        for (int radius = 8; radius >= 2; radius--) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    if (Math.abs(x) != radius && Math.abs(z) != radius) {
                        continue;
                    }
                    BlockPos target = center.add(x, 0, z);
                    if (this.canPlaceDebugBowl(world, target)) {
                        return target;
                    }
                }
            }
        }
        return null;
    }

    private boolean canPlaceDebugBowl(World world, BlockPos target) {
        IBlockState current = world.getBlockState(target);
        if (current.getBlock() instanceof BlockChalkGlyph || current.getBlock() == ModBlocks.GOLDEN_SACRIFICIAL_BOWL
                || current.getBlock() == ModBlocks.SACRIFICIAL_BOWL || this.isDebugRequirementBlock(world, target)) {
            return false;
        }
        return world.isAirBlock(target) || current.getBlock().isReplaceable(world, target);
    }

    private void clearDebugGlyphs(World world, BlockPos center, int radius) {
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                BlockPos target = center.add(x, 0, z);
                if (world.getBlockState(target).getBlock() instanceof BlockChalkGlyph
                        || world.getBlockState(target).getBlock() == ModBlocks.SACRIFICIAL_BOWL
                        || this.isDebugRequirementBlock(world, target)) {
                    world.setBlockToAir(target);
                }
            }
        }
    }

    private BlockChalkGlyph getGlyphForSymbol(char symbol) {
        switch (symbol) {
            case 'W':
                return ModBlocks.CHALK_GLYPH_WHITE;
            case 'S':
                return ModBlocks.CHALK_GLYPH_LIGHT_GRAY;
            case 'G':
                return ModBlocks.CHALK_GLYPH_GRAY;
            case 'K':
                return ModBlocks.CHALK_GLYPH_BLACK;
            case 'B':
                return ModBlocks.CHALK_GLYPH_BROWN;
            case 'R':
                return ModBlocks.CHALK_GLYPH_RED;
            case 'O':
                return ModBlocks.CHALK_GLYPH_ORANGE;
            case 'Y':
                return ModBlocks.CHALK_GLYPH_YELLOW;
            case 'L':
                return ModBlocks.CHALK_GLYPH_LIME;
            case 'E':
                return ModBlocks.CHALK_GLYPH_GREEN;
            case 'C':
                return ModBlocks.CHALK_GLYPH_CYAN;
            case 'A':
                return ModBlocks.CHALK_GLYPH_LIGHT_BLUE;
            case 'U':
                return ModBlocks.CHALK_GLYPH_BLUE;
            case 'X':
                return ModBlocks.CHALK_GLYPH_PURPLE;
            case 'M':
                return ModBlocks.CHALK_GLYPH_MAGENTA;
            case 'P':
                return ModBlocks.CHALK_GLYPH_PINK;
            default:
                return null;
        }
    }
}
