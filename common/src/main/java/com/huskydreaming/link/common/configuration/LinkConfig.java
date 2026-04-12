package com.huskydreaming.link.common.configuration;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Immutable representation of the plugin's {@code config.yml} link settings.
 *
 * @param cooldownMillis  How long (in milliseconds) a player must wait before re-linking.
 * @param servers         Per-backend-server command lists, keyed by server name (Velocity mode).
 * @param linkCommands    Commands dispatched when a player links (standalone mode).
 * @param unlinkCommands  Commands dispatched when a player unlinks (standalone mode).
 */
public record LinkConfig(
        long cooldownMillis,
        Map<String, ServerConfig> servers,
        List<String> linkCommands,
        List<String> unlinkCommands
) {

    /**
     * Parses a {@link LinkConfig} from the given {@link YamlConfig}.
     * <p>
     * Supports both Velocity (per-server blocks under {@code link.servers}) and
     * standalone Spigot (flat lists under {@code link.link-commands} /
     * {@code link.unlink-commands}) layouts.
     */
    public static LinkConfig fromYaml(YamlConfig config) {
        long cooldownSeconds = config.getLong("link.cooldown", 3600L);
        long cooldownMillis = TimeUnit.SECONDS.toMillis(cooldownSeconds);

        // Velocity: per-server commands under link.servers.<name>.link-commands / unlink-commands
        var serverNames = config.getKeys("link.servers");
        var servers = new HashMap<String, ServerConfig>();
        for (var serverName : serverNames) {
            var linkCmds = config.getStringList("link.servers." + serverName + ".link-commands");
            var unlinkCmds = config.getStringList("link.servers." + serverName + ".unlink-commands");
            servers.put(serverName, new ServerConfig(linkCmds, unlinkCmds));
        }

        // Spigot standalone: flat command lists under link
        var linkCommands = config.getStringList("link.link-commands");
        var unlinkCommands = config.getStringList("link.unlink-commands");

        return new LinkConfig(cooldownMillis, Collections.unmodifiableMap(servers), linkCommands, unlinkCommands);
    }
}
