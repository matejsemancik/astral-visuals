import java.util.*

plugins {
    kotlin("jvm")
    application
}

apply<dev.matsem.astral.CommonDependencies>()

// Plugin for running locally on development machine
application {
    val props = Properties().apply {
        load(file("${rootDir}/local.properties").inputStream())
    }
    val nativesDir = props["processing.core.natives"]

    mainClass.set("dev.matsem.astral.raspberrypi.RaspberryApp")
    applicationDefaultJvmArgs = listOf(
        "-Djava.library.path=$nativesDir"
    )
    applicationName = "visuals"
}

distributions {
    main {
        contents {
            val props = Properties().apply {
                load(file("${rootDir}/local.properties").inputStream())
            }
            val nativesDir = props["processing.core.natives.rpi"].toString()
            from(file(nativesDir)) {
                into("bin/natives/linux-aarch64")
            }
        }
    }
}

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(project(":core"))
}

group = ProjectSettings.group
version = ProjectSettings.version

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = ProjectSettings.jvmTarget
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = ProjectSettings.jvmTarget
    }
}

tasks.getByName<Zip>("distZip") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.getByName<Sync>("installDist") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}