package sketches.test

import processing.core.PApplet
import sketches.BaseSketch

class GreenSketch(override val sketch: PApplet): BaseSketch(sketch) {

    override fun setup() {
        // nevermind
    }

    override fun draw() {
        sketch.background(0f, 255f, 0f)
    }
}