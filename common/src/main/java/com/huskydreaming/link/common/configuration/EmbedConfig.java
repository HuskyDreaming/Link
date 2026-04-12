package com.huskydreaming.link.common.configuration;

import java.util.List;
import java.util.Map;

/**
 * Immutable representation of the {@code discord.embed} block in {@code config.yml}.
 *
 * @param title       Title shown at the top of the embed.
 * @param description Body text of the embed.
 * @param color       Hex colour string (e.g. {@code "#97BA52"}) for the embed's side bar.
 * @param buttonLabel Label text on the authentication button.
 * @param fields      Optional list of additional embed fields.
 */
public record EmbedConfig(
        String title,
        String description,
        String color,
        String buttonLabel,
        List<EmbedFieldConfig> fields
) {

    /**
     * Parses an {@link EmbedConfig} from the given {@link YamlConfig},
     * reading values from the {@code discord.embed} section.
     */
    public static EmbedConfig fromYaml(YamlConfig config) {
        var fields = config.getMapList("discord.embed.fields")
                .stream()
                .map(m -> new EmbedFieldConfig(
                        getStringOrEmpty(m, "name"),
                        getStringOrEmpty(m, "value"),
                        getInline(m)
                ))
                .toList();

        return new EmbedConfig(
                config.getString("discord.embed.title", "Account Linking"),
                config.getString("discord.embed.description", "Link your Minecraft account to Discord."),
                config.getString("discord.embed.color", "#97BA52"),
                config.getString("discord.embed.button-label", "✅ Authenticate Account"),
                fields
        );
    }

    private static String getStringOrEmpty(Map<String, Object> map, String key) {
        var val = map.get(key);
        return val != null ? val.toString() : "";
    }

    private static boolean getInline(Map<String, Object> map) {
        return map.get("inline") instanceof Boolean b && b;
    }
}
