package com.huskydreaming.link.spigotmc;

/**
 * Defines the operating mode of the SpigotMC module.
 *
 * <ul>
 *   <li>{@link #STANDALONE} – the full Discord bot + database runs inside this plugin.</li>
 *   <li>{@link #VELOCITY_BRIDGE} – this plugin forwards plugin-message commands issued by the Velocity proxy.</li>
 * </ul>
 */
public enum SpigotMode {

    STANDALONE,
    VELOCITY_BRIDGE;

    public static SpigotMode fromString(String value) {
        for (var mode : values()) {
            if (mode.name().equalsIgnoreCase(value) || mode.name().replace("_", "-").equalsIgnoreCase(value)) {
                return mode;
            }
        }
        return VELOCITY_BRIDGE;
    }
}

