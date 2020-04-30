import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.70"
    kotlin("plugin.serialization") version "1.3.70"
    application
}

group = "dev.matsem"
version = "2.0.0"

val props = org.jetbrains.kotlin.konan.properties.Properties().apply {
    load(file("local.properties").inputStream())
}
val processingCoreDir = props["processingCoreDir"]
val processingLibsDir = props["processingLibsDir"]
val processingLibs = listOf(
        "minim",
        "themidibus",
        "VideoExport",
        "box2d_processing",
        "video",
        "extruder",
        "geomerative",
        "PostFX"
)

application {
    mainClassName = "dev.matsem.astral.Main"
}

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.20.0")
    implementation("org.koin:koin-core:2.0.0-beta-1")

    implementation(fileTree(mapOf("dir" to processingCoreDir, "include" to listOf("*.jar"))))
    processingLibs.forEach { libName ->
        implementation(fileTree(mapOf("dir" to "$processingLibsDir/$libName/library", "include" to listOf("*.jar"))))
    }
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    freeCompilerArgs = listOf("-Xinline-classes")

}