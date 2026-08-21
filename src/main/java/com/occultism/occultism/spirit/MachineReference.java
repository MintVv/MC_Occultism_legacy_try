package com.occultism.occultism.spirit;

import javax.annotation.Nullable;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * The 1.12 representation of the upstream MachineReference. It intentionally
 * stores only block positions, dimension ids, registry names and handler faces;
 * chunk-load flags and upstream order metadata are not available in this port.
 */
public class MachineReference {

    public static final String TAG_INSERT_X = "InsertX";
    public static final String TAG_INSERT_Y = "InsertY";
    public static final String TAG_INSERT_Z = "InsertZ";
    public static final String TAG_INSERT_DIMENSION = "InsertDimension";
    public static final String TAG_INSERT_FACING = "InsertFacing";
    public static final String TAG_INSERT_REGISTRY = "InsertRegistryName";
    public static final String TAG_EXTRACT_X = "ExtractX";
    public static final String TAG_EXTRACT_Y = "ExtractY";
    public static final String TAG_EXTRACT_Z = "ExtractZ";
    public static final String TAG_EXTRACT_DIMENSION = "ExtractDimension";
    public static final String TAG_EXTRACT_FACING = "ExtractFacing";
    public static final String TAG_EXTRACT_REGISTRY = "ExtractRegistryName";
    public static final String TAG_CUSTOM_NAME = "CustomName";

    @Nullable
    private BlockPos insertPosition;
    private int insertDimension;
    private EnumFacing insertFacing = EnumFacing.UP;
    private String insertRegistryName = "";
    @Nullable
    private BlockPos extractPosition;
    private int extractDimension;
    private EnumFacing extractFacing = EnumFacing.DOWN;
    private String extractRegistryName = "";
    private String customName = "";

    public MachineReference() {
    }

    public MachineReference(BlockPos insertPosition, int insertDimension, EnumFacing insertFacing,
            BlockPos extractPosition, int extractDimension, EnumFacing extractFacing) {
        this.insertPosition = insertPosition;
        this.insertDimension = insertDimension;
        this.insertFacing = insertFacing == null ? EnumFacing.UP : insertFacing;
        this.extractPosition = extractPosition;
        this.extractDimension = extractDimension;
        this.extractFacing = extractFacing == null ? EnumFacing.DOWN : extractFacing;
    }

    public static MachineReference from(TileEntity insertTile, EnumFacing insertFacing) {
        if (insertTile == null) {
            return new MachineReference();
        }
        int dimension = insertTile.getWorld() == null ? 0 : insertTile.getWorld().provider.getDimension();
        String registry = insertTile.getBlockType().getRegistryName() == null ? ""
                : insertTile.getBlockType().getRegistryName().toString();
        MachineReference reference = new MachineReference(insertTile.getPos(), dimension,
                insertFacing, insertTile.getPos(), dimension, EnumFacing.DOWN);
        reference.insertRegistryName = registry;
        reference.extractRegistryName = registry;
        return reference;
    }

    public static MachineReference from(NBTTagCompound compound) {
        MachineReference reference = new MachineReference();
        reference.readFromNBT(compound);
        return reference;
    }

    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        if (compound == null) {
            compound = new NBTTagCompound();
        }
        writePos(compound, "Insert", this.insertPosition);
        compound.setInteger(TAG_INSERT_DIMENSION, this.insertDimension);
        compound.setInteger(TAG_INSERT_FACING, this.insertFacing.getIndex());
        if (!this.insertRegistryName.isEmpty()) {
            compound.setString(TAG_INSERT_REGISTRY, this.insertRegistryName);
        }

