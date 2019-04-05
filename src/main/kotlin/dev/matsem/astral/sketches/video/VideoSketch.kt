package dev.matsem.astral.sketches.video

import dev.matsem.astral.centerX
import dev.matsem.astral.centerY
import dev.matsem.astral.sketches.BaseSketch
import dev.matsem.astral.sketches.SketchLoader
import dev.matsem.astral.tools.audio.AudioProcessor
import org.koin.core.inject
import processing.video.Movie

class VideoSketch : BaseSketch() {

    override val sketch: SketchLoader by inject()
    val audioProcessor: AudioProcessor by inject()

    val movie = Movie(sketch, "movies/astral03_BACKGROUND.mp4")

    override fun onBecameActive() {
    }

    override fun setup() {
        movie.loop()
    }

    override fun draw() {
        background(0)
        if (movie.available()) {
            movie.read()
        }

        translate(centerX(), centerY())
        scale(1 + audioProcessor.getRange(20f..100f) / 4000f)
        sketch.image(movie, -centerX(), -centerY(), width.toFloat(), height.toFloat())
    }
}