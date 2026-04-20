package com.huskydreaming.link.common.configuration;

/**
 * Immutable representation of the {@code database.yml} configuration.
 *
 * @param driver            Database driver: sqlite, mysql, mariadb, or postgresql.
 * @param file              File path for SQLite databases (ignored for other drivers).
 * @param host              Database host address.
 * @param port              Database port.
 * @param name              Database (schema) name.
 * @param username          Login username.
 * @param password          Login password.
 * @param maximumPoolSize   Maximum number of connections in the HikariCP pool.
 * @param minimumIdle       Minimum number of idle connections maintained in the pool.
 * @param connectionTimeout Milliseconds to wait for a connection before throwing.
 * @param idleTimeout       Milliseconds a connection can sit idle before being retired.
 * @param maxLifetime       Milliseconds before a connection is retired regardless of activity.
 * @param keepaliveTime     Milliseconds between keepalive pings on idle connections.
 */
public record DatabaseConfig(
        String driver,
        String file,
        String host,
        int port,
        String name,
        String username,
        String password,
        int maximumPoolSize,
        int minimumIdle,
        long connectionTimeout,
        long idleTimeout,
        long maxLifetime,
        long keepaliveTime) {

    /**
     * Parses a {@link DatabaseConfig} from the given {@link YamlConfig},
     * reading all values from the {@code database} and {@code database.pool} sections.
     */
    public static DatabaseConfig fromYaml(YamlConfig config) {
        return new DatabaseConfig(
                config.getString("driver", "sqlite"),
                config.getString("file", "plugins/link/link.db"),
                config.getString("host", "localhost"),
                config.getInt("port", 3306),
                config.getString("name", "link"),
                config.getString("username", ""),
                config.getString("password", ""),
                config.getInt("pool.maximum-pool-size", 10),
                config.getInt("pool.minimum-idle", 2),
                config.getLong("pool.connection-timeout", 10000L),
                config.getLong("pool.idle-timeout", 600000L),
                config.getLong("pool.max-lifetime", 1800000L),
                config.getLong("pool.keepalive-time", 60000L)
        );
    }

    /** Returns true if the configured driver is SQLite. */
    public boolean isSqlite() {
        return "sqlite".equalsIgnoreCase(driver);
    }
}