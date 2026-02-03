plugins {
    java
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("org.beryx.jlink") version "3.0.1"
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
    version = "21"
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
val osName = System.getProperty("os.name").lowercase()
val installerType = when {
    osName.contains("mac") -> "dmg"
    osName.contains("win") -> "exe"
    osName.contains("linux") -> "deb"
    else -> "app-image"
}

jlink {
    options.set(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"))

    addExtraDependencies("javafx")

    launcher {
        name = "DominoGame"
    }

    jpackage {
        imageName = "DominoGame"
        installerName = "DominoGame"
        appVersion = "1.0.0"
        installerType = installerType

        jvmArgs = listOf(
            "--enable-native-access=javafx.graphics"
        )
    }
}