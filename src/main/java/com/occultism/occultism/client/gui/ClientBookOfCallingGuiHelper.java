package com.occultism.occultism.client.gui;

import com.occultism.occultism.spirit.MachineReference;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Client-only entry points used by common Book of Calling interaction code. */
@SideOnly(Side.CLIENT)
public final class ClientBookOfCallingGuiHelper {

    private ClientBookOfCallingGuiHelper() {
    }

    public static void openManagedMachine(MachineReference reference) {
        if (reference != null && reference.isStrictlyConfigured()) {
            Minecraft.getMinecraft().displayGuiScreen(new GuiBookOfCallingManagedMachine(reference));
        }
    }
}
