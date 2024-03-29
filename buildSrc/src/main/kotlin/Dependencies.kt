object Dependencies {
    const val koin = "io.insert-koin:koin-core:${Versions.koin}"
    const val serializationJson = "org.jetbrains.kotlinx:kotlinx-serialization-json:${Versions.serializationJson}"
    const val coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}"
    const val orxEasing = "org.openrndr.extra:orx-easing:${Versions.orx}"

    val processingLibs = listOf(
        "minim",
        "themidibus",
        "VideoExport",
        "box2d_processing",
        "video",
        "extruder",
        "geomerative",
        "peasycam",
        "PostFX",
        "oscP5",
        "blobDetection"
    )
}