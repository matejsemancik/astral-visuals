package sketches.boxes

import processing.core.PApplet

class Box(val sketch: PApplet, val x: Float, val y: Float) {
    var size = 25f
    var color = 0
    var strokeWeight = 3f

    fun draw() {
        with(sketch) {
            noFill()
            stroke(color)
            strokeWeight(strokeWeight)

            pushMatrix()
            translate(x, y)
            sketch.box(size)
            popMatrix()
        }
    }
}