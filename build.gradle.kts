plugins {
    java
}

group = "com.huskydreaming"
version = "1.0.0"

subprojects {
    apply(plugin = "java")

    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    }

    repositories {
        mavenCentral()
        maven {
            name = "papermc"
            url = uri("https://repo.papermc.io/repository/maven-public/")
        }
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
}

