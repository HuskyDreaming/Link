package com.huskydreaming.link.common.utilities;

/**
 * Shared constants for the Link plugin-message channel used to forward
 * configured commands from the Velocity proxy to Spigot backend servers.
 * <p>
 * Both the sender ({@code PluginMessages} on Velocity) and the receiver
 * ({@code VelocityBridgeInitializer} on Spigot) must agree on these values.
 */
public final class ChannelConstants {

    private ChannelConstants() {
    }

    /**
     * Namespace portion of the plugin-message channel identifier.
     */
    public static final String CHANNEL_NAMESPACE = "link";

    /**
     * Name portion of the plugin-message channel identifier.
     */
    public static final String CHANNEL_NAME = "commands";

    /**
     * Full channel string in {@code namespace:name} format.
     * Used on the PaperMC side when registering the incoming channel.
     */
    public static final String CHANNEL_ID = CHANNEL_NAMESPACE + ":" + CHANNEL_NAME;

    /**
     * Delimiter used to join multiple commands into a single plugin-message payload.
     * Commands are split by this string when received on the PaperMC side.
     */
    public static final String COMMAND_DELIMITER = ";;";
}