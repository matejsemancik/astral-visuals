package dev.matsem.astral.visuals.legacy.fibonaccisphere

import dev.matsem.astral.core.tools.extensions.centerX
import dev.matsem.astral.core.tools.extensions.centerY
import dev.matsem.astral.core.tools.extensions.shorterDimension
import dev.matsem.astral.visuals.legacy.BaseSketch
import dev.matsem.astral.visuals.legacy.SketchLoader
import dev.matsem.astral.core.tools.audio.AudioProcessor
import dev.matsem.astral.core.tools.midi.MidiAutomator
import dev.matsem.astral.core.tools.galaxy.Galaxy
import org.koin.core.inject
import processing.core.PApplet
import processing.core.PApplet.asin
import processing.core.PApplet.floor
import processing.core.PApplet.lerp
import processing.core.PApplet.map
import processing.core.PApplet.min
import processing.core.PApplet.radians
import processing.core.PApplet.sin
import processing.core.PConstants

/**
 * Based on https://www.openprocessing.org/sketch/103897
 */
class FibSphereSketch : BaseSketch() {

    override val sketch: SketchLoader by inject()
    private val audioProcessor: AudioProcessor by inject()
    private val galaxy: Galaxy by inject()
    private val automator: MidiAutomator by inject()

    data class SpherePoint(val lat: Float, val lon: Float, val radius: Float)

    companion object {
        val PHI = (PApplet.sqrt(5f) + 1f) / 2f - 1f
        val GA = PHI * PConstants.TWO_PI
        const val MAX_POINTS = 2000
    }

    enum class DrawMode {
        MODE_1,
        MODE_2,
        MODE_3,
        MODE_4,
        MODE_5,
        MODE_6,
        MODE_7,
        MODE_8,
        MODE_9,
        MODE_10
    }

    private val drawModeButtons = galaxy.createButtonGroup(2, listOf(1, 2, 3, 4, 5, 6, 7, 8, 9), listOf(9))

    private var numPoints = 500
    private val pts = Array(MAX_POINTS) { SpherePoint(0f, 0f, 0f) }

    private var radius = sketch.shorterDimension() / 2f

    private val joystick = galaxy.createJoystick(2, 11, 12, 13, 14, 15, 16)

    private var rotationX = 0f
    private var rotationY = 0f
    private var rotationZ = 0f
    private var velocityX = 0f
    private var velocityY = 0f
    private var velocityZ = 0f
    private var pushBack = 0f

    private val sphereSizePot = galaxy.createPot(2, 17, 0f, 10f, 5f)
    private val spikeSizePot = galaxy.createPot(2, 18, 0f, 8f, 4f)
    private val oscSpeedPot = galaxy.createPot(2, 19, 0f, 1f, 0.1f)
    private val oscLowPot = galaxy.createPot(2, 20, 0f, radius * 2f, radius)
    private val oscHiPot = galaxy.createPot(2, 21, 0f, radius * 2f, radius)

    private val encoder = galaxy.createEncoder(2, 0, 50, MAX_POINTS, numPoints)
    private var bass = 0f

    private val drawModeTimerButtton = galaxy.createToggleButton(2, 22, false)
    private val timerIntervalPot = galaxy.createPot(2, 23, 500f, 60000f, 1000f)
    private var timerLastTick = 0

    fun osc(timeStretch: Float = 1f, timeOffset: Float = 0f) = with(sketch) {
        sin(millis() / 1000f * PConstants.PI * 2 * timeStretch + timeOffset)
    }

    fun initSphere(num: Int) {
        drawModeButtons.activeButtonsIndices()
        for (i in 0 until num) {
            var lon = GA * i
            lon /= PConstants.TWO_PI
            lon -= floor(lon)
            lon *= PConstants.TWO_PI
            if (lon > PConstants.PI) {
                lon -= PConstants.TWO_PI
            }

            // Convert dome height (which is proportional to surface area) to latitude
            val lat = asin(-1 + 2 * i / num.toFloat())

            pts[i] = SpherePoint(lat, lon, radius)
        }
    }

    override fun setup() = with(sketch) {
        sphereDetail(8)
        initSphere(numPoints)

        automator.setupWithGalaxy(
            channel = 2,
            recordButtonCC = 24,
            playButtonCC = 25,
            loopButtonCC = 26,
            clearButtonCC = 27,
            channelFilter = null
        )
    }

