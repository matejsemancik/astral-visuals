package dev.matsem.astral.playground.sketches

import dev.matsem.astral.core.tools.extensions.colorModeHsb
import dev.matsem.astral.core.tools.extensions.withAlpha
import org.koin.core.KoinComponent
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PVector
import kotlin.math.pow

class Circle(
    val pos: PVector,
    val radius: Float,
    val vel: PVector
) {
    fun update(sketch: PApplet) {
        if (pos.x > sketch.width || pos.x < 0) {
            vel.x = vel.x * -1
        }

        if (pos.y > sketch.height || pos.y < 0) {
            vel.y = vel.y * -1
        }

        pos.x = pos.x + vel.x
        pos.y = pos.y + vel.y
    }
}

class MetaballsSketch : PApplet(), KoinComponent {

    val circles by lazy {
        Array(12) {
            Circle(
                pos = PVector(random(0f, 1f), random(0f, 1f))
                    .mult(width.toFloat()),
                radius = random(width / 20f, width / 10f),
                vel = PVector.random2D().mult(random(1f, 1.5f))
            )
        }
    }

    override fun settings() {
        size(600, 600, PConstants.P3D)
    }

    override fun setup() {
        colorModeHsb()
        surface.setTitle("Metaballs")
        surface.setResizable(true)
    }

    override fun draw() {
        background(0)

        // Update circles
        circles.forEach { it.update(this) }

        // Draw grid
        val grid = 4
        for (y in 0 until height step grid) {
            for (x in 0 until width step grid) {
                textAlign(CENTER, CENTER)
                textSize(12f)

                val sample = metaball(x + grid / 2, y + grid / 2)
                val threshold = 1f

                if (sample > threshold) {
                    noStroke()
                    rectMode(CORNER)
                    fill(0x00ba8c.withAlpha())
                    square(x.toFloat(), y.toFloat(), grid.toFloat())
                }

                // Draw isometric values
//                noStroke()
//                fill(100f)
//                text(sample.toString().take(4), x + grid / 2f, y + grid / 2f)
            }
        }

        // Draw circles
//        for (circle in circles) {
//            noFill()
//            stroke(0xff0000.withAlpha())
//            strokeWeight(2f)
//            circle(circle.pos.x, circle.pos.y, circle.radius * 2f)
//        }
    }

    // Calculates isometric surface value for metaball
    private fun metaball(x: Int, y: Int): Float = circles
        .sumByDouble { circle ->
            val result = circle.radius.pow(2) / ((x - circle.pos.x).pow(2) + (y - circle.pos.y).pow(2))
            result.toDouble()
        }
        .toFloat()
}