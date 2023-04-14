import java.util.*

plugins {
    kotlin("jvm")
    application
}

apply<dev.matsem.astral.CommonDependencies>()

application {
    val props = Properties().apply {
        load(file("${rootDir}/local.properties").inputStream())
    }
    val nativesDir = props["processing.core.natives.rpi"]

    mainClass.set("dev.matsem.astral.raspberrypi.RaspberryApp")
    applicationDefaultJvmArgs = listOf(
        "-Djava.library.path=$nativesDir"
    )
    applicationName = "visuals"
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