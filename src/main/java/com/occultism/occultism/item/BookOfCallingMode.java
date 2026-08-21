package com.occultism.occultism.item;

import com.occultism.occultism.spirit.SpiritJobType;

public enum BookOfCallingMode {
    SET_BASE("set_base", true),
    SET_DEPOSIT("set_deposit", false),
    SET_EXTRACT("set_extract", false),
    SET_STORAGE_CONTROLLER("set_storage_controller", false),
    SET_MANAGED_MACHINE("set_managed_machine", false);

    private static final String TRANSLATION_KEY_BASE = "enum.occultism.book_of_calling.item_mode.";

    private final String translationSuffix;
    private final boolean hasSize;

    BookOfCallingMode(String translationSuffix, boolean hasSize) {
        this.translationSuffix = translationSuffix;
        this.hasSize = hasSize;
    }

    public String getTranslationKey() {
        return TRANSLATION_KEY_BASE + this.translationSuffix;
    }

    public boolean hasSize() {
        return this.hasSize;
    }

    public static BookOfCallingMode[] getModesForJob(SpiritJobType jobType) {
        if (jobType == SpiritJobType.MANAGE_MACHINE) {
            return new BookOfCallingMode[] { SET_STORAGE_CONTROLLER, SET_MANAGED_MACHINE, SET_EXTRACT };
        }
        if (jobType == SpiritJobType.TRANSPORT_ITEMS) {
            return new BookOfCallingMode[] { SET_EXTRACT, SET_DEPOSIT };
        }
        if (jobType == SpiritJobType.CLEANER || jobType == SpiritJobType.FARMER || jobType == SpiritJobType.LUMBERJACK) {
            return new BookOfCallingMode[] { SET_BASE, SET_DEPOSIT };
        }
        return new BookOfCallingMode[] { SET_BASE, SET_EXTRACT, SET_DEPOSIT };
    }

    public static BookOfCallingMode byOrdinal(int ordinal) {
        BookOfCallingMode[] values = values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : SET_BASE;
    }
}
