package com.huskydreaming.link.common.database;

import com.huskydreaming.link.common.configuration.DatabaseConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConnector {

    private final HikariDataSource dataSource;

    public DatabaseConnector(DatabaseConfig databaseConfig, Logger logger) {

        var hikariConfig = new HikariConfig();
        var driver = databaseConfig.driver() == null ? "sqlite" : databaseConfig.driver().toLowerCase();

        switch (driver) {
            case "sqlite" -> configureSqlite(hikariConfig, databaseConfig, logger);
            case "mysql"  -> configureMysql(hikariConfig, databaseConfig, logger);
            case "mariadb" -> configureMariadb(hikariConfig, databaseConfig, logger);
            case "postgresql", "postgres" -> configurePostgresql(hikariConfig, databaseConfig, logger);
            default -> throw new IllegalArgumentException("Unknown database driver: '" + driver +
                    "'. Supported: sqlite, mysql, mariadb, postgresql");
        }

        hikariConfig.setPoolName("LinkPool");
        dataSource = new HikariDataSource(hikariConfig);

        try (var conn = dataSource.getConnection()) {
            if ("sqlite".equals(driver)) {
                // Enable WAL mode for better concurrent read performance
                try (var stmt = conn.createStatement()) {
                    stmt.execute("PRAGMA journal_mode=WAL");
                    stmt.execute("PRAGMA foreign_keys=ON");
                }
            }
            logger.info("Database connected successfully! (driver: {})", driver);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to database", e);
        }
    }

    private void configureSqlite(HikariConfig hikari, DatabaseConfig cfg, Logger logger) {
        loadDriverClass("org.sqlite.JDBC", "SQLite");

        var dbFile = new File(cfg.file());
        if (dbFile.getParentFile() != null) {
            dbFile.getParentFile().mkdirs();
        }

        logger.info("Using SQLite database at: {}", dbFile.getAbsolutePath());

        hikari.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
        // SQLite is single-writer; pool size must be 1
        hikari.setMaximumPoolSize(1);
        hikari.setMinimumIdle(1);
        hikari.setConnectionTimeout(cfg.connectionTimeout());
        // Disable keepalive and idle-timeout — not applicable for file-based DBs
        hikari.setIdleTimeout(0);
        hikari.setMaxLifetime(0);
        hikari.setKeepaliveTime(0);
        hikari.setConnectionTestQuery("SELECT 1");
    }

    private void configureMysql(HikariConfig hikari, DatabaseConfig cfg, Logger logger) {
        loadDriverClass("com.mysql.cj.jdbc.Driver", "MySQL");
        logRemoteConnection("MySQL", cfg, logger);
        validateRemote(cfg);

        hikari.setJdbcUrl("jdbc:mysql://" + cfg.host() + ":" + cfg.port() + "/" + cfg.name() +
                "?useSSL=false&autoReconnect=true&cachePrepStmts=true&useServerPrepStmts=true" +
                "&serverTimezone=UTC");
        applyRemotePoolSettings(hikari, cfg);
    }

    private void configureMariadb(HikariConfig hikari, DatabaseConfig cfg, Logger logger) {
        loadDriverClass("org.mariadb.jdbc.Driver", "MariaDB");
        logRemoteConnection("MariaDB", cfg, logger);
        validateRemote(cfg);

        hikari.setJdbcUrl("jdbc:mariadb://" + cfg.host() + ":" + cfg.port() + "/" + cfg.name() +
                "?useSSL=false&autoReconnect=true&cachePrepStmts=true&useServerPrepStmts=true");
        applyRemotePoolSettings(hikari, cfg);
    }

    private void configurePostgresql(HikariConfig hikari, DatabaseConfig cfg, Logger logger) {
        loadDriverClass("org.postgresql.Driver", "PostgreSQL");
        logRemoteConnection("PostgreSQL", cfg, logger);
        validateRemote(cfg);

        hikari.setJdbcUrl("jdbc:postgresql://" + cfg.host() + ":" + cfg.port() + "/" + cfg.name());
        applyRemotePoolSettings(hikari, cfg);
    }

    private void applyRemotePoolSettings(HikariConfig hikari, DatabaseConfig cfg) {
        hikari.setUsername(cfg.username());
        hikari.setPassword(cfg.password());
        hikari.setMaximumPoolSize(cfg.maximumPoolSize());
        hikari.setMinimumIdle(cfg.minimumIdle());
        hikari.setConnectionTimeout(cfg.connectionTimeout());
        hikari.setIdleTimeout(cfg.idleTimeout());
        hikari.setMaxLifetime(cfg.maxLifetime());
        hikari.setKeepaliveTime(cfg.keepaliveTime());
    }

    private void validateRemote(DatabaseConfig cfg) {
        require(cfg.host(), "host");
        require(cfg.name(), "database name");
        require(cfg.username(), "username");
        require(cfg.password(), "password");
    }

    private void logRemoteConnection(String driverName, DatabaseConfig cfg, Logger logger) {
        logger.info("Connecting to {} at {}:{}/{} (pool size: {}, max-lifetime: {}ms, keepalive: {}ms)",
                driverName, cfg.host(), cfg.port(), cfg.name(),
                cfg.maximumPoolSize(), cfg.maxLifetime(), cfg.keepaliveTime());
    }

    private static void loadDriverClass(String className, String driverName) {
        try {
            Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(driverName + " driver not found in classpath", e);
        }
    }

    private static String require(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Database " + field + " is missing in database.yml");
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