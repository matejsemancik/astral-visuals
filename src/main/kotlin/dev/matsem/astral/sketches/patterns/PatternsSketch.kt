package dev.matsem.astral.sketches.patterns

import dev.matsem.astral.sketches.BaseSketch
import dev.matsem.astral.sketches.SketchLoader
import dev.matsem.astral.tools.audio.AudioProcessor
import dev.matsem.astral.tools.extensions.centerX
import dev.matsem.astral.tools.extensions.centerY
import dev.matsem.astral.tools.extensions.longerDimension
import dev.matsem.astral.tools.extensions.shorterDimension
import dev.matsem.astral.tools.galaxy.Galaxy
import org.koin.core.inject
import processing.core.PApplet
import processing.core.PApplet.*

class PatternsSketch : BaseSketch() {

    override val sketch: SketchLoader by inject()
    private val audioProcessor: AudioProcessor by inject()
    private val galaxy: Galaxy by inject()

    var bass = 0f
    var numBeats = 0

    val countEncoder = galaxy.createEncoder(4, 0, 10, 50, 10)
    val expansionPot = galaxy.createPot(4, 1, 0f, longerDimension().toFloat(), shorterDimension().toFloat()).lerp(0.5f)
    val rotationSpeedPot = galaxy.createPot(4, 2, 0f, 1f).lerp(0.5f)
    val bassGainPot = galaxy.createPot(4, 3, 0f, 0.01f, 0.001f).lerp(0.1f)
    val strokeWeightPot = galaxy.createPot(4, 4, 1f, 5f, 2f)
    val randomOnBeatButton = galaxy.createToggleButton(4, 5, false)
    val randomModeButton = galaxy.createPushButton(4, 6) { setRandomControls() }
    val beatDividerButtons = galaxy.createButtonGroup(4, listOf(7, 8, 9, 10, 11), listOf(7))

    override fun setup() {

    }

    override fun onBecameActive() {
        rectMode(PApplet.CENTER)
    }

    fun setRandomControls() {
        expansionPot.random()
        rotationSpeedPot.random()
        strokeWeightPot.random()
        countEncoder.random()
    }

    override fun draw() {
        if (audioProcessor.beatDetect.isKick) {
            numBeats++
            if (randomOnBeatButton.isPressed && numBeats % pow(2f, beatDividerButtons.activeButtonsIndices().first().toFloat()).toInt() == 0) {
                setRandomControls()
            }
        }

        bass = lerp(bass, audioProcessor.getRange((20f..100f)), 0.5f)

        background(bgHue, bgSat, bgBrightness)

        noFill()
        stroke(fgHue, fgSat, fgBrightness)
        strokeWeight(strokeWeightPot.value)
        for (i in 0 until longerDimension() step longerDimension() / countEncoder.value) {
            pushMatrix()

            translate(centerX(), centerY())
            val size = i.toFloat() + ((i) * bass * bassGainPot.value)
            rotateZ(radians(millis() * 0.0001f) * i * rotationSpeedPot.value)
            rect(0f, 0f, size + expansionPot.value, size)

            popMatrix()
        }
    }
}