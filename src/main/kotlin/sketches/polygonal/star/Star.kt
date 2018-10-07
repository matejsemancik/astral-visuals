package sketches.polygonal.star

import processing.core.PApplet
import processing.core.PApplet.map
import processing.core.PVector

class Star(private val sketch: PApplet) {

    companion object {
        const val ELLIPSE_SIZE = 15f
        const val SPEED_DEFAULT = 8
    }

    var motion = Starfield.Motion.ZOOMING
    private var x: Float = 0f
    private var y: Float = 0f
    private var z: Float = 0f
    private var px: Float = 0f
    private var py: Float = 0f
    private var pz: Float = 0f
    private var color = PVector(255f, 255f, 255f)

    init {
        newLocation()
    }

    /**
     * Updates star's location and returns whether star
     * reached end of it's lifecycle or not
     *
     * @return true if star has ended it's lifecycle and generated new position,
     * false otherwise
     */
    fun update(speed: Int = SPEED_DEFAULT): Boolean {
        when (motion) {
            Starfield.Motion.ZOOMING -> {
                z -= speed

                if (z < 1) {
                    newLocation()
                    return true
                } else {
                    return false
                }
            }

            Starfield.Motion.TRANSLATING_BACKWARD -> {
                x += speed

                if (x > sketch.width / 2) {
                    newLocation()
                    return true
                } else {
                    return false
                }
            }

            Starfield.Motion.TRANSLATING_FORWARD -> {
                x -= speed

                if (x < -sketch.width / 2) {
                    newLocation()
                    return true
                } else {
                    return false
                }
            }
        }
    }

    private fun newLocation() {
        x = sketch.random(-sketch.width.toFloat(), sketch.width.toFloat())
        y = sketch.random(-sketch.height.toFloat(), sketch.height.toFloat())
        z = sketch.random(0f, sketch.width.toFloat())
        px = x
        py = z
        pz = z
    }

    fun setColor(a: Float, b: Float, c: Float) {
        color = PVector(a, b, c)
    }

    fun draw() {
        sketch.fill(color.x, color.y, color.z)
        sketch.noStroke()

        val sx = map(x / z, 0f, 1f, 0f, sketch.width.toFloat())
        val sy = map(y / z, 0f, 1f, 0f, sketch.height.toFloat())

        val r = map(z, 0f, sketch.width.toFloat(), ELLIPSE_SIZE, 0f)
        sketch.ellipse(sx, sy, r, r)

        val px = when (motion) {
            Starfield.Motion.ZOOMING -> map(x / pz, 0f, 1f, 0f, sketch.width.toFloat())
            Starfield.Motion.TRANSLATING_FORWARD -> map(x / pz, 0f, 1f, 0f, sketch.width.toFloat()) + (x - px)
            Starfield.Motion.TRANSLATING_BACKWARD -> map(x / pz, 0f, 1f, 0f, sketch.width.toFloat()) - (x - px)
        }

        val py = when (motion) {
            Starfield.Motion.ZOOMING -> map(y / pz, 0f, 1f, 0f, sketch.height.toFloat())
            Starfield.Motion.TRANSLATING_FORWARD -> map(y / pz, 0f, 1f, 0f, sketch.height.toFloat())
            Starfield.Motion.TRANSLATING_BACKWARD -> map(y / pz, 0f, 1f, 0f, sketch.height.toFloat())
        }

        sketch.stroke(color.x, color.y, color.z)
        sketch.strokeWeight(2f)
        sketch.line(sx, sy, px, py)
        this.pz = z
        this.px = x
        this.py = y
    }
}