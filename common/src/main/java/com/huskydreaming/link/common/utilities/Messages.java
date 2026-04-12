package com.huskydreaming.link.common.utilities;

import com.huskydreaming.link.common.configuration.YamlConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.nio.file.Path;

/**
 * Centralised message registry for all player-facing messages.
 * Messages are loaded from {@code messages.yml} in the plugin data directory
 * and support MiniMessage formatting plus simple {@code <key>} placeholders.
 * <p>
 * Call {@link #load(Path)} once during plugin initialisation.
 * Both the Velocity and PaperMC modules share this enum — each bundles its own
 * default {@code messages.yml} that is merged in when the file is first created.
 */
public enum Messages {

    ALREADY_LINKED("already-linked"),
    ALREADY_HAS_CODE("already-has-code"),
    BROADCAST_MESSAGE("broadcast-message"),
    COOLDOWN("cooldown"),
    LINK_ERROR("link-error"),
    NOT_LINKED("not-linked"),
    PLAYERS_ONLY("players-only"),
    PLAYER_CODE("player-code"),
    PLAYER_MESSAGE("player-message"),
    PLAYER_MESSAGE_NO_REWARD("player-message-no-reward"),
    ROLE_REMOVE_ERROR("role-remove-error"),
    UNLINKED("unlinked"),
    UNLINK_ERROR("unlink-error");

    private final String key;
    private String value;

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    Messages(String key) {
        this.key = key;
    }

    /**
     * Returns the deserialized {@link Component} for this message,
     * with any {@code <placeholder>} tokens substituted.
     *
     * @param replacements alternating key/value pairs, e.g. {@code "code", "ABC123"}
     * @return the formatted component
     */
    public Component get(String... replacements) {
        if (this.value == null) return Component.text("Missing: " + key);

        var workingValue = this.value;

        for (var i = 0; i < replacements.length - 1; i += 2) {
            workingValue = workingValue.replace("<" + replacements[i] + ">", replacements[i + 1]);
        }

        return MINI_MESSAGE.deserialize(workingValue);
    }

    /**
     * Loads all messages from {@code messages.yml} in {@code dataDirectory}.
     * Missing keys are merged from the bundled default resource automatically.
     *
     * @param dataDirectory the plugin's data folder
     */
    public static void load(Path dataDirectory) {
        var config = YamlConfig.loadAndMergeDefaults(dataDirectory, "messages.yml");

        for (var message : values()) {
            var resolved = config.getString(message.key, null);
            message.value = (resolved == null)
                    ? "<red>Missing translation: " + message.key
                    : resolved;
        }
    }
}

