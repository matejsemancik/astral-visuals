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
    var forceRegen = galaxy.createPushButton(7, 4) { shouldRegen = true }
    var shouldRegen = false
    var beatRegenCounter = 0

    var baseRotationBtns = galaxy.createButtonGroup(7, listOf(5, 6), listOf(5))
    var baseRotations = floatArrayOf(
            0f,
            PConstants.PI / 2f
    )

    var dotSize = galaxy.createPot(7, 7, 0f, 10f, 2f)
    var lineWeight = galaxy.createPot(7, 8, 0f, 10f, 4f)

    var noiseGain = galaxy.createPot(7, 9, 0f, 200f, 50f)
    var audioGain = galaxy.createPot(7, 10, 0f, 3f, 0.8f)

    var noiseResX = galaxy.createPot(7, 11, 0f, 0.02f, 0f)
    var noiseTravelX = galaxy.createPot(7, 12, -0.01f, 0.01f, 0f)
    var noiseResY = galaxy.createPot(7, 13, 0f, 0.02f, 0f)
    var noiseTravelY = galaxy.createPot(7, 14, -0.01f, 0.01f, 0f)

    override fun setup() {
        createArray(
                numX = numX,
                numY = numY,
                paddHorizontal = 100f,
                paddVertical = 100f
        )
    }

    override fun draw() {
        if (beatRegenEnabled.isPressed && audioProcessor.beatDetect.isKick) {
            beatRegenCounter++
            if (beatRegenCounter % 2 == 0) {
                beatRegenCounter = 0
                shouldRegen = true
            }
        }

        if (shouldRegen) {
            numX = sketch.random(20f, 50f).toInt()
            numY = sketch.random(10f, 25f).toInt()
            createArray(numX, numY, 100f, 100f)

            shouldRegen = false
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

                val audio = audioProcessor.getFftAvg(fftMapping[x][y]) * audioGain.value
                val noise = noise(
                        pos.x * noiseResX.value + millis() * noiseTravelX.value,
                        pos.y * noiseResY.value + millis() * noiseTravelY.value
                ) * noiseGain.value

                val elevation = noise + audio

                // Draw line
                noFill()
                stroke(fgColor)
                strokeWeight(lineWeight.value)

                sketch.line(pos.x, pos.y, noise, pos.x, pos.y, elevation)
                sketch.line(pos.x, pos.y, -noise, pos.x, pos.y, -elevation)

                // Draw dot
                noStroke()
                fill(fgColor)

                pushMatrix()
                translate(pos.x, pos.y, elevation)
                sphere(dotSize.value)
                popMatrix()

                pushMatrix()
                translate(pos.x, pos.y, -elevation)
                sphere(dotSize.value)
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