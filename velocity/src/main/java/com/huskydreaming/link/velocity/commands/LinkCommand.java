package com.huskydreaming.link.velocity.commands;

import com.huskydreaming.link.common.data.LinkResult;
import com.huskydreaming.link.common.services.interfaces.CodeService;
import com.huskydreaming.link.common.services.interfaces.LinkService;
import com.huskydreaming.link.common.utilities.DurationFormatter;
import com.huskydreaming.link.common.utilities.Messages;
import com.huskydreaming.link.velocity.initialization.VelocityInitializer;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;

import java.util.List;

public class LinkCommand implements SimpleCommand {

    private final VelocityInitializer initializer;
    private final CodeService codeService;
    private final LinkService linkService;

    public LinkCommand(VelocityInitializer initializer, CodeService codeService, LinkService linkService) {
        this.initializer = initializer;
        this.codeService = codeService;
        this.linkService = linkService;
    }

    @Override
    public void execute(Invocation invocation) {
        var source = invocation.source();
        var args = invocation.arguments();

        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!source.hasPermission("link.reload")) {
                source.sendMessage(Messages.NO_PERMISSION.get());
                return;
            }
            initializer.reload();
            source.sendMessage(Messages.RELOAD_SUCCESS.get());
            return;
        }

        if (!source.hasPermission("link.link")) {
            source.sendMessage(Messages.NO_PERMISSION.get());
            return;
        }

        if (!(source instanceof Player player)) return;

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

    @Override
    public boolean hasPermission(Invocation invocation) {
        var args = invocation.arguments();
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            return invocation.source().hasPermission("link.reload");
        }
        return invocation.source().hasPermission("link.link");
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        var args = invocation.arguments();
        if (args.length <= 1 && invocation.source().hasPermission("link.reload")) {
            var partial = args.length == 1 ? args[0].toLowerCase() : "";
            if ("reload".startsWith(partial)) {
                return List.of("reload");
            }
        }
        return List.of();
    }
}