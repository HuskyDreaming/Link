package com.huskydreaming.link.velocity.commands;

import com.huskydreaming.link.common.data.LinkResult;
import com.huskydreaming.link.common.services.interfaces.CodeService;
import com.huskydreaming.link.common.services.interfaces.LinkService;
import com.huskydreaming.link.common.utilities.DurationFormatter;
import com.huskydreaming.link.common.utilities.Messages;
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
        if (!(invocation.source() instanceof Player player)) return;

        var uuid = player.getUniqueId();

        if (codeService.hasCode(uuid)) {
            var remaining = DurationFormatter.format(codeService.getRemainingCodeTime(uuid));
            player.sendMessage(Messages.ALREADY_HAS_CODE.get("time", remaining));
            return;
        }

        linkService.checkLinkStatus(uuid).thenAccept(status -> {
            if (status == LinkResult.ALREADY_LINKED) {
                player.sendMessage(Messages.ALREADY_LINKED.get());
                return;
            }

            if (status == LinkResult.COOLDOWN) {
                linkService.getRemainingCooldown(uuid).thenAccept(remaining -> {
                    var duration = DurationFormatter.format(remaining);
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
}