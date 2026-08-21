package com.occultism.occultism.tileentity;

/**
 * Sort keys used by the storage controller terminal.
 *
 * <p>The values intentionally match the upstream controller protocol:
 * amount=0, name=1 and mod=2.</p>
 */
public enum StorageSortType {
    AMOUNT(0),
    NAME(1),
    MOD(2);

    private final int value;

    StorageSortType(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public static StorageSortType byValue(int value) {
        for (StorageSortType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        return AMOUNT;
    }

    public StorageSortType next() {
        return values()[(this.ordinal() + 1) % values().length];
    }
}
