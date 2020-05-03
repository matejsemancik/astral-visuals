package dev.matsem.astral.core.tools.audio

import ddf.minim.AudioInput
import ddf.minim.AudioListener
import ddf.minim.analysis.BeatDetect
import ddf.minim.analysis.FFT

class AudioProcessor constructor(
    private val lineIn: AudioInput,
    private val isInRenderMode: Boolean
) : AudioListener {

    private var mockLeft = arrayListOf<Float>()
    private var mockRight = arrayListOf<Float>()
    private var beatDetectMock = BeatDetectData(false, false, false)

    override fun samples(p0: FloatArray?) {
        fft.forward(lineIn.mix)
        beatDetect.detect(p0)
    }

    override fun samples(p0: FloatArray?, p1: FloatArray?) {
        fft.forward(lineIn.mix)
        beatDetect.detect(p0)
    }

    init {
        if (!isInRenderMode) {
            lineIn.addListener(this)
        }
    }

    var gain = 1f

    val fft = FFT(lineIn.bufferSize(), lineIn.sampleRate()).apply {
        logAverages(22, 3)
    }

    val beatDetect = BeatDetect(lineIn.bufferSize(), lineIn.sampleRate()).apply {
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

    fun mockFft(left: List<Float>, right: List<Float>) {
        mockLeft = ArrayList(left)
        mockRight = ArrayList(right)
        beatDetect.detect(left.toFloatArray())
    }

    fun mockBeatDetect(data: BeatDetectData) {
        beatDetectMock = data
    }
}