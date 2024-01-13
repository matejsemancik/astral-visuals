package dev.matsem.astral.visuals.layers.stars

import dev.matsem.astral.core.tools.audio.AudioProcessor
import dev.matsem.astral.core.tools.extensions.threshold
import dev.matsem.astral.core.tools.galaxy.Galaxy
import dev.matsem.astral.core.tools.osc.OscHandler
import dev.matsem.astral.core.tools.osc.OscManager
import dev.matsem.astral.visuals.ColorHandler
import dev.matsem.astral.visuals.Colorizer
import dev.matsem.astral.visuals.Layer
import dev.matsem.astral.visuals.layers.stars.star.Starfield2
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import processing.core.PApplet
import processing.core.PGraphics

class StarsLayer : Layer(), KoinComponent, OscHandler, ColorHandler {

    override val parent: PApplet by inject()
    override val oscManager: OscManager by inject()
    override val colorizer: Colorizer by inject()
    private val audioProcessor: AudioProcessor by inject()
    private val galaxy: Galaxy by inject()

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

    val starfield1 = Starfield2(parent, canvas, 300).apply { motion = Starfield2.Motion.TRANSLATING_FORWARD }
    val starfield2 = Starfield2(parent, canvas, 300).apply { motion = Starfield2.Motion.TRANSLATING_FORWARD }

    fun onTimerTick() {
        if (starModeTimerButtton.isPressed) {
            starModeButtons.switchToRandom()
        }
    }

    override fun PGraphics.draw() {
        clear()
        if (parent.millis() > timerLastTick + timerIntervalPot.value) {
            timerLastTick = parent.millis()
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
        starfield1.setColor(fgColor)
        starfield2.setColor(fgColor)
        starfield1.mode = starMode
        starfield2.mode = starMode
        starfield1.motion = starMotion
        starfield2.motion = starMotion
        starfield1.draw()
        starfield2.draw()
    }
}