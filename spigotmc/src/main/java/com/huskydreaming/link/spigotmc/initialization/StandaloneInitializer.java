package com.huskydreaming.link.spigotmc.initialization;

import com.huskydreaming.link.common.LinkCommonPlugin;
import com.huskydreaming.link.common.configuration.DatabaseConfig;
import com.huskydreaming.link.common.configuration.DiscordConfig;
import com.huskydreaming.link.common.configuration.LinkConfig;
import com.huskydreaming.link.common.configuration.YamlConfig;
import com.huskydreaming.link.common.utilities.Messages;
import com.huskydreaming.link.spigotmc.LinkSpigotPlugin;
import com.huskydreaming.link.spigotmc.commands.LinkCommand;
import com.huskydreaming.link.spigotmc.commands.UnlinkCommand;
import com.huskydreaming.link.spigotmc.listeners.SpigotLinkListener;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Handles initialization of standalone mode for SpigotMC.
 * Sets up the full Discord bot, database, and in-game commands locally without Velocity.
 */
public class StandaloneInitializer {

    private final LinkSpigotPlugin plugin;

    public StandaloneInitializer(LinkSpigotPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Initializes standalone mode: loads config, sets up database and Discord, registers commands.
     */
    public LinkCommonPlugin initialize() {
        var config = YamlConfig.loadAndMergeDefaults(plugin.getDataFolder().toPath(), "config.yml");

        var databaseConfig = DatabaseConfig.fromYaml(config);
        var discordConfig = DiscordConfig.fromYaml(config);
        var linkConfig = LinkConfig.fromYaml(config);

        // Load player-facing messages
        Messages.load(plugin.getDataFolder().toPath());

        var linkCommonPlugin = new LinkCommonPlugin();
        linkCommonPlugin.initialize(databaseConfig, discordConfig, linkConfig, LoggerFactory.getLogger("Link"));

        registerCommands(linkCommonPlugin, linkConfig.unlinkCommands());

        linkCommonPlugin.getLinkEventBus().register(
                new SpigotLinkListener(plugin, linkConfig.linkCommands()));

        plugin.getLogger().info("Standalone mode ready — Discord bot and database initialised.");
        return linkCommonPlugin;
    }

    private void registerCommands(LinkCommonPlugin linkCommonPlugin, List<String> unlinkCommands) {
        var linkCommand = plugin.getCommand("link");
        if (linkCommand != null) {
            linkCommand.setExecutor(new LinkCommand(
                    linkCommonPlugin.getCodeService(),
                    linkCommonPlugin.getLinkService()));
        }

        var unlinkCommand = plugin.getCommand("unlink");
        if (unlinkCommand != null) {
            unlinkCommand.setExecutor(new UnlinkCommand(
                    plugin,
                    linkCommonPlugin.getDiscordService(),
                    linkCommonPlugin.getLinkService(),
                    unlinkCommands));
        }
    }
}
