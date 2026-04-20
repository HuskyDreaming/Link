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

    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    compileOnly("org.slf4j:slf4j-api:2.0.17")

    implementation("net.kyori:adventure-api:4.17.0")
    implementation("net.kyori:adventure-text-minimessage:4.17.0")
    implementation("net.kyori:adventure-text-serializer-plain:4.17.0")
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

    relocate("com.zaxxer.hikari", "com.huskydreaming.link.libs.hikari")
    relocate("org.mariadb", "com.huskydreaming.link.libs.mariadb")
    relocate("com.github.benmanes.caffeine", "com.huskydreaming.link.libs.caffeine")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}