package sketches.fibonaccisphere

import centerX
import centerY
import processing.core.PApplet
import processing.core.PConstants
import tools.audio.AudioProcessor

class FibSphereSketch : PApplet() {

    data class SpherePoint(val lat: Float, val lon: Float, val radius: Float)

    companion object {
        val PHI = (PApplet.sqrt(5f) + 1f) / 2f - 1f
        val GA = PHI * PConstants.TWO_PI
        const val KMAX_POINTS = 2000
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

    lateinit var audioProcessor: AudioProcessor

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

    override fun settings() {
        size(1280, 720, PConstants.P3D)
    }

    override fun setup() {
        colorMode(HSB, 255f)
        audioProcessor = AudioProcessor(this)

        radius = height / 2f
        sphereDetail(8)
        initSphere(numPoints)
    }

    fun renderGlobe() {
        pushMatrix()
        translate(centerX(), centerY(), pushBack)

        val xradiusot = radians(-rotationX)
        val yradiusot = radians(270 + rotationY + millis() * .06f)
        rotateX(xradiusot)
        rotateY(yradiusot)

        noStroke()
        fill(110f, 255f, 255f)

        val radius = (sin(millis() * 0.0005f) * this.radius) / 4f + this.radius / 1.5f
        for (i in 0 until min(numPoints, pts.size)) {
            val pt = pts[i]
            pushMatrix()
            rotateY(pt.lon)
            rotateZ(-pt.lat)
            translate(if (i % 2 == 0) radius else radius / 2f, 0f, 0f)
            sphere(5f)
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

        background(0f)
        renderGlobe()

        rotationX += velocityX
        rotationY += velocityY

        velocityX *= 0.95f
        velocityY *= 0.95f

        velocityX += (mouseY - pmouseY) * 0.02f
        velocityY += (mouseX - pmouseX) * 0.02f

        audioProcessor.drawDebug()
    }

    override fun mouseClicked() {
        addPoints = !addPoints
    }
}