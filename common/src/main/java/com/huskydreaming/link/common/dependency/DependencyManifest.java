package com.huskydreaming.link.common.dependency;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads the build-time generated {@code runtime-dependencies.json} from the classpath
 * and provides dependency groups (core, drivers, jda, adventure, etc.).
 * <p>
 * This is the single source of truth — versions are defined once in {@code build.gradle.kts}
 * and synced here automatically at build time.
 */
public final class DependencyManifest {

    private static final Pattern ENTRY_PATTERN = Pattern.compile(
            "\"groupId\"\\s*:\\s*\"([^\"]+)\"\\s*,\\s*\"artifactId\"\\s*:\\s*\"([^\"]+)\"\\s*,\\s*\"version\"\\s*:\\s*\"([^\"]+)\""
    );

    private final Map<String, List<Dependency>> groups;

    private DependencyManifest(Map<String, List<Dependency>> groups) {
        this.groups = groups;
    }

    /**
     * Loads the manifest from the classpath resource {@code runtime-dependencies.json}.
     */
    public static DependencyManifest load() {
        try (InputStream is = DependencyManifest.class.getClassLoader()
                .getResourceAsStream("runtime-dependencies.json")) {
            if (is == null) {
                throw new IllegalStateException("runtime-dependencies.json not found on classpath — was the project built correctly?");
            }
            String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            return parse(json);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read runtime-dependencies.json", e);
        }
    }

    /**
     * Core libs needed by all modes (HikariCP, Caffeine).
     */
    public List<Dependency> core() {
        return groups.getOrDefault("core", List.of());
    }

    /**
     * All database driver JARs.
     */
    public List<Dependency> drivers() {
        return groups.getOrDefault("drivers", List.of());
    }

    /**
     * Transitive dependencies for database drivers (Waffle, JNA, Protobuf).
     */
    public List<Dependency> driverTransitives() {
        return groups.getOrDefault("driverTransitives", List.of());
    }

    /**
     * All driver-related dependencies (drivers + their transitives).
     */
    public List<Dependency> allDrivers() {
        var result = new ArrayList<Dependency>();
        result.addAll(drivers());
        result.addAll(driverTransitives());
        return result;
    }

    /**
     * JDA library.
     */
    public List<Dependency> jda() {
        var result = new ArrayList<Dependency>();
        result.addAll(groups.getOrDefault("jda", List.of()));
        result.addAll(groups.getOrDefault("jdaTransitives", List.of()));
        return result;
    }

    /**
     * Adventure libraries (needed on Spigot, provided by Paper/Folia and Velocity).
     */
    public List<Dependency> adventure() {
        return groups.getOrDefault("adventure", List.of());
    }

    // ── Simple JSON parser (no external dependency needed) ──

    private static DependencyManifest parse(String json) {
        Map<String, List<Dependency>> groups = new LinkedHashMap<>();

        // Split by top-level keys
        Pattern keyPattern = Pattern.compile("\"(\\w+)\"\\s*:\\s*\\[([^\\]]*)]");
        Matcher keyMatcher = keyPattern.matcher(json);

        while (keyMatcher.find()) {
            String groupName = keyMatcher.group(1);
            String arrayContent = keyMatcher.group(2);

            List<Dependency> deps = new ArrayList<>();
            Matcher entryMatcher = ENTRY_PATTERN.matcher(arrayContent);
            while (entryMatcher.find()) {
                deps.add(new Dependency(entryMatcher.group(1), entryMatcher.group(2), entryMatcher.group(3)));
            }
            groups.put(groupName, Collections.unmodifiableList(deps));
        }

        return new DependencyManifest(groups);
    }
}

