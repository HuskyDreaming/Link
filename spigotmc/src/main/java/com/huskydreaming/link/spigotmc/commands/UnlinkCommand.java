package com.huskydreaming.link.spigotmc.commands;

import com.huskydreaming.link.common.data.LinkResult;
import com.huskydreaming.link.common.services.interfaces.DiscordService;
import com.huskydreaming.link.common.services.interfaces.LinkService;
import com.huskydreaming.link.common.utilities.Messages;
import com.huskydreaming.link.common.utilities.PlaceholderUtil;
import com.huskydreaming.link.spigotmc.LinkSpigotPlugin;
import com.huskydreaming.link.spigotmc.utilities.SpigotMessenger;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class UnlinkCommand implements CommandExecutor {

    private final LinkSpigotPlugin plugin;
    private final DiscordService discordService;
    private final LinkService linkService;
    private final List<String> unlinkCommands;

    public UnlinkCommand(LinkSpigotPlugin plugin, DiscordService discordService,
                         LinkService linkService, List<String> unlinkCommands) {
        this.plugin = plugin;
        this.discordService = discordService;
        this.linkService = linkService;
        this.unlinkCommands = unlinkCommands;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            SpigotMessenger.send(sender, Messages.PLAYERS_ONLY.get());
            return true;
        }

        var uuid = player.getUniqueId();

        linkService.checkLinkStatus(uuid).thenAccept(status -> {
            if (status != LinkResult.ALREADY_LINKED) {
                SpigotMessenger.send(player, Messages.NOT_LINKED.get());
                return;
            }

            linkService.unlink(uuid).thenAccept(discordId -> {
                if (discordId == null || discordId == 0L) {
                    SpigotMessenger.send(player, Messages.NOT_LINKED.get());
                    return;
                }

                discordService.removeRole(discordId).exceptionally(ex -> {
                    SpigotMessenger.send(player, Messages.ROLE_REMOVE_ERROR.get());
                    return null;
                });

                PlaceholderUtil.resolvePlaceholders(unlinkCommands, player.getName())
                        .forEach(cmd ->
                                Bukkit.getScheduler().runTask(plugin, () ->
                                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd)));

                SpigotMessenger.send(player, Messages.UNLINKED.get());
            }).exceptionally(ex -> {
                SpigotMessenger.send(player, Messages.UNLINK_ERROR.get());
                return null;
            });
        }).exceptionally(ex -> {
            SpigotMessenger.send(player, Messages.UNLINK_ERROR.get());
            return null;
        });

        return true;
    }
}
