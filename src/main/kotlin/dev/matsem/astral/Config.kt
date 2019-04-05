package dev.matsem.astral

interface Config {

    object Sketch {
        const val DEFAULT_SELECTOR = 'm'
    }

    object Color {
        const val HUE_MAX = 360f
        const val SATURATION_MAX = 100f
        const val BRIGHTNESS_MAX = 100f
    }

    object VideoExport {
        const val IS_IN_RENDER_MODE = false
        const val AUDIO_FILE_PATH = "drama_switch.wav"
        const val SEP = "|"
        const val MOVIE_FPS = 60f
        const val FRAME_DURATION = 1f / MOVIE_FPS
    }
}
