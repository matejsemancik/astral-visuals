package sketches.blank

import sketches.BaseSketch
import sketches.SketchLoader
import tools.audio.AudioProcessor
import tools.galaxy.Galaxy

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