package sketches.wiggler

import containToRange
import processing.core.PApplet
import processing.core.PApplet.map
import processing.core.PConstants
import processing.core.PConstants.TWO_PI
import processing.core.PShape
import processing.core.PVector
import toRad

class Wiggler(val sketch: PApplet,
              val fillColor: Int,
              val radius: Float,
              val x: Float = sketch.width / 2f,
              val y: Float = sketch.height / 2f,
              val bubbleOffset: Float = BUBBLE_MAX_OFFSET_DEFAULT,
              val bubbleFactor: Int = BUBBLE_Q_FACTOR_DEFAULT) {

    companion object {
        const val DEBUG_DRAW = false
        const val NUMBER_EDGES_DEFAULT = 128
        const val BUBBLE_Q_FACTOR_DEFAULT = 8
        const val BUBBLE_MAX_OFFSET_DEFAULT = 40f
    }

    // The PShape to be "wiggled"
    var shape: PShape

    // For 2D Perlin noise
    var yoff = 0f

    // We are using list to keep a duplicate copy of vertices original locations
    val original = mutableListOf<PVector>()

    init {
        // Create original shape
        repeat(NUMBER_EDGES_DEFAULT, action = { i ->
            val vector = PVector.fromAngle(TWO_PI * i / NUMBER_EDGES_DEFAULT)
            vector.mult(radius)
            original.add(vector)
        })

        shape = sketch.createShape()
        shape.beginShape()
        shape.fill(fillColor)
        shape.noStroke()

        for (vector: PVector in original) {
            shape.vertex(vector.x, vector.y)
        }
        shape.endShape(PConstants.CLOSE)
    }

    fun wiggle() {
        var xoff = 0f
        // Apply an offset to each vertex

        for (i in 0 until shape.vertexCount) {
            val original = original.get(i)
            val current = shape.getVertex(i)

            if (DEBUG_DRAW) {
                sketch.strokeWeight(1.toFloat())
                sketch.stroke(250)
                sketch.line(x, y, x + original.x, y + original.y)

                sketch.strokeWeight(1.toFloat())
                sketch.stroke(sketch.color(0f, 0f, 255f))
                sketch.line(x, y, x + current.x, y + current.y)
            }

            // Add random offset to original circle shape in each iteration (creates wiggle effect)
            val a = TWO_PI * sketch.noise(xoff, yoff)
            val r = PVector.fromAngle(a)
            r.mult(6f)
            r.add(original)

            if (sketch.mousePressed) {
                // Create "bubble" effect on the shape based on mouse cursor position
                val mouse = PVector(sketch.mouseX - x, sketch.mouseY - y)
                val weight = map(PVector.angleBetween(original, mouse), 0f, PConstants.PI * 2 / bubbleFactor, 1f, 0f)
                        .let { if (it < 0f) 0f else it }

                val strength = (mouse.mag() - original.mag()).containToRange(-bubbleOffset, bubbleOffset / 1.5f)
                r.add(PVector.fromAngle(mouse.heading().toRad()).mult(weight * strength))
            }

            // set new vertex location
            shape.setVertex(i, r.x, r.y)

            // increment perlin noise x val
            xoff += 0.1f
        }

        // increment perlin noise y val
        yoff += 0.01f

        if (DEBUG_DRAW) {
            sketch.stroke(0)
            sketch.strokeWeight(1.toFloat())
        sketch.line(x, y, sketch.mouseX.toFloat(), sketch.mouseY.toFloat())
    }
}

fun display() {
    sketch.pushMatrix()
    sketch.translate(x, y)
    sketch.shape(shape)
        sketch.popMatrix()
    }
}