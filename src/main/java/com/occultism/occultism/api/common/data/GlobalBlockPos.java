package com.occultism.occultism.api.common.data;

import java.util.Objects;
import java.util.Locale;

import javax.annotation.Nullable;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * A 1.12 equivalent of the upstream {@code GlobalBlockPos}.
 *
 * <p>The dimension is kept as the Forge integer dimension id.  Resolving a
 * world is intentionally left to the caller: this value is a reference only
 * and never loads a dimension or chunk by itself.</p>
 */
public final class GlobalBlockPos implements INBTSerializable<NBTTagCompound> {

    public static final String TAG_X = "X";
    public static final String TAG_Y = "Y";
    public static final String TAG_Z = "Z";
    public static final String TAG_DIMENSION = "Dimension";
    /** Modern Occultism schema (packed position + dimension resource id). */
    public static final String TAG_UPSTREAM_POS = "pos";
    public static final String TAG_UPSTREAM_DIMENSION = "dimension";

    @Nullable
    private BlockPos pos;
    private int dimension;

    public GlobalBlockPos() {
        this(null, 0);
    }

    public GlobalBlockPos(@Nullable BlockPos pos, int dimension) {
        this.pos = pos == null ? null : pos.toImmutable();
        this.dimension = dimension;
    }

    public GlobalBlockPos(@Nullable BlockPos pos, @Nullable World world) {
        this(pos, world == null ? 0 : world.provider.getDimension());
    }

    public static GlobalBlockPos from(NBTTagCompound compound) {
        GlobalBlockPos result = new GlobalBlockPos();
        result.deserializeNBT(compound);
        return result;
    }

    public static GlobalBlockPos from(TileEntity tile) {
        if (tile == null) {
            return new GlobalBlockPos();
        }
        return new GlobalBlockPos(tile.getPos(), tile.getWorld());
    }

    @Nullable
    public BlockPos getPos() {
        return this.pos;
    }

    public int getDimension() {
        return this.dimension;
    }

    public boolean isPresent() {
        return this.pos != null;
    }

    /** Returns true only when the supplied world is the referenced dimension. */
    public boolean isIn(@Nullable World world) {
        return this.pos != null && world != null && world.provider.getDimension() == this.dimension;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        return this.write(new NBTTagCompound());
    }

    @Override
    public void deserializeNBT(NBTTagCompound compound) {
        this.read(compound);
    }

    public NBTTagCompound write(NBTTagCompound compound) {
        if (compound == null) {
            compound = new NBTTagCompound();
        }
        if (this.pos == null) {
            return compound;
        }
        compound.setInteger(TAG_X, this.pos.getX());
        compound.setInteger(TAG_Y, this.pos.getY());
        compound.setInteger(TAG_Z, this.pos.getZ());
        compound.setInteger(TAG_DIMENSION, this.dimension);
        // Keep the 1.12 keys above for existing saves, while also emitting the
        // upstream format so references can travel between modern and legacy
        // Occultism worlds without a one-way conversion.
        compound.setLong(TAG_UPSTREAM_POS, this.pos.toLong());
        compound.setString(TAG_UPSTREAM_DIMENSION, dimensionName(this.dimension));
        return compound;
    }

    public void read(@Nullable NBTTagCompound compound) {
        if (compound == null) {
            this.pos = null;
            this.dimension = 0;
            return;
        }

        if (compound.hasKey(TAG_X, 3) && compound.hasKey(TAG_Y, 3) && compound.hasKey(TAG_Z, 3)) {
            Integer dimension = readDimension(compound, TAG_DIMENSION, TAG_UPSTREAM_DIMENSION);
            if (dimension == null) {
                this.pos = null;
                this.dimension = 0;
                return;
            }
            BlockPos legacyPos = new BlockPos(compound.getInteger(TAG_X), compound.getInteger(TAG_Y),
                    compound.getInteger(TAG_Z));
            // A ported link can carry both schemas.  Reject conflicting
            // representations instead of silently trusting whichever one is
            // checked first by a caller.
            if (compound.hasKey(TAG_UPSTREAM_POS, 4)) {
                Integer modernDimension = readDimension(compound, TAG_UPSTREAM_DIMENSION, TAG_DIMENSION);
                if (modernDimension == null || modernDimension.intValue() != dimension.intValue()
                        || !legacyPos.equals(BlockPos.fromLong(compound.getLong(TAG_UPSTREAM_POS)))) {
                    this.pos = null;
                    this.dimension = 0;
                    return;
                }
            }
            this.pos = legacyPos;
            this.dimension = dimension;
            return;
        }

        // Modern Occultism stores BlockPos as a packed long under "pos" and
        // the dimension as a resource-location string under "dimension".
        if (compound.hasKey(TAG_UPSTREAM_POS, 4)) {
            Integer dimension = readDimension(compound, TAG_UPSTREAM_DIMENSION, TAG_DIMENSION);
            if (dimension == null) {
                this.pos = null;
                this.dimension = 0;
                return;
            }
            this.pos = BlockPos.fromLong(compound.getLong(TAG_UPSTREAM_POS));
            this.dimension = dimension;
            return;
        }

        this.pos = null;
        this.dimension = 0;
    }

    /** Reads either a legacy integer or a modern dimension resource id. */
    @Nullable
    private static Integer readDimension(NBTTagCompound compound, String preferred, String fallback) {
        if (compound.hasKey(preferred, 3)) {
            return compound.getInteger(preferred);
        }
        if (compound.hasKey(preferred, 8)) {
            return parseDimensionName(compound.getString(preferred));
        }
        if (compound.hasKey(fallback, 3)) {
            return compound.getInteger(fallback);
        }
        if (compound.hasKey(fallback, 8)) {
            return parseDimensionName(compound.getString(fallback));
        }
        return null;
    }

    /** Maps vanilla resource ids and numeric custom ids to 1.12 dimensions. */
    @Nullable
    private static Integer parseDimensionName(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if ("minecraft:overworld".equals(normalized) || "overworld".equals(normalized)) {
            return 0;
        }
        if ("minecraft:the_nether".equals(normalized) || "the_nether".equals(normalized)) {
            return -1;
        }
        if ("minecraft:the_end".equals(normalized) || "the_end".equals(normalized)) {
            return 1;
        }
        try {
            return Integer.valueOf(normalized);
        } catch (NumberFormatException ignored) {
            // A custom 1.20 dimension key has no stable 1.12 integer mapping;
            // reject it rather than accidentally resolving the overworld.
            return null;
        }
    }

    private static String dimensionName(int dimension) {
        if (dimension == 0) {
            return "minecraft:overworld";
        }
        if (dimension == -1) {
            return "minecraft:the_nether";
        }
        if (dimension == 1) {
            return "minecraft:the_end";
        }
        return Integer.toString(dimension);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof GlobalBlockPos)) {
            return false;
        }
        GlobalBlockPos that = (GlobalBlockPos) other;
        return this.dimension == that.dimension && Objects.equals(this.pos, that.pos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.pos, this.dimension);
    }

    @Override
    public String toString() {
        return this.pos == null ? "[unbound]"
                : "[dimension=" + this.dimension + ", x=" + this.pos.getX() + ", y=" + this.pos.getY()
                        + ", z=" + this.pos.getZ() + "]";
    }
}
