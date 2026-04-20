package com.huskydreaming.link.common.initialization;

import com.huskydreaming.link.common.LinkCommonPlugin;
import com.huskydreaming.link.common.configuration.DiscordConfig;
import com.huskydreaming.link.common.discord.DiscordClient;
import com.huskydreaming.link.common.discord.commands.SetupCommand;
import com.huskydreaming.link.common.discord.listeners.ButtonListener;
import com.huskydreaming.link.common.discord.listeners.ModalListener;
import com.huskydreaming.link.common.services.impl.DiscordServiceImpl;
import com.huskydreaming.link.common.services.interfaces.DiscordService;
import net.dv8tion.jda.api.JDA;
import org.slf4j.Logger;

/**
 * Handles initialization of Discord-related components.
 */
public class DiscordInitializer {

    private final DiscordConfig config;
    private final Logger logger;

    public DiscordInitializer(DiscordConfig config, Logger logger) {
        this.config = config;
        this.logger = logger;
    }

    /**
     * Creates and initializes a DiscordClient (JDA connection).
     * Returns {@code null} if the token is not configured.
     */
    public DiscordClient initializeDiscordClient() {
        if (config.token() == null || config.token().isBlank()) {
            logger.error("Discord bot will not start — please set a valid token in discord.yml and restart.");
            return null;
        }
        config.validate(logger);
        return new DiscordClient(config);
    }

    /**
     * Creates a DiscordService (role management).
     */
    public DiscordService initializeDiscordService(DiscordClient client) {
        return new DiscordServiceImpl(config, client, logger);
    }

    /**
     * Registers Discord event listeners (button, modal, setup command).
     */
    public void registerDiscordListeners(JDA jda, LinkCommonPlugin plugin, SetupCommand setupCommand) {
        jda.addEventListener(
                new ButtonListener(),
                setupCommand,
                new ModalListener(plugin, logger)
        );
    }
}