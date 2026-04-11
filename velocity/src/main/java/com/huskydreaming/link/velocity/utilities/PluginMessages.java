package com.huskydreaming.link.velocity.utilities;

import com.huskydreaming.link.common.configuration.MinecraftConfig;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import org.slf4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class PluginMessages {

    private static final MinecraftChannelIdentifier COMMANDS_CHANNEL =
            MinecraftChannelIdentifier.create("wildenlink", "commands");

    public static void sendConfiguredCommands(ProxyServer proxyServer, Player player, MinecraftConfig minecraftConfig, Logger logger) {
        var servers = minecraftConfig.servers();
        if (servers.isEmpty()) {
            logger.warn("No servers configured, did not send any messages!");
            return;
        }

        servers.forEach((serverName, serverConfig) ->
                proxyServer.getServer(serverName).ifPresent(registeredServer -> {
                    var commands = resolvePlaceholders(serverConfig.commands(), player);
                    sendToServer(registeredServer, commands, logger);
                }));
    }

    private static void sendToServer(RegisteredServer server, List<String> commands, Logger logger) {
        var sent = sendCommands(server, commands);
        if (sent) {
            logger.info("Sent commands to {}", server.getServerInfo().getName());
        } else {
            logger.warn("Failed to send commands to {}", server.getServerInfo().getName());
        }
    }

    private static List<String> resolvePlaceholders(List<String> commands, Player player) {
        return commands.stream()
                .map(cmd -> cmd.replace("%player%", player.getUsername()))
                .toList();
    }

    private static boolean sendCommands(RegisteredServer server, List<String> commands) {
        var payload = String.join(";;", commands);
        var data = payload.getBytes(StandardCharsets.UTF_8);

        return server.sendPluginMessage(COMMANDS_CHANNEL, data);
    }
}