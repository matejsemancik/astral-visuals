package sketches.blank

import processing.core.PApplet
import sketches.BaseSketch

class BlankSketch(override val sketch: PApplet) : BaseSketch(sketch) {
    
    override fun setup() {
        // nothing
    }

    override fun onBecameActive() {

    }

    override fun draw() {
        sketch.background(0)
    }
}