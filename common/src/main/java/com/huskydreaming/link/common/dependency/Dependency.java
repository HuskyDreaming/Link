package com.huskydreaming.link.common.dependency;

/**
 * Represents a single external library to be downloaded from Maven Central at runtime.
 *
 * @param groupId    Maven group ID (e.g. "com.zaxxer")
 * @param artifactId Maven artifact ID (e.g. "HikariCP")
 * @param version    Maven version (e.g. "5.1.0")
 */
public record Dependency(String groupId, String artifactId, String version) {

    private static final String MAVEN_CENTRAL = "https://repo1.maven.org/maven2/";

    /**
     * Maven Central download URL.
     */
    public String url() {
        return MAVEN_CENTRAL + groupId.replace('.', '/') + "/"
                + artifactId + "/" + version + "/" + fileName();
    }

    /**
     * JAR file name used for local caching.
     */
    public String fileName() {
        return artifactId + "-" + version + ".jar";
    }
}