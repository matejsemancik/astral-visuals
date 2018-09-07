package tools.audio

import ddf.minim.AudioListener
import ddf.minim.Minim
import ddf.minim.analysis.BeatDetect
import ddf.minim.analysis.FFT
import processing.core.PApplet

class AudioProcessor constructor(private val sketch: PApplet) : AudioListener {

    override fun samples(p0: FloatArray?) {
        beatDetect.detect(audioInput.mix)
        fft.forward(audioInput.mix)
    }

    override fun samples(p0: FloatArray?, p1: FloatArray?) {
        beatDetect.detect(audioInput.mix)
        fft.forward(audioInput.mix)
    }

    val minim = Minim(sketch)
    val audioInput = minim.lineIn.apply {
        addListener(this@AudioProcessor)
    }
    val fft = FFT(audioInput.bufferSize(), audioInput.sampleRate()).apply {
        logAverages(22, 1)
    }

    val beatDetect = BeatDetect(audioInput.bufferSize(), audioInput.sampleRate()).apply {
        setSensitivity(150)
    }

    fun drawDebug() {
        val rectHeight = 8 // px

        sketch.pushMatrix()
        sketch.translate(12f, 12f)
        for (i in 0 until fft.avgSize()) {
            // Draw frequency band
            sketch.fill(110f, 225f, 255f)
            sketch.rect(0f, i.toFloat() * rectHeight, fft.getAvg(i), rectHeight.toFloat())

            // Draw band frequency value
            sketch.fill(255f, 255f, 255f)
            sketch.textSize(10f)
            sketch.text("${fft.getAverageCenterFrequency(i)} Hz", 0f, i.toFloat() * rectHeight + rectHeight)
        }

        sketch.popMatrix()
    }
}