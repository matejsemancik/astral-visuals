package tools

import processing.core.PApplet
import tools.audio.AudioProcessor

class FFTLogger(private val sketch: PApplet, private val audioProcessor: AudioProcessor) {

    companion object {
        val BAR_HEIGHT = 14f
    }

    init {

    }

    fun draw(x: Float, y: Float) {
        sketch.pushMatrix()
        sketch.translate(x, y)
        for (i in 0 until audioProcessor.fft.avgSize()) {
            // Draw frequency band
            sketch.rect(0f, i.toFloat() * BAR_HEIGHT, audioProcessor.getFftAvg(i), BAR_HEIGHT)

            // Draw band frequency value
            sketch.textSize(10f)
            sketch.text("${audioProcessor.fft.getAverageCenterFrequency(i)} Hz", 0f, i.toFloat() * BAR_HEIGHT + BAR_HEIGHT)
        }

        sketch.popMatrix()
    }

    fun draw() {
        for (i in 0 until audioProcessor.fft.avgSize()) {
            // Draw frequency band
            sketch.rect(0f, i.toFloat() * BAR_HEIGHT, audioProcessor.getFftAvg(i), BAR_HEIGHT)

            // Draw band frequency value
//            sketch.fill(255f, 0f, 0f)
//            sketch.textSize(10f)
//            sketch.text("${audioProcessor.fft.getAverageCenterFrequency(i)} Hz", 0f, i.toFloat() * BAR_HEIGHT + BAR_HEIGHT)
        }
    }
}