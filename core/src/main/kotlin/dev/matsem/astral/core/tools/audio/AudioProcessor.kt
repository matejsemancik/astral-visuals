package dev.matsem.astral.core.tools.audio

import ddf.minim.AudioInput
import ddf.minim.AudioListener
import ddf.minim.analysis.BeatDetect
import ddf.minim.analysis.FFT

/**
 * This class handles everything live input audio-related.
 * Use it to get current FFT values or BeatDetect data.
 */
class AudioProcessor constructor(private val lineIn: AudioInput) : AudioListener {

    /**
     * Operation mode. Affects behavior of [beatDetectData], [getRange] and [getFftAvg] methods.
     * [Mode.LIVE] uses live AudioInput as source for mentioned methods.
     * [Mode.MOCK] uses mocked FFT samples and BeatDetect data as source for mentioned methods. Pass the mocked data
     * using the [mockFft] and [mockBeatDetect] methods.
     */
    enum class Mode {
        LIVE, MOCK
    }

    companion object {
        const val FFT_BANDWIDTH = 22
        const val FFT_OCTAVES = 3
        const val BEAT_DETECT_SENTIVITY = 150
    }

    private var mode: Mode = Mode.LIVE
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
        setMode(Mode.LIVE)
    }

    /**
     * Sets operation mode. Refer to [Mode] enum documentation.
     */
    fun setMode(mode: Mode) {
        this.mode = mode

        when (mode) {
            Mode.LIVE -> {
                lineIn.addListener(this)
            }
            Mode.MOCK -> {
                // Listener needs to be removed for MOCK mode,
                // so the live audio input does not affect the exported video
                lineIn.removeListener(this)
            }
        }
    }

    /**
     * Software gain which affects FFT values.
     */
    var gain = 1f

    /**
     * FFT object used for live audio input.
     */
    val fft = FFT(lineIn.bufferSize(), lineIn.sampleRate()).apply {
        logAverages(FFT_BANDWIDTH, FFT_OCTAVES)
    }

    /**
     * BeatDetect object used for live audio input.
     */
    val beatDetect = BeatDetect(lineIn.bufferSize(), lineIn.sampleRate()).apply {
        setSensitivity(BEAT_DETECT_SENTIVITY)
    }

    /**
     * BeatDetect data which take [Mode] setting into account.
     */
    val beatDetectData: BeatDetectData
        get() = when (mode) {
            Mode.LIVE -> BeatDetectData(
                beatDetect.isKick,
                beatDetect.isSnare,
                beatDetect.isHat
            )
            Mode.MOCK -> beatDetectMock
        }

    /**
     * Calculates average amplitude of FFT samples in given frequency [range].
     */
    fun getRange(range: ClosedFloatingPointRange<Float>): Float {
        return when(mode) {
            Mode.LIVE -> fft.calcAvg(range.start, range.endInclusive) * gain
            Mode.MOCK -> {
                val values = mutableListOf<Float>()
                for (i in 0 until mockLeft.size) {
                    if (range.contains(fft.getAverageCenterFrequency(i))) {
                        values.add((mockLeft[i] + mockRight[i]) / 2f)
                    }
                }

                values.average().toFloat() * gain
            }
        }
    }

    /**
     * Returns FFT average of band under index [i]. [i] should be in range 0 to [FFT_BANDWIDTH].
     */
    fun getFftAvg(i: Int): Float {
        return when(mode) {
            Mode.LIVE -> fft.getAvg(i) * gain
            Mode.MOCK -> {
                val l = mockLeft[i]
                val r = mockRight[i]
                (l + r) / 2f
            }
        }
    }

    /**
     * Provides mock FFT data for current frame while exporting video
     */
    fun mockFft(left: List<Float>, right: List<Float>) {
        mockLeft = ArrayList(left)
        mockRight = ArrayList(right)
        beatDetect.detect(left.toFloatArray())
    }

    /**
     * Provides mock BeatDetect data for current frame while exporting video
     */
    fun mockBeatDetect(data: BeatDetectData) {
        beatDetectMock = data
    }
}