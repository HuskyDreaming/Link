import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.gradleup.shadow") version "9.4.1"
}

repositories {
    mavenCentral()
    maven {
        name = "spigotmc"
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    }
}

dependencies {
    implementation(project(":common"))

    compileOnly("org.spigotmc:spigot-api:1.21.4-R0.1-SNAPSHOT")
    compileOnly("org.slf4j:slf4j-api:2.0.17")

    // Serialize Adventure Components to BungeeCord BaseComponent[] for sending.
    // BungeeCord-style components support hover/click natively in Spigot via
    // player.spigot().sendMessage() — no NMS reflection needed, works on all versions.
    implementation("net.kyori:adventure-api:4.17.0")
    implementation("net.kyori:adventure-text-minimessage:4.17.0")
    implementation("net.kyori:adventure-text-serializer-plain:4.17.0")
    implementation("net.kyori:adventure-text-serializer-bungeecord:4.3.4")
}

tasks.jar {
    enabled = false
}

tasks.withType<ShadowJar> {
    archiveBaseName.set("Link")
    archiveClassifier.set("")
    mergeServiceFiles()

    // Exclude libs already provided by Velocity / bundled elsewhere
    dependencies {
        exclude(dependency("org.yaml:snakeyaml:.*"))
        exclude(dependency("org.slf4j:.*"))
    }

    // Relocate shaded libs to avoid classpath conflicts
    relocate("com.zaxxer.hikari", "com.huskydreaming.link.libs.hikari")
    relocate("org.mariadb", "com.huskydreaming.link.libs.mariadb")
    relocate("com.github.benmanes.caffeine", "com.huskydreaming.link.libs.caffeine")
    // Note: net.kyori.adventure is intentionally NOT relocated – Bukkit 1.17+ shares
    // Adventure classes across the server and plugins via a common classloader.
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
