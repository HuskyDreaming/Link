package com.huskydreaming.link.common.database;

import com.huskydreaming.link.common.configuration.DatabaseConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConnector {

    private final HikariDataSource dataSource;

    public DatabaseConnector(DatabaseConfig databaseConfig, Logger logger) {

        try {
            Class.forName("org.mariadb.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MariaDB driver not found in classpath", e);
        }

        var host = require(databaseConfig.host(), "host");
        var name = require(databaseConfig.name(), "database name");
        var port = databaseConfig.port();
        var username = require(databaseConfig.username(), "username");
        var password = require(databaseConfig.password(), "password");


        var hikariConfig = new HikariConfig();

        // Log connection summary (no password)
        logger.info("Connecting to {}:{}/{} (pool size: {}, max-lifetime: {}ms, keepalive: {}ms)",
                host, port, name,
                databaseConfig.maximumPoolSize(),
                databaseConfig.maxLifetime(),
                databaseConfig.keepaliveTime());

        // Sanity-check pool settings and warn if they look problematic
        if (databaseConfig.keepaliveTime() == 0) {
            logger.warn("'keepalive-time' is 0 — idle connections may be closed by the server. Consider setting it to ~60000.");
        }
        if (databaseConfig.maxLifetime() > 0 && databaseConfig.keepaliveTime() > 0
                && databaseConfig.keepaliveTime() >= databaseConfig.maxLifetime()) {
            logger.warn("'keepalive-time' ({}) should be less than 'max-lifetime' ({}).",
                    databaseConfig.keepaliveTime(), databaseConfig.maxLifetime());
        }

        hikariConfig.setJdbcUrl(
                "jdbc:mariadb://" + host + ":" + port + "/" + name +
                        "?useSSL=false&autoReconnect=true&cachePrepStmts=true&useServerPrepStmts=true"
        );

        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);
        hikariConfig.setMaximumPoolSize(databaseConfig.maximumPoolSize());
        hikariConfig.setMinimumIdle(databaseConfig.minimumIdle());
        hikariConfig.setConnectionTimeout(databaseConfig.connectionTimeout());
        hikariConfig.setIdleTimeout(databaseConfig.idleTimeout());
        hikariConfig.setMaxLifetime(databaseConfig.maxLifetime());
        hikariConfig.setKeepaliveTime(databaseConfig.keepaliveTime());
        hikariConfig.setPoolName("LinkPool");

        dataSource = new HikariDataSource(hikariConfig);

        try (var ignored = dataSource.getConnection()) {
            logger.info("Database connected successfully!");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to database", e);
        }
    }

    private String require(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Database " + field + " is missing in config.yml");
        }
        return value;
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}