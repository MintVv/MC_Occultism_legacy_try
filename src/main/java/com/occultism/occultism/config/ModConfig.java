package com.occultism.occultism.config;

import com.occultism.occultism.Tags;
import com.occultism.occultism.spirit.SpiritJobType;

import net.minecraftforge.common.config.Config;

@Config(modid = Tags.MOD_ID, name = Tags.MOD_ID, category = "")
public final class ModConfig {

    @Config.Name("spirit_job")
    @Config.Comment("Spirit job and familiar settings.")
    public static final SpiritJobSettings SPIRIT_JOBS = new SpiritJobSettings();

    @Config.Name("storage")
    @Config.Comment("Storage controller capacity settings.")
    public static final StorageSettings STORAGE = new StorageSettings();

    @Config.Name("rituals")
    @Config.Comment("Weather and time ritual permissions, matching the upstream server settings.")
    public static final RitualSettings RITUALS = new RitualSettings();

    private ModConfig() {
    }

    public static final class RitualSettings {
        public boolean enableClearWeatherRitual = true;
        public boolean enableRainWeatherRitual = true;
        public boolean enableThunderWeatherRitual = true;
        public boolean enableDayTimeRitual = true;
        public boolean enableNightTimeRitual = true;

        public boolean isEnabled(SpiritJobType type) {
            switch (type) {
                case CLEAR_WEATHER: return this.enableClearWeatherRitual;
                case RAIN_WEATHER: return this.enableRainWeatherRitual;
                case THUNDER_WEATHER: return this.enableThunderWeatherRitual;
                case DAY_TIME: return this.enableDayTimeRitual;
                case NIGHT_TIME: return this.enableNightTimeRitual;
                default: return false;
            }
        }
    }

    public static final class SpiritJobSettings {

        @Config.Name("crusherFoliot")
        public final ProcessingSpiritSettings crusherFoliot = new ProcessingSpiritSettings(2.0D, 1.0D, 1);

        @Config.Name("crusherDjinni")
        public final ProcessingSpiritSettings crusherDjinni = new ProcessingSpiritSettings(1.0D, 1.5D, 1);

        @Config.Name("crusherAfrit")
        public final ProcessingSpiritSettings crusherAfrit = new ProcessingSpiritSettings(0.5D, 2.0D, 1);

        @Config.Name("crusherMarid")
        public final ProcessingSpiritSettings crusherMarid = new ProcessingSpiritSettings(0.2D, 3.0D, 1);

        @Config.Name("crystallizerFoliot")
        public final ProcessingSpiritSettings crystallizerFoliot = new ProcessingSpiritSettings(1.0D, 1.0D, 1);

        @Config.Name("crystallizerDjinni")
        public final ProcessingSpiritSettings crystallizerDjinni = new ProcessingSpiritSettings(0.5D, 1.5D, 1);

        @Config.Name("crystallizerAfrit")
        public final ProcessingSpiritSettings crystallizerAfrit = new ProcessingSpiritSettings(0.3D, 2.0D, 1);

        @Config.Name("crystallizerMarid")
        public final ProcessingSpiritSettings crystallizerMarid = new ProcessingSpiritSettings(0.1D, 3.0D, 1);

        @Config.Name("smelterFoliot")
        public final ProcessingSpiritSettings smelterFoliot = new ProcessingSpiritSettings(1.0D, 1.0D, 1);

        @Config.Name("smelterDjinni")
        public final ProcessingSpiritSettings smelterDjinni = new ProcessingSpiritSettings(0.5D, 1.0D, 1);

        @Config.Name("smelterAfrit")
        public final ProcessingSpiritSettings smelterAfrit = new ProcessingSpiritSettings(0.1D, 1.0D, 1);

        @Config.Name("smelterMarid")
        public final ProcessingSpiritSettings smelterMarid = new ProcessingSpiritSettings(0.01D, 1.0D, 1);

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

        public static final class ProcessingSpiritSettings {

            @Config.Name("timeMultiplier")
            @Config.Comment("Multiplier applied to the local processing time for this tier.")
            @Config.RangeDouble(min = 0.0D)
            public double timeMultiplier;

            @Config.Name("outputMultiplier")
            @Config.Comment("Multiplier applied to processing output for this tier.")
            @Config.RangeDouble(min = 0.0D)
            public double outputMultiplier;

            @Config.Name("operationCount")
            @Config.Comment("Maximum number of input items processed in one operation.")
            @Config.RangeInt(min = 1)
            public int operationCount;

            private ProcessingSpiritSettings(double timeMultiplier, double outputMultiplier, int operationCount) {
                this.timeMultiplier = timeMultiplier;
                this.outputMultiplier = outputMultiplier;
                this.operationCount = operationCount;
            }
        }
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

        @Config.Name("stabilizerTier5Slots")
        @Config.Comment("Additional slots provided by a Tier 5 storage stabilizer.")
        @Config.RangeInt(min = 0)
        public int stabilizerTier5Slots = 1024;

        @Config.Name("controllerStackSize")
        @Config.Comment("Maximum number of items stored in one controller slot.")
        @Config.RangeInt(min = 1)
        public int controllerStackSize = 1024;

        @Config.Name("overrideItemStackSizes")
        @Config.Comment("Use controllerStackSize for every item, instead of each item's normal stack size.")
        public boolean overrideItemStackSizes = true;
    }
}
