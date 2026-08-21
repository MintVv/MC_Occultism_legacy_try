package com.occultism.occultism.registry;

import com.occultism.occultism.Tags;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

public final class ModCreativeTabs {

    public static final CreativeTabs OCCULTISM = new CreativeTabs(Tags.MOD_ID) {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(ModItems.DICTIONARY_OF_SPIRITS);
        }
    };

    private ModCreativeTabs() {
    }
}
