package dev.matsem.astral.sketches.starglitch.star

import processing.core.PApplet
import processing.core.PApplet.map
import processing.core.PVector

class Star2(private val sketch: PApplet) {

    companion object {
        const val ELLIPSE_SIZE = 15f
        const val SPEED_DEFAULT = 8
    }

    var motion = Starfield2.Motion.ZOOMING
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
            Starfield2.Motion.ZOOMING -> {
                z -= speed

                if (z < 1) {
                    newLocation()
                    return true
                } else {
                    return false
                }
            }

            Starfield2.Motion.TRANSLATING_BACKWARD -> {
                x += speed

                if (x > sketch.width / 2) {
                    newLocation()
                    return true
                } else {
                    return false
                }
            }

            Starfield2.Motion.TRANSLATING_FORWARD -> {
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

    var mode = 6

    fun draw() {
        sketch.fill(color.x, color.y, color.z)
        sketch.noStroke()

        val sx = map(x / z, 0f, 1f, 0f, sketch.width.toFloat())
        val sy = map(y / z, 0f, 1f, 0f, sketch.height.toFloat())

        val r = map(z, 0f, sketch.width.toFloat(), ELLIPSE_SIZE, 0f)
        sketch.ellipse(sx, sy, r, r)

        val px = when (mode) {
            0 -> map(x / pz, 0f, 1f, 0f, sketch.width.toFloat())
            1 -> map(x / pz, 0f, 1f, 0f, sketch.height.toFloat())
            2 -> map(x / pz, 0f, 1f, 0f, sketch.width.toFloat())
            3 -> map(x / pz, 0f, 1f, 0f, sketch.height.toFloat())
            4 -> px - (py / z)
            5 -> map(x / pz, 0f, 1f, 0f, sketch.height.toFloat())
            6 -> map(x / pz, 0f, 1f, 0f, sketch.height.toFloat()) / 2
            7 -> map(x / pz, 0f, 1f, 0f, sketch.height.toFloat()) * 2
            8 -> map(x / pz, 0f, 1f, 0f, sketch.height.toFloat())
            9 -> map(x / pz, 0f, 1f, 0f, sketch.width.toFloat() / 2)
            10 -> map(x / pz, 0f, 1f, 0f, sketch.width.toFloat() / 2)
            11 -> map(x / pz, 0f, 1f, 0f, sketch.width.toFloat() * 2)
            12 -> px - sketch.width / 2 * 5 * pz
            13 -> px - sketch.width / 2 * 5 * pz
            else -> 0f
        }

        val py = when (mode) {
            0 -> map(y / pz, 0f, 1f, 0f, sketch.height.toFloat())
            1 -> map(y / pz, 0f, 1f, 0f, sketch.height.toFloat())
            2 -> py / x
            3 -> map(x / pz, 0f, 1f, 0f, sketch.width.toFloat()) / py
            4 -> py - (y / z)
            5 -> map(y / pz, 0f, 1f, 0f, sketch.width.toFloat())
            6 -> map(y / pz, 0f, 1f, 0f, sketch.height.toFloat()) / 2
            7 -> map(y / pz, 0f, 1f, 0f, sketch.height.toFloat()) * 2
            8 -> map(x / pz, 0f, 1f, 0f, sketch.height.toFloat())
            9 -> map(y / pz, 0f, 1f, 0f, sketch.height.toFloat() / 2)
            10 -> map(y / pz, 0f, 1f, 0f, sketch.width.toFloat() / 2)
            11 -> map(y / pz, 0f, 1f, 0f, sketch.width.toFloat() * 2)
            12 -> px - sketch.height / 2 * 5 * pz
            13 -> this.py - sketch.width / 5 * 10 * pz
            else -> 0f
        }

        sketch.stroke(color.x, color.y, color.z)
        sketch.strokeWeight(2f)
        sketch.line(sx, sy, px, py)
        this.pz = z
        this.px = x
        this.py = y
    }
}