package com.huskydreaming.link.folia.initialization;

import com.huskydreaming.link.common.LinkCommonPlugin;
import com.huskydreaming.link.common.configuration.DatabaseConfig;
import com.huskydreaming.link.common.configuration.DiscordConfig;
import com.huskydreaming.link.common.configuration.LinkConfig;
import com.huskydreaming.link.common.configuration.YamlConfig;
import com.huskydreaming.link.common.utilities.Messages;
import com.huskydreaming.link.common.utilities.LogSuppressor;
import com.huskydreaming.link.folia.LinkFoliaPlugin;
import com.huskydreaming.link.folia.commands.LinkCommand;
import com.huskydreaming.link.folia.commands.UnlinkCommand;
import com.huskydreaming.link.folia.listeners.FoliaLinkListener;
import org.slf4j.LoggerFactory;

/**
 * Handles initialization of standalone mode for Folia.
 */
public class StandaloneInitializer {

    private final LinkFoliaPlugin plugin;
    private LinkCommonPlugin linkCommonPlugin;

    public StandaloneInitializer(LinkFoliaPlugin plugin) {
        this.plugin = plugin;
    }

    public LinkCommonPlugin initialize() {
        var config = YamlConfig.loadAndMergeDefaults(plugin.getDataFolder().toPath(), "config.yml");
        var databaseYaml = YamlConfig.loadAndMergeDefaults(plugin.getDataFolder().toPath(), "database.yml");
        var discordYaml = YamlConfig.loadAndMergeDefaults(plugin.getDataFolder().toPath(), "discord.yml");

        LogSuppressor.applyFromConfig(config);

        var databaseConfig = DatabaseConfig.fromYaml(plugin.getDataFolder().toPath(), databaseYaml);
        var discordConfig = DiscordConfig.fromYaml(discordYaml);
        var linkConfig = LinkConfig.fromYaml(config);

        Messages.load(plugin.getDataFolder().toPath());

        linkCommonPlugin = new LinkCommonPlugin();
        linkCommonPlugin.initialize(databaseConfig, discordConfig, linkConfig, LoggerFactory.getLogger("Link"));

        registerCommands(linkConfig);

        linkCommonPlugin.getLinkEventBus().register(
                new FoliaLinkListener(plugin, linkConfig.linkCommands()));

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