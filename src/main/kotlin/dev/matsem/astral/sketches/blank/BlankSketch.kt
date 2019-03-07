package dev.matsem.astral.sketches.blank

import dev.matsem.astral.sketches.BaseSketch
import dev.matsem.astral.sketches.SketchLoader
import dev.matsem.astral.tools.audio.AudioProcessor
import dev.matsem.astral.tools.galaxy.Galaxy

class BlankSketch(override val sketch: SketchLoader,
                  audioProcessor: AudioProcessor,
                  galaxy: Galaxy)
    : BaseSketch(sketch, audioProcessor, galaxy) {

    override fun setup() {
        // nothing
    }

    override fun onBecameActive() {

    }

    override fun draw() {
        sketch.background(0)
    }
}