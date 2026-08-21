package com.occultism.occultism.block;

import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

public class BlockOtherplanksTrapDoor extends BlockTrapDoor {

    public BlockOtherplanksTrapDoor() {
        super(Material.WOOD);
        this.setSoundType(SoundType.WOOD);
    }
}
