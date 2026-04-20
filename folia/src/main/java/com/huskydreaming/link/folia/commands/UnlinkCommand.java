package com.huskydreaming.link.folia.commands;

import com.huskydreaming.link.common.data.LinkResult;
import com.huskydreaming.link.common.services.interfaces.DiscordService;
import com.huskydreaming.link.common.services.interfaces.LinkService;
import com.huskydreaming.link.common.utilities.Messages;
import com.huskydreaming.link.common.utilities.PlaceholderUtil;
import com.huskydreaming.link.folia.LinkFoliaPlugin;
import com.huskydreaming.link.folia.utilities.FoliaMessenger;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class UnlinkCommand implements CommandExecutor {

    private final LinkFoliaPlugin plugin;
    private final DiscordService discordService;
    private final LinkService linkService;
    private final List<String> unlinkCommands;

    public UnlinkCommand(LinkFoliaPlugin plugin, DiscordService discordService,
                         LinkService linkService, List<String> unlinkCommands) {
        this.plugin = plugin;
        this.discordService = discordService;
        this.linkService = linkService;
        this.unlinkCommands = unlinkCommands;
    }

    @Override
    public boolean onCommand(@NonNull CommandSender sender, @NonNull Command command,
                             @NonNull String label, @NonNull String[] args) {
        if (!(sender instanceof Player player)) {
            FoliaMessenger.send(sender, Messages.PLAYERS_ONLY.get());
            return true;
        }

        if (!player.hasPermission("link.unlink")) {
            FoliaMessenger.send(player, Messages.NO_PERMISSION.get());
            return true;
        }

        var uuid = player.getUniqueId();

        linkService.checkLinkStatus(uuid).thenAccept(status -> {
            if (status != LinkResult.ALREADY_LINKED) {
                FoliaMessenger.send(player, Messages.NOT_LINKED.get());
                return;
            }

            linkService.unlink(uuid).thenAccept(discordId -> {
                if (discordId == null || discordId == 0L) {
                    FoliaMessenger.send(player, Messages.NOT_LINKED.get());
                    return;
                }

                discordService.removeRole(discordId).exceptionally(ex -> {
                    FoliaMessenger.send(player, Messages.ROLE_REMOVE_ERROR.get());
                    return null;
                });

                // Console commands need the main thread — use GlobalRegionScheduler
                PlaceholderUtil.resolvePlaceholders(unlinkCommands, player.getName())
                        .forEach(cmd ->
                                plugin.getServer().getGlobalRegionScheduler().run(plugin, t ->
                                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd)));

                FoliaMessenger.send(player, Messages.UNLINKED.get());
            }).exceptionally(ex -> {
                FoliaMessenger.send(player, Messages.UNLINK_ERROR.get());
                return null;
            });
        }).exceptionally(ex -> {
            FoliaMessenger.send(player, Messages.UNLINK_ERROR.get());
            return null;
        });

        return true;
    }
}