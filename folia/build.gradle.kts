import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.gradleup.shadow") version "9.4.1"
}

repositories {
    mavenCentral()
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
    implementation(project(":common"))

    compileOnly("io.papermc.paper:paper-api:26.1.1.build.29-alpha")
    compileOnly("org.slf4j:slf4j-api:2.0.17")

    // Adventure is provided by Paper/Folia at runtime
    compileOnly("net.kyori:adventure-api:4.26.1")
    compileOnly("net.kyori:adventure-text-minimessage:4.26.1")
    compileOnly("net.kyori:adventure-text-serializer-plain:4.26.1")
}

tasks.jar {
    enabled = false
}

tasks.withType<ShadowJar> {
    archiveBaseName.set("Link")
    archiveClassifier.set("folia")
    mergeServiceFiles()

    dependencies {
        exclude(dependency("org.yaml:snakeyaml:.*"))
        exclude(dependency("org.slf4j:.*"))
    }

    // No relocations — all heavy libs are external (downloaded at runtime)
}

tasks.build {
    dependsOn(tasks.shadowJar)
}