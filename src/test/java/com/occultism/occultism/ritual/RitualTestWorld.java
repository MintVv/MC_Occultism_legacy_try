package com.occultism.occultism.ritual;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.occultism.occultism.registry.ModBlocks;
import com.occultism.occultism.tileentity.TileEntitySacrificialBowl;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.profiler.Profiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.WorldInfo;

/** In-memory block/tile/item fixture; no server, rendering, chunks, or disk saves. */
final class RitualTestWorld extends World {
    final Map<BlockPos, TileEntity> tiles = new HashMap<>();
    final Map<BlockPos, IBlockState> states = new HashMap<>();
    final Set<BlockPos> unloaded = new HashSet<>();
    final List<EntityItem> drops = new ArrayList<>();
    /** Every entity the bowl tried to spawn, so dispatch can be asserted per ritual type. */
    final List<Entity> spawned = new ArrayList<>();
    int updates;
    boolean loaded = true;

    RitualTestWorld(boolean client) {
        super(null, new WorldInfo(new NBTTagCompound()), new WorldProviderSurface(), new Profiler(), client);
        this.getWorldInfo().setDifficulty(EnumDifficulty.NORMAL);
    }

    TileEntitySacrificialBowl installBowl(BlockPos pos, boolean golden) {
        TileEntitySacrificialBowl bowl = new TileEntitySacrificialBowl();
        bowl.setWorld(this);
        bowl.setPos(pos);
        this.tiles.put(pos, bowl);
        this.states.put(pos, (golden ? ModBlocks.GOLDEN_SACRIFICIAL_BOWL : ModBlocks.SACRIFICIAL_BOWL).getDefaultState());
        this.loadedTileEntityList.add(bowl);
        return bowl;
    }

    void placeFoliotPentacle(BlockPos center) {
        for (PentacleDetector.PentacleSymbol symbol : PentacleDetector.getRequiredSymbols(
                PentacleDetector.getDefinitionById("summon_foliot"), center)) {
            this.states.put(symbol.getPos(), (symbol.getSymbol() == 'W'
                    ? ModBlocks.CHALK_GLYPH_WHITE : ModBlocks.LARGE_CANDLE).getDefaultState());
        }
    }

    @Override
    protected IChunkProvider createChunkProvider() { return null; }

    @Override
    protected boolean isChunkLoaded(int x, int z, boolean allowEmpty) { return this.loaded; }

    @Override
    public boolean isBlockLoaded(BlockPos pos) { return this.loaded && !this.unloaded.contains(pos); }

    @Override
    public BlockPos getSpawnPoint() { return BlockPos.ORIGIN; }

    @Override
    public IBlockState getBlockState(BlockPos pos) {
        if (!this.isBlockLoaded(pos)) {
            throw new AssertionError("Attempted to read an unloaded position: " + pos);
        }
        return this.states.getOrDefault(pos, Blocks.AIR.getDefaultState());
    }

    @Override
    public TileEntity getTileEntity(BlockPos pos) { return this.tiles.get(pos); }

    /**
     * The fixture has no chunk provider, so the real lookup would dereference a null chunk. A
     * ritual only needs some difficulty instance for the spawn hook.
     */
    @Override
    public net.minecraft.world.DifficultyInstance getDifficultyForLocation(BlockPos pos) {
        return new net.minecraft.world.DifficultyInstance(EnumDifficulty.NORMAL, 0L, 0L, 0.0F);
    }

    /** The bowl scans for nearby players to fire advancements; there are none in a unit test. */
    @Override
    public <T extends Entity> List<T> getEntitiesWithinAABB(Class<? extends T> clazz,
            net.minecraft.util.math.AxisAlignedBB aabb) {
        return new ArrayList<>();
    }

    @Override
    public void markChunkDirty(BlockPos pos, TileEntity tile) { }

    @Override
    public void updateComparatorOutputLevel(BlockPos pos, Block block) { }

    @Override
    public void notifyBlockUpdate(BlockPos pos, IBlockState oldState, IBlockState newState, int flags) {
        this.updates++;
    }

    @Override
    public boolean spawnEntity(Entity entity) {
        this.spawned.add(entity);
        if (entity instanceof EntityItem) {
            this.drops.add((EntityItem) entity);
        }
        return true;
    }

    @Override
    public void playSound(EntityPlayer player, double x, double y, double z, SoundEvent sound,
            SoundCategory category, float volume, float pitch) { }
}
