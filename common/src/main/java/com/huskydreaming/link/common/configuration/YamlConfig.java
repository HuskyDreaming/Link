package com.huskydreaming.link.common.configuration;

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * A simple YAML configuration reader that supports dot-path lookups.
 * <p>
 * Usage:
 * <pre>
 *   var cfg = YamlConfig.load(dataDirectory, "config.yml");
 *   String host = cfg.getString("database.host", "localhost");
 *   int    port = cfg.getInt("database.port", 3306);
 *   List&lt;String&gt; cmds = cfg.getStringList("standalone.link-commands");
 * </pre>
 */
public class YamlConfig {

    private final Map<String, Object> root;

    private YamlConfig(Map<String, Object> root) {
        this.root = root;
    }

    // ──────────────────────────────────────────────
    // Factory
    // ──────────────────────────────────────────────

    /**
     * Loads a YAML file from {@code dataDirectory}. If the file doesn't exist it
     * is copied from the JAR's resources first.
     */
    public static YamlConfig load(Path dataDirectory, String fileName) {
        var file = dataDirectory.resolve(fileName);

        if (!Files.exists(file)) {
            copyDefault(fileName, file, dataDirectory);
        }

        try (var in = Files.newInputStream(file)) {
            Map<String, Object> map = new Yaml().load(in);
            if (map == null) {
                throw new RuntimeException(fileName + " is empty or invalid");
            }
            return new YamlConfig(map);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read " + fileName, e);
        }
    }

    /**
     * Loads a YAML file from a raw {@link InputStream}.
     * Useful for Bukkit's {@code getResource()} or tests.
     */
    public static YamlConfig load(InputStream inputStream) {
        Map<String, Object> map = new Yaml().load(inputStream);
        if (map == null) {
            throw new RuntimeException("YAML input is empty or invalid");
        }
        return new YamlConfig(map);
    }

    // ──────────────────────────────────────────────
    // Dot-path getters
    // ──────────────────────────────────────────────

    /**
     * Returns a string value at the given dot-separated path.
     *
     * @param path     e.g. {@code "database.host"}
     * @param fallback returned when the path doesn't exist
     */
    public String getString(String path, String fallback) {
        var value = resolve(path);
        return value != null ? value.toString() : fallback;
    }

    /**
     * Returns an int value at the given dot-separated path.
     */
    public int getInt(String path, int fallback) {
        var value = resolve(path);
        return value instanceof Number n ? n.intValue() : fallback;
    }

    /**
     * Returns a long value at the given dot-separated path.
     */
    public long getLong(String path, long fallback) {
        var value = resolve(path);
        return value instanceof Number n ? n.longValue() : fallback;
    }

    /**
     * Returns a boolean value at the given dot-separated path.
     */
    public boolean getBoolean(String path, boolean fallback) {
        var value = resolve(path);
        return value instanceof Boolean b ? b : fallback;
    }

    /**
     * Returns a list of strings at the given dot-separated path.
     * Returns an empty list if the path is missing or not a list.
     */
    public List<String> getStringList(String path) {
        var value = resolve(path);
        if (value instanceof List<?> list) {
            return list.stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .toList();
        }
        return Collections.emptyList();
    }

    /**
     * Returns the keys of a section at the given dot-separated path.
     * Returns an empty set if the path is missing or not a map.
     */
    public Set<String> getKeys(String path) {
        var value = resolve(path);
        if (value instanceof Map<?, ?> map) {
            var keys = new LinkedHashSet<String>();
            for (var key : map.keySet()) {
                if (key instanceof String s) keys.add(s);
            }
            return keys;
        }
        return Collections.emptySet();
    }

    /**
     * Returns true if the given dot-separated path exists in the configuration.
     */
    public boolean contains(String path) {
        return resolve(path) != null;
    }

    // ──────────────────────────────────────────────
    // Internal
    // ──────────────────────────────────────────────

    /**
     * Walks the nested map structure following a dot-separated path.
     */
    private Object resolve(String path) {
        var parts = path.split("\\.");
        Object current = root;

        for (var part : parts) {
            if (current instanceof Map<?, ?> map) {
                current = map.get(part);
            } else {
                return null;
            }
        }

        return current;
    }

    private static void copyDefault(String fileName, Path target, Path dataDirectory) {
        try (InputStream stream = YamlConfig.class.getClassLoader().getResourceAsStream(fileName)) {
            if (stream == null) {
                throw new RuntimeException("Default " + fileName + " not found in JAR");
            }
            Files.createDirectories(dataDirectory);
            Files.copy(stream, target);
        } catch (IOException e) {
            throw new RuntimeException("Failed to copy default " + fileName, e);
        }
    }
}