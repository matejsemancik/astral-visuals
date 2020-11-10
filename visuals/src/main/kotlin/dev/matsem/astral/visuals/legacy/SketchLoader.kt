package dev.matsem.astral.visuals.legacy

import ch.bildspur.postfx.builder.PostFX
import com.hamoid.VideoExport
import ddf.minim.AudioSample
import ddf.minim.Minim
import ddf.minim.analysis.BeatDetect
import ddf.minim.analysis.FFT
import dev.matsem.astral.core.ColorConfig
import dev.matsem.astral.core.VideoExportConfig
import dev.matsem.astral.core.tools.audio.AudioProcessor
import dev.matsem.astral.core.tools.audio.BeatDetectData
import dev.matsem.astral.core.tools.extensions.colorModeHsb
import dev.matsem.astral.core.tools.galaxy.Galaxy
import dev.matsem.astral.core.tools.galaxy.controls.ButtonGroup
import dev.matsem.astral.core.tools.galaxy.controls.Pot
import dev.matsem.astral.core.tools.galaxy.controls.PushButton
import dev.matsem.astral.core.tools.galaxy.controls.ToggleButton
import dev.matsem.astral.core.tools.kontrol.KontrolF1
import dev.matsem.astral.core.tools.midi.MidiAutomator
import dev.matsem.astral.core.tools.midi.MidiFileParser
import dev.matsem.astral.core.tools.midi.MidiPlayer
import dev.matsem.astral.visuals.legacy.boxes.BoxesSketch
import dev.matsem.astral.visuals.legacy.cubes.CubesSketch
import dev.matsem.astral.visuals.legacy.patterns.PatternsSketch
import dev.matsem.astral.visuals.tools.tapper.Tapper
import org.koin.core.KoinComponent
import org.koin.core.inject
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PVector
import processing.event.KeyEvent
import java.io.BufferedReader
import java.io.IOException
import java.io.PrintWriter

@Deprecated("This has been deprecated in favor of Mixer class")
class SketchLoader : PApplet(), KoinComponent {

    // region shared resources

    private val audioProcessor: AudioProcessor by inject()
    private val galaxy: Galaxy by inject()
    private val kontrolF1: KontrolF1 by inject()
    private val videoExport: VideoExport by inject()
    private val midiPlayer: MidiPlayer by inject()
    private val midiFileParser: MidiFileParser by inject()
    private val tapper: Tapper by inject()
    private val automator: MidiAutomator by inject()

    private lateinit var debugButton: ToggleButton
    private lateinit var gainPot: Pot
    private lateinit var resendButton: PushButton
    private lateinit var colorResetButton: PushButton

    private val bgColor = PVector(0f, 0f, 10f)
    private val fgColor = PVector(150f, 100f, 100f)
    private val accentColor = PVector(0f, 0f, 10f)

    lateinit var bgHuePot: Pot
    lateinit var bgSatPot: Pot
    lateinit var bgBriPot: Pot

    lateinit var fgHuePot: Pot
    lateinit var fgSatPot: Pot
    lateinit var fgBriPot: Pot

    lateinit var accentHuePot: Pot
    lateinit var accentSatPot: Pot
    lateinit var accentBriPot: Pot

    lateinit var autoSwitchButton: ToggleButton
    lateinit var autoSwitchIntervalPot: Pot
    lateinit var autoSwitchSelectorButtons: ButtonGroup
    private var lastAutoSwitchMs = 0

    private lateinit var tapperButton: PushButton

    lateinit var reader: BufferedReader

    // Effects will render with center offset if injected
    private lateinit var fx: PostFX

    private lateinit var fxRgbSplitButton: ToggleButton
    private lateinit var fxRgbSplitSlider: Pot

    private lateinit var fxPixelateButton: ToggleButton
    private lateinit var fxPixelateSlider: Pot

    private lateinit var fxBloomButton: ToggleButton
    private lateinit var fxBloomThresholdSlider: Pot
    private lateinit var fxBloomBlurSizeSlider: Pot
    private lateinit var fxBloomSigmaSlider: Pot

    private lateinit var fxChromaticAbberationButton: ToggleButton

    // endregion

    // region Sketches

    private val patternsSketch: PatternsSketch by inject()
    private val boxesSketch: BoxesSketch by inject()
    private val cubesSketch: CubesSketch by inject()

    // endregion

    var selector = 't'
    val sketches = mutableMapOf<Char, BaseSketch>()

    override fun settings() {
        size(1280, 720, PConstants.P3D)
        // fullScreen(P3D, 2) - use in live environment (projector extends desktop)
//        noSmooth()
        smooth()
    }

