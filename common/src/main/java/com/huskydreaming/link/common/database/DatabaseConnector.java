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
        var driver = databaseConfig.driver() == null ? "h2" : databaseConfig.driver().toLowerCase();

        switch (driver) {
            case "sqlite" -> configureSqlite(hikariConfig, databaseConfig, logger);
            case "h2" -> configureH2(hikariConfig, databaseConfig, logger);
            case "mysql"  -> configureMysql(hikariConfig, databaseConfig, logger);
            case "mariadb" -> configureMariadb(hikariConfig, databaseConfig, logger);
            case "postgresql", "postgres" -> configurePostgresql(hikariConfig, databaseConfig, logger);
            default -> throw new IllegalArgumentException("Unknown database driver: '" + driver +
                    "'. Supported: sqlite, h2, mysql, mariadb, postgresql");
        }

        hikariConfig.setPoolName("LinkPool");
        dataSource = new HikariDataSource(hikariConfig);

        try (var conn = dataSource.getConnection()) {
            if ("sqlite".equals(driver)) {
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
        loadDriver("org.sqlite.JDBC", "SQLite");

        var dbFile = cfg.dataDirectory().resolve(cfg.file()).toFile();
        mkdirs(dbFile);

        logger.info("Using SQLite database at: {}", dbFile.getAbsolutePath());

        hikari.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
        applyFilePoolSettings(hikari, cfg);
    }

    private void configureH2(HikariConfig hikari, DatabaseConfig cfg, Logger logger) {
        loadDriver("org.h2.Driver", "H2");

        // Strip .mv.db / .h2.db extension if present so H2 doesn't double-suffix
        String filePath = cfg.dataDirectory().resolve(cfg.file()).toAbsolutePath().toString();
        if (filePath.endsWith(".mv.db")) filePath = filePath.substring(0, filePath.length() - 6);
        if (filePath.endsWith(".h2.db")) filePath = filePath.substring(0, filePath.length() - 6);
        if (filePath.endsWith(".db"))    filePath = filePath.substring(0, filePath.length() - 3);

        File parent = new File(filePath).getParentFile();
        if (parent != null) parent.mkdirs();

        logger.info("Using H2 database at: {}", filePath);

        hikari.setJdbcUrl("jdbc:h2:file:" + filePath + ";MODE=MySQL;DATABASE_TO_LOWER=TRUE");
        applyFilePoolSettings(hikari, cfg);
    }

    private void configureMysql(HikariConfig hikari, DatabaseConfig cfg, Logger logger) {
        loadDriver("com.mysql.cj.jdbc.Driver", "MySQL");
        logRemoteConnection("MySQL", cfg, logger);
        validateRemote(cfg);

        hikari.setJdbcUrl("jdbc:mysql://" + cfg.host() + ":" + cfg.port() + "/" + cfg.name() +
                "?useSSL=false&autoReconnect=true&cachePrepStmts=true&useServerPrepStmts=true" +
                "&serverTimezone=UTC");
        applyRemotePoolSettings(hikari, cfg);
    }

    private void configureMariadb(HikariConfig hikari, DatabaseConfig cfg, Logger logger) {
        loadDriver("org.mariadb.jdbc.Driver", "MariaDB");
        logRemoteConnection("MariaDB", cfg, logger);
        validateRemote(cfg);

        hikari.setJdbcUrl("jdbc:mariadb://" + cfg.host() + ":" + cfg.port() + "/" + cfg.name() +
                "?useSSL=false&autoReconnect=true&cachePrepStmts=true&useServerPrepStmts=true");
        applyRemotePoolSettings(hikari, cfg);
    }

    private void configurePostgresql(HikariConfig hikari, DatabaseConfig cfg, Logger logger) {
        loadDriver("org.postgresql.Driver", "PostgreSQL");
        logRemoteConnection("PostgreSQL", cfg, logger);
        validateRemote(cfg);

        hikari.setJdbcUrl("jdbc:postgresql://" + cfg.host() + ":" + cfg.port() + "/" + cfg.name());
        applyRemotePoolSettings(hikari, cfg);
    }

    /** Pool settings for file-based databases (SQLite, H2). */
    private void applyFilePoolSettings(HikariConfig hikari, DatabaseConfig cfg) {
        hikari.setMaximumPoolSize(1);
        hikari.setMinimumIdle(1);
        hikari.setConnectionTimeout(cfg.connectionTimeout());
        hikari.setIdleTimeout(0);
        hikari.setMaxLifetime(0);
        hikari.setKeepaliveTime(0);
        hikari.setConnectionTestQuery("SELECT 1");
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

    /**
     * Loads a JDBC driver class by name. Since libraries are no longer relocated
     * (they're downloaded externally), Class.forName() works with literal class names.
     */
    private static void loadDriver(String className, String driverName) {
        try {
            Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(driverName + " driver not found in classpath. " +
                    "Check that the libraries/ folder exists and contains the required JAR.", e);
        }
    }

    private static void mkdirs(File file) {
        if (file.getParentFile() != null) file.getParentFile().mkdirs();
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