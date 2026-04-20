package com.huskydreaming.link.common.dependency;

import java.nio.file.Path;

/**
 * Platform-specific strategy for adding downloaded JARs to the runtime classpath.
 */
@FunctionalInterface
public interface ClassPathAppender {

    /**
     * Adds the given JAR file to the plugin's runtime classpath.
     *
     * @param jar absolute path to the JAR to load
     */
    void addJar(Path jar);
}