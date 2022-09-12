package dev.matsem.astral

import Dependencies
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.kotlin
import java.util.*

open class CommonDependencies : Plugin<Project> {
    override fun apply(target: Project) {
        target.configureCommonDependencies()
    }
}

internal fun Project.configureCommonDependencies() {
    val props = Properties().apply {
        load(file("${rootDir}/local.properties").inputStream())
    }
    val processingCoreDir = props["processing.core.jars"]
    val processingLibsDir = props["processing.libs.jars"]

    dependencies {
        add("implementation", kotlin("bom"))
        add("implementation", kotlin("stdlib-jdk8"))
        add("implementation", Dependencies.koin)
        add("implementation", Dependencies.coroutines)
        add(
            "implementation",
            fileTree(
                mapOf(
                    "dir" to processingCoreDir,
                    "include" to listOf("*.jar")
                )
            )
        )

        Dependencies.processingLibs.forEach { libName ->
            add(
                "implementation",
                fileTree(
                    mapOf(
                        "dir" to "$processingLibsDir/$libName/library",
                        "include" to listOf("*.jar")
                    )
                )
            )
        }
    }
}