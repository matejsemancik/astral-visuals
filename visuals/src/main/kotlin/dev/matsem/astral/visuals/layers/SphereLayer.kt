package dev.matsem.astral.visuals.layers

import dev.matsem.astral.core.tools.audio.AudioProcessor
import dev.matsem.astral.core.tools.extensions.centerX
import dev.matsem.astral.core.tools.extensions.centerY
import dev.matsem.astral.core.tools.extensions.colorModeHsb
import dev.matsem.astral.core.tools.extensions.shorterDimension
import dev.matsem.astral.core.tools.galaxy.Galaxy
import dev.matsem.astral.core.tools.midi.MidiAutomator
import dev.matsem.astral.visuals.ColorHandler
import dev.matsem.astral.visuals.Colorizer
import dev.matsem.astral.visuals.Layer
import org.koin.core.KoinComponent
import org.koin.core.inject
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PGraphics

class SphereLayer : Layer(), KoinComponent, ColorHandler {
    override val parent: PApplet by inject()

    override val colorizer: Colorizer by inject()
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

    private var radius = canvas.shorterDimension() / 2f

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

    fun osc(timeStretch: Float = 1f, timeOffset: Float = 0f) = with(parent) {
        PApplet.sin(millis() / 1000f * PConstants.PI * 2 * timeStretch + timeOffset)
    }

    fun initSphere(num: Int) {
        drawModeButtons.activeButtonsIndices()
        for (i in 0 until num) {
            var lon = GA * i
            lon /= PConstants.TWO_PI
            lon -= PApplet.floor(lon)
            lon *= PConstants.TWO_PI
            if (lon > PConstants.PI) {
                lon -= PConstants.TWO_PI
            }

            // Convert dome height (which is proportional to surface area) to latitude
            val lat = PApplet.asin(-1 + 2 * i / num.toFloat())

            pts[i] = SpherePoint(lat, lon, radius)
        }
    }

