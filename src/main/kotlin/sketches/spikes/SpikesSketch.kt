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
        private val audioProcessor: AudioProcessor,
        galaxy: Galaxy
) : BaseSketch(
        sketch,
        audioProcessor,
        galaxy
) {

    lateinit var positions: Array<Array<PVector>>
    lateinit var fftMapping: Array<IntArray>

    var numX = 45
    var numY = 25
    var rotationEnabled = false

    private val kontrol = KontrolF1()

    override fun setup() {
        createArray(
                numX = numX,
                numY = numY,
                paddHorizontal = 100f,
                paddVertical = 100f
        )

        kontrol.connect()
    }

    override fun onBecameActive() = Unit

    override fun draw() {
        rotationEnabled = kontrol.pad(0, 0).state

        background(bgColor)
        translate(centerX(), centerY())
        if (rotationEnabled) {
            rotateY((PConstants.TWO_PI * millis() / 1000f / 16f).quantize(PConstants.TWO_PI / kontrol.encoder.midiRange(500f)))
        } else {
            rotateY(0f)
        }

        for (x in 0 until numX) {
            for (y in 0 until numY) {
                var dotSize = kontrol.slider2.midiRange(0f, 10f)
                var lineWeight = kontrol.slider3.midiRange(0f, 10f)

                var noiseGain: Float = kontrol.slider1.midiRange(200f)
                var noiseResX: Float = kontrol.knob1.midiRange(0.1f)
                var noiseRexY: Float = kontrol.knob2.midiRange(0.1f)
                var noiseTravelX: Float = kontrol.knob3.midiRange(0.01f)
                var noiseTravelY: Float = kontrol.knob4.midiRange(0.01f)
                var audioGain: Float = kontrol.slider4.midiRange(10f)

                val pos = positions[x][y]

                val audio = audioProcessor.getFftAvg(fftMapping[x][y]) * audioGain
                val noise = noise(
                        pos.x * noiseResX + millis() * noiseTravelX,
                        pos.y * noiseRexY + millis() * noiseTravelY
                ) * noiseGain

                val elevation = noise + audio

                // Draw line
                noFill()
                stroke(fgColor)
                strokeWeight(lineWeight)

                sketch.line(pos.x, pos.y, 0f, pos.x, pos.y, elevation)

                // Draw dot
                noStroke()
                fill(fgColor)

                pushMatrix()
                translate(pos.x, pos.y, elevation)
                sphere(dotSize)
                popMatrix()
            }
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

        fftMapping = Array(numX) { IntArray(numY) { sketch.random(audioProcessor.fft.avgSize().toFloat()).toInt() } }
    }
}