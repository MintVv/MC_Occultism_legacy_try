package com.occultism.occultism.item;

public enum WorkAreaSize {
    SMALL(16, "small"),
    MEDIUM(32, "medium"),
    LARGE(64, "large");

    private static final String TRANSLATION_KEY_BASE = "enum.occultism.work_area_size.";

    private final int value;
    private final String translationSuffix;

    WorkAreaSize(int value, String translationSuffix) {
        this.value = value;
        this.translationSuffix = translationSuffix;
    }

    public int getValue() {
        return this.value;
    }

    public String getTranslationKey() {
        return TRANSLATION_KEY_BASE + this.translationSuffix;
    }

    public static WorkAreaSize byOrdinal(int ordinal) {
        WorkAreaSize[] values = values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : SMALL;
    }
}
