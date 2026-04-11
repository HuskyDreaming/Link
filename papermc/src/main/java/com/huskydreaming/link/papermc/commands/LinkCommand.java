package com.huskydreaming.link.papermc.commands;

import com.huskydreaming.link.common.services.interfaces.CodeService;
import com.huskydreaming.link.common.services.interfaces.LinkService;
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
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }

        var uuid = player.getUniqueId();

        if (codeService.hasCode(uuid)) {
            player.sendMessage("§eYou already have a pending link code. Check your previous message!");
            return true;
        }

        linkService.checkLinkStatus(uuid).thenAccept(status -> {
            switch (status) {
                case ALREADY_LINKED ->
                        player.sendMessage("§cYour account is already linked to Discord.");
                case COOLDOWN -> linkService.getRemainingCooldown(uuid).thenAccept(remaining ->
                        player.sendMessage("§cYou are on cooldown. Try again in §e" + formatDuration(remaining) + "§c."));
                case OK -> {
                    var code = codeService.generate(uuid);
                    player.sendMessage("§7Your link code: §b" + code);
                    player.sendMessage("§7Enter this code in the Discord §b#link §7channel. It expires in §e5 minutes§7.");
                }
            }
        }).exceptionally(ex -> {
            player.sendMessage("§cAn error occurred while processing your request. Please try again later.");
            return null;
        });

        return true;
    }

    private String formatDuration(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        seconds %= 60;
        minutes %= 60;

        if (hours > 0) return hours + "h " + minutes + "m " + seconds + "s";
        if (minutes > 0) return minutes + "m " + seconds + "s";
        return seconds + "s";
    }
}

