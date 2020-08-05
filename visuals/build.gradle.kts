plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    application
}

group = ProjectSettings.group
version = ProjectSettings.version

val props = org.jetbrains.kotlin.konan.properties.Properties().apply {
    load(file("${rootDir}/local.properties").inputStream())
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
    "PostFX",
    "oscP5"
)

@Suppress("UnstableApiUsage")
application {
    mainClassName = "dev.matsem.astral.visuals.VisualsApp"
}

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(Dependencies.serializationRuntime)
    implementation(Dependencies.koin)

    implementation(fileTree(mapOf("dir" to processingCoreDir, "include" to listOf("*.jar"))))
    processingLibs.forEach { libName ->
        implementation(fileTree(mapOf("dir" to "$processingLibsDir/$libName/library", "include" to listOf("*.jar"))))
    }

    implementation(project(Modules.core))
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}