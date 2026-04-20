package com.huskydreaming.link.spigot.commands;

import com.huskydreaming.link.common.services.interfaces.CodeService;
import com.huskydreaming.link.common.services.interfaces.LinkService;
import com.huskydreaming.link.common.utilities.DurationFormatter;
import com.huskydreaming.link.common.utilities.Messages;
import com.huskydreaming.link.spigot.LinkSpigotPlugin;
import com.huskydreaming.link.spigot.utilities.SpigotMessenger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LinkCommand implements TabExecutor {

    private final LinkSpigotPlugin plugin;
    private final CodeService codeService;
    private final LinkService linkService;

    public LinkCommand(LinkSpigotPlugin plugin, CodeService codeService, LinkService linkService) {
        this.plugin = plugin;
        this.codeService = codeService;
        this.linkService = linkService;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            return handleReload(sender);
        }

        if (!sender.hasPermission("link.link")) {
            SpigotMessenger.send(sender, Messages.NO_PERMISSION.get());
            return true;
        }

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

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("link.reload")) {
            SpigotMessenger.send(sender, Messages.NO_PERMISSION.get());
            return false;
        }

        plugin.reload();
        SpigotMessenger.send(sender, Messages.RELOAD_SUCCESS.get());
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String label, @NotNull String[] args) {
        if (args.length == 1 && sender.hasPermission("link.reload")) {
            if ("reload".startsWith(args[0].toLowerCase())) {
                return List.of("reload");
            }
        }
        return List.of();
    }
}
