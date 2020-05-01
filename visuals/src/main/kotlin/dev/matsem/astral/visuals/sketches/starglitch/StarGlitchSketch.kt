package dev.matsem.astral.visuals.sketches.starglitch

import dev.matsem.astral.core.tools.extensions.newLine
import dev.matsem.astral.core.tools.extensions.threshold
import dev.matsem.astral.visuals.sketches.BaseSketch
import dev.matsem.astral.visuals.sketches.SketchLoader
import dev.matsem.astral.visuals.sketches.starglitch.star.Starfield2
import dev.matsem.astral.core.tools.audio.AudioProcessor
import dev.matsem.astral.core.tools.midi.MidiAutomator
import dev.matsem.astral.core.tools.galaxy.Galaxy
import org.koin.core.inject
import processing.core.PApplet

class StarGlitchSketch : BaseSketch() {

    override val sketch: SketchLoader by inject()
    private val audioProcessor: AudioProcessor by inject()
    private val galaxy: Galaxy by inject()
    private val automator: MidiAutomator by inject()

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

    val starModeTimerButtton = galaxy.createToggleButton(3, 21, false)
    val timerIntervalPot = galaxy.createPot(3, 20, 500f, 60000f, 1000f)
    var timerLastTick = 0

    val starSpeedPot = galaxy.createPot(3, 22, 0.5f, 5f, 0.5f).lerp(0.1f)
    val starCountPot = galaxy.createPot(3, 23, 0f, 1200f, 100f).lerp(0.005f)
    val starfieldBassRotationPot = galaxy.createPot(3, 24, 0f, 3f, 1f)
    val starfieldRotationZPot = galaxy.createPot(3, 25, -1f, 1f, 0f)
    val starfieldRotationZResetBtn = galaxy.createPushButton(3, 26) { starfieldRotationZPot.reset() }
    val starAccelPot = galaxy.createPot(3, 27, 0f, 1f, 0f)

    var bassSum = 0f
    var starVz = 0f
    var starfieldRotationZ = 0f

    // endregion

    lateinit var starfield1: Starfield2
    lateinit var starfield2: Starfield2

    override fun setup() {
        starfield1 = Starfield2(sketch, 300).apply { motion = Starfield2.Motion.TRANSLATING_FORWARD }
        starfield2 = Starfield2(sketch, 300).apply { motion = Starfield2.Motion.TRANSLATING_FORWARD }

        automator.setupWithGalaxy(
            channel = 3,
            recordButtonCC = 28,
            playButtonCC = 29,
            loopButtonCC = 30,
            clearButtonCC = 31,
            channelFilter = null
        )
    }

    fun onTimerTick() {
        if (starModeTimerButtton.isPressed) {
            starModeButtons.switchToRandom()
        }
    }

    override fun draw() = with(sketch) {
        automator.update()

        if (millis() > timerLastTick + timerIntervalPot.value) {
            timerLastTick = millis()
            onTimerTick()
        }

        bassSum += audioProcessor.getRange(0f..50f)
        bassSum *= 0.2f

        starVz += starfieldRotationZPot.value.threshold(0.05f) * 0.15f
        starVz *= 0.95f
        starfieldRotationZ += starVz

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
        starfield1.rotate(
            PApplet.map(bassSum, 0f, 50f, 0f, 0.04f * starfieldBassRotationPot.value) + PApplet.radians(
                starfieldRotationZ
            )
        )
        starfield2.rotate(
            PApplet.map(bassSum, 0f, 50f, 0f, 0.08f * starfieldBassRotationPot.value) + PApplet.radians(
                starfieldRotationZ
            )
        )
        starfield1.setCount(starCountPot.value.toInt())
        starfield2.setCount(starCountPot.value.toInt())
        starfield1.update(speed = (2 * starSpeedPot.value).toInt() + (bassSum * starAccelPot.value).toInt())
        starfield2.update(speed = (4 * starSpeedPot.value).toInt())
        starfield1.setColor(fgHue, fgSat, fgBrightness)
        starfield2.setColor(fgHue, fgSat, fgBrightness)
        starfield1.mode = starMode
        starfield2.mode = starMode
        starfield1.motion = starMotion
        starfield2.motion = starMotion
        starfield1.draw()
        starfield2.draw()
    }

    fun debugWindow() = with(sketch) {
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