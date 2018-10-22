package sketches

import com.hamoid.VideoExport
import ddf.minim.AudioSample
import ddf.minim.Minim
import ddf.minim.analysis.FFT
import processing.core.PApplet
import processing.core.PConstants
import processing.event.KeyEvent
import sketches.blank.BlankSketch
import sketches.fibonaccisphere.FibSphereSketch
import sketches.polygonal.PolygonalSketch
import sketches.starglitch.StarGlitchSketch
import sketches.terrain.TerrainSketch
import tools.audio.AudioProcessor
import tools.galaxy.Galaxy
import tools.galaxy.controls.Pot
import tools.galaxy.controls.PushButton
import tools.galaxy.controls.ToggleButton
import java.io.BufferedReader
import java.io.IOException
import java.io.PrintWriter


class SketchLoader : PApplet() {

    // region shared resources

    private lateinit var audioProcessor: AudioProcessor
    private val galaxy: Galaxy = Galaxy()
    private lateinit var debugButton: ToggleButton
    private lateinit var gainPot: Pot
    private lateinit var resendButton: PushButton

    val isInRenderMode = false
    val audioFilePath = "bop2.wav"
    val sep = "|"
    val movieFps = 30f
    val frameDuration = 1f / movieFps

    lateinit var videoExport: VideoExport
    lateinit var reader: BufferedReader

    // endregion

    lateinit var blankSketch: BaseSketch
    var selector = '2'
    val sketches = mutableMapOf<Char, BaseSketch>()

    override fun settings() {
        size(1280, 720, PConstants.P3D)
        noSmooth()
    }

    override fun setup() {
        colorMode(PConstants.HSB, 360f, 100f, 100f)

        galaxy.connect()
        audioProcessor = AudioProcessor(this, isInRenderMode)

        gainPot = galaxy.createPot(15, 64, 0f, 5f, 1f)
        debugButton = galaxy.createToggleButton(15, 65, false)
        resendButton = galaxy.createPushButton(15, 66) {
            galaxy.sendClientUpdates()
        }

        blankSketch = BlankSketch(this, audioProcessor, galaxy)

        sketches.apply {
            put('0', blankSketch)
            put('1', PolygonalSketch(this@SketchLoader, audioProcessor, galaxy))
            put('2', TerrainSketch(this@SketchLoader, audioProcessor, galaxy))
            put('3', FibSphereSketch(this@SketchLoader, audioProcessor, galaxy))
            put('4', StarGlitchSketch(this@SketchLoader, audioProcessor, galaxy))
        }

        sketches.forEach { key, sketch ->
            sketch.setup()
        }

        activeSketch().onBecameActive()

        PushButton(galaxy.midiBus, 15, 1) { switchSketch('1') }
        PushButton(galaxy.midiBus, 15, 2) { switchSketch('2') }
        PushButton(galaxy.midiBus, 15, 3) { switchSketch('3') }

        if (isInRenderMode) {
            frameRate(1000f)
            audioToTextFile(audioFilePath)
            reader = createReader("$audioFilePath.txt")
            videoExport = VideoExport(this).apply {
                setFrameRate(movieFps)
                setAudioFileName(audioFilePath)
                startMovie()
            }
        }
    }

    override fun draw() {
        galaxy.update()

        if (!isInRenderMode) {
            audioProcessor.gain = gainPot.value
            activeSketch().isInDebugMode = debugButton.isPressed
            activeSketch().draw()
        } else {
            audioProcessor.gain = 2f
            activeSketch().isInDebugMode = false

            var line: String? = null
            try {
                line = reader.readLine()
            } catch (exception: IOException) {
                println(exception.toString())
                line = null
            }

            if (line == null) {
                // Done reading file, end video render
                videoExport.endMovie()
                exit()
            } else {
                val p = split(line, sep)
                val soundTime = parseFloat(p[0])

                // Our movie will have 30 frames per second.
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
                    for (i in 1 until p.size) {
                        val value = parseFloat(p[i])
                        if (i % 2 == 1) {
                            channelLeft.add(value)
                        } else {
                            channelRight.add(value)
                        }
                    }

                    audioProcessor.mockFft(channelLeft, channelRight)
                    activeSketch().draw()
                    videoExport.saveFrame()
                }
            }
        }
    }

    private fun activeSketch(): BaseSketch {
        return sketches.getOrDefault(selector, blankSketch)
    }

    private fun switchSketch(num: Char) {
        selector = num
        activeSketch().onBecameActive()
    }

    override fun keyPressed(event: KeyEvent?) {
        event?.let {
            if (sketches.keys.contains(it.key)) {
                selector = it.key
                activeSketch().onBecameActive()
            } else {
                activeSketch().keyPressed(event)
            }
        }
    }

    override fun mouseClicked() {
        activeSketch().mouseClicked()
    }

    fun audioToTextFile(fileName: String) {
        val output: PrintWriter = createWriter(dataPath("$fileName.txt"))

        val minim = Minim(this)

        val track = minim.loadSample(fileName, 2048)

        val fftSize = 1024
        val sampleRate = track.sampleRate()

        val fftSamplesL = FloatArray(fftSize)
        val fftSamplesR = FloatArray(fftSize)

        val samplesL = track.getChannel(AudioSample.LEFT)
        val samplesR = track.getChannel(AudioSample.RIGHT)

        val fftL = FFT(fftSize, sampleRate)
        val fftR = FFT(fftSize, sampleRate)

        fftL.logAverages(22, 3)
        fftR.logAverages(22, 3)

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
            fftR.forward(fftSamplesL)

            // The format of the saved txt file.
            // The file contains many rows. Each row looks like this:
            // T|L|R|L|R|L|R|... etc
            // where T is the time in seconds
            // Then we alternate left and right channel FFT values
            // The first L and R values in each row are low frequencies (bass)
            // and they go towards high frequency as we advance towards
            // the end of the line.
            val msg = StringBuilder(PApplet.nf(chunkStartIndex / sampleRate, 0, 3).replace(',', '.'))
            for (i in 0 until fftSlices) {
                msg.append(sep + nf(fftL.getAvg(i), 0, 4).replace(',', '.'))
                msg.append(sep + nf(fftR.getAvg(i), 0, 4).replace(',', '.'))
            }
            output.println(msg.toString())
        }
        track.close()
        output.flush()
        output.close()
        PApplet.println("Sound analysis done")
    }
}