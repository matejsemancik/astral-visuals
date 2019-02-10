package sketches.androidtalk

import processing.core.PApplet
import processing.core.PConstants

class WallpaperSketch : PApplet() {

    var bgColor = 0
    var fgColor = 0

    var numX = 20
    var numY = 30
    var paddingVerticalPx = 40
    var paddingHorizontalPx = 40
    var dotSize = 5f

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
    }

    override fun draw() {
        background(bgColor)
        noStroke()
        fill(fgColor)

        for (x in 0 until numX) {
            for (y in 0 until numY) {
                ellipse(
                        PApplet.map(x.toFloat(), 0f, numX.toFloat() - 1, 0f + paddingVerticalPx, width.toFloat() - paddingVerticalPx),
                        PApplet.map(y.toFloat(), 0f, numY.toFloat() - 1, 0f + paddingVerticalPx, height.toFloat() - paddingVerticalPx),
                        dotSize,
                        dotSize
                )
            }
        }
    }
}