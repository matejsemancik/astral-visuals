package sketches.test

import processing.core.PApplet
import sketches.BaseSketch

class BlueSketch(override val sketch: PApplet): BaseSketch(sketch) {

    override fun setup() {
        // bleh
    }

    override fun draw() {
        sketch.background(0f, 0f, 255f)
    }
}