package com.huskydreaming.link.folia.utilities;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Sends Adventure {@link Component}s on Folia (Paper-based).
 * Uses Paper's native Adventure API directly.
 */
public final class FoliaMessenger {

    private FoliaMessenger() {
    }

    public static void send(CommandSender sender, Component component) {
        sender.sendMessage(component);
    }

    public static void send(Player player, Component component) {
        player.sendMessage(component);
    }

    public static void broadcast(Component component) {
        Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(component));
    }
}