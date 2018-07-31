package sketches.test

import centerX
import centerY
import processing.core.PApplet
import processing.core.PShape
import sketches.BaseSketch

class TestSketch(override val sketch: PApplet) : BaseSketch(sketch) {

    lateinit var model: PShape

    override fun setup() {
        model = sketch.loadShape("model.obj")
        model.scale(100f)
        sketch.lights()
    }

    override fun draw() {
        sketch.background(204)
        sketch.translate(sketch.centerX().toFloat(), sketch.centerY().toFloat())
        sketch.shape(model)
    }
}