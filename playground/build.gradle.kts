plugins {
    kotlin("jvm")
    application
}

apply<dev.matsem.astral.CommonDependencies>()

@Suppress("UnstableApiUsage")
application {
    mainClassName = "dev.matsem.astral.playground.PlaygroundApp"
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
        kotlinOptions.jvmTarget = "11"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "11"
    }
}