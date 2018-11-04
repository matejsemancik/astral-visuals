package sketches.fibonaccisphere

import centerX
import centerY
import processing.core.PApplet
import processing.core.PApplet.*
import processing.core.PConstants
import shorterDimension
import sketches.BaseSketch
import sketches.SketchLoader
import tools.audio.AudioProcessor
import tools.galaxy.Galaxy

/**
 * Based on https://www.openprocessing.org/sketch/103897
 */
class FibSphereSketch(
        override val sketch: SketchLoader,
        val audioProcessor: AudioProcessor,
        val galaxy: Galaxy)
    : BaseSketch(sketch, audioProcessor, galaxy) {

    override fun onBecameActive() {
        audioProcessor.drawRanges(listOf((30f..200f)))
    }

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

    val drawModeButtons = galaxy.createButtonGroup(2, listOf(1, 2, 3, 4, 5, 6, 7, 8), listOf(8))

    var numPoints = 500
    val pts = Array(MAX_POINTS) { SpherePoint(0f, 0f, 0f) }

    var radius = shorterDimension() / 2f

    val joystick = galaxy.createJoystick(2, 11, 12, 13, 14, 15, 16)

    var rotationX = 0f
    var rotationY = 0f
    var rotationZ = 0f
    var velocityX = 0f
    var velocityY = 0f
    var velocityZ = 0f
    var pushBack = 0f

    val sphereSizePot = galaxy.createPot(2, 17, 0f, 10f, 5f)
    val spikeSizePot = galaxy.createPot(2, 18, 0f, 8f, 4f)
    val oscSpeedPot = galaxy.createPot(2, 19, 0f, 1f, 0.1f)
    val oscLowPot = galaxy.createPot(2, 20, 0f, radius * 2f, radius)
    val oscHiPot = galaxy.createPot(2, 21, 0f, radius * 2f, radius)

    val encoder = galaxy.createEncoder(2, 0, 50, MAX_POINTS, numPoints)
    var bass = 0f

    val drawModeTimerButtton = galaxy.createToggleButton(2, 22, false)
    val timerIntervalPot = galaxy.createPot(2, 23, 500f, 60000f, 1000f)
    var timerLastTick = 0

    fun osc(
            timeStretch: Float = 1f,
            timeOffset: Float = 0f
    ) = sin(millis() / 1000f * PConstants.PI * 2 * timeStretch + timeOffset)

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

    override fun setup() {
        sphereDetail(8)
        initSphere(numPoints)
    }

    override fun draw() {
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

        if (isInDebugMode) {
            debugWindow()
        }
    }

    fun onTimerTick() {
        if (drawModeTimerButtton.isPressed) {
            drawModeButtons.switchToRandom()
        }
    }

    fun renderGlobe(mode: DrawMode) {
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

                    translate(if (i % 2 == 0) {
                        radius * 1f
                    } else {
                        radius * 2f
                    }, 0f, 0f)

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

                else -> {
                    // Nothing
                }
            }

            popMatrix()
        }

        popMatrix()
    }

    fun debugWindow() {
        audioProcessor.drawDebug()
    }
}