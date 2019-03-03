package sketches.spikes

import centerX
import centerY
import midiRange
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PVector
import quantize
import sketches.BaseSketch
import sketches.SketchLoader
import tools.audio.AudioProcessor
import tools.galaxy.Galaxy
import tools.kontrol.KontrolF1

class SpikesSketch(
        sketch: SketchLoader,
        audioProcessor: AudioProcessor,
        galaxy: Galaxy
) : BaseSketch(
        sketch,
        audioProcessor,
        galaxy
) {

    lateinit var positions: Array<Array<PVector>>
    private val kontrol = KontrolF1()

    override fun setup() {
        createArray(
                numX = 45,
                numY = 25,
                paddHorizontal = 100f,
                paddVertical = 100f
        )

        kontrol.connect()
    }

    override fun onBecameActive() = Unit

    override fun draw() {
        background(bgColor)

        translate(centerX(), centerY())
        rotateY((PConstants.TWO_PI * millis() / 1000f / 16f).quantize(PConstants.TWO_PI / 200f))

        positions.flatten().forEach {
            var dotSize = kontrol.slider2.midiRange(0f, 10f)
            var lineWeight = kontrol.slider3.midiRange(0f, 10f)

            var noiseGain: Float = kontrol.slider1.midiRange(200f)
            var noiseResX: Float = kontrol.knob1.midiRange(0.1f)
            var noiseRexY: Float = kontrol.knob2.midiRange(0.1f)
            var noiseTravelX: Float = kontrol.knob3.midiRange(0.01f)
            var noiseTravelY: Float = kontrol.knob4.midiRange(0.01f)

            val noise = noise(
                    it.x * noiseResX + millis() * noiseTravelX,
                    it.y * noiseRexY + millis() * noiseTravelY
            ) * noiseGain

            // Draw line
            noFill()
            stroke(fgColor)
            strokeWeight(lineWeight)

            sketch.line(it.x, it.y, 0f, it.x, it.y, noise)

            // Draw dot
            noStroke()
            fill(fgColor)

            pushMatrix()
            translate(it.x, it.y, noise)
            sphere(dotSize)
            popMatrix()
        }
    }

    private fun createArray(
            numX: Int,
            numY: Int,
            paddHorizontal: Float = 0f,
            paddVertical: Float = 0f) {

        positions = Array(numX) { Array(numY) { PVector() } }
        for (x in 0 until numX) {
            for (y in 0 until numY) {
                positions[x][y] = PVector(
                        PApplet.map(x.toFloat(), 0f, numX - 1f, -centerX() + paddHorizontal, centerX() - paddHorizontal),
                        PApplet.map(y.toFloat(), 0f, numY - 1f, -centerY() + paddVertical, centerY() - paddVertical),
                        0f
                )
            }
        }
    }
}