    override fun setup() {
        println("sketch data path: ${dataPath("")}")
        surface.setTitle("Astral Visuals")
        colorModeHsb()

        fx = PostFX(this)
        galaxy.connect()
        kontrolF1.connect()

        gainPot = galaxy.createPot(15, 64, 0f, 5f, 1f)
        debugButton = galaxy.createToggleButton(15, 65, false)
        resendButton = galaxy.createPushButton(15, 66) {
            galaxy.updatePhone()
        }

        val colorPots = mutableListOf<Pot>()
        colorResetButton = galaxy.createPushButton(15, 76) {
            colorPots.forEach { it.reset() }
        }

        val lerping = 0.08f
        bgHuePot =
            galaxy.createPot(15, 67, 0f, ColorConfig.HUE_MAX, bgColor.x).also { colorPots.add(it) }.lerp(lerping)
        bgSatPot = galaxy.createPot(15, 68, 0f, ColorConfig.SATURATION_MAX, bgColor.y).also { colorPots.add(it) }
            .lerp(lerping)
        bgBriPot = galaxy.createPot(15, 69, 0f, ColorConfig.BRIGHTNESS_MAX, bgColor.z).also { colorPots.add(it) }
            .lerp(lerping)

        fgHuePot =
            galaxy.createPot(15, 70, 0f, ColorConfig.HUE_MAX, fgColor.x).also { colorPots.add(it) }.lerp(lerping)
        fgSatPot = galaxy.createPot(15, 71, 0f, ColorConfig.SATURATION_MAX, fgColor.y).also { colorPots.add(it) }
            .lerp(lerping)
        fgBriPot = galaxy.createPot(15, 72, 0f, ColorConfig.BRIGHTNESS_MAX, fgColor.z).also { colorPots.add(it) }
            .lerp(lerping)

        accentHuePot =
            galaxy.createPot(15, 73, 0f, ColorConfig.HUE_MAX, accentColor.x).also { colorPots.add(it) }.lerp(lerping)
        accentSatPot =
            galaxy.createPot(15, 74, 0f, ColorConfig.SATURATION_MAX, accentColor.y).also { colorPots.add(it) }
                .lerp(lerping)
        accentBriPot =
            galaxy.createPot(15, 75, 0f, ColorConfig.BRIGHTNESS_MAX, accentColor.z).also { colorPots.add(it) }
                .lerp(lerping)

        autoSwitchButton = galaxy.createToggleButton(15, 77, false)
        autoSwitchIntervalPot = galaxy.createPot(15, 78, 5000f, 5 * 60 * 1000f, 5000f) // interval in millis, 5s to 5m
        autoSwitchSelectorButtons = galaxy.createButtonGroup(15, (79..94).toList(), (79..94).toList())

        tapperButton = galaxy.createPushButton(15, 95) {
            tapper.tap()
        }

        fxRgbSplitButton = galaxy.createToggleButton(15, 96, false)
        fxRgbSplitSlider = galaxy.createPot(15, 97, 0f, 200f, 25f)

        fxPixelateButton = galaxy.createToggleButton(15, 98, false)
        fxPixelateSlider = galaxy.createPot(15, 99, 0f, 500f, 400f)

        fxBloomButton = galaxy.createToggleButton(15, 100, false)
        fxBloomThresholdSlider = galaxy.createPot(15, 101, 0f, 1f, 0.5f)
        fxBloomBlurSizeSlider = galaxy.createPot(15, 102, 0f, 80f, 40f)
        fxBloomSigmaSlider = galaxy.createPot(15, 103, 0f, 30f, 20f)

        fxChromaticAbberationButton = galaxy.createToggleButton(15, 104, false)

        automator.setupWithGalaxy(
            channel = 15,
            recordButtonCC = 105,
            playButtonCC = 106,
            loopButtonCC = 107,
            clearButtonCC = 108,
            channelFilter = 15
        )

        sketches.apply {
            put('5', patternsSketch)
            put('7', boxesSketch)
            put('p', cubesSketch)
        }

        sketches.forEach { key, sketch ->
            sketch.setup()
        }

        activeSketch().onBecameActive()

        galaxy.createPushButton(15, 1) { switchSketch('1') }
        galaxy.createPushButton(15, 2) { switchSketch('2') }
        galaxy.createPushButton(15, 3) { switchSketch('3') }
        galaxy.createPushButton(15, 4) { switchSketch('4') }
        galaxy.createPushButton(15, 5) { switchSketch('5') }
        galaxy.createPushButton(15, 6) { switchSketch('8') }
        galaxy.createPushButton(15, 7) { switchSketch('7') }
        galaxy.createPushButton(15, 8) { switchSketch('9') }
        galaxy.createPushButton(15, 9) { switchSketch('p') }
        galaxy.createPushButton(15, 10) { switchSketch('m') }
        galaxy.createPushButton(15, 11) { switchSketch('s') }
        galaxy.createPushButton(15, 12) { switchSketch('g') }
        galaxy.createPushButton(15, 13) { switchSketch('o') }
        galaxy.createPushButton(15, 14) { switchSketch('t') }

        if (VideoExportConfig.IS_IN_RENDER_MODE) {
            frameRate(1000f)
            audioToTextFile(VideoExportConfig.AUDIO_FILE_PATH)
            reader = createReader("${VideoExportConfig.AUDIO_FILE_PATH}.txt")
            midiFileParser.loadFile(VideoExportConfig.MIDI_AUTOMATION_FILE)?.let {
                midiPlayer.plugIn(kontrolF1)
                midiPlayer.enqueue(it)
                midiPlayer.play()
            }
            videoExport.startMovie()
        }
    }

