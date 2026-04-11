package com.huskydreaming.link.velocity.listeners;

import com.huskydreaming.link.common.configuration.MinecraftConfig;
import com.huskydreaming.link.common.discord.events.LinkEvent;
import com.huskydreaming.link.common.discord.events.Subscribe;
import com.huskydreaming.link.velocity.utilities.Messages;
import com.huskydreaming.link.velocity.utilities.PluginMessages;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

public class LinkListener {

    private final MinecraftConfig minecraftConfig;
    private final ProxyServer proxy;
    private final Logger logger;

    public LinkListener(ProxyServer proxy, Logger logger, MinecraftConfig minecraftConfig) {
        this.proxy = proxy;
        this.logger = logger;
        this.minecraftConfig = minecraftConfig;
    }

    @Subscribe
    public void onLink(LinkEvent event) {
        var uuid = event.getUniqueId();
        proxy.getPlayer(uuid).ifPresent(player -> {

            player.sendMessage(Messages.PLAYER_MESSAGE.get());
            proxy.sendMessage(Messages.BROADCAST_MESSAGE.get("player", player.getUsername()));

            PluginMessages.sendConfiguredCommands(proxy, player, minecraftConfig, logger);
        });
    }
}

