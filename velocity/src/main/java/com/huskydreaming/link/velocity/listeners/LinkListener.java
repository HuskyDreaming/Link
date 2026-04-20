package com.huskydreaming.link.velocity.listeners;

import com.huskydreaming.link.common.configuration.LinkConfig;
import com.huskydreaming.link.common.discord.events.LinkEvent;
import com.huskydreaming.link.common.discord.events.Subscribe;
import com.huskydreaming.link.common.utilities.Messages;
import com.huskydreaming.link.velocity.utilities.BackendCommandSender;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

public class LinkListener {

    private final LinkConfig linkConfig;
    private final ProxyServer proxy;
    private final Logger logger;

    public LinkListener(ProxyServer proxy, Logger logger, LinkConfig linkConfig) {
        this.proxy = proxy;
        this.logger = logger;
        this.linkConfig = linkConfig;
    }

    @Subscribe
    public void onLink(LinkEvent event) {
        var uuid = event.getUniqueId();
        proxy.getPlayer(uuid).ifPresent(player -> {

            var message = event.isRewardGranted()
                    ? Messages.PLAYER_MESSAGE.get()
                    : Messages.PLAYER_MESSAGE_NO_REWARD.get();

            player.sendMessage(message);
            proxy.sendMessage(Messages.BROADCAST_MESSAGE.get("player", player.getUsername()));

            BackendCommandSender.sendLinkCommands(proxy, player, linkConfig, logger);
        });
    }
}