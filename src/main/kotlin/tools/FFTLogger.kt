package tools

import ddf.minim.analysis.FFT
import processing.core.PApplet

class FFTLogger(private val sketch: PApplet, private val fft: FFT) {

    companion object {
        val BAR_HEIGHT = 12f
    }

    init {

    }

    fun draw(x: Float, y: Float) {
        sketch.pushMatrix()
        sketch.translate(x, y)
        for (i in 0 until fft.avgSize()) {
            // Draw frequency band
            sketch.fill(0f, 255f, 100f)
            sketch.rect(0f, i.toFloat() * BAR_HEIGHT, fft.getAvg(i), BAR_HEIGHT)

            // Draw band frequency value
            sketch.fill(255f, 0f, 0f)
            sketch.textSize(10f)
            sketch.text("${fft.getAverageCenterFrequency(i)} Hz", 0f, i.toFloat() * BAR_HEIGHT + BAR_HEIGHT)
        }

        sketch.popMatrix()
    }
}