import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version Versions.kotlin
    kotlin("plugin.serialization") version Versions.kotlin
}

group = ProjectSettings.group
version = ProjectSettings.version

repositories {
    mavenCentral()
    jcenter()
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = ProjectSettings.jvmTarget
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = ProjectSettings.jvmTarget
    }
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    freeCompilerArgs = listOf("-Xinline-classes")
}
