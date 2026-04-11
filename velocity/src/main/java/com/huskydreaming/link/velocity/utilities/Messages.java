package com.huskydreaming.link.velocity.utilities;

import com.huskydreaming.link.common.configuration.YamlConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.nio.file.Path;

public enum Messages {
    ALREADY_LINKED("already-linked"),
    ALREADY_HAS_CODE("already-has-code"),
    BROADCAST_MESSAGE("broadcast-message"),
    COOLDOWN("cooldown"),
    LINK_ERROR("link-error"),
    NOT_LINKED("not-linked"),
    PLAYER_CODE("player-code"),
    PLAYER_MESSAGE("player-message"),
    ROLE_REMOVE_ERROR("role-remove-error"),
    UNLINKED("unlinked"),
    UNLINK_ERROR("unlink-error");

    private final String path;
    private String value;

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    Messages(String path) {
        this.path = path;
    }

    /**
     * Gets the deserialized component with optional placeholders.
     * @param replacements Key-value pairs for placeholders (e.g., "code", "1234")
     * @return The formatted Component
     */
    public Component get(String... replacements) {
        if (this.value == null) return Component.text("Missing: " + path);

        var workingValue = this.value;

        for (var i = 0; i < replacements.length; i += 2) {
            workingValue = workingValue.replace("<" + replacements[i] + ">", replacements[i + 1]);
        }

        return MINI_MESSAGE.deserialize(workingValue);
    }

    public static void load(Path dataDirectory) {
        var config = YamlConfig.load(dataDirectory, "messages.yml");

        for (Messages message : values()) {
            String resolved = config.getString(message.path, null);
            message.value = (resolved == null)
                    ? "<red>Missing translation: " + message.path
                    : resolved;
        }
    }
}