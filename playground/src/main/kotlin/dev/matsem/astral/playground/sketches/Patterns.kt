package dev.matsem.astral.playground.sketches

import dev.matsem.astral.core.tools.animations.AnimationHandler
import dev.matsem.astral.core.tools.audio.AudioProcessor
import dev.matsem.astral.core.tools.extensions.longerDimension
import dev.matsem.astral.core.tools.extensions.translateCenter
import dev.matsem.astral.core.tools.extensions.withAlpha
import dev.matsem.astral.core.tools.galaxy.Galaxy
import org.koin.core.KoinComponent
import org.koin.core.inject
import processing.core.PApplet

class Patterns : PApplet(), KoinComponent, AnimationHandler {

    override fun provideMillis(): Int {
        return millis()
    }

    private val audioProcessor: AudioProcessor by inject()
    private val galaxy: Galaxy by inject()

    private var bass = 0f
    private var numBeats = 0

    private val countEncoder = galaxy.createEncoder(4, 0, 10, 50, 10)
    private val expansionPot =
        galaxy.createPot(4, 1, 0f, 1280f, 720f).lerp(0.5f)
    private val rotationSpeedPot = galaxy.createPot(4, 2, 0f, 1f).lerp(0.5f)
    private val bassGainPot = galaxy.createPot(4, 3, 0f, 0.01f, 0.001f).lerp(0.1f)
    private val strokeWeightPot = galaxy.createPot(4, 4, 1f, 5f, 2f)
    private val randomOnBeatButton = galaxy.createToggleButton(4, 5, false)
    private val randomModeButton = galaxy.createPushButton(4, 6) { setRandomControls() }
    private val beatDividerButtons = galaxy.createButtonGroup(4, listOf(7, 8, 9, 10, 11), listOf(7))
    private val bgAlphaSlider = galaxy.createPot(4, 12, 0f, 50f, 25f)

    override fun setup() = Unit

    private fun setRandomControls() {
        expansionPot.random()
        rotationSpeedPot.random()
        strokeWeightPot.random()
        countEncoder.random()
    }

    override fun draw() {
        if (audioProcessor.beatDetect.isKick) {
            numBeats++
            if (randomOnBeatButton.isPressed && numBeats % pow(
                    2f,
                    beatDividerButtons.activeButtonsIndices().first().toFloat()
                ).toInt() == 0
            ) {
                setRandomControls()
            }
        }

        bass = lerp(bass, audioProcessor.getRange((20f..100f)), 0.5f)

        translateCenter()
        noStroke()
        fill(0x000000.withAlpha(), bgAlphaSlider.value)
        rect(0f, 0f, width.toFloat(), height.toFloat())

        noFill()
        stroke(0xffffff.withAlpha())
        strokeWeight(strokeWeightPot.value)
        for (i in 0 until longerDimension() step longerDimension() / countEncoder.value) {
            pushMatrix()

            val size = i.toFloat() + ((i) * bass * bassGainPot.value)
            rotateZ(radians(millis() * 0.0001f) * i * rotationSpeedPot.value)
            rect(0f, 0f, size + expansionPot.value, size)

            popMatrix()
        }
    }
}