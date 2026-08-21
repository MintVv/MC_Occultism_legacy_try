package com.occultism.occultism.registry;

import com.occultism.occultism.advancement.RitualTrigger;
import com.occultism.occultism.advancement.FamiliarTrigger;
import net.minecraft.advancements.CriteriaTriggers;

public final class ModAdvancements {

    public static final RitualTrigger RITUAL = new RitualTrigger();
    public static final FamiliarTrigger FAMILIAR = new FamiliarTrigger();

    private static boolean registered;

    private ModAdvancements() {
    }

    public static void register() {
        if (!registered) {
            CriteriaTriggers.register(RITUAL);
            CriteriaTriggers.register(FAMILIAR);
            registered = true;
        }
    }
}
