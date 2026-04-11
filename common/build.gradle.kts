plugins {
    id("java")
}

group = "com.huskydreaming"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.mariadb.jdbc:mariadb-java-client:3.4.1")
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("org.yaml:snakeyaml:2.6")
    implementation("com.github.ben-manes.caffeine:caffeine:3.2.0")

    implementation("net.dv8tion:JDA:6.3.1") {
        exclude(module = "opus-java")
        exclude(module = "tink")
    }
}