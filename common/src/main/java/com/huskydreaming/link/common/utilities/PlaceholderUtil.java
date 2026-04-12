package com.huskydreaming.link.common.utilities;

import java.util.List;

/**
 * Utility for resolving {@code %player%} placeholders inside command strings.
 * <p>
 * Used whenever a configured command list needs the player's name injected
 * before dispatch (link rewards, unlink cleanup, etc.).
 */
public final class PlaceholderUtil {

    private PlaceholderUtil() {}

    /**
     * Replaces {@code %player%} in every command string with {@code playerName}.
     *
     * @param commands   list of raw command templates
     * @param playerName the player's name to substitute
     * @return new list with placeholders resolved
     */
    public static List<String> resolvePlaceholders(List<String> commands, String playerName) {
        return commands.stream()
                .map(cmd -> cmd.replace("%player%", playerName))
                .toList();
    }

    /**
     * Replaces {@code %player%} in a single command string with {@code playerName}.
     *
     * @param command    raw command template
     * @param playerName the player's name to substitute
     * @return command with placeholder resolved
     */
    public static String resolvePlaceholder(String command, String playerName) {
        return command.replace("%player%", playerName);
    }
}

