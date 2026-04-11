package com.huskydreaming.link.papermc;

public enum PaperMode {

    /**
     * Receives plugin messages from a Velocity proxy and dispatches them
     * as console commands. No Discord bot or database is initialised.
     */
    VELOCITY_BRIDGE,

    /**
     * Runs the full WildenLink stack on this Paper server — Discord bot,
     * database and in-game /link /unlink commands — without any Velocity proxy.
     */
    STANDALONE;

    public static PaperMode fromString(String value) {
        if (value == null) return VELOCITY_BRIDGE;
        return switch (value.trim().toLowerCase()) {
            case "standalone" -> STANDALONE;
            default -> VELOCITY_BRIDGE;
        };
    }
}

