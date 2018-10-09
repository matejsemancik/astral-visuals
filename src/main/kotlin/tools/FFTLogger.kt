package tools

import processing.core.PApplet
import tools.audio.AudioProcessor

class FFTLogger(private val sketch: PApplet, private val audioProcessor: AudioProcessor) {

    companion object {
        val BAR_HEIGHT = 14f
    }

    var hue = 130f
    var sat = 255f
    var bri = 255f

    init {

    }

    fun draw(x: Float, y: Float) {
        sketch.pushMatrix()
        sketch.translate(x, y)
        for (i in 0 until audioProcessor.fft.avgSize()) {
            // Draw frequency band
            sketch.fill(0f, 255f, 100f)
            sketch.rect(0f, i.toFloat() * BAR_HEIGHT, audioProcessor.getFftAvg(i), BAR_HEIGHT)

            // Draw band frequency value
            sketch.fill(255f, 0f, 0f)
            sketch.textSize(10f)
            sketch.text("${audioProcessor.fft.getAverageCenterFrequency(i)} Hz", 0f, i.toFloat() * BAR_HEIGHT + BAR_HEIGHT)
        }

        sketch.popMatrix()
    }

    fun setColor(hue: Float, sat: Float, bri:Float) {
        this.hue = hue
        this.sat = sat
        this.bri = bri
    }

    fun draw() {
        for (i in 0 until audioProcessor.fft.avgSize()) {
            // Draw frequency band
            sketch.fill(hue, sat, bri)
            sketch.rect(0f, i.toFloat() * BAR_HEIGHT, audioProcessor.getFftAvg(i), BAR_HEIGHT)

            // Draw band frequency value
//            sketch.fill(255f, 0f, 0f)
//            sketch.textSize(10f)
//            sketch.text("${audioProcessor.fft.getAverageCenterFrequency(i)} Hz", 0f, i.toFloat() * BAR_HEIGHT + BAR_HEIGHT)
        }
    }
}