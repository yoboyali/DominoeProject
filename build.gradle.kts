plugins {
    java
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

javafx {
    version = "21.0.2"
    modules("javafx.controls", "javafx.graphics", "javafx.media")
}

dependencies {
    implementation("com.esotericsoftware:kryonet:2.22.0-RC1")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("org.EntryPoint")
}

tasks.shadowJar {
    archiveBaseName.set("DominoGame")
    archiveClassifier.set("")
    archiveVersion.set("1.0.0")
    manifest {
        attributes(mapOf("Main-Class" to "org.EntryPoint"))
    }
}