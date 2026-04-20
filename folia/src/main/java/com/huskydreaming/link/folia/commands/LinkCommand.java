package com.huskydreaming.link.folia.commands;

import com.huskydreaming.link.common.services.interfaces.CodeService;
import com.huskydreaming.link.common.services.interfaces.LinkService;
import com.huskydreaming.link.common.utilities.DurationFormatter;
import com.huskydreaming.link.common.utilities.Messages;
import com.huskydreaming.link.folia.LinkFoliaPlugin;
import com.huskydreaming.link.folia.utilities.FoliaMessenger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class LinkCommand implements CommandExecutor, TabExecutor {

    private final LinkFoliaPlugin plugin;
    private final CodeService codeService;
    private final LinkService linkService;

    public LinkCommand(LinkFoliaPlugin plugin, CodeService codeService, LinkService linkService) {
        this.plugin = plugin;
        this.codeService = codeService;
        this.linkService = linkService;
    }

    @Override
    public boolean onCommand(@NonNull CommandSender sender, @NonNull Command command,
                             @NonNull String label, @NonNull String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            return handleReload(sender);
        }

        if (!sender.hasPermission("link.link")) {
            FoliaMessenger.send(sender, Messages.NO_PERMISSION.get());
            return true;
        }

        if (!(sender instanceof Player player)) {
            FoliaMessenger.send(sender, Messages.PLAYERS_ONLY.get());
            return true;
        }

        var uuid = player.getUniqueId();

        if (codeService.hasCode(uuid)) {
            var remaining = DurationFormatter.format(codeService.getRemainingCodeTime(uuid));
            FoliaMessenger.send(player, Messages.ALREADY_HAS_CODE.get("time", remaining));
            return true;
        }

        linkService.checkLinkStatus(uuid).thenAccept(status -> {
            switch (status) {
                case ALREADY_LINKED -> FoliaMessenger.send(player, Messages.ALREADY_LINKED.get());
                case COOLDOWN -> linkService.getRemainingCooldown(uuid).thenAccept(remaining ->
                        FoliaMessenger.send(player, Messages.COOLDOWN.get("time", DurationFormatter.format(remaining))));
                case OK -> {
                    var code = codeService.generate(uuid);
                    FoliaMessenger.send(player, Messages.PLAYER_CODE.get("code", code));
                }
            }
        }).exceptionally(ex -> {
            FoliaMessenger.send(player, Messages.LINK_ERROR.get());
            return null;
        });

        return true;
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("link.reload")) {
            FoliaMessenger.send(sender, Messages.NO_PERMISSION.get());
            return false;
        }

        plugin.reload();
        FoliaMessenger.send(sender, Messages.RELOAD_SUCCESS.get());
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NonNull CommandSender sender, @NonNull Command command,
                                                @NonNull String label, @NonNull String[] args) {
        if (args.length == 1 && sender.hasPermission("link.reload")) {
            if ("reload".startsWith(args[0].toLowerCase())) {
                return List.of("reload");
            }
        }
        return List.of();
    }
}