package com.huskydreaming.link.common.dependency;

import org.slf4j.Logger;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Downloads external library JARs from Maven Central on first startup and
 * injects them into the plugin's classloader via a {@link ClassPathAppender}.
 * <p>
 * Downloaded JARs are cached in a {@code libraries/} folder inside the plugin's
 * data directory, so subsequent starts are instant.
 */
public class DependencyManager {

    private final Path librariesDir;
    private final ClassPathAppender appender;
    private final Logger logger;

    public DependencyManager(Path dataDirectory, ClassPathAppender appender, Logger logger) {
        this.librariesDir = dataDirectory.resolve("libraries");
        this.appender = appender;
        this.logger = logger;
    }

    /**
     * Downloads (if missing) and loads all given dependencies into the classpath.
     * Removes any stale JARs from previous versions.
     */
    public void loadAll(Collection<Dependency> dependencies) {
        try {
            Files.createDirectories(librariesDir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create libraries directory: " + librariesDir, e);
        }

        // Clean up old JARs before loading
        cleanStaleJars(dependencies);

        int downloaded = 0;
        for (var dep : dependencies) {
            var file = librariesDir.resolve(dep.fileName());

            if (!Files.exists(file) || isEmptyFile(file)) {
                download(dep, file);
                downloaded++;
            }

            appender.addJar(file);
        }

        if (downloaded > 0) {
            logger.info("Downloaded {} new library JAR(s) to {}", downloaded, librariesDir);
        }
        logger.info("Loaded {} libraries from cache.", dependencies.size());
    }

    /**
     * Removes any JAR files in the libraries directory that are not part of
     * the current dependency set. This prevents old versions from lingering
     * on the classpath after an update.
     */
    private void cleanStaleJars(Collection<Dependency> dependencies) {
        var expectedFiles = dependencies.stream()
                .map(Dependency::fileName)
                .collect(Collectors.toSet());

        try (var files = Files.list(librariesDir)) {
            files.filter(p -> p.toString().endsWith(".jar"))
                    .filter(p -> !expectedFiles.contains(p.getFileName().toString()))
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                            logger.info("Removed outdated library: {}", p.getFileName());
                        } catch (IOException e) {
                            logger.warn("Failed to remove outdated library: {}", p.getFileName());
                        }
                    });
        } catch (IOException e) {
            logger.warn("Failed to scan libraries directory for cleanup", e);
        }
    }

    private void download(Dependency dep, Path target) {
        var url = dep.url();
        logger.info("Downloading {}:{}:{} ...", dep.groupId(), dep.artifactId(), dep.version());

        try {
            var connection = (HttpURLConnection) URI.create(url).toURL().openConnection();
            connection.setConnectTimeout(15_000);
            connection.setReadTimeout(30_000);
            connection.setRequestProperty("User-Agent", "Link-Minecraft-Plugin");

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                throw new IOException("HTTP " + responseCode + " for " + url);
            }

            // Download to a temp file first, then atomically move
            var temp = target.resolveSibling(target.getFileName() + ".tmp");
            try (var in = connection.getInputStream()) {
                Files.copy(in, temp, StandardCopyOption.REPLACE_EXISTING);
            }
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);

        } catch (IOException e) {
            throw new RuntimeException("Failed to download dependency: " + dep.fileName() + " from " + url, e);
        }
    }

    private static boolean isEmptyFile(Path file) {
        try {
            return Files.size(file) == 0;
        } catch (IOException e) {
            return true;
        }
    }
}
