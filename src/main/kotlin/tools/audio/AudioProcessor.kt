package tools.audio

import ddf.minim.AudioListener
import ddf.minim.Minim
import ddf.minim.analysis.BeatDetect
import ddf.minim.analysis.FFT
import processing.core.PApplet

class AudioProcessor constructor(private val sketch: PApplet) : AudioListener {

    override fun samples(p0: FloatArray?) {
        fft.forward(audioInput.mix)
    }

    override fun samples(p0: FloatArray?, p1: FloatArray?) {
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

    var gain = 1f
    private val ranges = mutableListOf<ClosedFloatingPointRange<Float>>()

    fun drawRange(range: ClosedFloatingPointRange<Float>) {
        ranges.add(range)
    }

    fun getRange(range: ClosedFloatingPointRange<Float>): Float {
        return fft.calcAvg(range.start, range.endInclusive) * gain
    }

    fun drawDebug() {
        val rectHeight = 12 // px

        sketch.pushMatrix()
        sketch.translate(12f, 12f)

        if (ranges.isNotEmpty()) {
            for (i in 0 until ranges.size) {
                val range = ranges[i]
                sketch.fill(110f, 225f, 255f)
                sketch.rect(0f, i.toFloat() * rectHeight, getRange(range), rectHeight.toFloat())

                // Draw band frequency value
                sketch.fill(255f, 255f, 255f)
                sketch.textSize(10f)
                sketch.text("${range.start} - ${range.endInclusive} Hz", 0f, i.toFloat() * rectHeight + rectHeight)
            }
        } else {
            for (i in 0 until fft.avgSize()) {
                sketch.fill(110f, 225f, 255f)
                sketch.rect(0f, i.toFloat() * rectHeight, fft.getAvg(i) * gain, rectHeight.toFloat())

                // Draw band frequency value
                sketch.fill(255f, 255f, 255f)
                sketch.textSize(10f)
                sketch.text("${fft.getAverageCenterFrequency(i)} Hz", 0f, i.toFloat() * rectHeight + rectHeight)
            }
        }

        sketch.popMatrix()
    }
}