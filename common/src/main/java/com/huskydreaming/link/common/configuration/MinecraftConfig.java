package com.huskydreaming.link.common.configuration;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public record MinecraftConfig(
        Map<String, ServerConfig> servers
) {

    public static MinecraftConfig fromYaml(YamlConfig config) {
        var serverNames = config.getKeys("minecraft");
        if (serverNames.isEmpty()) {
            return empty();
        }

        var servers = new HashMap<String, ServerConfig>();
        for (var serverName : serverNames) {
            var commands = config.getStringList("minecraft." + serverName + ".commands");
            servers.put(serverName, new ServerConfig(commands));
        }

        return new MinecraftConfig(servers);
    }

    public static MinecraftConfig empty() {
        return new MinecraftConfig(Collections.emptyMap());
    }
}