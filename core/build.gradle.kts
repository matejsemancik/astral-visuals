plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

apply<dev.matsem.astral.CommonDependencies>()

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(Dependencies.serializationJson)
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