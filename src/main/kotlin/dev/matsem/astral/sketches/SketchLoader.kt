package dev.matsem.astral.sketches

import com.hamoid.VideoExport
import ddf.minim.AudioSample
import ddf.minim.Minim
import ddf.minim.analysis.BeatDetect
import ddf.minim.analysis.FFT
import dev.matsem.astral.Config
import dev.matsem.astral.sketches.attractor.AttractorSketch
import dev.matsem.astral.sketches.blank.BlankSketch
import dev.matsem.astral.sketches.boxes.BoxesSketch
import dev.matsem.astral.sketches.fibonaccisphere.FibSphereSketch
import dev.matsem.astral.sketches.machina.MachinaSketch
import dev.matsem.astral.sketches.patterns.PatternsSketch
import dev.matsem.astral.sketches.polygonal.PolygonalSketch
import dev.matsem.astral.sketches.starglitch.StarGlitchSketch
import dev.matsem.astral.sketches.terrain.TerrainSketch
import dev.matsem.astral.tools.audio.AudioProcessor
import dev.matsem.astral.tools.audio.BeatDetectData
import dev.matsem.astral.tools.galaxy.Galaxy
import dev.matsem.astral.tools.galaxy.controls.Pot
import dev.matsem.astral.tools.galaxy.controls.PushButton
import dev.matsem.astral.tools.galaxy.controls.ToggleButton
import dev.matsem.astral.tools.kontrol.KontrolF1
import org.koin.core.KoinComponent
import org.koin.core.inject
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PVector
import processing.event.KeyEvent
import java.io.BufferedReader
import java.io.IOException
import java.io.PrintWriter


class SketchLoader : PApplet(), KoinComponent {

    // region shared resources

    private val audioProcessor: AudioProcessor by inject()
    private val galaxy: Galaxy by inject()
    private val kontrolF1: KontrolF1 by inject()
    private val videoExport: VideoExport by inject()

    private lateinit var debugButton: ToggleButton
    private lateinit var gainPot: Pot
    private lateinit var resendButton: PushButton
    private lateinit var colorResetButton: PushButton

    private val bgColor = PVector(0f, 0f, 10f)
    private val fgColor = PVector(0f, 0f, 100f)
    private val accentColor = PVector(97f, 100f, 100f)

    lateinit var bgHuePot: Pot
    lateinit var bgSatPot: Pot
    lateinit var bgBriPot: Pot

    lateinit var fgHuePot: Pot
    lateinit var fgSatPot: Pot
    lateinit var fgBriPot: Pot

    lateinit var accentHuePot: Pot
    lateinit var accentSatPot: Pot
    lateinit var accentBriPot: Pot

    lateinit var reader: BufferedReader

    // endregion

    // region Sketches

    private val blankSketch: BlankSketch by inject()
    private val polygonalSketch: PolygonalSketch by inject()
    private val terrainSketch: TerrainSketch by inject()
    private val fibSphereSketch: FibSphereSketch by inject()
    private val starGlitchSketch: StarGlitchSketch by inject()
    private val patternsSketch: PatternsSketch by inject()
    private val machinaSketch: MachinaSketch by inject()
    private val boxesSketch: BoxesSketch by inject()
    private val attractorSketch: AttractorSketch by inject()

    // endregion

    var selector = Config.Sketch.DEFAULT_SELECTOR
    val sketches = mutableMapOf<Char, BaseSketch>()

    override fun settings() {
//        size(1280, 720, PConstants.P3D)
        size(1280, 1280, PConstants.P3D)
        // fullScreen(P3D, 2) - use in live environment (projector extends desktop)
        noSmooth()
    }

