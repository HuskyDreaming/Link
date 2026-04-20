package com.huskydreaming.link.spigot.utilities;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Sends Adventure {@link Component}s on Spigot and Paper.
 *
 * <p>Components are serialized to {@code BaseComponent[]} and sent via
 * {@code player.spigot().sendMessage()} — supported natively in Spigot
 * since 1.7, with no NMS reflection required.</p>
 */
public final class SpigotMessenger {

    private static final BungeeComponentSerializer COMPONENT = BungeeComponentSerializer.get();
    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();

    private SpigotMessenger() {
    }

    /**
     * Sends a {@link Component} to any {@link CommandSender}.
     * Players receive the full component (with hover/click); the console receives
     * a plain-text fallback (hover/click are not meaningful in console output).
     */
    public static void send(CommandSender sender, Component component) {
        if (sender instanceof Player player) {
            player.spigot().sendMessage(COMPONENT.serialize(component));
        } else {
            sender.sendMessage(PLAIN.serialize(component));
        }
    }

    /**
     * Sends a {@link Component} to a {@link Player}, preserving all hover and click events.
     */
    public static void send(Player player, Component component) {
        player.spigot().sendMessage(COMPONENT.serialize(component));
    }

    /**
     * Broadcasts a {@link Component} to all online players.
     */
    public static void broadcast(Component component) {
        var serialized = COMPONENT.serialize(component);
        Bukkit.getOnlinePlayers().forEach(p -> p.spigot().sendMessage(serialized));
    }
}
