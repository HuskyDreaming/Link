package com.huskydreaming.link.common.utilities;

import com.huskydreaming.link.common.configuration.YamlConfig;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;

/**
 * Applies log-level overrides for third-party libraries (HikariCP, JDA)
 * based on values read from {@code config.yml} under the {@code logging:} key.
 */
public final class LogSuppressor {

    private LogSuppressor() {}

    public static void applyFromConfig(YamlConfig config) {
        Level hikariLevel = parseLevel(config.getString("logging.hikari", "WARN"));
        Level jdaLevel    = parseLevel(config.getString("logging.jda",    "WARN"));

        // HikariCP — loaded externally, original package names
        Configurator.setLevel("com.zaxxer.hikari", hikariLevel);

        // JDA — loaded externally, original package names
        Configurator.setLevel("net.dv8tion.jda", jdaLevel);
    }

    private static Level parseLevel(String value) {
        Level level = Level.getLevel(value.toUpperCase());
        return level != null ? level : Level.WARN;
    }
}
