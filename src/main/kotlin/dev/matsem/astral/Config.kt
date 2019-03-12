package dev.matsem.astral

interface Config {

    object Sketch {
        const val IS_IN_RENDER_MODE = false
        const val DEFAULT_SELECTOR = '1'
    }

    object VideoExport {
        const val AUDIO_FILE_PATH = "yoga.mp3"
        const val SEP = "|"
        const val MOVIE_FPS = 30f
        const val FRAME_DURATION = 1f / MOVIE_FPS
    }
}