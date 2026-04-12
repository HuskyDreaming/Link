package com.huskydreaming.link.common.configuration;

import org.slf4j.Logger;

/**
 * Immutable representation of the {@code discord} block in {@code config.yml}.
 *
 * @param token       Bot token used to authenticate with the Discord API.
 * @param guildId     Snowflake ID of the Discord server.
 * @param roleId      Snowflake ID of the role awarded on successful linking.
 * @param embedConfig Configuration for the link embed posted by {@code /setup}.
 */
public record DiscordConfig(
        String token,
        long guildId,
        long roleId,
        EmbedConfig embedConfig
) {

    /**
     * Parses a {@link DiscordConfig} from the given {@link YamlConfig},
     * reading values from the {@code discord} section.
     */
    public static DiscordConfig fromYaml(YamlConfig config) {
        return new DiscordConfig(
                config.getString("discord.token", ""),
                config.getLong("discord.guild-id", 0L),
                config.getLong("discord.role-id", 0L),
                EmbedConfig.fromYaml(config)
        );
    }

    /**
     * Logs warnings for any misconfigured Discord values.
     * Call this before building the JDA client.
     */
    public void validate(Logger logger) {
        if (token == null || token.isBlank()) {
            logger.error("'discord.token' is not set — the bot will not start.");
        }
        if (guildId == 0L) {
            logger.warn("'discord.guild-id' is 0 — role assignment will not work until this is set.");
        }
        if (roleId == 0L) {
            logger.warn("'discord.role-id' is 0 — role assignment will not work until this is set.");
        }
    }
}