package com.huskydreaming.link.velocity.initialization;

import com.huskydreaming.link.common.LinkCommonPlugin;
import com.huskydreaming.link.common.configuration.DatabaseConfig;
import com.huskydreaming.link.common.configuration.DiscordConfig;
import com.huskydreaming.link.common.configuration.LinkConfig;
import com.huskydreaming.link.common.configuration.YamlConfig;
import com.huskydreaming.link.velocity.commands.LinkCommand;
import com.huskydreaming.link.velocity.commands.UnlinkCommand;
import com.huskydreaming.link.velocity.listeners.LinkListener;
import com.huskydreaming.link.common.utilities.Messages;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

import java.nio.file.Path;

/**
 * Handles initialization of the Velocity proxy plugin.
 * Sets up database, Discord bot, commands, and event listeners.
 */
public class VelocityInitializer {

    private final ProxyServer proxy;
    private final Path dataDirectory;
    private final Logger logger;
    private LinkCommonPlugin linkCommon;

    public VelocityInitializer(ProxyServer proxy, Path dataDirectory, Logger logger) {
        this.proxy = proxy;
        this.dataDirectory = dataDirectory;
        this.logger = logger;
    }

    /**
     * Initializes the Velocity plugin: loads configs, sets up common resources, and registers commands.
     */
    public void initialize(LinkCommonPlugin linkCommonPlugin) {
        this.linkCommon = linkCommonPlugin;

        var config = YamlConfig.loadAndMergeDefaults(dataDirectory, "config.yml");
        var dbConfig = YamlConfig.loadAndMergeDefaults(dataDirectory, "database.yml");
        var discordYaml = YamlConfig.loadAndMergeDefaults(dataDirectory, "discord.yml");

        var databaseConfiguration = DatabaseConfig.fromYaml(dbConfig);
        var discordConfiguration = DiscordConfig.fromYaml(discordYaml);
        var linkConfig = LinkConfig.fromYaml(config);

        // Load messages
        Messages.load(dataDirectory);

        // Initialize common resources
        linkCommon.initialize(databaseConfiguration, discordConfiguration, linkConfig, logger);

        // Register Velocity commands
        registerCommands(linkConfig);

        // Register link event listener
        linkCommon.getLinkEventBus().register(new LinkListener(proxy, logger, linkConfig));
    }

    /**
     * Safely reloads messages and config values without restarting JDA or the database.
     */
    public void reload() {
        var config = YamlConfig.loadAndMergeDefaults(dataDirectory, "config.yml");
        YamlConfig.loadAndMergeDefaults(dataDirectory, "database.yml");
        var discordYaml = YamlConfig.loadAndMergeDefaults(dataDirectory, "discord.yml");
        var linkConfig = LinkConfig.fromYaml(config);

        Messages.load(dataDirectory);

        if (linkCommon != null) {
            linkCommon.getLinkService().updateCooldown(linkConfig.cooldownMillis());
            linkCommon.reloadDiscordConfig(DiscordConfig.fromYaml(discordYaml));
        }

        registerCommands(linkConfig);
        logger.info("Configuration reloaded.");
    }

    /**
     * Registers /link and /unlink commands with the command manager.
     */
    private void registerCommands(LinkConfig linkConfig) {
        var commandManager = proxy.getCommandManager();
        var pluginContainer = proxy.getPluginManager().getPlugin("link").orElse(null);

        var linkMeta = commandManager.metaBuilder("link")
                .plugin(pluginContainer)
                .build();

        var unlinkMeta = commandManager.metaBuilder("unlink")
                .plugin(pluginContainer)
                .build();

        commandManager.register(linkMeta, new LinkCommand(this, linkCommon.getCodeService(), linkCommon.getLinkService()));
        commandManager.register(unlinkMeta, new UnlinkCommand(linkCommon.getDiscordService(), linkCommon.getLinkService(), proxy, linkConfig, logger));
    }
}