        writePos(compound, "Extract", this.extractPosition);
        compound.setInteger(TAG_EXTRACT_DIMENSION, this.extractDimension);
        compound.setInteger(TAG_EXTRACT_FACING, this.extractFacing.getIndex());
        if (!this.extractRegistryName.isEmpty()) {
            compound.setString(TAG_EXTRACT_REGISTRY, this.extractRegistryName);
        }
        if (!this.customName.isEmpty()) {
            compound.setString(TAG_CUSTOM_NAME, this.customName);
        }
        return compound;
    }

    public void readFromNBT(NBTTagCompound compound) {
        if (compound == null) {
            this.insertPosition = null;
            this.extractPosition = null;
            this.insertDimension = 0;
            this.extractDimension = 0;
            this.insertFacing = EnumFacing.UP;
            this.extractFacing = EnumFacing.DOWN;
            this.insertRegistryName = "";
            this.extractRegistryName = "";
            this.customName = "";
            return;
        }
        this.insertPosition = readPos(compound, "Insert");
        this.insertDimension = compound.getInteger(TAG_INSERT_DIMENSION);
        this.insertFacing = facing(compound.getInteger(TAG_INSERT_FACING), EnumFacing.UP);
        this.insertRegistryName = compound.getString(TAG_INSERT_REGISTRY);
        this.extractPosition = readPos(compound, "Extract");
        this.extractDimension = compound.getInteger(TAG_EXTRACT_DIMENSION);
        this.extractFacing = facing(compound.getInteger(TAG_EXTRACT_FACING), EnumFacing.DOWN);
        this.extractRegistryName = compound.getString(TAG_EXTRACT_REGISTRY);
        this.customName = compound.getString(TAG_CUSTOM_NAME);
    }

    @Nullable
    public BlockPos getInsertPosition() {
        return this.insertPosition;
    }

    public void setInsertPosition(BlockPos position, int dimension, EnumFacing facing, String registryName) {
        this.insertPosition = position;
        this.insertDimension = dimension;
        this.insertFacing = facing == null ? EnumFacing.UP : facing;
        this.insertRegistryName = registryName == null ? "" : registryName;
    }

    public int getInsertDimension() {
        return this.insertDimension;
    }

    public EnumFacing getInsertFacing() {
        return this.insertFacing;
    }

    @Nullable
    public BlockPos getExtractPosition() {
        return this.extractPosition;
    }

    public void setExtractPosition(BlockPos position, int dimension, EnumFacing facing, String registryName) {
        this.extractPosition = position;
        this.extractDimension = dimension;
        this.extractFacing = facing == null ? EnumFacing.DOWN : facing;
        this.extractRegistryName = registryName == null ? "" : registryName;
    }

    public int getExtractDimension() {
        return this.extractDimension;
    }

    public EnumFacing getExtractFacing() {
        return this.extractFacing;
    }

    public String getInsertRegistryName() {
        return this.insertRegistryName;
    }

    public String getExtractRegistryName() {
        return this.extractRegistryName;
    }

    public String getCustomName() {
        return this.customName;
    }

    public void setCustomName(String customName) {
        this.customName = customName == null ? "" : customName;
    }

    public boolean isConfigured() {
        return this.insertPosition != null;
    }

    /** Both sides are required by the machine manager before any transfer starts. */
    public boolean isStrictlyConfigured() {
        return this.insertPosition != null && this.extractPosition != null && this.insertFacing != null
                && this.extractFacing != null;
    }

    public boolean hasInsertEndpoint() {
        return this.insertPosition != null && this.insertFacing != null;
    }

    public boolean hasExtractEndpoint() {
        return this.extractPosition != null && this.extractFacing != null;
    }

    public boolean isInsertEndpointLoaded(@Nullable World world) {
        return world != null && this.hasInsertEndpoint() && this.insertDimension == world.provider.getDimension()
                && world.isBlockLoaded(this.insertPosition);
    }

    public boolean isExtractEndpointLoaded(@Nullable World world) {
        return world != null && this.hasExtractEndpoint() && this.extractDimension == world.provider.getDimension()
                && world.isBlockLoaded(this.extractPosition);
    }

    /**
     * Checks only the loaded, active world.  This method deliberately never
     * loads a chunk and rejects references into another dimension.
     */
    public boolean isValidFor(World world) {
        if (!this.isStrictlyConfigured() || world == null) {
            return false;
        }
        int dimension = world.provider.getDimension();
        if (!this.isInsertEndpointLoaded(world) || !this.isExtractEndpointLoaded(world)) {
            return false;
        }
        TileEntity insertTile = world.getTileEntity(this.insertPosition);
        TileEntity extractTile = world.getTileEntity(this.extractPosition);
        return this.matchesInsertTile(insertTile) && this.matchesExtractTile(extractTile);
    }

    public boolean matchesInsertTile(@Nullable TileEntity tile) {
        return tile != null && this.matchesTile(tile, this.insertPosition, this.insertRegistryName);
    }

    public boolean matchesExtractTile(@Nullable TileEntity tile) {
        return tile != null && this.matchesTile(tile, this.extractPosition, this.extractRegistryName);
    }

    private static boolean matchesTile(TileEntity tile, @Nullable BlockPos expectedPosition,
            @Nullable String expectedRegistryName) {
        if (expectedPosition == null || !expectedPosition.equals(tile.getPos())) {
            return false;
        }
        if (expectedRegistryName == null || expectedRegistryName.isEmpty()) {
            return true;
        }
        ResourceLocation actual = tile.getBlockType().getRegistryName();
        return actual != null && expectedRegistryName.equals(actual.toString());
    }

    private static void writePos(NBTTagCompound compound, String prefix, @Nullable BlockPos position) {
        if (position == null) {
            return;
        }
        compound.setInteger(prefix + "X", position.getX());
        compound.setInteger(prefix + "Y", position.getY());
        compound.setInteger(prefix + "Z", position.getZ());
    }

    @Nullable
    private static BlockPos readPos(NBTTagCompound compound, String prefix) {
        if (!compound.hasKey(prefix + "X", 3) || !compound.hasKey(prefix + "Y", 3)
                || !compound.hasKey(prefix + "Z", 3)) {
            return null;
        }
        return new BlockPos(compound.getInteger(prefix + "X"), compound.getInteger(prefix + "Y"),
                compound.getInteger(prefix + "Z"));
    }

    private static EnumFacing facing(int index, EnumFacing fallback) {
        for (EnumFacing facing : EnumFacing.values()) {
            if (facing.getIndex() == index) {
                return facing;
            }
        }
        return fallback;
    }
}
