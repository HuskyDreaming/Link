package com.huskydreaming.link.common.configuration;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.Writer;
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
 *   List&lt;String&gt; cmds = cfg.getStringList("link.link-commands");
 * </pre>
 */
public class YamlConfig {

    private final Map<String, Object> root;

    private YamlConfig(Map<String, Object> root) {
        this.root = root;
    }

    private static final Yaml BLOCK_YAML;

    static {
        var options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        // PLAIN as global default — numbers/booleans emit without !! type tags.
        // Strings are double-quoted by QuotedValuesRepresenter.
        options.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);
        options.setIndent(2);
        options.setPrettyFlow(true);
        BLOCK_YAML = new Yaml(new QuotedValuesRepresenter(options), options);
    }

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
     * Loads a YAML file and merges in missing keys from the bundled default resource.
     * Existing user-defined values are preserved.
     */
    public static YamlConfig loadAndMergeDefaults(Path dataDirectory, String fileName) {
        var file = dataDirectory.resolve(fileName);

        if (!Files.exists(file)) {
            copyDefault(fileName, file, dataDirectory);
            return load(dataDirectory, fileName);
        }

        var yaml = new Yaml();
        try (
                var fileIn = Files.newInputStream(file);
                var defaultIn = YamlConfig.class.getClassLoader().getResourceAsStream(fileName)
        ) {
            if (defaultIn == null) {
                throw new RuntimeException("Default " + fileName + " not found in JAR");
            }

            Map<String, Object> existing = yaml.load(fileIn);
            Map<String, Object> defaults = yaml.load(defaultIn);

            if (existing == null) existing = new LinkedHashMap<>();
            if (defaults == null) {
                throw new RuntimeException("Default " + fileName + " is empty or invalid");
            }

            if (mergeMissing(existing, defaults)) {
                Files.createDirectories(dataDirectory);
                try (Writer writer = Files.newBufferedWriter(file)) {
                    BLOCK_YAML.dump(existing, writer);
                }
            }

            return new YamlConfig(existing);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read " + fileName, e);
        }
    }

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
     * Returns an int value at the given dot-separated path, or {@code fallback} if missing.
     */
    public int getInt(String path, int fallback) {
        var value = resolve(path);
        return value instanceof Number n ? n.intValue() : fallback;
    }

    /**
     * Returns a long value at the given dot-separated path, or {@code fallback} if missing.
     */
    public long getLong(String path, long fallback) {
        var value = resolve(path);
        return value instanceof Number n ? n.longValue() : fallback;
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
     * Returns a list of maps at the given dot-separated path.
     * Returns an empty list if the path is missing or not a list of maps.
     */
    public List<Map<String, Object>> getMapList(String path) {
        var value = resolve(path);
        if (value instanceof List<?> list) {
            var result = new ArrayList<Map<String, Object>>();
            for (var item : list) {
                if (item instanceof Map<?, ?> map) {
                    result.add(toStringObjectMap(map));
                }
            }
            return result;
        }
        return Collections.emptyList();
    }

    /**
     * Returns the child keys of a map section at the given dot-separated path.
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
     * Walks the nested map structure following a dot-separated path.
     * Returns {@code null} if any segment is missing.
     */
    private Object resolve(String path) {
        var parts = path.split("\\.");
        var current = (Object) root;

        for (var part : parts) {
            if (current instanceof Map<?, ?> map) {
                current = map.get(part);
            } else {
                return null;
            }
        }

        return current;
    }

    /**
     * Recursively merges keys from {@code defaults} into {@code existing}, adding only
     * those that are absent. Nested maps are merged depth-first.
     *
     * @return {@code true} if any key was added (file needs re-saving)
     */
    private static boolean mergeMissing(Map<String, Object> existing, Map<?, ?> defaults) {
        var changed = false;

        for (var entry : defaults.entrySet()) {
            if (!(entry.getKey() instanceof String key)) {
                continue;
            }

            var defaultValue = entry.getValue();
            if (!existing.containsKey(key)) {
                existing.put(key, deepCopy(defaultValue));
                changed = true;
            } else {
                var existingValue = existing.get(key);
                if (existingValue instanceof Map<?, ?> existingSection && defaultValue instanceof Map<?, ?> defaultSection) {
                    var existingStringMap = toStringObjectMap(existingSection);
                    var nestedChanged = mergeMissing(existingStringMap, defaultSection);
                    if (nestedChanged) {
                        existing.put(key, existingStringMap);
                        changed = true;
                    }
                }
            }
        }

        return changed;
    }

    /**
     * Returns a deep copy of a YAML value (map, list, or scalar).
     */
    private static Object deepCopy(Object value) {
        if (value instanceof Map<?, ?> map) {
            var copied = new LinkedHashMap<String, Object>();
            for (var entry : map.entrySet()) {
                copied.put(String.valueOf(entry.getKey()), deepCopy(entry.getValue()));
            }
            return copied;
        }
        if (value instanceof List<?> list) {
            var copied = new ArrayList<>(list.size());
            for (var element : list) {
                copied.add(deepCopy(element));
            }
            return copied;
        }
        return value;
    }

    /**
     * Safely converts a {@code Map<?, ?>} to a mutable {@code Map<String, Object>}
     * by iterating entries and stringifying keys. SnakeYAML always produces
     * {@code String} keys for YAML mappings, so no data is lost in practice.
     */
    private static Map<String, Object> toStringObjectMap(Map<?, ?> map) {
        var result = new LinkedHashMap<String, Object>();
        for (var entry : map.entrySet()) {
            result.put(String.valueOf(entry.getKey()), entry.getValue());
        }
        return result;
    }

    /**
     * Copies the bundled default resource {@code fileName} to {@code target},
     * creating parent directories as needed.
     */
    private static void copyDefault(String fileName, Path target, Path dataDirectory) {
        try (var stream = YamlConfig.class.getClassLoader().getResourceAsStream(fileName)) {
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
