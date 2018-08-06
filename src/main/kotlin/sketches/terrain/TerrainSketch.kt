package sketches.terrain

import processing.core.PApplet
import processing.core.PConstants

class TerrainSketch : PApplet() {

    val w = 2000f
    val h = 1000f
    var scale = 20f

    var cols = (w / scale).toInt()
    var rows = (h / scale).toInt()

    val terrain = Array(cols, {FloatArray(rows)})
    var flying = 0.06f

    override fun settings() {
        size(1280, 720, PConstants.P3D)
        smooth(4)
    }

    override fun setup() {
        regenerate()
    }

    override fun draw() {
        background(32f, 32f, 32f)
        stroke(0f, 255f, 100f)
        strokeWeight(1.4f)
        noFill()

        translate(width.toFloat() / 2, height.toFloat() / 2)
        rotateX(map(mouseY.toFloat(), height.toFloat(), 0f, PConstants.PI /2, 0f))

        translate(-w / 2, -h / 2)

        regenerate()
        flying -= 0.1f

        for (y in 0 until rows - 1) {
            beginShape(PConstants.TRIANGLE_STRIP)

            for (x in 0 until cols) {
                vertex(x * scale, y * scale, terrain[x][y])
                vertex(x * scale, (y + 1) * scale, terrain[x][y+1])
            }

            endShape()
        }
    }

    private fun regenerate() {
        var yoff = flying
        for (y in 0 until rows) {
            var xoff = 0f
            for (x in 0 until cols) {
                terrain[x][y] = map(noise(xoff, yoff), 0f, 1f, -80f, 80f)
                xoff += map(mouseX.toFloat(), 0f, width.toFloat(), 0f, 0.5f)
            }

            yoff += 0.2f
        }
    }
}