    override fun draw() {
        automator.update()

        if (autoSwitchButton.isPressed) {
            if (millis() > lastAutoSwitchMs + autoSwitchIntervalPot.value.toInt()) {
                // Time to switch to random sketch
                val activeButtons = autoSwitchSelectorButtons.activeButtonsIndices(exclusive = false)
                if (activeButtons.isEmpty().not()) {
                    val randomSketchIndex = activeButtons.random()
                    val currentSketchIndex = sketches.keys.toList().indexOf(selector)
                    if (randomSketchIndex < sketches.keys.count() && randomSketchIndex != currentSketchIndex) {
                        switchSketch(sketches.keys.toList()[randomSketchIndex])
                        lastAutoSwitchMs = millis()
                    }
                }
            }
        } else {
            lastAutoSwitchMs = millis()
        }

        if (VideoExportConfig.IS_IN_RENDER_MODE.not()) {
            audioProcessor.gain = gainPot.value
            activeSketch().isInDebugMode = debugButton.isPressed
            activeSketch().draw()
        } else {
            audioProcessor.gain = 1.0f
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
                val p = split(line, VideoExportConfig.SEP)
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

                while (videoExport.currentTime < soundTime + VideoExportConfig.FRAME_DURATION * 0.5) {
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
                    midiPlayer.update(soundTime * 1000f)
                    activeSketch().draw()
                    videoExport.saveFrame()
                }
            }
        }

        if (isFxActivated()) {
            fx.render().apply {
                if (fxRgbSplitButton.isPressed) {
                    rgbSplit(fxRgbSplitSlider.value)
                }

                if (fxPixelateButton.isPressed) {
                    pixelate(fxPixelateSlider.value)
                }

                if (fxBloomButton.isPressed) {
                    bloom(
                        fxBloomThresholdSlider.value,
                        fxBloomBlurSizeSlider.value.toInt(),
                        fxBloomSigmaSlider.value
                    )
                }

                if (fxChromaticAbberationButton.isPressed) {
                    chromaticAberration()
                }

                compose()
            }
        }
    }

    private fun isFxActivated(): Boolean =
        fxRgbSplitButton.isPressed
                || fxPixelateButton.isPressed
                || fxBloomButton.isPressed
                || fxChromaticAbberationButton.isPressed

    private fun activeSketch(): BaseSketch {
        return sketches.getOrDefault(selector, sketches.values.first())
    }

    private fun switchSketch(num: Char) {
        selector = num
        activeSketch().onBecameActive()
    }

    override fun keyPressed(event: KeyEvent?) {
        event?.let {
            if (sketches.keys.contains(it.key)) {
                switchSketch(it.key)
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
            val msg = StringBuilder(nf(chunkStartIndex / sampleRate, 0, 3).replace(',', '.'))
            val beat = when {
                beatDetect.isKick -> 1
                beatDetect.isSnare -> 2
                beatDetect.isHat -> 3
                else -> 0
            }

            msg.append(VideoExportConfig.SEP + beat.toString())
            for (i in 0 until fftSlices) {
                msg.append(VideoExportConfig.SEP + nf(fftL.getAvg(i), 0, 4).replace(',', '.'))
                msg.append(VideoExportConfig.SEP + nf(fftR.getAvg(i), 0, 4).replace(',', '.'))
            }
            output.println(msg.toString())
        }

        track.close()
        output.flush()
        output.close()
        println("Sound analysis done")
    }
}