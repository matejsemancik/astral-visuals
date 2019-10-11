package dev.matsem.astral

object Config {

    object Sketch {
        const val DEFAULT_SELECTOR = '1'
    }

    object Color {
        const val HUE_MAX = 360f
        const val SATURATION_MAX = 100f
        const val BRIGHTNESS_MAX = 100f
    }

    object VideoExport {
        const val IS_IN_RENDER_MODE = false
        const val AUDIO_FILE_PATH = "music/seba2.wav"
        const val SEP = "|"
        const val MOVIE_FPS = 25f
        const val FRAME_DURATION = 1f / MOVIE_FPS
        const val MIDI_AUTOMATION_FILE = "midi/videoexport_midi.json"
    }
}
