package sketches.wiggler

import processing.core.PApplet
import processing.core.PConstants
import processing.core.PVector
import toRad

class WigglerSketch : PApplet() {

    lateinit var wigglerFg: Wiggler

    override fun settings() {
        fullScreen(PConstants.P2D)
    }

    override fun setup() {
        smooth()
        wigglerFg = Wiggler(this, color(0, 200, 150), width / 5f)
    }

    override fun draw() {
        background(244)

        wigglerFg.display()
        wigglerFg.wiggle()

        fill(0)
        textSize(12f)

        val angle = PVector(mouseX - width / 2.toFloat(), mouseY - height / 2.toFloat()).heading()
        val str = StringBuilder()
                .append("resolution: ${width}x${height}").append("\n")
                .append("frameRate: ${frameRate.toInt()}").append("\n")
                .append("center mouseX: ${mouseX - width / 2}").append("\n")
                .append("center mouseY: ${mouseY - height / 2}").append("\n")
                .append("angle: ${angle.toRad() / PConstants.PI} π*rad")
                .toString()

        text(str, 12f, 24f)
    }
}