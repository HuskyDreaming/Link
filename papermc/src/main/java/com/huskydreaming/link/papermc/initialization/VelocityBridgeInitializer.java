package com.huskydreaming.link.papermc.initialization;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Handles initialization of Velocity bridge mode for PaperMC.
 * Registers a plugin message channel to receive commands from Velocity proxy.
 */
public class VelocityBridgeInitializer {

    private static final String BRIDGE_CHANNEL = "wildenlink:commands";

    private final JavaPlugin plugin;
    private final Logger logger;

    public VelocityBridgeInitializer(JavaPlugin plugin, Logger logger) {
        this.plugin = plugin;
        this.logger = logger;
    }

    /**
     * Initializes the velocity bridge mode by registering the plugin message channel.
     */
    public void initialize() {
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, BRIDGE_CHANNEL,
                (channel, player, message) -> {
                    if (!channel.equals(BRIDGE_CHANNEL)) return;

                    var payload = new String(message, StandardCharsets.UTF_8);
                    Arrays.stream(payload.split(";;"))
                            .filter(cmd -> !cmd.isBlank())
                            .forEach(cmd ->
                                    Bukkit.getScheduler().runTask(plugin, () ->
                                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd)));
                });

        logger.info("Velocity bridge mode initialized - listening on channel: " + BRIDGE_CHANNEL);
    }

    /**
     * Shuts down the velocity bridge mode.
     */
    public void shutdown() {
        plugin.getServer().getMessenger().unregisterIncomingPluginChannel(plugin, BRIDGE_CHANNEL);
        logger.info("Velocity bridge mode disabled");
    }
}

