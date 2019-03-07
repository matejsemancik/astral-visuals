package dev.matsem.astral.tools.audio

import ddf.minim.AudioListener
import ddf.minim.Minim
import ddf.minim.analysis.BeatDetect
import ddf.minim.analysis.FFT
import processing.core.PApplet

class AudioProcessor constructor(
        private val sketch: PApplet,
        private val isInRenderMode: Boolean
) : AudioListener {

    private var mockLeft = arrayListOf<Float>()
    private var mockRight = arrayListOf<Float>()
    private var beatDetectMock = BeatDetectData(false, false, false)

    override fun samples(p0: FloatArray?) {
        fft.forward(audioInput.mix)
        beatDetect.detect(p0)
    }

    override fun samples(p0: FloatArray?, p1: FloatArray?) {
        fft.forward(audioInput.mix)
        beatDetect.detect(p0)
    }

    val minim = Minim(sketch)
    val audioInput = minim.lineIn.apply {
        if (!isInRenderMode) addListener(this@AudioProcessor)
    }

    val fft = FFT(audioInput.bufferSize(), audioInput.sampleRate()).apply {
        logAverages(22, 3)
    }

    val beatDetect = BeatDetect(audioInput.bufferSize(), audioInput.sampleRate()).apply {
        setSensitivity(150)
    }

    val beatDetectData: BeatDetectData
        get() = if (isInRenderMode) {
            beatDetectMock
        } else {
            BeatDetectData(
                    beatDetect.isKick,
                    beatDetect.isSnare,
                    beatDetect.isHat
            )
        }

    var gain = 1f
    private val ranges = mutableListOf<ClosedFloatingPointRange<Float>>()

    fun drawRanges(newRanges: List<ClosedFloatingPointRange<Float>>) {
        ranges.clear()
        ranges.addAll(newRanges)
    }

    fun getRange(range: ClosedFloatingPointRange<Float>): Float {
        if (isInRenderMode) {
            val values = mutableListOf<Float>()
            for (i in 0 until mockLeft.size) {
                if (range.contains(fft.getAverageCenterFrequency(i))) {
                    values.add((mockLeft[i] + mockRight[i]) / 2f)
                }
            }

            return values.average().toFloat() * gain
        } else {
            return fft.calcAvg(range.start, range.endInclusive) * gain
        }
    }

    fun getFftAvg(i: Int): Float {
        return if (isInRenderMode) {
            val l = mockLeft[i]
            val r = mockRight[i]
            (l + r) / 2f
        } else {
            fft.getAvg(i) * gain
        }
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

    fun mockFft(left: List<Float>, right: List<Float>) {
        mockLeft = ArrayList(left)
        mockRight = ArrayList(right)
        beatDetect.detect(left.toFloatArray())
    }

    fun mockBeatDetect(data: BeatDetectData) {
        beatDetectMock = data
    }
}