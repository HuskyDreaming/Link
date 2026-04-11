package com.huskydreaming.link.common;

import com.huskydreaming.link.common.configuration.DatabaseConfig;
import com.huskydreaming.link.common.configuration.DiscordConfig;
import com.huskydreaming.link.common.database.DatabaseConnector;
import com.huskydreaming.link.common.discord.DiscordClient;
import com.huskydreaming.link.common.discord.events.LinkEventBus;
import com.huskydreaming.link.common.initialization.DatabaseInitializer;
import com.huskydreaming.link.common.initialization.DiscordInitializer;
import com.huskydreaming.link.common.initialization.ServiceInitializer;
import com.huskydreaming.link.common.repositories.LinkRepository;
import com.huskydreaming.link.common.services.interfaces.CodeService;
import com.huskydreaming.link.common.services.interfaces.DiscordService;
import com.huskydreaming.link.common.services.interfaces.LinkService;
import org.slf4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LinkCommonPlugin {

    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    private final LinkEventBus linkEventBus = new LinkEventBus(executor);

    private CodeService codeService;
    private DiscordService discordService;
    private LinkService linkService;

    private DiscordClient discordClient;
    private DatabaseConnector databaseConnector;

    public void initialize(DatabaseConfig databaseConfig, DiscordConfig discordConfig, Logger logger) {
        // Database & repository initialization
        var dbInit = new DatabaseInitializer(databaseConfig, logger);
        databaseConnector = dbInit.initializeDatabaseConnector();
        LinkRepository linkRepository = dbInit.initializeRepository(databaseConnector);

        // Service initialization
        var svcInit = new ServiceInitializer(executor);
        codeService = svcInit.initializeCodeService();
        linkService = svcInit.initializeLinkService(linkRepository);
        linkService.initialize().exceptionally(ex -> {
            logger.error("Failed to initialize LinkService", ex);
            return null;
        });

        // Discord initialization
        var discordInit = new DiscordInitializer(discordConfig, logger);
        discordClient = discordInit.initializeDiscordClient();
        discordService = discordInit.initializeDiscordService(discordClient);
        discordInit.registerDiscordListeners(discordClient.getJda(), this);
    }

    public CodeService getCodeService() {
        return codeService;
    }

    public DiscordService getDiscordService() {
        return discordService;
    }

    public LinkEventBus getLinkEventBus() {
        return linkEventBus;
    }

    public LinkService getLinkService() {
        return linkService;
    }

    public void shutdown() {
        if (discordClient != null) {
            discordClient.close();
        }
        if (databaseConnector != null) {
            databaseConnector.close();
        }
        executor.shutdownNow();
    }
}