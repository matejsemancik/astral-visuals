package dev.matsem.astral.sketches.cubes

import dev.matsem.astral.centerX
import dev.matsem.astral.centerY
import dev.matsem.astral.sketches.BaseSketch
import dev.matsem.astral.sketches.SketchLoader
import dev.matsem.astral.tools.audio.AudioProcessor
import dev.matsem.astral.tools.audio.beatcounter.BeatCounter
import dev.matsem.astral.tools.audio.beatcounter.OnKick
import dev.matsem.astral.tools.audio.beatcounter.OnSnare
import dev.matsem.astral.tools.galaxy.Galaxy
import org.koin.core.inject
import processing.core.PApplet
import processing.core.PConstants

class CubesSketch : BaseSketch() {

    override val sketch: SketchLoader by inject()
    private val audioProcessor: AudioProcessor by inject()
    private val galaxy: Galaxy by inject()
    private val beatCounter: BeatCounter by inject()

    var bass = 0f
    var mid = 0f
    var snare = 0f
    var num = 12

    var sweep = 0
    var sweepModulo = 1

    var rotationOffset = galaxy.createPot(8, 0, 32f, 64f, 64f).lerp(0.2f)

    var rotationXSpeed = galaxy.createPot(8, 1, -0.0015f, 0.0015f, 0f).lerp(0.6f)
    var rotationYSpeed = galaxy.createPot(8, 2, -0.0015f, 0.0015f, 0f).lerp(0.6f)
    var rotationZSpeed = galaxy.createPot(8, 3, -0.0015f, 0.0015f, 0f).lerp(0.6f)

    var bassGain = galaxy.createPot(8, 4, 0f, 1f, 1f)
    var midGain = galaxy.createPot(8, 5, 0f, 1f, 1f)
    var snareGain = galaxy.createPot(8, 6, 0f, 1f, 1f)
    var sweepEnabled = galaxy.createToggleButton(8, 7, true)
    var pillEnabled = galaxy.createToggleButton(8, 8, false)

    var randomizeEnabled = galaxy.createToggleButton(8, 9, true)

    override fun setup() {
        beatCounter.addListener(OnKick, 4) {
            num = random(6f, 12f).toInt()
        }

        beatCounter.addListener(OnSnare, 1) {
            rotationOffset.random()
        }

        beatCounter.addListener(OnSnare, 4) {
            if (sweepEnabled.isPressed) sweep = 0
        }

        beatCounter.addListener(OnSnare, 16) {
            sweepModulo = arrayOf(1, 2, 3).random()
        }

        beatCounter.addListener(OnKick, 16) {
            rotationXSpeed.random()
            rotationYSpeed.random()
            rotationZSpeed.random()
        }

        beatCounter.addListener(OnSnare, 32) {
            bassGain.random()
            midGain.random()
            snareGain.random()
        }
    }

    override fun draw() {
        if (frameCount % sweepModulo == 0) sweep++

        if (randomizeEnabled.isPressed) {
            beatCounter.update()
        }

        bass += audioProcessor.getRange(20f..60f)
        bass *= 0.5f

        mid += audioProcessor.getRange(400f..1000f)
        mid *= 0.2f

        snare = audioProcessor.getRange(900f..1100f) * 10f
        snare *= 0.4f

        background(bgColor)

        translate(centerX(), centerY())
        for (i in 0 until num) {

            pushMatrix()

            rotateX(millis() * rotationXSpeed.value + PConstants.PI / rotationOffset.value * (i + 1))
            rotateY(millis() * rotationYSpeed.value + PConstants.PI / rotationOffset.value * (i + 1))
            rotateZ(millis() * rotationZSpeed.value + PConstants.PI / rotationOffset.value * (i + 1))

            stroke(
                    fgHue,
                    PApplet.map(i.toFloat(), 0f, num.toFloat(), fgSat, bgSat),
                    PApplet.map(i.toFloat(), 0f, num.toFloat(), fgBrightness, bgBrightness)
            )

            if (i == 0) {
                fill(fgColor)
            } else {
                noFill()
            }

            if (sweep == i) {
                strokeWeight(24f)
                stroke(fgColor)
            } else {
                strokeWeight(4f)
            }

            sketch.box(
                    50f + i * 100f + bass * bassGain.value,
                    50f + i * 100f + mid * midGain.value,
                    50f + i * 100f + snare * snareGain.value
            )
            popMatrix()
        }
    }

}