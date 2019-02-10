package sketches.androidtalk

import processing.core.PApplet
import processing.core.PConstants
import processing.core.PVector

class WallpaperSketch : PApplet() {

    var bgColor = 0
    var fgColor = 0

    var numX = 20
    var numY = 30
    var paddingVerticalPx = 80
    var paddingHorizontalPx = 80
    var dotSize = 8f
    val lineWeight = 2f
    var dotPositions = Array(numX) { Array(numY) { PVector() } }
    var fingerVector: PVector? = null

    override fun settings() {
        size(480, 720, PConstants.P3D)
//        fullScreen(PConstants.P2D)
//        fullScreen(PConstants.P3D)
    }

    override fun setup() {
        orientation(PConstants.PORTRAIT)
        colorMode(PConstants.HSB, 360f, 100f, 100f)
//        colorMode(PConstants.RGB, 255f, 255f, 255f)

        bgColor = color(0, 0, 15) // Dark gray
        fgColor = color(33, 100, 100) // Orange

        for (x in 0 until numX) {
            for (y in 0 until numY) {
                val posX = PApplet.map(
                        x.toFloat(),
                        0f,
                        numX.toFloat() - 1,
                        -width / 2f + paddingVerticalPx,
                        width / 2f - paddingVerticalPx
                )
                val posY = PApplet.map(
                        y.toFloat(),
                        0f,
                        numY.toFloat() - 1,
                        -height / 2f + paddingHorizontalPx,
                        height / 2f - paddingHorizontalPx
                )

                dotPositions[x][y] = PVector(posX, posY, 0f)
            }
        }
    }

    override fun draw() {
        background(bgColor)

        translate(width / 2f, height / 2f)

        for (x in 0 until numX) {
            for (y in 0 until numY) {
                val noise = noise(x / 10f, y / 10f + millis() / 1000f) * 100f
                val position = dotPositions[x][y]

                // Draw a line
                strokeWeight(lineWeight)
                stroke(fgColor)
                line(
                        position.x,
                        position.y,
                        0f,
                        position.x,
                        position.y,
                        noise
                )

                // Draw dot
                pushMatrix()
                noStroke()
                fill(fgColor)
                translate(0f, 0f, noise)
                ellipse(
                        dotPositions[x][y].x,
                        dotPositions[x][y].y,
                        dotSize,
                        dotSize
                )
                popMatrix()
            }
        }
    }

    override fun mouseDragged() {
        fingerVector = PVector(mouseX.toFloat(), mouseY.toFloat())
    }

    override fun mouseReleased() {
        fingerVector = null
    }
}