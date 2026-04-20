import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    id("com.gradleup.shadow") version "9.4.1"
}


repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") // Velocity
}

dependencies {
    implementation(project(":common"))

    compileOnly("com.velocitypowered:velocity-api:3.5.0-SNAPSHOT")
}

tasks.jar {
    enabled = false
}

tasks.withType<ShadowJar> {
    archiveBaseName.set("Link")
    archiveClassifier.set("velocity")
    mergeServiceFiles()

    // Relocate shaded libs to avoid classpath conflicts on the proxy
    relocate("com.zaxxer.hikari", "com.huskydreaming.link.libs.hikari")
    relocate("org.mariadb", "com.huskydreaming.link.libs.mariadb")
    relocate("com.github.benmanes.caffeine", "com.huskydreaming.link.libs.caffeine")
    relocate("net.dv8tion.jda", "com.huskydreaming.link.libs.jda")
    relocate("net.sf.trove4j", "com.huskydreaming.link.libs.trove4j")
    relocate("com.neovisionaries.ws", "com.huskydreaming.link.libs.neovisionaries")
    relocate("okhttp3", "com.huskydreaming.link.libs.okhttp3")
    relocate("okio", "com.huskydreaming.link.libs.okio")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}