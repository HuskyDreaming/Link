package com.huskydreaming.link.papermc.initialization;

import com.huskydreaming.link.common.LinkCommonPlugin;
import com.huskydreaming.link.common.configuration.DatabaseConfig;
import com.huskydreaming.link.common.configuration.DiscordConfig;
import com.huskydreaming.link.common.configuration.YamlConfig;
import com.huskydreaming.link.papermc.LinkPaperPlugin;
import com.huskydreaming.link.papermc.commands.LinkCommand;
import com.huskydreaming.link.papermc.commands.UnlinkCommand;
import com.huskydreaming.link.papermc.listeners.PaperLinkListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Handles initialization of standalone mode for PaperMC.
 * Sets up the full Discord bot, database, and in-game commands locally without Velocity.
 */
public class StandaloneInitializer {

    private final LinkPaperPlugin plugin;
    private final Logger logger;

    public StandaloneInitializer(LinkPaperPlugin plugin) {
        this.plugin = plugin;
        this.logger = LoggerFactory.getLogger("Link");
    }

    /**
     * Initializes standalone mode: loads config, sets up database and Discord, registers commands.
     */
    public LinkCommonPlugin initialize() {
        var config = YamlConfig.load(plugin.getDataFolder().toPath(), "config.yml");

        var dbConfig = DatabaseConfig.fromYaml(config);
        var discordConfig = DiscordConfig.fromYaml(config);
        var linkCmds = config.getStringList("standalone.link-commands");
        var unlinkCmds = config.getStringList("standalone.unlink-commands");

        var linkCommon = new LinkCommonPlugin();
        linkCommon.initialize(dbConfig, discordConfig, logger);

        registerCommands(linkCommon, unlinkCmds);

        linkCommon.getLinkEventBus().register(new PaperLinkListener(plugin, linkCmds));

        plugin.getLogger().info("Standalone mode ready — Discord bot and database initialised.");
        return linkCommon;
    }

    private void registerCommands(LinkCommonPlugin linkCommon, List<String> unlinkCmds) {
        var linkCmd = plugin.getCommand("link");
        var unlinkCmd = plugin.getCommand("unlink");

        if (linkCmd != null) {
            linkCmd.setExecutor(new LinkCommand(linkCommon.getCodeService(), linkCommon.getLinkService()));
        }
        if (unlinkCmd != null) {
            unlinkCmd.setExecutor(new UnlinkCommand(plugin, linkCommon.getDiscordService(), linkCommon.getLinkService(), unlinkCmds));
        }
    }
}

