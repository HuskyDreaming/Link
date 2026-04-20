plugins {
    id("java")
}

group = "com.huskydreaming"
version = "1.0.0"

repositories {
    mavenCentral()
}

// ── Custom configurations for runtime-downloaded dependency groups ──
val runtimeCore by configurations.creating { isTransitive = false }
val runtimeDrivers by configurations.creating { isTransitive = false }
val runtimeDriverTransitives by configurations.creating { isTransitive = false }
val runtimeJda by configurations.creating { isTransitive = false }
val runtimeJdaTransitives by configurations.creating { isTransitive = false }
val runtimeAdventure by configurations.creating { isTransitive = false }

// ── Version catalog ──
val hikariVersion = "7.0.2"
val caffeineVersion = "3.2.3"
val sqliteVersion = "3.53.0.0"
val h2Version = "2.4.240"
val mariadbVersion = "3.5.8"
val mysqlVersion = "9.6.0"
val postgresVersion = "42.7.10"
val jdaVersion = "6.4.1"
val adventureVersion = "4.26.1"
val adventureBungeeVersion = "4.4.1"
val examinationVersion = "1.3.0"

dependencies {
    // All external libraries are compileOnly — downloaded at runtime by DependencyManager
    compileOnly("net.kyori:adventure-api:$adventureVersion")
    compileOnly("net.kyori:adventure-text-minimessage:$adventureVersion")
    compileOnly("org.apache.logging.log4j:log4j-core:2.25.4")

    compileOnly("org.xerial:sqlite-jdbc:$sqliteVersion")
    compileOnly("org.mariadb.jdbc:mariadb-java-client:$mariadbVersion")
    compileOnly("com.mysql:mysql-connector-j:$mysqlVersion")
    compileOnly("org.postgresql:postgresql:$postgresVersion")
    compileOnly("com.h2database:h2:$h2Version")
    compileOnly("com.zaxxer:HikariCP:$hikariVersion")
    compileOnly("org.yaml:snakeyaml:2.6")
    compileOnly("com.github.ben-manes.caffeine:caffeine:$caffeineVersion")

    compileOnly("net.dv8tion:JDA:$jdaVersion") {
        exclude(module = "opus-java")
        exclude(module = "tink")
    }

    // ── Runtime download groups ──
    runtimeCore("com.zaxxer:HikariCP:$hikariVersion")
    runtimeCore("com.github.ben-manes.caffeine:caffeine:$caffeineVersion")

    runtimeDrivers("org.xerial:sqlite-jdbc:$sqliteVersion")
    runtimeDrivers("com.h2database:h2:$h2Version")
    runtimeDrivers("org.mariadb.jdbc:mariadb-java-client:$mariadbVersion")
    runtimeDrivers("com.mysql:mysql-connector-j:$mysqlVersion")
    runtimeDrivers("org.postgresql:postgresql:$postgresVersion")

    // Driver transitives
    runtimeDriverTransitives("com.google.protobuf:protobuf-java:4.31.1")

    // JDA
    runtimeJda("net.dv8tion:JDA:$jdaVersion")

    // JDA transitives
    runtimeJdaTransitives("net.sf.trove4j:core:3.1.0")
    runtimeJdaTransitives("com.fasterxml.jackson.core:jackson-core:2.21.2")
    runtimeJdaTransitives("com.fasterxml.jackson.core:jackson-databind:2.21.2")
    runtimeJdaTransitives("com.fasterxml.jackson.core:jackson-annotations:2.21")
    runtimeJdaTransitives("com.neovisionaries:nv-websocket-client:2.14")
    runtimeJdaTransitives("com.squareup.okhttp3:okhttp-jvm:5.3.2")
    runtimeJdaTransitives("com.squareup.okio:okio-jvm:3.16.4")
    runtimeJdaTransitives("org.jetbrains.kotlin:kotlin-stdlib:2.2.21")
    runtimeJdaTransitives("org.apache.commons:commons-collections4:4.5.0")

    // Adventure (Spigot only)
    runtimeAdventure("net.kyori:adventure-api:$adventureVersion")
    runtimeAdventure("net.kyori:adventure-key:$adventureVersion")
    runtimeAdventure("net.kyori:adventure-text-minimessage:$adventureVersion")
    runtimeAdventure("net.kyori:adventure-text-serializer-plain:$adventureVersion")
    runtimeAdventure("net.kyori:adventure-text-serializer-bungeecord:$adventureBungeeVersion")
    runtimeAdventure("net.kyori:adventure-text-serializer-gson:$adventureVersion")
    runtimeAdventure("net.kyori:examination-api:$examinationVersion")
    runtimeAdventure("net.kyori:examination-string:$examinationVersion")
}

// ── Generate runtime-dependencies.json ──
val generateDependencyManifest by tasks.registering {
    val outputDir = layout.buildDirectory.dir("generated/resources/dependencies")
    outputs.dir(outputDir)

    // Depend on resolution of all custom configurations
    inputs.files(runtimeCore, runtimeDrivers, runtimeDriverTransitives,
            runtimeJda, runtimeJdaTransitives, runtimeAdventure)

    doLast {
        fun configToJson(config: Configuration): String {
            return config.resolvedConfiguration.resolvedArtifacts.joinToString(",\n    ") { artifact ->
                val id = artifact.moduleVersion.id
                """{"groupId":"${id.group}","artifactId":"${id.name}","version":"${id.version}"}"""
            }
        }

        val json = buildString {
            appendLine("{")
            appendLine("""  "core": [${"\n    "}${configToJson(runtimeCore)}${"\n  "}],""")
            appendLine("""  "drivers": [${"\n    "}${configToJson(runtimeDrivers)}${"\n  "}],""")
            appendLine("""  "driverTransitives": [${"\n    "}${configToJson(runtimeDriverTransitives)}${"\n  "}],""")
            appendLine("""  "jda": [${"\n    "}${configToJson(runtimeJda)}${"\n  "}],""")
            appendLine("""  "jdaTransitives": [${"\n    "}${configToJson(runtimeJdaTransitives)}${"\n  "}],""")
            appendLine("""  "adventure": [${"\n    "}${configToJson(runtimeAdventure)}${"\n  "}]""")
            appendLine("}")
        }

        val dir = outputDir.get().asFile
        dir.mkdirs()
        dir.resolve("runtime-dependencies.json").writeText(json)
    }
}

sourceSets.main {
    resources.srcDir(generateDependencyManifest.map { layout.buildDirectory.dir("generated/resources/dependencies") })
}

tasks.named("processResources") {
    dependsOn(generateDependencyManifest)
}