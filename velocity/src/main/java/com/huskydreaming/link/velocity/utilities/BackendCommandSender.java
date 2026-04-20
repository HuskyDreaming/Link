package com.huskydreaming.link.velocity.utilities;

import com.huskydreaming.link.common.configuration.LinkConfig;
import com.huskydreaming.link.common.configuration.ServerConfig;
import com.huskydreaming.link.common.utilities.ChannelConstants;
import com.huskydreaming.link.common.utilities.PlaceholderUtil;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import org.slf4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Function;

public class BackendCommandSender {

    private static final MinecraftChannelIdentifier COMMANDS_CHANNEL =
            MinecraftChannelIdentifier.create(ChannelConstants.CHANNEL_NAMESPACE, ChannelConstants.CHANNEL_NAME);

    public static void sendLinkCommands(ProxyServer proxyServer, Player player, LinkConfig linkConfig, Logger logger) {
        dispatch(proxyServer, player, linkConfig, ServerConfig::linkCommands, "link", logger);
    }

    public static void sendUnlinkCommands(ProxyServer proxyServer, Player player, LinkConfig linkConfig, Logger logger) {
        dispatch(proxyServer, player, linkConfig, ServerConfig::unlinkCommands, "unlink", logger);
    }

    private static void dispatch(ProxyServer proxyServer, Player player, LinkConfig linkConfig,
                                 Function<ServerConfig, List<String>> commandExtractor, String label, Logger logger) {
        var servers = linkConfig.servers();
        if (servers.isEmpty()) {
            logger.warn("No servers configured — no {} commands will be dispatched.", label);
            return;
        }

        servers.forEach((serverName, serverConfig) -> {
            var commands = commandExtractor.apply(serverConfig);
            if (commands.isEmpty()) return;

            proxyServer.getServer(serverName).ifPresent(registeredServer -> {
                var resolved = PlaceholderUtil.resolvePlaceholders(commands, player.getUsername());
                sendToServer(registeredServer, resolved, logger);
            });
        });
    }

    private static void sendToServer(RegisteredServer server, List<String> commands, Logger logger) {
        var sent = sendCommands(server, commands);
        if (sent) {
            logger.info("Sent commands to {}", server.getServerInfo().getName());
        } else {
            logger.warn("Failed to send commands to {}", server.getServerInfo().getName());
        }
    }

    private static boolean sendCommands(RegisteredServer server, List<String> commands) {
        var payload = String.join(ChannelConstants.COMMAND_DELIMITER, commands);
        var data = payload.getBytes(StandardCharsets.UTF_8);
        return server.sendPluginMessage(COMMANDS_CHANNEL, data);
    }
}