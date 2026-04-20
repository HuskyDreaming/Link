package com.huskydreaming.link.folia.listeners;

import com.huskydreaming.link.common.discord.events.LinkEvent;
import com.huskydreaming.link.common.discord.events.Subscribe;
import com.huskydreaming.link.common.utilities.Messages;
import com.huskydreaming.link.common.utilities.PlaceholderUtil;
import com.huskydreaming.link.folia.LinkFoliaPlugin;
import com.huskydreaming.link.folia.utilities.FoliaMessenger;
import org.bukkit.Bukkit;

import java.util.List;

/**
 * Handles {@link LinkEvent} in STANDALONE mode on Folia.
 * Dispatches commands and messages via Folia's GlobalRegionScheduler.
 */
public class FoliaLinkListener {

    private final LinkFoliaPlugin plugin;
    private final List<String> linkCommands;

    public FoliaLinkListener(LinkFoliaPlugin plugin, List<String> linkCommands) {
        this.plugin = plugin;
        this.linkCommands = linkCommands;
    }

    @Subscribe
    public void onLink(LinkEvent event) {
        var player = Bukkit.getPlayer(event.getUniqueId());
        if (player == null) {
            return;
        }

        // Run console commands on the global region (not tied to a specific region/entity)
        PlaceholderUtil.resolvePlaceholders(linkCommands, player.getName())
                .forEach(cmd ->
                        plugin.getServer().getGlobalRegionScheduler().run(plugin, t ->
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd)));

        var message = event.isRewardGranted()
                ? Messages.PLAYER_MESSAGE.get()
                : Messages.PLAYER_MESSAGE_NO_REWARD.get();

        // player.sendMessage() is network I/O — thread-safe, no scheduler needed
        FoliaMessenger.send(player, message);
        FoliaMessenger.broadcast(Messages.BROADCAST_MESSAGE.get("player", player.getName()));
    }
}