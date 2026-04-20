package com.huskydreaming.link.folia;

/**
 * Defines the operating mode of the Folia module.
 */
public enum FoliaMode {

    STANDALONE,
    VELOCITY_BRIDGE;

    public static FoliaMode fromString(String value) {
        for (var mode : values()) {
            if (mode.name().equalsIgnoreCase(value) || mode.name().replace("_", "-").equalsIgnoreCase(value)) {
                return mode;
            }
        }
        return VELOCITY_BRIDGE;
    }
}