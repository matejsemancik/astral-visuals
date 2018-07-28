package sketches.polygonal.starfield

import processing.core.PApplet
import processing.core.PApplet.map

class Star(private val sketch: PApplet) {

    companion object {
        const val ELLIPSE_SIZE = 10f
        const val SPEED = 8
    }

    var x: Float = 0f
    var y: Float = 0f
    var z: Float = 0f
    var pz: Float = 0f

    init {
        x = sketch.random(-sketch.width.toFloat(), sketch.width.toFloat())
        y = sketch.random(-sketch.height.toFloat(), sketch.height.toFloat())
        z = sketch.random(0f, sketch.width.toFloat())
        pz = z
    }

    fun update() {
        z -= map(sketch.mouseX.toFloat(), 0f, sketch.width.toFloat(), 0f, 20f)

        if (z < 1) {
            x = sketch.random(-sketch.width.toFloat(), sketch.width.toFloat())
            y = sketch.random(-sketch.height.toFloat(), sketch.height.toFloat())
            z = sketch.random(0f, sketch.width.toFloat())
            pz = z
        }
    }

    fun draw() {
        sketch.fill(0f, 255f, 100f)
        sketch.noStroke()

        val sx = map(x / z, 0f, 1f, 0f, sketch.width.toFloat())
        val sy = map(y / z, 0f, 1f, 0f , sketch.height.toFloat())

        val r = map(z, 0f, sketch.width.toFloat(), ELLIPSE_SIZE, 0f)
        sketch.ellipse(sx, sy, r, r)

        val px = map(x / pz, 0f, 1f, 0f, sketch.width.toFloat())
        val py = map(y / pz, 0f, 1f, 0f, sketch.height.toFloat())
        sketch.stroke(0f, 255f, 100f)
        sketch.line(sx, sy, px, py)
        pz = z
    }
}