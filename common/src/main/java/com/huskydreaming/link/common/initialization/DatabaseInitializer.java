package com.huskydreaming.link.common.initialization;

import com.huskydreaming.link.common.configuration.DatabaseConfig;
import com.huskydreaming.link.common.database.DatabaseConnector;
import com.huskydreaming.link.common.repositories.LinkRepository;
import com.huskydreaming.link.common.repositories.LinkRepositoryImpl;
import org.slf4j.Logger;

/**
 * Handles initialization of database-related components.
 */
public class DatabaseInitializer {

    private final DatabaseConfig config;
    private final Logger logger;

    public DatabaseInitializer(DatabaseConfig config, Logger logger) {
        this.config = config;
        this.logger = logger;
    }

    /**
     * Creates and initializes a DatabaseConnector.
     */
    public DatabaseConnector initializeDatabaseConnector() {
        return new DatabaseConnector(config, logger);
    }

    /**
     * Creates a LinkRepository using the given DatabaseConnector.
     */
    public LinkRepository initializeRepository(DatabaseConnector connector) {
        return new LinkRepositoryImpl(connector);
    }
}

