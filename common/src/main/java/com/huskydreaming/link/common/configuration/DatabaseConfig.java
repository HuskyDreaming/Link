package com.huskydreaming.link.common.configuration;

/**
 * Immutable representation of the {@code database} block in {@code config.yml}.
 *
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
                config.getString("database.host", "localhost"),
                config.getInt("database.port", 3306),
                config.getString("database.name", ""),
                config.getString("database.username", ""),
                config.getString("database.password", ""),
                config.getInt("database.pool.maximum-pool-size", 10),
                config.getInt("database.pool.minimum-idle", 2),
                config.getLong("database.pool.connection-timeout", 10000L),
                config.getLong("database.pool.idle-timeout", 600000L),
                config.getLong("database.pool.max-lifetime", 1800000L),
                config.getLong("database.pool.keepalive-time", 60000L)
        );
    }
}