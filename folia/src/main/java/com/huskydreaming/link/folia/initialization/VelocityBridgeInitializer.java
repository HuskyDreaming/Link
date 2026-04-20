package com.huskydreaming.link.folia.initialization;

import com.huskydreaming.link.common.utilities.ChannelConstants;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Handles initialization of Velocity bridge mode for Folia.
 * Uses Folia's GlobalRegionScheduler instead of Bukkit's scheduler.
 */
public class VelocityBridgeInitializer {

    private final JavaPlugin plugin;
    private final Logger logger;

    public VelocityBridgeInitializer(JavaPlugin plugin, Logger logger) {
        this.plugin = plugin;
        this.logger = logger;
    }

    public void initialize() {
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, ChannelConstants.CHANNEL_ID,
                (channel, player, message) -> {
                    var payload = new String(message, StandardCharsets.UTF_8);
                    Arrays.stream(payload.split(ChannelConstants.COMMAND_DELIMITER))
                            .filter(cmd -> !cmd.isBlank())
                            .forEach(cmd ->
                                    plugin.getServer().getGlobalRegionScheduler().run(plugin, t ->
                                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd)));
                });

        logger.info("Velocity bridge mode initialized - listening on channel: {}", ChannelConstants.CHANNEL_ID);
    }

    public void shutdown() {
        plugin.getServer().getMessenger().unregisterIncomingPluginChannel(plugin, ChannelConstants.CHANNEL_ID);
        logger.info("Velocity bridge mode disabled");
    }
}