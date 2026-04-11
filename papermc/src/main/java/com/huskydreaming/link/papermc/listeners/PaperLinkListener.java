package com.huskydreaming.link.papermc.listeners;

import com.huskydreaming.link.common.discord.events.LinkEvent;
import com.huskydreaming.link.common.discord.events.Subscribe;
import com.huskydreaming.link.papermc.LinkPaperPlugin;
import org.bukkit.Bukkit;

import java.util.List;

/**
 * Handles {@link LinkEvent} in STANDALONE mode.
 * Fires on the Discord bot thread — commands are dispatched back on the main server thread.
 */
public class PaperLinkListener {

    private final LinkPaperPlugin plugin;
    private final List<String> linkCommands;

    public PaperLinkListener(LinkPaperPlugin plugin, List<String> linkCommands) {
        this.plugin = plugin;
        this.linkCommands = linkCommands;
    }

    @Subscribe
    public void onLink(LinkEvent event) {
        var player = Bukkit.getPlayer(event.getUniqueId());
        if (player == null) return;

        var username = player.getName();

        linkCommands.stream()
                .map(cmd -> cmd.replace("%player%", username))
                .forEach(cmd ->
                        Bukkit.getScheduler().runTask(plugin, () ->
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd)));

        Bukkit.getScheduler().runTask(plugin, () ->
                player.sendMessage("§a✅ Your account has been linked to Discord!"));
    }
}

