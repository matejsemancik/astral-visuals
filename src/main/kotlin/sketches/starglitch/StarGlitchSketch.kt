package sketches.starglitch

import newLine
import sketches.BaseSketch
import sketches.SketchLoader
import sketches.starglitch.star.Starfield2
import tools.audio.AudioProcessor
import tools.galaxy.Galaxy

class StarGlitchSketch(override val sketch: SketchLoader,
                       val audioProcessor: AudioProcessor,
                       val galaxy: Galaxy)
    : BaseSketch(sketch, audioProcessor, galaxy) {

    // region params

    val starModeButtons = galaxy.createButtonGroup(
            3,
            listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15),
            listOf(1)
    )

    val starMotionButtons = galaxy.createButtonGroup(
            3,
            listOf(16, 17, 19),
            listOf(16)
    )

    var starSpeed = 1f
    var starCount = 400

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
        val starMotion = when (starMotionButtons.activeButtonsIndices().first()) {
            0 -> Starfield2.Motion.ZOOMING
            1 -> Starfield2.Motion.TRANSLATING_BACKWARD
            2 -> Starfield2.Motion.TRANSLATING_FORWARD
            else -> Starfield2.Motion.ZOOMING
        }

        val starMode = starModeButtons.activeButtonsIndices().first()

        background(bgHue, bgSat, bgBrightness)

        if (isInDebugMode) {
            debugWindow()
        }

        // Stars
        starfield1.setCount(starCount)
        starfield2.setCount(starCount)
        starfield1.update(speed = (2 * starSpeed).toInt())
        starfield2.update(speed = (4 * starSpeed).toInt())
        starfield1.setColor(fgHue, fgSat, fgBrightness)
        starfield2.setColor(fgHue, fgSat, fgBrightness)
        starfield1.mode = starMode
        starfield2.mode = starMode
        starfield1.motion = starMotion
        starfield2.motion = starMotion
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
        fill(fgHue, fgSat, fgBrightness)

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
}