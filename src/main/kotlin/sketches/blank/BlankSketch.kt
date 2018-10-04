package sketches.blank

import processing.core.PApplet
import sketches.BaseSketch
import tools.audio.AudioProcessor
import tools.galaxy.Galaxy

class BlankSketch(override val sketch: PApplet,
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