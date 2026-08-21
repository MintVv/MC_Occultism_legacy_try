package com.occultism.occultism.ritual;

public class PentacleDefinition {

    private final String id;
    private final String displayName;
    private final String[] pattern;

    public PentacleDefinition(String id, String displayName, String... pattern) {
        this.id = id;
        this.displayName = displayName;
        this.pattern = pattern;
    }

    public String getId() {
        return this.id;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String[] getPattern() {
        return this.pattern;
    }

    public int getWidth() {
        return this.pattern[0].length();
    }

    public int getHeight() {
        return this.pattern.length;
    }

    public int getCenterX() {
        return this.getWidth() / 2;
    }

    public int getCenterZ() {
        return this.getHeight() / 2;
    }
}
