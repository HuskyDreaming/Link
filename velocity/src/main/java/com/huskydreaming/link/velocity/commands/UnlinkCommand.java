package com.huskydreaming.link.velocity.commands;

import com.huskydreaming.link.common.data.LinkResult;
import com.huskydreaming.link.common.services.interfaces.DiscordService;
import com.huskydreaming.link.common.services.interfaces.LinkService;
import com.huskydreaming.link.velocity.utilities.Messages;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;

public class UnlinkCommand implements SimpleCommand {

    private final DiscordService discordService;
    private final LinkService linkService;

    public UnlinkCommand(DiscordService discordService, LinkService linkService) {
        this.discordService = discordService;
        this.linkService = linkService;
    }

    @Override
    public void execute(Invocation invocation) {
        if (!(invocation.source() instanceof Player player)) return;

        var uuid = player.getUniqueId();

        linkService.checkLinkStatus(uuid).thenAccept(result -> {
            if (result == LinkResult.ALREADY_LINKED) {
                linkService.unlink(uuid).thenAccept(discordId -> {
                    if(discordId == null || discordId == 0L) {
                        player.sendMessage(Messages.NOT_LINKED.get());
                        return;
                    }

                    discordService.removeRole(discordId).exceptionally(ex -> {
                        player.sendMessage(Messages.ROLE_REMOVE_ERROR.get());
                        return null;
                    });

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