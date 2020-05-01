package dev.matsem.astral.visuals.sketches.polygonal

import dev.matsem.astral.core.tools.extensions.centerX
import dev.matsem.astral.core.tools.extensions.centerY
import dev.matsem.astral.core.tools.extensions.newLine
import dev.matsem.astral.core.tools.extensions.rotate
import dev.matsem.astral.core.tools.extensions.threshold
import dev.matsem.astral.visuals.sketches.BaseSketch
import dev.matsem.astral.visuals.sketches.SketchLoader
import dev.matsem.astral.visuals.sketches.polygonal.asteroid.Asteroid
import dev.matsem.astral.visuals.sketches.polygonal.star.Starfield
import dev.matsem.astral.visuals.tools.audio.AudioProcessor
import dev.matsem.astral.visuals.tools.automator.MidiAutomator
import dev.matsem.astral.visuals.tools.galaxy.Galaxy
import dev.matsem.astral.visuals.tools.logging.FFTLogger
import org.koin.core.inject
import processing.core.PApplet.map
import processing.core.PApplet.radians
import processing.core.PApplet.sin
import processing.core.PConstants

class PolygonalSketch : BaseSketch() {

    companion object {
        const val NUMBER_ASTEROIDS = 3
    }

    override val sketch: SketchLoader by inject()
    private val audioProcessor: AudioProcessor by inject()
    private val galaxy: Galaxy by inject()
    private val automator: MidiAutomator by inject()

    // region params

    var shouldRegenerate = false
    var starMotion = Starfield.Motion.ZOOMING

    val joystick = galaxy.createJoystick(0, 0, 1, 2, 20, 21, 22).flipped()
    val beatDetectButton = galaxy.createToggleButton(0, 7, true)
    val starSpeedPot = galaxy.createPot(0, 3, 0.5f, 5f, 0.5f).lerp(0.1f)
    val starCountPot = galaxy.createPot(0, 4, 0f, 1200f, 100f).lerp(0.005f)
    val starfieldRotationPot = galaxy.createPot(0, 5, 0f, 3f, 1f)
    val starAccelPot = galaxy.createPot(0, 12, 0f, 1f, 0.4f)
    val asteroidSizePot = galaxy.createPot(0, 13, 0f, 1f, 0.2f)
    val asteroidSizeMultPot = galaxy.createPot(0, 14, 0f, 5f, 1f)
    val flickerButton = galaxy.createToggleButton(0, 15, false)
    val centerWeightButton = galaxy.createToggleButton(0, 16, false)
    val wiggleButton = galaxy.createToggleButton(0, 17, false)
    val starfieldRotationEnabledButton = galaxy.createToggleButton(0, 18, true)
    val wiggleMultiplierPot = galaxy.createPot(0, 19, 0f, 20f, 5f)
    val hudButton = galaxy.createToggleButton(0, 23, false)
    val rotationZPot = galaxy.createPot(0, 24, -1f, 1f, 0f)
    val rotationZResetButton = galaxy.createPushButton(0, 25) {
        rotationZPot.reset()
    }

    val regenerateBtn = galaxy.createPushButton(0, 6) { shouldRegenerate = true }
    val motionZoomBtn = galaxy.createPushButton(0, 8) { starMotion = Starfield.Motion.ZOOMING }
    val motionTranslateBWDBtn = galaxy.createPushButton(0, 9) { starMotion = Starfield.Motion.TRANSLATING_BACKWARD }
    val motionTranslateFWDBtn = galaxy.createPushButton(0, 11) { starMotion = Starfield.Motion.TRANSLATING_FORWARD }

    // endregion

    // region TouchOSC

    // endregion

    lateinit var starfield1: Starfield
    lateinit var starfield2: Starfield
    val triangloids = mutableListOf<Asteroid>()
    lateinit var fftLogger: FFTLogger

    var rmsSum = 0f
    var bassSum = 0f
    var vx = 0f
    var vy = 0f
    var vz = 0f
    var starVz = 0f
    var starRotationZ = 0f

    private fun regenerate() {
        triangloids.removeAt(0)
        triangloids.add(Asteroid(sketch, centerWeightButton.isPressed, audioProcessor))
    }

    override fun setup() = with(sketch) {
        starfield1 = Starfield(sketch, 300).apply { motion = starMotion }
        starfield2 = Starfield(sketch, 300).apply { motion = starMotion }
        repeat(
            NUMBER_ASTEROIDS,
            action = { triangloids.add(Asteroid(sketch, centerWeightButton.isPressed, audioProcessor)) })
        fftLogger = FFTLogger(sketch, audioProcessor)
        automator.setupWithGalaxy(
            channel = 0,
            recordButtonCC = 26,
            playButtonCC = 27,
            loopButtonCC = 28,
            clearButtonCC = 29
        )
    }

    override fun onBecameActive() = with(sketch) {
        rectMode(PConstants.CORNER)
    }