    override fun draw() = with(sketch) {
        automator.update()
        if (millis() > timerLastTick + timerIntervalPot.value) {
            timerLastTick = millis()
            onTimerTick()
        }

        if (encoder.value != numPoints) {
            numPoints = encoder.value
            initSphere(numPoints)
        }

        velocityX += joystick.x * .15f
        velocityY += joystick.y * .15f
        velocityZ += joystick.z * .15f
        velocityX *= 0.95f
        velocityY *= 0.95f
        velocityZ *= 0.95f

        rotationX += velocityX
        rotationY += velocityY
        rotationZ += velocityZ

        val drawMode = DrawMode.values()[drawModeButtons.activeButtonsIndices().first()]

        background(bgHue, bgSat, bgBrightness)
        renderGlobe(drawMode)
    }

    fun onTimerTick() {
        if (drawModeTimerButtton.isPressed) {
            drawModeButtons.switchToRandom()
        }
    }

    fun renderGlobe(mode: DrawMode) = with(sketch) {
        pushMatrix()
        translate(centerX(), centerY(), pushBack)

        rotateX(radians(rotationX))
        rotateY(radians(rotationY + 270))
        rotateZ(radians(rotationZ))

        val radius = map(
            osc(oscSpeedPot.value),
            -1f,
            1f,
            oscLowPot.value,
            oscHiPot.value
        )

        val bass = lerp(bass, audioProcessor.getRange(30f..200f), 0.5f)
        for (i in 0 until min(numPoints, pts.size)) {
            val pt = pts[i]
            pushMatrix()

            when (mode) {
                DrawMode.MODE_1 -> {
                    noStroke()
                    fill(fgHue, fgSat, fgBrightness)

                    pushMatrix()
                    rotateY(pt.lon + osc(0.5f, i + 10f) / 20f)
                    rotateZ(-pt.lat)
                    translate(radius * map(bass, 0f, 300f, 1f, 2f), 0f, 0f)
                    sphere(sphereSizePot.value)
                    popMatrix()

                    pushMatrix()
                    rotateY(pt.lon)
                    rotateZ(-pt.lat)
                    translate(radius * bass / 8f + radius * 2f, 0f, 0f)
                    sphere(sphereSizePot.value)
                    popMatrix()
                }

                DrawMode.MODE_2 -> {
                    noStroke()
                    fill(fgHue, fgSat, fgBrightness)

                    rotateY(pt.lon)
                    rotateZ(-pt.lat)

                    translate(
                        if (i % 2 == 0) {
                            radius * 1f
                        } else {
                            radius * 2f
                        }, 0f, 0f
                    )

                    if (i % 2 == 0) {
                        sphere(sphereSizePot.value)
                    } else {
                        sphere(map(bass, 0f, 80f, sphereSizePot.value, sphereSizePot.value * 5f))
                    }
                }

                DrawMode.MODE_3 -> {
                    noStroke()
                    fill(fgHue, fgSat, fgBrightness)

                    rotateY(pt.lon)
                    rotateZ(-pt.lat)

                    pushMatrix()
                    translate(radius + audioProcessor.getFftAvg((i % audioProcessor.fft.avgSize())), 0f, 0f)
                    sphere(sphereSizePot.value)
                    popMatrix()

                    pushMatrix()
                    translate(radius * map(bass, 0f, 300f, 1f, 2f), 0f, 0f)
                    sphere(sphereSizePot.value)
                    popMatrix()

                    pushMatrix()
                    rotateY(pt.lon)
                    rotateZ(-pt.lat)
                    translate(radius * bass / 8f + radius * 2f, 0f, 0f)
                    sphere(sphereSizePot.value)
                    popMatrix()
                }

                DrawMode.MODE_4 -> {
                    noStroke()
                    fill(fgHue, fgSat, fgBrightness)

                    rotateY(pt.lon)
                    rotateZ(-pt.lat)

                    pushMatrix()
                    translate(radius + audioProcessor.getFftAvg((i % audioProcessor.fft.avgSize())), 0f, 0f)
                    sphere(sphereSizePot.value)
                    popMatrix()

                    pushMatrix()
                    translate(radius * map(bass, 0f, 300f, 1f, 2f), 0f, 0f)
                    sphere(sphereSizePot.value)
                    popMatrix()

                    pushMatrix()
                    rotateY(pt.lon)
                    rotateZ(-pt.lat)
                    translate(radius * bass / 8f + radius * 2f, 0f, 0f)
                    sphere(sphereSizePot.value)
                    popMatrix()

                    pushMatrix()
                    translate(radius * map(bass, 0f, 300f, 1f, 2f), 0f, 0f)
                    stroke(accentHue, accentSat, accentBrightness)
                    strokeWeight(spikeSizePot.value)
                    sketch.line(0f, 0f, 0f, 0.5f * audioProcessor.getFftAvg((i % audioProcessor.fft.avgSize())), 0f, 0f)
                    popMatrix()
                }

                DrawMode.MODE_5 -> {
                    noStroke()
                    fill(fgHue, fgSat, fgBrightness)

                    rotateY(pt.lon)
                    rotateZ(-pt.lat)

                    pushMatrix()
                    translate(radius * map(bass, 0f, 300f, 1f, 2f), 0f, 0f)
                    sphere(sphereSizePot.value)
                    popMatrix()

                    pushMatrix()
                    rotateY(pt.lon)
                    rotateZ(-pt.lat)
                    translate(radius * bass / 8f + radius * 2f, 0f, 0f)
                    sphere(sphereSizePot.value)
                    popMatrix()

                    pushMatrix()
                    translate(radius * map(bass, 0f, 300f, 1f, 2f), 0f, 0f)
                    stroke(fgHue, fgSat, fgBrightness)
                    strokeWeight(spikeSizePot.value + 2f)
                    sketch.line(0f, 0f, 0f, audioProcessor.getFftAvg((i % audioProcessor.fft.avgSize())), 0f, 0f)
                    popMatrix()
                }

                DrawMode.MODE_6 -> {
                    noStroke()
                    fill(accentHue, accentSat, accentBrightness)

                    rotateY(pt.lon)
                    rotateZ(-pt.lat)

                    pushMatrix()
                    translate(radius + audioProcessor.getFftAvg((i % audioProcessor.fft.avgSize())), 0f, 0f)
                    sphere(sphereSizePot.value)
                    popMatrix()

                    fill(fgHue, fgSat, fgBrightness)
                    pushMatrix()
                    translate(radius * map(bass, 0f, 300f, 1f, 2f), 0f, 0f)
                    sphere(sphereSizePot.value)
                    popMatrix()

                    pushMatrix()
                    rotateY(pt.lon)
                    rotateZ(-pt.lat)
                    translate(radius * bass / 8f + radius * 2f, 0f, 0f)
                    sphere(sphereSizePot.value)
                    popMatrix()
                }

                DrawMode.MODE_7 -> {
                    noStroke()

                    rotateY(pt.lon)
                    rotateZ(-pt.lat)

                    (0..4).forEach {
                        pushMatrix()
                        val spacing = audioProcessor.getRange((100f..200f)) * 0.1f
                        translate(radius + sphereSizePot.value * it + (it * spacing), 0f, 0f)
                        fill(fgHue + it * 15f, fgSat, fgBrightness)
                        sphere(sphereSizePot.value - it * .8f)
                        popMatrix()
                    }

                    pushMatrix()
                    rotateY(pt.lon)
                    rotateZ(-pt.lat)
                    translate(radius * bass / 4f + radius * 2f, 0f, 0f)
                    fill(fgHue, fgSat, fgBrightness)
                    sphere(sphereSizePot.value + 1f)
                    popMatrix()
                }

                DrawMode.MODE_8 -> {
                    noStroke()
                    val hue = (i % audioProcessor.fft.avgSize()) * 2 + fgHue
                    fill(hue, fgSat, fgBrightness)

                    rotateY(pt.lon)
                    rotateZ(-pt.lat)

                    pushMatrix()
                    translate(radius + audioProcessor.getFftAvg((i % audioProcessor.fft.avgSize())), 0f, 0f)
                    sphere(sphereSizePot.value)
                    popMatrix()

                    pushMatrix()
                    translate(radius * map(bass, 0f, 300f, 1f, 2f), 0f, 0f)
                    sphere(sphereSizePot.value)
                    popMatrix()

                    pushMatrix()
                    rotateY(pt.lon)
                    rotateZ(-pt.lat)
                    translate(radius * bass / 8f + radius * 2f, 0f, 0f)
                    sphere(sphereSizePot.value)
                    popMatrix()
                }

                DrawMode.MODE_9 -> {
                    noStroke()
                    rotateY(pt.lon + if (i % 2 == 0) millis() * 0.0001f else millis() * -0.0001f)
                    rotateZ(-pt.lat)

                    pushMatrix()
                    fill(fgHue, fgSat, fgBrightness)
                    translate(radius, 0f, 0f)
                    sphere(sphereSizePot.value)
                    popMatrix()

                    pushMatrix()
                    fill(fgHue + 20f, fgSat, fgBrightness)
                    translate(radius * 0.6f, 0f, 0f)
                    sphere(sphereSizePot.value)
                    popMatrix()

                    pushMatrix()
                    fill(accentHue, accentBrightness, accentSat)
                    rotateY(pt.lon)
                    rotateZ(-pt.lat)
                    translate(radius * bass / 8f + radius * 2f, 0f, 0f)
                    sphere(sphereSizePot.value)
                    popMatrix()
                }

                else -> {
                    // Nothing
                }
            }

            popMatrix()
        }

        popMatrix()
    }
}