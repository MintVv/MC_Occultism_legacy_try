package com.occultism.occultism.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

public class ContainerBookOfCalling extends Container {

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return true;
    }
}
