package com.occultism.occultism.block;

import java.util.Random;

import net.minecraft.block.BlockTorch;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Spirit fire torch, ported from upstream {@code SpiritTorchBlock}.
 *
 * <p>Upstream has two block classes because 1.13+ splits floor and wall torches. 1.12 keeps a
 * single {@link BlockTorch} with a {@code facing} property, so {@code spirit_wall_torch} does not
 * exist here and the standing block covers both placements - the same merge this port already
 * makes for other version-split blocks.</p>
 *
 * <p>Upstream emits {@code OccultismParticles.SPIRIT_FIRE_FLAME}; that custom particle type does
 * not exist in this port, so the closest vanilla particle pair (flame plus smoke) is used. This is
 * a visual approximation and is recorded as such.</p>
 */
public class BlockSpiritTorch extends BlockTorch {

    public BlockSpiritTorch() {
        super();
        this.setLightLevel(0.9375F);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void randomDisplayTick(net.minecraft.block.state.IBlockState state, World world, BlockPos pos,
            Random random) {
        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.7D;
        double z = pos.getZ() + 0.5D;
        world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, x, y, z, 0.0D, 0.0D, 0.0D);
        world.spawnParticle(EnumParticleTypes.FLAME, x, y, z, 0.0D, 0.0D, 0.0D);
    }
}