    override fun setup() {
        colorMode(
                PConstants.HSB,
                Config.Color.HUE_MAX,
                Config.Color.SATURATION_MAX,
                Config.Color.BRIGHTNESS_MAX
        )

        galaxy.connect()
        kontrolF1.connect()

        gainPot = galaxy.createPot(15, 64, 0f, 5f, 1f)
        debugButton = galaxy.createToggleButton(15, 65, false)
        resendButton = galaxy.createPushButton(15, 66) {
            galaxy.sendClientUpdates()
        }

        val colorPots = mutableListOf<Pot>()
        colorResetButton = galaxy.createPushButton(15, 76) {
            colorPots.forEach { it.reset() }
        }

        bgHuePot = galaxy.createPot(15, 67, 0f, Config.Color.HUE_MAX, bgColor.x).also { colorPots.add(it) }
        bgSatPot = galaxy.createPot(15, 68, 0f, Config.Color.SATURATION_MAX, bgColor.y).also { colorPots.add(it) }
        bgBriPot = galaxy.createPot(15, 69, 0f, Config.Color.BRIGHTNESS_MAX, bgColor.z).also { colorPots.add(it) }

        fgHuePot = galaxy.createPot(15, 70, 0f, Config.Color.HUE_MAX, fgColor.x).also { colorPots.add(it) }
        fgSatPot = galaxy.createPot(15, 71, 0f, Config.Color.SATURATION_MAX, fgColor.y).also { colorPots.add(it) }
        fgBriPot = galaxy.createPot(15, 72, 0f, Config.Color.BRIGHTNESS_MAX, fgColor.z).also { colorPots.add(it) }

        accentHuePot = galaxy.createPot(15, 73, 0f, Config.Color.HUE_MAX, accentColor.x).also { colorPots.add(it) }
        accentSatPot = galaxy.createPot(15, 74, 0f, Config.Color.SATURATION_MAX, accentColor.y).also { colorPots.add(it) }
        accentBriPot = galaxy.createPot(15, 75, 0f, Config.Color.BRIGHTNESS_MAX, accentColor.z).also { colorPots.add(it) }

        sketches.apply {
            put('0', blankSketch)
            put('1', polygonalSketch)
            put('2', terrainSketch)
            put('3', fibSphereSketch)
            put('4', starGlitchSketch)
            put('5', patternsSketch)
            put('6', machinaSketch)
            put('7', boxesSketch)
            put('8', attractorSketch)
        }

        sketches.forEach { key, sketch ->
            sketch.setup()
        }

        activeSketch().onBecameActive()

        PushButton(galaxy.midiBus, 15, 1) { switchSketch('1') }
        PushButton(galaxy.midiBus, 15, 2) { switchSketch('2') }
        PushButton(galaxy.midiBus, 15, 3) { switchSketch('3') }
        PushButton(galaxy.midiBus, 15, 4) { switchSketch('4') }
        PushButton(galaxy.midiBus, 15, 5) { switchSketch('5') }

        if (Config.VideoExport.IS_IN_RENDER_MODE) {
            frameRate(1000f)
            audioToTextFile(Config.VideoExport.AUDIO_FILE_PATH)
            reader = createReader("${Config.VideoExport.AUDIO_FILE_PATH}.txt")
            videoExport.startMovie()
        }
    }

    override fun draw() {
        galaxy.update()

        if (Config.VideoExport.IS_IN_RENDER_MODE.not()) {
            audioProcessor.gain = gainPot.value
            activeSketch().isInDebugMode = debugButton.isPressed
            activeSketch().draw()
        } else {
            audioProcessor.gain = 1.5f
            activeSketch().isInDebugMode = false

            val line: String?
            line = try {
                reader.readLine()
            } catch (exception: IOException) {
                println(exception.toString())
                null
            }

            if (line == null) {
                // Done reading file, end video render
                videoExport.endMovie()
                exit()
            } else {
                val p = split(line, Config.VideoExport.SEP)
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

                while (videoExport.currentTime < soundTime + Config.VideoExport.FRAME_DURATION * 0.5) {
                    val channelLeft = mutableListOf<Float>()
                    val channelRight = mutableListOf<Float>()
                    val beat = parseInt(p[1])

                    for (i in 2 until p.size) {
                        val value = parseFloat(p[i])
                        if (i % 2 == 1) {
                            channelLeft.add(value)
                        } else {
                            channelRight.add(value)
                        }
                    }

                    audioProcessor.mockFft(channelLeft, channelRight)
                    audioProcessor.mockBeatDetect(BeatDetectData(beat == 1, beat == 2, beat == 3))
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

    override fun mousePressed() {
        activeSketch().mousePressed()
    }

    fun audioToTextFile(fileName: String) {
        val output: PrintWriter = createWriter(dataPath("$fileName.txt"))

        val minim = Minim(this)

        val track = minim.loadSample(fileName, 2048)

        val fftSize = 1024
        val sampleRate = track.sampleRate()

        val beatDetect = BeatDetect(fftSize, sampleRate)

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

            msg.append(Config.VideoExport.SEP + beat.toString())
            for (i in 0 until fftSlices) {
                msg.append(Config.VideoExport.SEP + nf(fftL.getAvg(i), 0, 4).replace(',', '.'))
                msg.append(Config.VideoExport.SEP + nf(fftR.getAvg(i), 0, 4).replace(',', '.'))
            }
            output.println(msg.toString())
        }

        track.close()
        output.flush()
        output.close()
        PApplet.println("Sound analysis done")
    }
}