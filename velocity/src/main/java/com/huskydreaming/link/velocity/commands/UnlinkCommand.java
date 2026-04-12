package com.huskydreaming.link.velocity.commands;

import com.huskydreaming.link.common.configuration.LinkConfig;
import com.huskydreaming.link.common.data.LinkResult;
import com.huskydreaming.link.common.services.interfaces.DiscordService;
import com.huskydreaming.link.common.services.interfaces.LinkService;
import com.huskydreaming.link.common.utilities.Messages;
import com.huskydreaming.link.velocity.utilities.BackendCommandSender;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

public class UnlinkCommand implements SimpleCommand {

    private final DiscordService discordService;
    private final LinkService linkService;
    private final ProxyServer proxy;
    private final LinkConfig linkConfig;
    private final Logger logger;

    public UnlinkCommand(DiscordService discordService, LinkService linkService,
                         ProxyServer proxy, LinkConfig linkConfig, Logger logger) {
        this.discordService = discordService;
        this.linkService = linkService;
        this.proxy = proxy;
        this.linkConfig = linkConfig;
        this.logger = logger;
    }

    @Override
    public void execute(Invocation invocation) {
        if (!(invocation.source() instanceof Player player)) return;

        var uuid = player.getUniqueId();

        linkService.checkLinkStatus(uuid).thenAccept(result -> {
            if (result == LinkResult.ALREADY_LINKED) {
                linkService.unlink(uuid).thenAccept(discordId -> {
                    if (discordId == 0L) {
                        player.sendMessage(Messages.NOT_LINKED.get());
                        return;
                    }

                    discordService.removeRole(discordId).exceptionally(ex -> {
                        player.sendMessage(Messages.ROLE_REMOVE_ERROR.get());
                        return null;
                    });

                    BackendCommandSender.sendUnlinkCommands(proxy, player, linkConfig, logger);

                    player.sendMessage(Messages.UNLINKED.get());
                }).exceptionally(ex -> {
                    player.sendMessage(Messages.UNLINK_ERROR.get());
                    return null;
                });
            } else {
                player.sendMessage(Messages.NOT_LINKED.get());
            }
        }).exceptionally(ex -> {
            player.sendMessage(Messages.UNLINK_ERROR.get());
            return null;
        });
    }
}

