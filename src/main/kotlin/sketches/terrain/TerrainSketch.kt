package sketches.terrain

import ddf.minim.AudioInput
import ddf.minim.AudioListener
import ddf.minim.Minim
import ddf.minim.analysis.FFT
import processing.core.PApplet
import processing.core.PConstants
import sketches.polygonal.star.Starfield
import tools.FFTLogger

class TerrainSketch : PApplet(), AudioListener {

    override fun samples(p0: FloatArray?) {
        fft.forward(p0)
    }

    override fun samples(p0: FloatArray?, p1: FloatArray?) {
        fft.forward(p0, p1)
    }

    // region Terrain
    val w = 800f
    val h = 800f
    var scale = 20f

    var cols = (w / scale).toInt()
    var rows = (h / scale).toInt()

    val terrain = Array(rows, { FloatArray(cols) })
    var flying = 0.06f

    // endregion

    // region stars

    lateinit var starfield: Starfield

    // endregion

    lateinit var minim: Minim
    lateinit var audioIn: AudioInput
    lateinit var fft: FFT
    lateinit var fftLogger: FFTLogger

    override fun settings() {
        size(1280, 720, PConstants.P3D)
        smooth(4)
    }

    override fun setup() {
        minim = Minim(this)
        audioIn = minim.lineIn
        audioIn.addListener(this)
        fft = FFT(audioIn.bufferSize(), audioIn.sampleRate())
        fft.logAverages(22, 3)
        fftLogger = FFTLogger(this, fft)

        starfield = Starfield(this, 800)
    }

    override fun draw() {
        background(32f, 32f, 32f)

        starfield.update()
        starfield.draw()

        stroke(0f, 255f, 100f)
        strokeWeight(1.4f)
        noFill()

        pushMatrix()
        translate(width.toFloat() / 2, height.toFloat() / 2)
        rotateX(map(mouseY.toFloat(), height.toFloat(), 0f, PConstants.PI, 0f))

        translate(-w / 2, -h / 2)

        regenerate()
        flying -= 0.1f

        for (y in 0 until rows - 1) {
            beginShape(PConstants.TRIANGLE_STRIP)

            for (x in 0 until cols) {
                vertex(x * scale, y * scale, terrain[y][x])
                vertex(x * scale, (y + 1) * scale, terrain[y + 1][x])
            }

            endShape()
        }

        popMatrix()

        fftLogger.draw(12, 12)
    }

    private fun regenerate() {
        var yoff = flying
        for (y in 0 until rows) {
            var xoff = 0f
            for (x in 0 until cols) {
                terrain[y][x] = map(noise(xoff, yoff), 0f, 1f, -20f, 50f)
                xoff += map(mouseX.toFloat(), 0f, width.toFloat(), 0f, 0.5f)
            }

            yoff += 0.2f
        }
    }
}