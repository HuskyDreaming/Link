package com.huskydreaming.link.common.initialization;

import com.huskydreaming.link.common.LinkCommonPlugin;
import com.huskydreaming.link.common.configuration.DiscordConfig;
import com.huskydreaming.link.common.discord.DiscordClient;
import com.huskydreaming.link.common.discord.commands.SetupCommand;
import com.huskydreaming.link.common.discord.listeners.ButtonListener;
import com.huskydreaming.link.common.discord.listeners.ModalListener;
import com.huskydreaming.link.common.services.implementations.DiscordServiceImpl;
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
     */
    public DiscordClient initializeDiscordClient() {
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
    public void registerDiscordListeners(JDA jda, LinkCommonPlugin plugin) {
        jda.addEventListener(
                new ButtonListener(),
                new SetupCommand(),
                new ModalListener(plugin, logger)
        );
    }
}

