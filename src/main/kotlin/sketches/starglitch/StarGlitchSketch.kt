package sketches.starglitch

import newLine
import processing.core.PApplet
import processing.event.KeyEvent
import sketches.BaseSketch
import sketches.starglitch.star.Starfield2
import tools.audio.AudioProcessor
import tools.galaxy.Galaxy

class StarGlitchSketch(override val sketch: PApplet,
                       val audioProcessor: AudioProcessor,
                       val galaxy: Galaxy)
    : BaseSketch(sketch, audioProcessor, galaxy) {

    // region params

    var starSpeed = 1f
    var starCount = 400
    var starMode = 0

    var hue = 130f
    var sat = 255f
    var bri = 255f

    // endregion

    // region TouchOSC

    // endregion

    lateinit var starfield1: Starfield2
    lateinit var starfield2: Starfield2


    override fun setup() {
        starfield1 = Starfield2(sketch, 300).apply { motion = Starfield2.Motion.TRANSLATING_FORWARD }
        starfield2 = Starfield2(sketch, 300).apply { motion = Starfield2.Motion.TRANSLATING_FORWARD }

        // TouchOSC
    }

    override fun onBecameActive() {

    }

    override fun draw() {
        background(258f, 84f, 25f)

        if (isInDebugMode) {
            debugWindow()
        }

        // Stars
        starfield1.setCount(starCount)
        starfield2.setCount(starCount)
        starfield1.update(speed = (2 * starSpeed).toInt())
        starfield2.update(speed = (4 * starSpeed).toInt())
        starfield1.setColor(hue, sat, bri)
        starfield2.setColor(hue, sat, bri)
        starfield1.mode = starMode
        starfield2.mode = starMode
        starfield1.draw()
        starfield2.draw()
    }

    fun debugWindow() {
        pushMatrix()
        // debug values
        val basicInfoStr = StringBuilder()
                .append("resolution: ${width}x${height}").newLine()
                .append("frameRate: ${frameRate.toInt()}").newLine()
                .append("mouseX: ${mouseX - width / 2}").newLine()
                .append("mouseY: ${mouseY - height / 2}").newLine()
                .toString()

        noStroke()
        fill(hue, sat, bri)

        textSize(14f)
        text(basicInfoStr, 12f, 24f)

        popMatrix()
    }

    override fun mouseClicked() {
        val newMotion = when (starfield1.motion) {
            Starfield2.Motion.ZOOMING -> Starfield2.Motion.TRANSLATING_BACKWARD
            Starfield2.Motion.TRANSLATING_BACKWARD -> Starfield2.Motion.TRANSLATING_FORWARD
            Starfield2.Motion.TRANSLATING_FORWARD -> Starfield2.Motion.ZOOMING
        }

        starfield1.motion = newMotion
        starfield2.motion = newMotion
    }

    override fun keyPressed(event: KeyEvent?) {
        event?.let {
            if (event.key.toInt() in 97..122) {
                starMode = event.key.toInt() - 97
                println("mode: $starMode")
            }
        }

        super.keyPressed(event)
    }
}