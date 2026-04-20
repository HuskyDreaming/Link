import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    id("com.gradleup.shadow") version "9.4.1"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    implementation(project(":common"))

    compileOnly("com.velocitypowered:velocity-api:3.5.0-SNAPSHOT")

    // All runtime libs downloaded by DependencyManager — only needed at compile time here
}

tasks.jar {
    enabled = false
}

tasks.withType<ShadowJar> {
    archiveBaseName.set("Link")
    archiveClassifier.set("velocity")
    mergeServiceFiles()

    // No relocations — all heavy libs are external (downloaded at runtime)
}

tasks.build {
    dependsOn(tasks.shadowJar)
}