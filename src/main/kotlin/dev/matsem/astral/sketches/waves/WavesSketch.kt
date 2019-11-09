package dev.matsem.astral.sketches.waves

import dev.matsem.astral.tools.audio.AudioProcessor
import dev.matsem.astral.tools.extensions.centerY
import dev.matsem.astral.tools.extensions.midiRange
import dev.matsem.astral.tools.extensions.saw
import dev.matsem.astral.tools.kontrol.KontrolF1
import dev.matsem.astral.tools.kontrol.onTogglePad
import org.koin.core.KoinComponent
import org.koin.core.inject
import processing.core.PApplet
import processing.core.PConstants

// TODO to BaseSketch
class WavesSketch : PApplet(), KoinComponent {

    val kontrol: KontrolF1 by inject()
    val audioProcessor: AudioProcessor by inject()

    var oscillatorEnabled = false

    override fun settings() {
        size(1280, 720, PConstants.P3D)
    }

    override fun setup() {
        kontrol.connect()

        kontrol.onTogglePad(0, 0, 100) {
            oscillatorEnabled = it
        }
    }

    override fun draw() {
        background(0)

        noFill()
        stroke(0f, 0f, 100f)

        val spacing = kontrol.knob2.midiRange(40f, 10f)
        val saw = if (oscillatorEnabled) saw(1 / 2f) else 1f
        val count = (kontrol.knob1.midiRange(1f, height / spacing) * saw).toInt()
        val strkWeight = kontrol.knob3.midiRange(1f, 10f)
        strokeWeight(strkWeight)

        val h = count * spacing
        for (i in 0 until count) {
            drawCurve(centerY() - h / 2f + spacing * i)
        }
    }

    private fun drawCurve(y: Float) {
        beginShape()
        curveVertex(-20f, y)
        curveVertex(-20f, y)
        val count = kontrol.knob4.midiRange(2f, 128f).toInt()
        val ampGain = kontrol.slider3.midiRange(1f, 4f)
        val amp = kontrol.slider1.midiRange(-150f, 150f) + audioProcessor.getRange(20f..200f) * ampGain
        val speed = kontrol.slider2.midiRange(0.0001f, 0.001f)

        for (x in 0 until width step width / count) {
            val noise = noise(millis() * speed + x / 100f, y / 50f)
            curveVertex(x.toFloat(), y + noise * amp)
        }
        curveVertex(width.toFloat() + 20f, y)
        curveVertex(width.toFloat() + 20f, y)
        endShape()
    }
}