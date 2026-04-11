package com.huskydreaming.link.papermc.commands;

import com.huskydreaming.link.common.data.LinkResult;
import com.huskydreaming.link.common.services.interfaces.DiscordService;
import com.huskydreaming.link.common.services.interfaces.LinkService;
import com.huskydreaming.link.papermc.LinkPaperPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class UnlinkCommand implements CommandExecutor {

    private final LinkPaperPlugin plugin;
    private final DiscordService discordService;
    private final LinkService linkService;
    private final List<String> unlinkCommands;

    public UnlinkCommand(LinkPaperPlugin plugin, DiscordService discordService,
                         LinkService linkService, List<String> unlinkCommands) {
        this.plugin = plugin;
        this.discordService = discordService;
        this.linkService = linkService;
        this.unlinkCommands = unlinkCommands;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String @NonNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }

        var uuid = player.getUniqueId();

        linkService.checkLinkStatus(uuid).thenAccept(status -> {
            if (status != LinkResult.ALREADY_LINKED) {
                player.sendMessage("§cYour account is not linked to Discord.");
                return;
            }

            linkService.unlink(uuid).thenAccept(discordId -> {
                if (discordId == null || discordId == 0L) {
                    player.sendMessage("§cYour account is not linked to Discord.");
                    return;
                }

                // Remove Discord role
                discordService.removeRole(discordId).exceptionally(ex -> {
                    player.sendMessage("§eUnlinked, but failed to remove Discord role. Please contact an admin.");
                    return null;
                });

                // Dispatch unlink commands on main thread
                var username = player.getName();
                unlinkCommands.stream()
                        .map(cmd -> cmd.replace("%player%", username))
                        .forEach(cmd ->
                                Bukkit.getScheduler().runTask(plugin, () ->
                                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd)));

                player.sendMessage("§aYour account has been unlinked from Discord.");
            }).exceptionally(ex -> {
                player.sendMessage("§cAn error occurred while unlinking. Please try again later.");
                return null;
            });
        }).exceptionally(ex -> {
            player.sendMessage("§cAn error occurred while processing your request. Please try again later.");
            return null;
        });

        return true;
    }
}