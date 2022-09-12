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
        kotlinOptions.jvmTarget = "11"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "11"
    }
}