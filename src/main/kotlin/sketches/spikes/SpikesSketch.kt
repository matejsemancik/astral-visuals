package sketches.spikes

import dev.matsem.astral.centerX
import dev.matsem.astral.centerY
import dev.matsem.astral.quantize
import dev.matsem.astral.sketches.BaseSketch
import dev.matsem.astral.sketches.SketchLoader
import dev.matsem.astral.tools.audio.AudioProcessor
import dev.matsem.astral.tools.galaxy.Galaxy
import org.koin.core.inject
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PVector

class SpikesSketch : BaseSketch() {

    override val sketch: SketchLoader by inject()
    val galaxy: Galaxy by inject()
    val audioProcessor: AudioProcessor by inject()

    lateinit var positions: Array<Array<PVector>>
    lateinit var fftMapping: Array<IntArray>

    var numX = 45
    var numY = 25
    var rotationXEnabled = galaxy.createToggleButton(7, 0, false)
    var rotationZEnabled = galaxy.createToggleButton(7, 1, false)
    var rotationQuantize = galaxy.createEncoder(7, 2, 0, 500, 20)

    var beatRegenEnabled = galaxy.createToggleButton(7, 3, false)
    var forceRegenBtn = galaxy.createPushButton(7, 4) { forceRegen = true }
    var forceRegen = false

    var baseRotationBtns = galaxy.createButtonGroup(7, listOf(5, 6), listOf(5))
    var baseRotations = floatArrayOf(
            0f,
            PConstants.PI / 2f
    )

//    var dotSize = kontrol.slider2.midiRange(0f, 10f)
//    var lineWeight = kontrol.slider3.midiRange(0f, 10f)
//
//    var noiseGain: Float = kontrol.slider1.midiRange(200f)
//    var noiseResX: Float = kontrol.knob1.midiRange(0.02f)
//    var noiseRexY: Float = kontrol.knob2.midiRange(0.02f)
//    var noiseTravelX: Float = kontrol.knob3.midiRange(-0.01f, 0.01f)
//    var noiseTravelY: Float = kontrol.knob4.midiRange(-0.01f, 0.01f)
//    var audioGain: Float = kontrol.slider4.midiRange(3f)

    var dotSize = 4f
    var lineWeight = 2f

    var noiseGain: Float = 100f
    var noiseResX: Float = 0.02f
    var noiseRexY: Float = 0.02f
    var noiseTravelX: Float = 0f
    var noiseTravelY: Float = 0f
    var audioGain: Float = 1f

    override fun setup() {
        createArray(
                numX = numX,
                numY = numY,
                paddHorizontal = 100f,
                paddVertical = 100f
        )
    }

    override fun draw() {
        if ((beatRegenEnabled.isPressed && audioProcessor.beatDetect.isKick) || forceRegen) {
            numX = sketch.random(20f, 50f).toInt()
            numY = sketch.random(10f, 25f).toInt()
            createArray(numX, numY, 100f, 100f)

            if (forceRegen) {
                forceRegen = false
            }
        }

        background(bgColor)
        translate(centerX(), centerY())
        if (rotationXEnabled.isPressed) {
            rotateX((PConstants.TWO_PI * millis() / 1000f / 16f).quantize(PConstants.TWO_PI / rotationQuantize.value.toFloat()))
        } else {
            rotateX(baseRotations[baseRotationBtns.activeButtonsIndices().first()])
        }

        if (rotationZEnabled.isPressed) {
            rotateZ((PConstants.TWO_PI * millis() / 1000f / 16f).quantize(PConstants.TWO_PI / rotationQuantize.value.toFloat()))
        } else {
            rotateZ(0f)
        }

        for (x in 0 until numX) {
            for (y in 0 until numY) {
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

                sketch.line(pos.x, pos.y, noise, pos.x, pos.y, elevation)
                sketch.line(pos.x, pos.y, -noise, pos.x, pos.y, -elevation)

                // Draw dot
                noStroke()
                fill(fgColor)

                pushMatrix()
                translate(pos.x, pos.y, elevation)
                sphere(dotSize)
                popMatrix()

                pushMatrix()
                translate(pos.x, pos.y, -elevation)
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