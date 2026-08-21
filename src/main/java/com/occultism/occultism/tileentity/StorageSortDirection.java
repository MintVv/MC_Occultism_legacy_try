package com.occultism.occultism.tileentity;

/** Sort direction used by the storage controller terminal. */
public enum StorageSortDirection {
    DOWN(0),
    UP(1);

    private final int value;

    StorageSortDirection(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public static StorageSortDirection byValue(int value) {
        for (StorageSortDirection direction : values()) {
            if (direction.value == value) {
                return direction;
            }
        }
        return DOWN;
    }

    public boolean isDown() {
        return this == DOWN;
    }

    public StorageSortDirection next() {
        return values()[(this.ordinal() + 1) % values().length];
    }
}
