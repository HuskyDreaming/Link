package com.huskydreaming.link.velocity.commands;

import com.huskydreaming.link.common.data.LinkResult;
import com.huskydreaming.link.common.services.interfaces.CodeService;
import com.huskydreaming.link.common.services.interfaces.LinkService;
import com.huskydreaming.link.velocity.utilities.Messages;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;

public class LinkCommand implements SimpleCommand {

    private final CodeService codeService;
    private final LinkService linkService;

    public LinkCommand(CodeService codeService, LinkService linkService) {
        this.codeService = codeService;
        this.linkService = linkService;
    }

    @Override
    public void execute(Invocation invocation) {
        var player = (Player) invocation.source();
        var uuid = player.getUniqueId();

        if (codeService.hasCode(uuid)) {
            player.sendMessage(Messages.ALREADY_HAS_CODE.get());
            return;
        }

        linkService.checkLinkStatus(uuid).thenAccept(status -> {
            if (status == LinkResult.ALREADY_LINKED) {
                player.sendMessage(Messages.ALREADY_LINKED.get());
                return;
            }

            if (status == LinkResult.COOLDOWN) {
                linkService.getRemainingCooldown(uuid).thenAccept(remaining -> {
                    var duration = formatDuration(remaining);
                    player.sendMessage(Messages.COOLDOWN.get("time", duration));
                });
                return;
            }

            var code = codeService.generate(uuid);
            player.sendMessage(Messages.PLAYER_CODE.get("code", code));
        }).exceptionally(ex -> {
            player.sendMessage(Messages.LINK_ERROR.get());
            return null;
        });
    }

    private String formatDuration(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        seconds %= 60;
        minutes %= 60;

        if (hours > 0) {
            return hours + "h " + minutes + "m " + seconds + "s";
        } else if (minutes > 0) {
            return minutes + "m " + seconds + "s";
        } else {
            return seconds + "s";
        }
    }
}