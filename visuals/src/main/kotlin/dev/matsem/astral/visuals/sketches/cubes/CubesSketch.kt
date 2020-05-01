package dev.matsem.astral.visuals.sketches.cubes

import dev.matsem.astral.core.tools.extensions.centerX
import dev.matsem.astral.core.tools.extensions.centerY
import dev.matsem.astral.visuals.sketches.BaseSketch
import dev.matsem.astral.visuals.sketches.SketchLoader
import dev.matsem.astral.visuals.tools.audio.AudioProcessor
import dev.matsem.astral.visuals.tools.audio.beatcounter.BeatCounter
import dev.matsem.astral.visuals.tools.audio.beatcounter.OnKick
import dev.matsem.astral.visuals.tools.audio.beatcounter.OnSnare
import dev.matsem.astral.visuals.tools.galaxy.Galaxy
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

    var sweep = 0
    var sweepModulo = 1

    var rotationOffset = galaxy.createPot(8, 0, 16f, 64f, 64f).lerp(0.2f)
    var rotationXSpeed = galaxy.createPot(8, 1, -0.0015f, 0.0015f, 0f).lerp(0.3f)
    var rotationYSpeed = galaxy.createPot(8, 2, -0.0015f, 0.0015f, 0f).lerp(0.3f)
    var rotationZSpeed = galaxy.createPot(8, 3, -0.0015f, 0.0015f, 0f).lerp(0.3f)

    var bassGain = galaxy.createPot(8, 4, 0f, 1f, 1f)
    var midGain = galaxy.createPot(8, 5, 0f, 1f, 1f)
    var snareGain = galaxy.createPot(8, 6, 0f, 1f, 1f)
    var sweepEnabled = galaxy.createToggleButton(8, 7, true)
    var pillEnabled = galaxy.createToggleButton(8, 8, false)

    var num = galaxy.createPot(8, 9, 6f, 32f, 6f)

    var rotationOffsetRandom = galaxy.createToggleButton(8, 10, false)
    var rotationXSpeedRandom = galaxy.createToggleButton(8, 11, false)
    var rotationYSpeedRandom = galaxy.createToggleButton(8, 12, false)
    var rotationZSpeedRandom = galaxy.createToggleButton(8, 13, false)
    var bassGainRandom = galaxy.createToggleButton(8, 14, false)
    var midGainRandom = galaxy.createToggleButton(8, 15, false)
    var snareGainRandom = galaxy.createToggleButton(8, 16, false)
    var numRandom = galaxy.createToggleButton(8, 17, false)

    override fun setup() {
        beatCounter.addListener(OnKick, 4) {
            if (numRandom.isPressed) {
                num.random(maxRaw = 40)
            }
        }

        beatCounter.addListener(OnSnare, 1) {
            if (rotationOffsetRandom.isPressed) rotationOffset.random()
        }

        beatCounter.addListener(OnSnare, 4) {
            if (sweepEnabled.isPressed) sweep = 0
        }

        beatCounter.addListener(OnSnare, 16) {
            sweepModulo = arrayOf(1, 2, 3).random()
        }

        beatCounter.addListener(OnKick, 16) {
            if (rotationXSpeedRandom.isPressed) rotationXSpeed.random()
            if (rotationYSpeedRandom.isPressed) rotationYSpeed.random()
            if (rotationZSpeedRandom.isPressed) rotationZSpeed.random()
        }

        beatCounter.addListener(OnSnare, 32) {
            if (bassGainRandom.isPressed) bassGain.random()
            if (midGainRandom.isPressed) midGain.random()
            if (snareGainRandom.isPressed) snareGain.random()
        }
    }

    override fun draw() = with(sketch) {
        if (frameCount % sweepModulo == 0) sweep++
        beatCounter.update()

        bass += audioProcessor.getRange(20f..60f)
        bass *= 0.5f

        mid += audioProcessor.getRange(200f..400f) * 4f
        mid *= 0.2f

        snare = audioProcessor.getRange(900f..1100f) * 10f
        snare *= 0.4f

        background(bgColor)

        translate(centerX(), centerY())
        for (i in 0 until num.value.toInt()) {

            pushMatrix()

            rotateX(millis() * rotationXSpeed.value + PConstants.PI / rotationOffset.value * (i + 1))
            rotateY(millis() * rotationYSpeed.value + PConstants.PI / rotationOffset.value * (i + 1))
            rotateZ(millis() * rotationZSpeed.value + PConstants.PI / rotationOffset.value * (i + 1))

            stroke(
                fgHue,
                PApplet.map(i.toFloat(), 0f, num.value, fgSat, bgSat),
                PApplet.map(i.toFloat(), 0f, num.value, fgBrightness, bgBrightness)
            )

            if (i == 0 && pillEnabled.isPressed) {
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

            box(
                50f + i * 100f + bass * bassGain.value,
                50f + i * 100f + mid * midGain.value,
                50f + i * 100f + snare * snareGain.value
            )
            popMatrix()
        }
    }

}