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
    implementation(Dependencies.serializationCore)
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