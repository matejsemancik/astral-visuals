package dev.matsem.astral.core.tools.videoexport

import ddf.minim.AudioSample
import ddf.minim.Minim
import ddf.minim.analysis.BeatDetect
import ddf.minim.analysis.FFT
import dev.matsem.astral.core.tools.audio.AudioProcessor
import processing.core.PApplet
import java.io.PrintWriter

/**
 * This class generates FFT and BeatDetect analysis of music file using Minim library and then serializes this info
 * to text file for later use by [VideoExporter] to mock the audio samples into exported movie.
 */
class FFTSerializer(private val parent: PApplet, private val minim: Minim) {

    companion object {
        /**
         * Text separator used for FFT sample (de)serialization.
         */
        const val SEP = "|"
    }

    /**
     * Serializes FFT and BeatDetect analysis of provided [audioFilePath] audio file into text file at the same location
     * as the [audioFilePath].
     */
    fun serialize(audioFilePath: String) {
        val output: PrintWriter = parent.createWriter(parent.dataPath("$audioFilePath.txt"))
        val track = minim.loadSample(audioFilePath, 2048)

        val fftSize = 1024
        val sampleRate = track.sampleRate()

        val beatDetect = BeatDetect(fftSize, sampleRate)

        val fftSamplesL = FloatArray(fftSize)
        val fftSamplesR = FloatArray(fftSize)

        val samplesL = track.getChannel(AudioSample.LEFT)
        val samplesR = track.getChannel(AudioSample.RIGHT)

        val fftL = FFT(fftSize, sampleRate)
        val fftR = FFT(fftSize, sampleRate)

        fftL.logAverages(AudioProcessor.FFT_BANDWIDTH, AudioProcessor.FFT_OCTAVES)
        fftR.logAverages(AudioProcessor.FFT_BANDWIDTH, AudioProcessor.FFT_OCTAVES)

        val totalChunks = samplesL.size / fftSize + 1
        val fftSlices = fftL.avgSize()

        for (ci in 0 until totalChunks) {
            val chunkStartIndex = ci * fftSize
            val chunkSize = PApplet.min(samplesL.size - chunkStartIndex, fftSize)

            System.arraycopy(samplesL, chunkStartIndex, fftSamplesL, 0, chunkSize)
            System.arraycopy(samplesR, chunkStartIndex, fftSamplesR, 0, chunkSize)
            if (chunkSize < fftSize) {
                java.util.Arrays.fill(fftSamplesL, chunkSize, fftSamplesL.size - 1, 0.0f)
                java.util.Arrays.fill(fftSamplesR, chunkSize, fftSamplesR.size - 1, 0.0f)
            }

            fftL.forward(fftSamplesL)
            fftR.forward(fftSamplesR)
            beatDetect.detect(fftSamplesL)

            // The format of the saved txt file.
            // The file contains many rows. Each row looks like this:
            // T|B|L|R|L|R|L|R|... etc
            // where T is the time in seconds and B is BeatDetect data
            // Then we alternate left and right channel FFT values
            // The first L and R values in each row are low frequencies (bass)
            // and they go towards high frequency as we advance towards
            // the end of the line.
            val msg = StringBuilder(PApplet.nf(chunkStartIndex / sampleRate, 0, 3).replace(',', '.'))
            val beat = when {
                beatDetect.isKick -> 1
                beatDetect.isSnare -> 2
                beatDetect.isHat -> 3
                else -> 0
            }

            msg.append(SEP + beat.toString())
            for (i in 0 until fftSlices) {
                msg.append(SEP + PApplet.nf(fftL.getAvg(i), 0, 4).replace(',', '.'))
                msg.append(SEP + PApplet.nf(fftR.getAvg(i), 0, 4).replace(',', '.'))
            }
            output.println(msg.toString())
        }

        track.close()
        output.flush()
        output.close()
    }
}