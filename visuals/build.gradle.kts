plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    application
}

apply<dev.matsem.astral.CommonDependencies>()

@Suppress("UnstableApiUsage")
application {
    mainClassName = "dev.matsem.astral.visuals.VisualsApp"
}

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(Dependencies.serializationRuntime)
    implementation(project(Modules.core))
}

group = ProjectSettings.group
version = ProjectSettings.version

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}