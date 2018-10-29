package sketches.fibonaccisphere

import centerX
import centerY
import processing.core.PApplet
import processing.core.PApplet.*
import processing.core.PConstants
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
        const val KMAX_POINTS = 2000
    }

    enum class DrawMode {
        MODE_1,
        MODE_2
    }

    val pts = Array(KMAX_POINTS) { SpherePoint(0f, 0f, 0f) }
    var numPoints = 100
    var radius = 0f
    var addPoints = false

    var rotationX = 0f
    var rotationY = 0f
    var velocityX = 0f
    var velocityY = 0f
    var pushBack = 0f

    var drawMode = DrawMode.MODE_2
    var bass = 0f

    fun initSphere(num: Int) {
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
        radius = height / 2f
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
        fill(130f, 255f, 255f)

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
            }

            popMatrix()
        }

        popMatrix()
    }

    override fun draw() {
        if (addPoints) {
            numPoints++
            numPoints = min(numPoints, KMAX_POINTS)
            initSphere(numPoints)
        }

        background(258f, 84f, 25f)
        renderGlobe(DrawMode.MODE_1)

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

    override fun mouseClicked() {
        addPoints = !addPoints
    }
}