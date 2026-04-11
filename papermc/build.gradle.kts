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
}

tasks.jar {
    enabled = false
}

tasks.withType<ShadowJar> {
    archiveClassifier.set("")
    mergeServiceFiles()

    // Exclude libs already provided by Paper/Velocity
    dependencies {
        exclude(dependency("org.yaml:snakeyaml:.*"))
        exclude(dependency("org.slf4j:.*"))
    }

    // Relocate shaded libs to avoid classpath conflicts
    relocate("com.zaxxer.hikari", "com.huskydreaming.link.libs.hikari")
    relocate("org.mariadb", "com.huskydreaming.link.libs.mariadb")
    relocate("com.github.benmanes.caffeine", "com.huskydreaming.link.libs.caffeine")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
