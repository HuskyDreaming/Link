package com.huskydreaming.link.spigotmc.commands;

import com.huskydreaming.link.common.services.interfaces.CodeService;
import com.huskydreaming.link.common.services.interfaces.LinkService;
import com.huskydreaming.link.common.utilities.DurationFormatter;
import com.huskydreaming.link.common.utilities.Messages;
import com.huskydreaming.link.spigotmc.utilities.SpigotMessenger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class LinkCommand implements CommandExecutor {

    private final CodeService codeService;
    private final LinkService linkService;

    public LinkCommand(CodeService codeService, LinkService linkService) {
        this.codeService = codeService;
        this.linkService = linkService;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            SpigotMessenger.send(sender, Messages.PLAYERS_ONLY.get());
            return true;
        }

        var uuid = player.getUniqueId();

        if (codeService.hasCode(uuid)) {
            var remaining = DurationFormatter.format(codeService.getRemainingCodeTime(uuid));
            SpigotMessenger.send(player, Messages.ALREADY_HAS_CODE.get("time", remaining));
            return true;
        }

        linkService.checkLinkStatus(uuid).thenAccept(status -> {
            switch (status) {
                case ALREADY_LINKED -> SpigotMessenger.send(player, Messages.ALREADY_LINKED.get());
                case COOLDOWN -> linkService.getRemainingCooldown(uuid).thenAccept(remaining ->
                        SpigotMessenger.send(player, Messages.COOLDOWN.get("time", DurationFormatter.format(remaining))));
                case OK -> {
                    var code = codeService.generate(uuid);
                    SpigotMessenger.send(player, Messages.PLAYER_CODE.get("code", code));
                }
            }
        }).exceptionally(ex -> {
            SpigotMessenger.send(player, Messages.LINK_ERROR.get());
            return null;
        });

        return true;
    }
}

