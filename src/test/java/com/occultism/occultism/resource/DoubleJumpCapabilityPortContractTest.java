package com.occultism.occultism.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.occultism.occultism.capability.DoubleJumpCapability;

import net.minecraft.nbt.NBTTagCompound;
import org.junit.jupiter.api.Test;

/** Contract coverage for the 1.12 aerial-jump counter. */
class DoubleJumpCapabilityPortContractTest {

    @Test
    void jumpCounterClampsAndRoundTripsThroughNbt() {
        DoubleJumpCapability capability = new DoubleJumpCapability();
        capability.addJump();
        capability.addJump();
        assertEquals(2, capability.getJumps());

        NBTTagCompound encoded = capability.serializeNBT();
        DoubleJumpCapability decoded = new DoubleJumpCapability();
        decoded.deserializeNBT(encoded);
        assertEquals(2, decoded.getJumps());

        decoded.setJumps(-4);
        assertEquals(0, decoded.getJumps());
    }
}
