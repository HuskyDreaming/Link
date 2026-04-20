plugins {
    java
}

group = "com.huskydreaming"
version = "1.0.3"

subprojects {
    apply(plugin = "java")

    group = rootProject.group
    version = rootProject.version

    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    }

    repositories {
        mavenCentral()
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    tasks.withType<ProcessResources> {
        val projectVersion = project.version.toString()
        filesMatching(listOf("plugin.yml", "velocity-plugin.json")) {
            expand("version" to projectVersion)
        }
    }
}