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
        MODE_5
    }

    var drawMode = DrawMode.MODE_1
    val drawModeButtons = galaxy.createButtonGroup(2, listOf(1, 2, 3, 4, 5), listOf(1))

    var numPoints = 100
    val pts = Array(MAX_POINTS) { SpherePoint(0f, 0f, 0f) }

    var radius = shorterDimension() / 2f
    var rotationX = 0f
    var rotationY = 0f
    var velocityX = 0f
    var velocityY = 0f
    var pushBack = 0f

    val encoder = galaxy.createEncoder(2, 0, 50, MAX_POINTS, numPoints)

    var bass = 0f

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

    fun renderGlobe(mode: DrawMode) {
        pushMatrix()
        translate(centerX(), centerY(), pushBack)

        val xradiusot = radians(-rotationX)
        val yradiusot = radians(270 + rotationY + millis() * .06f)
        rotateX(xradiusot)
        rotateY(yradiusot)

        noStroke()
        fill(fgHue, fgSat, fgBrightness)

        val radius = (sin(millis() * 0.0005f) * this.radius) / 4f + this.radius / 1.5f
        val bass = lerp(bass, audioProcessor.getRange(30f..200f), 0.5f)
        for (i in 0 until min(numPoints, pts.size)) {
            val pt = pts[i]
            pushMatrix()

            when (mode) {
                DrawMode.MODE_1 -> {
                    rotateY(pt.lon)
                    rotateZ(-pt.lat)

                    translate(if (i % 2 == 1) {
                        radius * map(bass, 0f, 300f, 1f, 2f)
                    } else {
                        radius * bass / 8f + radius * 2f
                    }, 0f, 0f)

                    sphere(5f)
                }

                DrawMode.MODE_2 -> {
                    rotateY(pt.lon)
                    rotateZ(-pt.lat)

                    translate(if (i % 2 == 0) {
                        radius * 1f
                    } else {
                        radius * 2f
                    }, 0f, 0f)

                    if (i % 2 == 0) {
                        sphere(5f)
                    } else {
                        sphere(map(bass, 0f, 300f, 5f, 15f))
                    }
                }

                DrawMode.MODE_3 -> {
                    rotateY(pt.lon)
                    rotateZ(-pt.lat)

                    translate(
                            radius + audioProcessor.getFftAvg((i % audioProcessor.fft.avgSize())),
                            0f,
                            0f
                    )
                    sphere(5f)
                    translate(
                            -radius + audioProcessor.getFftAvg((i % audioProcessor.fft.avgSize())),
                            0f,
                            0f
                    )


                    translate(
                            radius * map(bass, 0f, 300f, 1f, 2f),
                            0f,
                            0f
                    )
                    sphere(5f)
                }
            }

            popMatrix()
        }

        popMatrix()
    }

    override fun draw() {
        if (encoder.value != numPoints) {
            numPoints = encoder.value
            initSphere(numPoints)
        }

        drawMode = DrawMode.values()[drawModeButtons.activeButtonsIndices().first()]

        background(bgHue, bgSat, bgBrightness)
        renderGlobe(drawMode)

        rotationX += velocityX
        rotationY += velocityY

        velocityX *= 0.95f
        velocityY *= 0.95f

        velocityX += (mouseY - pmouseY) * 0.02f
        velocityY += (mouseX - pmouseX) * 0.02f

        if (isInDebugMode) {
            debugWindow()
        }
    }

    fun debugWindow() {
        audioProcessor.drawDebug()
    }
}