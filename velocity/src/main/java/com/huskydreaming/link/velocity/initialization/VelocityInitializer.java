package com.huskydreaming.link.velocity.initialization;

import com.huskydreaming.link.common.LinkCommonPlugin;
import com.huskydreaming.link.common.configuration.DatabaseConfig;
import com.huskydreaming.link.common.configuration.DiscordConfig;
import com.huskydreaming.link.common.configuration.MinecraftConfig;
import com.huskydreaming.link.common.configuration.YamlConfig;
import com.huskydreaming.link.velocity.commands.LinkCommand;
import com.huskydreaming.link.velocity.commands.UnlinkCommand;
import com.huskydreaming.link.velocity.listeners.LinkListener;
import com.huskydreaming.link.velocity.utilities.Messages;
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

    public VelocityInitializer(ProxyServer proxy, Path dataDirectory, Logger logger) {
        this.proxy = proxy;
        this.dataDirectory = dataDirectory;
        this.logger = logger;
    }

    /**
     * Initializes the Velocity plugin: loads configs, sets up common resources, and registers commands.
     */
    public void initialize(LinkCommonPlugin linkCommon) {
        // Load configuration
        var config = YamlConfig.load(dataDirectory, "config.yml");
        var databaseConfiguration = DatabaseConfig.fromYaml(config);
        var discordConfiguration = DiscordConfig.fromYaml(config);
        var minecraftConfig = MinecraftConfig.fromYaml(config);

        // Load messages
        Messages.load(dataDirectory);

        // Initialize common resources
        linkCommon.initialize(databaseConfiguration, discordConfiguration, logger);

        // Register Velocity commands
        registerCommands(linkCommon);

        // Register link event listener
        linkCommon.getLinkEventBus().register(new LinkListener(proxy, logger, minecraftConfig));
    }

    /**
     * Registers /link and /unlink commands with the command manager.
     */
    private void registerCommands(LinkCommonPlugin linkCommon) {
        var commandManager = proxy.getCommandManager();

        var linkCommandMeta = commandManager.metaBuilder("link")
                .plugin(proxy.getPluginManager().getPlugin("link").orElse(null))
                .build();

        var unlinkCommandMeta = commandManager.metaBuilder("unlink")
                .plugin(proxy.getPluginManager().getPlugin("link").orElse(null))
                .build();

        commandManager.register(linkCommandMeta, new LinkCommand(linkCommon.getCodeService(), linkCommon.getLinkService()));
        commandManager.register(unlinkCommandMeta, new UnlinkCommand(linkCommon.getDiscordService(), linkCommon.getLinkService()));
    }
}