    init {
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

    override fun PGraphics.draw() {
        clear()
        colorModeHsb()
        sphereDetail(8)

        automator.update()

        if (parent.millis() > timerLastTick + timerIntervalPot.value) {
            timerLastTick = parent.millis()
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

        renderGlobe(drawMode)
    }

    fun onTimerTick() {
        if (drawModeTimerButtton.isPressed) {
            drawModeButtons.switchToRandom()
        }
    }

    fun renderGlobe(mode: DrawMode) = with(canvas) {
        pushMatrix()
        translate(centerX(), centerY(), pushBack)

        rotateX(PApplet.radians(rotationX))
        rotateY(PApplet.radians(rotationY + 270))
        rotateZ(PApplet.radians(rotationZ))

        val radius = PApplet.map(
            osc(oscSpeedPot.value),
            -1f,
            1f,
            oscLowPot.value,
            oscHiPot.value
        )

        val bass = PApplet.lerp(bass, audioProcessor.getRange(30f..200f), 0.5f)
        for (i in 0 until PApplet.min(numPoints, pts.size)) {
            val pt = pts[i]
            pushMatrix()

            when (mode) {
                DrawMode.MODE_1 -> {
                    noStroke()
                    fill(fgColor)

                    pushMatrix()
                    rotateY(pt.lon + osc(0.5f, i + 10f) / 20f)
                    rotateZ(-pt.lat)
                    translate(radius * PApplet.map(bass, 0f, 300f, 1f, 2f), 0f, 0f)
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
                    fill(fgColor)

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
                        sphere(PApplet.map(bass, 0f, 80f, sphereSizePot.value, sphereSizePot.value * 5f))
                    }
                }

                DrawMode.MODE_3 -> {
                    noStroke()
                    fill(fgColor)

                    rotateY(pt.lon)
                    rotateZ(-pt.lat)

                    pushMatrix()
                    translate(radius + audioProcessor.getFftAvg((i % audioProcessor.fft.avgSize())), 0f, 0f)
                    sphere(sphereSizePot.value)
                    popMatrix()

                    pushMatrix()
                    translate(radius * PApplet.map(bass, 0f, 300f, 1f, 2f), 0f, 0f)
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
                    fill(fgColor)

                    rotateY(pt.lon)
                    rotateZ(-pt.lat)

                    pushMatrix()
                    translate(radius + audioProcessor.getFftAvg((i % audioProcessor.fft.avgSize())), 0f, 0f)
                    sphere(sphereSizePot.value)
                    popMatrix()

                    pushMatrix()
                    translate(radius * PApplet.map(bass, 0f, 300f, 1f, 2f), 0f, 0f)
                    sphere(sphereSizePot.value)
                    popMatrix()

                    pushMatrix()
                    rotateY(pt.lon)
                    rotateZ(-pt.lat)
                    translate(radius * bass / 8f + radius * 2f, 0f, 0f)
                    sphere(sphereSizePot.value)
                    popMatrix()

                    pushMatrix()
                    translate(radius * PApplet.map(bass, 0f, 300f, 1f, 2f), 0f, 0f)
                    stroke(fgColor)
                    strokeWeight(spikeSizePot.value)
                    line(0f, 0f, 0f, 0.5f * audioProcessor.getFftAvg((i % audioProcessor.fft.avgSize())), 0f, 0f)
                    popMatrix()
                }

                DrawMode.MODE_5 -> {
                    noStroke()
                    fill(fgColor)

                    rotateY(pt.lon)
                    rotateZ(-pt.lat)

                    pushMatrix()
                    translate(radius * PApplet.map(bass, 0f, 300f, 1f, 2f), 0f, 0f)
                    sphere(sphereSizePot.value)
                    popMatrix()

                    pushMatrix()
                    rotateY(pt.lon)
                    rotateZ(-pt.lat)
                    translate(radius * bass / 8f + radius * 2f, 0f, 0f)
                    sphere(sphereSizePot.value)
                    popMatrix()

                    pushMatrix()
                    translate(radius * PApplet.map(bass, 0f, 300f, 1f, 2f), 0f, 0f)
                    stroke(fgColor)
                    strokeWeight(spikeSizePot.value + 2f)
                    line(0f, 0f, 0f, audioProcessor.getFftAvg((i % audioProcessor.fft.avgSize())), 0f, 0f)
                    popMatrix()
                }

                DrawMode.MODE_6 -> {
                    noStroke()
                    fill(fgColor)

                    rotateY(pt.lon)
                    rotateZ(-pt.lat)

                    pushMatrix()
                    translate(radius + audioProcessor.getFftAvg((i % audioProcessor.fft.avgSize())), 0f, 0f)
                    sphere(sphereSizePot.value)
                    popMatrix()

                    fill(fgColor)
                    pushMatrix()
                    translate(radius * PApplet.map(bass, 0f, 300f, 1f, 2f), 0f, 0f)
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
                        fill(parent.hue(fgColor) + it * 15f, parent.saturation(fgColor), parent.brightness(fgColor))
                        sphere(sphereSizePot.value - it * .8f)
                        popMatrix()
                    }

                    pushMatrix()
                    rotateY(pt.lon)
                    rotateZ(-pt.lat)
                    translate(radius * bass / 4f + radius * 2f, 0f, 0f)
                    fill(fgColor)
                    sphere(sphereSizePot.value + 1f)
                    popMatrix()
                }

                DrawMode.MODE_8 -> {
                    noStroke()
                    val hue = (i % audioProcessor.fft.avgSize()) * 2 + parent.hue(fgColor)
                    fill(hue, parent.saturation(fgColor), parent.brightness(fgColor))

                    rotateY(pt.lon)
                    rotateZ(-pt.lat)

                    pushMatrix()
                    translate(radius + audioProcessor.getFftAvg((i % audioProcessor.fft.avgSize())), 0f, 0f)
                    sphere(sphereSizePot.value)
                    popMatrix()

                    pushMatrix()
                    translate(radius * PApplet.map(bass, 0f, 300f, 1f, 2f), 0f, 0f)
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
                    rotateY(pt.lon + if (i % 2 == 0) parent.millis() * 0.0001f else parent.millis() * -0.0001f)
                    rotateZ(-pt.lat)

                    pushMatrix()
                    fill(fgColor)
                    translate(radius, 0f, 0f)
                    sphere(sphereSizePot.value)
                    popMatrix()

                    pushMatrix()
                    fill(parent.hue(fgColor) + 20f, parent.saturation(fgColor), parent.brightness(fgColor))
                    translate(radius * 0.6f, 0f, 0f)
                    sphere(sphereSizePot.value)
                    popMatrix()

                    pushMatrix()
                    fill(fgColor)
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