    override fun draw() = with(sketch) {
        automator.update()
        if (shouldRegenerate) {
            regenerate()
            shouldRegenerate = false
        }

        starVz += rotationZPot.value.threshold(0.05f) * 0.15f
        starVz *= 0.95f
        starRotationZ += starVz

        vx += joystick.x * .01f
        vy += joystick.y * .01f
        vz += joystick.z * .01f
        vx *= 0.95f
        vy *= 0.95f
        vz *= 0.95f

        rmsSum += audioProcessor.audioInput.mix.level()
        rmsSum *= 0.2f

        bassSum += audioProcessor.getRange(0f..50f)
        bassSum *= 0.2f

        if (beatDetectButton.isPressed && audioProcessor.beatDetectData.isKick) {
            shouldRegenerate = true
        }

        background(bgColor)

        if (isInDebugMode) {
            debugWindow()
        }

        if (flickerButton.isPressed && sketch.frameCount % 4 == 0) {
            return
        }

        // Stars
        if (starfieldRotationEnabledButton.isPressed) {
            starfield1.rotate(map(bassSum, 0f, 50f, 0f, 0.04f * starfieldRotationPot.value) + radians(starRotationZ))
            starfield2.rotate(map(bassSum, 0f, 50f, 0f, 0.08f * starfieldRotationPot.value) + radians(starRotationZ))
        }

        starfield1.setCount(starCountPot.value.toInt())
        starfield2.setCount(starCountPot.value.toInt())
        starfield1.update(speed = (2 * starSpeedPot.value).toInt() + (bassSum * starAccelPot.value).toInt())
        starfield2.update(speed = (4 * starSpeedPot.value).toInt())
        starfield1.color = fgColor
        starfield2.color = fgColor
        starfield1.motion = starMotion
        starfield2.motion = starMotion
        starfield1.draw()
        starfield2.draw()

        for ((index, triangloid) in triangloids.withIndex()) {
            triangloid.getShape().rotate(
                angleX = 0f + map(bassSum, 0f, 50f, 0f, 0.05f) + vx,
                angleY = 0.005f * (index + 1) + vy,
                angleZ = 0.010f + vz
            )

            if (wiggleButton.isPressed) {
                triangloid.wiggle(wiggleMultiplierPot.value)
            }

            pushMatrix()
            translate(width / 2f, height / 2f)
            scale(asteroidSizePot.value + (rmsSum * asteroidSizeMultPot.value))

            triangloid.strokeColor = accentColor
            triangloid.fillColor = bgColor
            triangloid.draw()

            popMatrix()

            if (hudButton.isPressed) {
                hud()
            }
        }
    }

    fun hud() = with(sketch) {
        pushMatrix()
        fill(fgColor)
        stroke(fgColor)
        translate(centerX(), centerY())
        rotateX(sin(millis() * 0.0005f) * PConstants.PI / 100f)
        rotateZ(sin(millis() * 0.0005f) * PConstants.PI / 100f)
        rotateY(sin(millis() * 0.0005f) * PConstants.PI / 20f)
        translate(0f, 0f, sin(millis() * 0.001f) * PConstants.PI / 50f + 100f)

        translate(-centerX() * 0.65f, -centerY() * 0.65f)
        fftLogger.draw()
        popMatrix()

        pushMatrix()
        translate(centerX(), centerY())
        rotateX(sin(millis() * 0.0005f) * PConstants.PI / 100f)
        rotateZ(sin(millis() * 0.0005f) * PConstants.PI / 100f)
        rotateY(sin(millis() * 0.0005f) * PConstants.PI / 80f)
        translate(0f, 0f, sin(millis() * 0.001f) * PConstants.PI / 50f)

        translate(centerX() * 0.8f + 40f, centerY() * 0.8f - 20)
        rect(0f, 0f, -bassSum * 4, -5f)
        rect(0f, -10f, -rmsSum * 200, -5f)

        popMatrix()
    }

    fun debugWindow() = with(sketch) {
        // debug values
        val basicInfoStr = StringBuilder()
            .append("resolution: ${width}x${height}").newLine()
            .append("frameRate: ${frameRate.toInt()}").newLine()
            .append("mouseX: ${mouseX - width / 2}").newLine()
            .append("mouseY: ${mouseY - height / 2}").newLine()
            .toString()

        noStroke()
        fill(accentHue, accentSat, accentBrightness)

        textSize(14f)
        text(basicInfoStr, 12f, 24f)

        // menu
        val menuStr = "If you see this, find Matsem and tell him that he left debug mode on"

        noStroke()
        fill(accentHue, accentSat, accentBrightness)
        textSize(14f)
        text(menuStr, 12f, height - menuStr.lines().size * 20f)

        val rectHeight = 8

        pushMatrix()
        translate(12f, 100f)

        // audio RMS
        fill(accentHue, accentSat, accentBrightness)
        rect(0f, 1f * rectHeight, rmsSum * 200, rectHeight.toFloat())

        // bass RMS
        fill(accentHue, accentSat, accentBrightness)
        rect(0f, 2f * rectHeight, bassSum, rectHeight.toFloat())

        popMatrix()

        // FFT
        pushMatrix()
        translate(12f, 130f)
        for (i in 0 until audioProcessor.fft.avgSize()) {
            // Draw frequency band
            fill(accentHue, accentSat, accentBrightness)
            rect(0f, i.toFloat() * rectHeight, audioProcessor.getFftAvg(i), rectHeight.toFloat())

            // Draw band frequency value
            fill(accentHue, accentSat, accentBrightness)
            textSize(10f)
            text("${audioProcessor.fft.getAverageCenterFrequency(i)} Hz", 0f, i.toFloat() * rectHeight + rectHeight)
        }

        popMatrix()

        // Star count noStroke()
        fill(accentHue, accentSat, accentBrightness)
        textSize(14f)
        text("Star count: ${starCountPot.value}", mouseX.toFloat(), mouseY.toFloat())
    }

    override fun mouseClicked() {
        shouldRegenerate = true
    }
}