package com.occultism.occultism.compat.patchouli;

import com.occultism.occultism.ritual.PentacleDefinition;
import com.occultism.occultism.ritual.PentacleDetector;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import vazkii.patchouli.common.multiblock.Multiblock;
import vazkii.patchouli.common.util.RotationUtil;

/** Patchouli's state predicates alone cannot distinguish 1.12 skull tile data. */
public class PentacleMultiblock extends Multiblock {
    private final PentacleDefinition definition;

    public PentacleMultiblock(PentacleDefinition definition, String[][] pattern, Object... mappings) {
        super(pattern, mappings);
        this.definition = definition;
    }

    @Override
    public boolean test(World world, BlockPos origin, int x, int y, int z, Rotation rotation) {
        if (!super.test(world, origin, x, y, z, rotation)) {
            return false;
        }
        char symbol = this.definition.getPattern()[z].charAt(x);
        if ("24NZ".indexOf(symbol) < 0) {
            return true;
        }
        BlockPos target = origin.add(RotationUtil.x(rotation, x, z), y, RotationUtil.z(rotation, x, z));
        // Definition-aware check: the preview places blocks using this pentacle's own symbol
        // meanings (e.g. summon_wild_afrit uses C for candles and P for purple chalk), so a
        // definition-blind check would reject those cells and make the preview unplaceable.
        return PentacleDetector.matchesRequiredSymbol(world, target, symbol, world.getBlockState(target),
                this.definition);
    }

    @Override
    public TileEntity getTileEntity(BlockPos pos) {
        TileEntity tile = super.getTileEntity(pos);
        if (tile instanceof TileEntitySkull && pos.getX() >= 0 && pos.getZ() >= 0
                && pos.getX() < this.definition.getWidth() && pos.getZ() < this.definition.getHeight()) {
            char symbol = this.definition.getPattern()[pos.getZ()].charAt(pos.getX());
            ((TileEntitySkull) tile).setType(symbol == '4' || symbol == 'N' ? 1 : 0);
        }
        return tile;
    }
}
