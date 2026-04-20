plugins {
    id("java")
}

group = "com.huskydreaming"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("net.kyori:adventure-api:4.17.0")
    compileOnly("net.kyori:adventure-text-minimessage:4.17.0")

    implementation("org.xerial:sqlite-jdbc:3.47.1.0")
    implementation("org.mariadb.jdbc:mariadb-java-client:3.4.1")
    implementation("com.mysql:mysql-connector-j:9.2.0")
    implementation("org.postgresql:postgresql:42.7.5")
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("org.yaml:snakeyaml:2.6")
    implementation("com.github.ben-manes.caffeine:caffeine:3.2.0")

    implementation("net.dv8tion:JDA:6.3.1") {
        exclude(module = "opus-java")
        exclude(module = "tink")
    }
}