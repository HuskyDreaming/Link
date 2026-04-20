package com.huskydreaming.link.spigot.initialization;

import com.huskydreaming.link.common.LinkCommonPlugin;
import com.huskydreaming.link.common.configuration.DatabaseConfig;
import com.huskydreaming.link.common.configuration.DiscordConfig;
import com.huskydreaming.link.common.configuration.LinkConfig;
import com.huskydreaming.link.common.configuration.YamlConfig;
import com.huskydreaming.link.common.utilities.Messages;
import com.huskydreaming.link.spigot.LinkSpigotPlugin;
import com.huskydreaming.link.spigot.commands.LinkCommand;
import com.huskydreaming.link.spigot.commands.UnlinkCommand;
import com.huskydreaming.link.spigot.listeners.SpigotLinkListener;
import org.slf4j.LoggerFactory;

/**
 * Handles initialization of standalone mode for SpigotMC.
 * Sets up the full Discord bot, database, and in-game commands locally without Velocity.
 */
public class StandaloneInitializer {

    private final LinkSpigotPlugin plugin;
    private LinkCommonPlugin linkCommonPlugin;

    public StandaloneInitializer(LinkSpigotPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Initializes standalone mode: loads config, sets up database and Discord, registers commands.
     */
    public LinkCommonPlugin initialize() {
        var config = YamlConfig.loadAndMergeDefaults(plugin.getDataFolder().toPath(), "config.yml");
        var dbConfig = YamlConfig.loadAndMergeDefaults(plugin.getDataFolder().toPath(), "database.yml");
        var discordYaml = YamlConfig.loadAndMergeDefaults(plugin.getDataFolder().toPath(), "discord.yml");

        var databaseConfig = DatabaseConfig.fromYaml(dbConfig);
        var discordConfig = DiscordConfig.fromYaml(discordYaml);
        var linkConfig = LinkConfig.fromYaml(config);

        // Load player-facing messages
        Messages.load(plugin.getDataFolder().toPath());

        linkCommonPlugin = new LinkCommonPlugin();
        linkCommonPlugin.initialize(databaseConfig, discordConfig, linkConfig, LoggerFactory.getLogger("Link"));

        registerCommands(linkConfig);

        linkCommonPlugin.getLinkEventBus().register(
                new SpigotLinkListener(plugin, linkConfig.linkCommands()));

        plugin.getLogger().info("Standalone mode ready — Discord bot and database initialised.");
        return linkCommonPlugin;
    }

    /**
     * Safely reloads messages and config values without restarting JDA or the database.
     */
    public void reload() {
        var config = YamlConfig.loadAndMergeDefaults(plugin.getDataFolder().toPath(), "config.yml");
        YamlConfig.loadAndMergeDefaults(plugin.getDataFolder().toPath(), "database.yml");
        var discordYaml = YamlConfig.loadAndMergeDefaults(plugin.getDataFolder().toPath(), "discord.yml");
        var linkConfig = LinkConfig.fromYaml(config);

        Messages.load(plugin.getDataFolder().toPath());

        if (linkCommonPlugin != null) {
            linkCommonPlugin.getLinkService().updateCooldown(linkConfig.cooldownMillis());
            linkCommonPlugin.reloadDiscordConfig(DiscordConfig.fromYaml(discordYaml));
        }

        registerCommands(linkConfig);
        plugin.getLogger().info("Configuration reloaded.");
    }

    private void registerCommands(LinkConfig linkConfig) {
        var linkCommand = plugin.getCommand("link");
        if (linkCommand != null) {
            var executor = new LinkCommand(
                    plugin,
                    linkCommonPlugin.getCodeService(),
                    linkCommonPlugin.getLinkService());
            linkCommand.setExecutor(executor);
            linkCommand.setTabCompleter(executor);
        }

        var unlinkCommand = plugin.getCommand("unlink");
        if (unlinkCommand != null) {
            unlinkCommand.setExecutor(new UnlinkCommand(
                    plugin,
                    linkCommonPlugin.getDiscordService(),
                    linkCommonPlugin.getLinkService(),
                    linkConfig.unlinkCommands()));
        }
    }
}
