package com.huskydreaming.link.common.configuration;

public record DatabaseConfig(
        String host,
        int port,
        String name,
        String username,
        String password) {

    public static DatabaseConfig fromYaml(YamlConfig config) {
        if (!config.contains("database")) {
            return empty();
        }
        return new DatabaseConfig(
                config.getString("database.host", "localhost"),
                config.getInt("database.port", 3306),
                config.getString("database.name", ""),
                config.getString("database.username", ""),
                config.getString("database.password", "")
        );
    }

    public static DatabaseConfig empty() {
        return new DatabaseConfig("localhost", 3306, "", "", "");
    }
}