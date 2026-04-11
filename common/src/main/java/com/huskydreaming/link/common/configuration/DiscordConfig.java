package com.huskydreaming.link.common.configuration;

public record DiscordConfig(
        String token,
        long guildId,
        long roleId
) {

    public static DiscordConfig fromYaml(YamlConfig config) {
        if (!config.contains("discord")) {
            return empty();
        }
        return new DiscordConfig(
                config.getString("discord.token", ""),
                config.getLong("discord.guild-id", 0L),
                config.getLong("discord.role-id", 0L)
        );
    }

    public static DiscordConfig empty() {
        return new DiscordConfig("", 0L, 0L);
    }
}