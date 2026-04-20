package com.huskydreaming.link.spigot.listeners;

import com.huskydreaming.link.common.discord.events.LinkEvent;
import com.huskydreaming.link.common.discord.events.Subscribe;
import com.huskydreaming.link.common.utilities.Messages;
import com.huskydreaming.link.common.utilities.PlaceholderUtil;
import com.huskydreaming.link.spigot.LinkSpigotPlugin;
import com.huskydreaming.link.spigot.utilities.SpigotMessenger;
import org.bukkit.Bukkit;

import java.util.List;

/**
 * Handles {@link LinkEvent} in STANDALONE mode.
 * Fires on the Discord bot thread — commands and messages are dispatched back on the main server thread.
 */
public class SpigotLinkListener {

    private final LinkSpigotPlugin plugin;
    private final List<String> linkCommands;

    public SpigotLinkListener(LinkSpigotPlugin plugin, List<String> linkCommands) {
        this.plugin = plugin;
        this.linkCommands = linkCommands;
    }

    @Subscribe
    public void onLink(LinkEvent event) {
        var player = Bukkit.getPlayer(event.getUniqueId());
        if (player == null) {
            return;
        }

        PlaceholderUtil.resolvePlaceholders(linkCommands, player.getName())
                .forEach(cmd ->
                        Bukkit.getScheduler().runTask(plugin, () ->
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd)));

        var message = event.isRewardGranted()
                ? Messages.PLAYER_MESSAGE.get()
                : Messages.PLAYER_MESSAGE_NO_REWARD.get();

        Bukkit.getScheduler().runTask(plugin, () -> {
            SpigotMessenger.send(player, message);
            SpigotMessenger.broadcast(Messages.BROADCAST_MESSAGE.get("player", player.getName()));
        });
    }
}
