package com.occultism.occultism.config;

import com.occultism.occultism.Tags;

import net.minecraftforge.common.config.Config;

@Config(modid = Tags.MOD_ID, name = Tags.MOD_ID, category = "")
public final class ModConfig {

    @Config.Name("spirit_job")
    @Config.Comment("Spirit job and familiar settings.")
    public static final SpiritJobSettings SPIRIT_JOBS = new SpiritJobSettings();

    @Config.Name("storage")
    @Config.Comment("Storage controller capacity settings.")
    public static final StorageSettings STORAGE = new StorageSettings();

    private ModConfig() {
    }

    public static final class SpiritJobSettings {

        @Config.Name("blacksmithFamiliarRepairChance")
        @Config.Comment("Chance for a blacksmith familiar to repair one item by 2 durability when stone is picked up.")
        @Config.RangeDouble(min = 0.0D)
        public double blacksmithFamiliarRepairChance = 0.05D;

        @Config.Name("blacksmithFamiliarUpgradeCost")
        @Config.Comment("Iron units required for a blacksmith familiar to upgrade another familiar.")
        @Config.RangeInt(min = 1)
        public int blacksmithFamiliarUpgradeCost = 18;

        @Config.Name("blacksmithFamiliarUpgradeCooldown")
        @Config.Comment("Cooldown in ticks between blacksmith familiar upgrades.")
        @Config.RangeInt(min = 0)
        public int blacksmithFamiliarUpgradeCooldown = 20 * 20;
    }

    public static final class StorageSettings {

        @Config.Name("controllerBaseSlots")
        @Config.Comment("Base storage slots provided by a newly placed controller.")
        @Config.RangeInt(min = 1)
        public int controllerBaseSlots = 128;

        @Config.Name("stabilizerTier1Slots")
        @Config.Comment("Additional slots provided by a Tier 1 storage stabilizer.")
        @Config.RangeInt(min = 0)
        public int stabilizerTier1Slots = 256;

        @Config.Name("stabilizerTier2Slots")
        @Config.Comment("Additional slots provided by a Tier 2 storage stabilizer.")
        @Config.RangeInt(min = 0)
        public int stabilizerTier2Slots = 512;

        @Config.Name("stabilizerTier3Slots")
        @Config.Comment("Additional slots provided by a Tier 3 storage stabilizer.")
        @Config.RangeInt(min = 0)
        public int stabilizerTier3Slots = 1024;

        @Config.Name("stabilizerTier4Slots")
        @Config.Comment("Additional slots provided by a Tier 4 storage stabilizer.")
        @Config.RangeInt(min = 0)
        public int stabilizerTier4Slots = 2048;

        @Config.Name("controllerStackSize")
        @Config.Comment("Maximum number of items stored in one controller slot.")
        @Config.RangeInt(min = 1)
        public int controllerStackSize = 1024;

        @Config.Name("overrideItemStackSizes")
        @Config.Comment("Use controllerStackSize for every item, instead of each item's normal stack size.")
        public boolean overrideItemStackSizes = true;
    }
}
