package dev.matsem.astral.core.tools.videoexport

import com.hamoid.VideoExport
import dev.matsem.astral.core.tools.animations.AnimationHandler
import dev.matsem.astral.core.tools.audio.AudioProcessor
import dev.matsem.astral.core.tools.audio.BeatDetectData
import processing.core.PApplet
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import kotlin.properties.Delegates

/**
 * This class exports movie with provided music file as a background.
 * Based on https://github.com/hamoid/video_export_processing/blob/master/examples/withAudioViz/withAudioViz.pde
 */
class VideoExporter(
    private val parent: PApplet,
    private val videoExport: VideoExport,
    private val fftSerializer: FFTSerializer,
    private val audioProcessor: AudioProcessor
) {

    companion object {
        const val EXPORT_PATH = "data/videoexport"
        const val VIDEO_FILE_EXTENSION = ".mp4"
    }

    private var frameDuration: Float by Delegates.notNull()
    private var dryRun: Boolean by Delegates.notNull()
    private var audioGain: Float = 1f

    private lateinit var fileReader: BufferedReader
    private lateinit var drawLambda: PApplet.() -> Unit
    private var fileReaderLine: String? = null

    /**
     * Initialization method. When called, prepares the video exporter for movie export and video
     * will be exported starting from the first frame.
     *
     * Call in the sketch setup() method and move your draw logic to [draw] lambda.
     * Note, that you still have to override the parent's [draw] method, but leave it empty in order for this to work
     * (super.draw() must not be called).
     *
     * When using [AnimationHandler], don't forget to provide it with
     * exporter's [videoMillis] instead of [PApplet.millis].
     *
     * Video will be exported at [EXPORT_PATH] = <project-dir>/data/videoexport
     * directory using provided [outputVideoFileName].
     */
    fun prepare(
        audioFilePath: String,
        movieFps: Float,
        dryRun: Boolean,
        audioGain: Float = 1f,
        outputVideoFileName: String = "export.mp4",
        draw: PApplet.() -> Unit
    ) {
        this.dryRun = dryRun
        this.frameDuration = 1f / movieFps
        this.audioGain = audioGain

        parent.frameRate(if (dryRun) movieFps else 1000f)
        parent.registerMethod("draw", this)
        drawLambda = draw

        if (!dryRun) {
            audioProcessor.setMode(AudioProcessor.Mode.MOCK)
            videoExport.setFrameRate(movieFps)
            videoExport.setAudioFileName(audioFilePath)

            println("Analyzing sound file ${parent.dataPath(audioFilePath)}")
            fftSerializer.serialize(audioFilePath)
            fileReader = parent.createReader(parent.dataPath("$audioFilePath.txt"))
            println("Sound analysis done")

            File(EXPORT_PATH).mkdirs()
            videoExport.setMovieFileName("$EXPORT_PATH/${outputVideoFileName.withFileExtension()}")
            videoExport.startMovie()
        }
    }

    /**
     * This will be called at the ond of [parent]'s draw() method.
     *
     * What it basically does is reads the exported FFT analysis from file, and mocks it into the AudioProcessor,
     * preparing the sketch to be drawn with the mocked AudioProcessor.
     * Then keeps drawing the sketch until next FFT sample is ready to be loaded. Rinse and repeat.
     */
    fun draw() {
        // Keep the gain normalized during export
        audioProcessor.gain = audioGain

        if (dryRun) {
            drawLambda(parent)
            return
        }

        fileReaderLine = try {
            fileReader.readLine()
        } catch (exception: IOException) {
            println(exception.toString())
            null
        }

        if (fileReaderLine == null) {
            // Done reading file, end video render
            videoExport.endMovie()
            parent.exit()
        } else {
            val p = PApplet.split(fileReaderLine, FFTSerializer.SEP)
            val soundTime = PApplet.parseFloat(p[0])

            // Our movie will have cca 30 frames per second.
            // Our FFT analysis probably produces
            // 43 rows per second (44100 / fftSize) or
            // 46.875 rows per second (48000 / fftSize).
            // We have two different data rates: 30fps vs 43rps.
            // How to deal with that? We render frames as
            // long as the movie time is less than the latest
            // data (sound) time.
            // I added an offset of half frame duration,
            // but I'm not sure if it's useful nor what
            // would be the ideal value. Please experiment :)

            while (videoExport.currentTime < soundTime + frameDuration * 0.5) {
                val channelLeft = mutableListOf<Float>()
                val channelRight = mutableListOf<Float>()
                val beat = PApplet.parseInt(p[1])

                for (i in 2 until p.size) {
                    val value = PApplet.parseFloat(p[i])
                    if (i % 2 == 1) {
                        channelLeft.add(value)
                    } else {
                        channelRight.add(value)
                    }
                }

                audioProcessor.mockFft(channelLeft, channelRight)
                audioProcessor.mockBeatDetect(
                    BeatDetectData(
                        isKick = beat == 1,
                        isSnare = beat == 2,
                        isHat = beat == 3
                    )
                )

                drawLambda(parent)
                videoExport.saveFrame()
            }
        }
    }

    /**
     * Returns the duration of the movie (so far) in milliseconds.
     */
    fun videoMillis(): Int = if (dryRun) parent.millis() else (videoExport.currentTime * 1000).toInt()

    /**
     * Adds [VIDEO_FILE_EXTENSION] to file name if file name without extension
     * was provided.
     */
    private fun String.withFileExtension() = when {
        this.endsWith(VIDEO_FILE_EXTENSION) -> this
        else -> "$this$VIDEO_FILE_EXTENSION"
    }
}