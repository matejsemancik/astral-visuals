package dev.matsem.astral.sketches.gameoflife

import controlP5.ControlP5Constants.CENTER
import dev.matsem.astral.sketches.BaseSketch
import dev.matsem.astral.sketches.SketchLoader
import dev.matsem.astral.tools.audio.AudioProcessor
import dev.matsem.astral.tools.audio.beatcounter.BeatCounter
import dev.matsem.astral.tools.audio.beatcounter.OnKick
import dev.matsem.astral.tools.extensions.*
import dev.matsem.astral.tools.kontrol.KontrolF1
import dev.matsem.astral.tools.kontrol.onTogglePad
import dev.matsem.astral.tools.kontrol.onTriggerPad
import dev.matsem.astral.tools.midi.MidiFileParser
import dev.matsem.astral.tools.midi.MidiPlayer
import dev.matsem.astral.tools.midi.MidiRecorder
import org.koin.core.inject
import processing.core.PApplet.constrain
import processing.core.PApplet.lerp
import processing.core.PConstants
import processing.core.PFont
import processing.core.PGraphics
import processing.core.PImage

// ideas:
// - color inversion
// - Astral logo
// - optimization: do not create new array for each new universe generation
class GameOfLifeSketch : BaseSketch() {

    override val sketch: SketchLoader by inject()

    private val kontrol: KontrolF1 by inject()
    private val beatCounter: BeatCounter by inject()
    private val audioProcessor: AudioProcessor by inject()
    private val midiRecorder: MidiRecorder by inject()
    private val midiPlayer: MidiPlayer by inject()
    private val midiFileParser: MidiFileParser by inject()
    private val musicPlayer = audioProcessor.loadFile("music/seba2.wav")

    private var cellSize = 5
    private var nextRound = 0

    lateinit var universe: Universe
    lateinit var overlay: PGraphics
    lateinit var pixelFont: PFont

    private lateinit var semLogo: PImage
    private lateinit var astralLogo: PImage

    private var overlayText: String? = null
    private var overlayImage: PImage? = null

    private var hueStart: Float = 210f
    private var hueEnd: Float = 241f
    private var heatMapEnabled: Boolean = false
    private var randomizeThresh: Float = 1f
    private var stepMillis = 40
    private var coolingFactor = 0.99f
    private var heatMapSaturation = 100f
    private var outlineEnabled = true
    private var targetZoom = 1f
    private var actualZoom = 1f

    private val blacklistButtons = listOf(14, 15, 16, 17) // record control buttons

    override fun onBecameActive() = with(sketch) {
        rectMode(PConstants.CORNER)

        kontrol.reset()
        kontrol.onTriggerPad(0, 0, midiHue = 0) { if (it) randomize(randomizeThresh) }
        kontrol.onTogglePad(0, 1, midiHue = 8) { heatMapEnabled = it }
        kontrol.onTogglePad(0, 2, midiHue = 16) { outlineEnabled = !it }
        kontrol.onTriggerPad(3, 0, midiHue = 48) {
            overlayImage = if (it) astralLogo else null
        }
        kontrol.onTriggerPad(3, 1, midiHue = 48) { overlayText = if (it) "16/11" else null }
        kontrol.onTriggerPad(3, 2, midiHue = 48) { overlayText = if (it) "SEBA" else null }
        kontrol.onTriggerPad(3, 3, midiHue = 48) {
            overlayImage = if (it) semLogo else null
        }

        // Record button
        kontrol.onTogglePad(1, 0, midiHue = 50) {
            if (it) {
                midiRecorder.startRecording()
                midiPlayer.enqueue(
                        midiRecorder.getMessages(excludedCCs = blacklistButtons)
                )
                midiPlayer.play()
//                musicPlayer.play()
            } else {
                midiRecorder.stopRecording()
                midiPlayer.stop()
                musicPlayer.pause()
                musicPlayer.rewind()
            }
        }

        // Play button
        kontrol.onTriggerPad(1, 1, midiHue = 65) {
            if (it) {
                if (!midiPlayer.isPlaying) {
                    midiPlayer.enqueue(
                            midiRecorder.getMessages(excludedCCs = blacklistButtons)
                    )
                    midiPlayer.play()
//                    musicPlayer.play()
                } else {
                    midiPlayer.stop()
                    musicPlayer.pause()
                    musicPlayer.rewind()
                }
            }
        }

        // Save automation to file button
        kontrol.onTriggerPad(1, 2, midiHue = 70) {
            if (it) {
                midiFileParser.saveFile(midiRecorder.getMessages(excludedCCs = blacklistButtons), "midi/automation.json")
            }
        }
    }

    override fun setup() = with(sketch) {
        midiRecorder.plugIn(kontrol)
        midiPlayer.plugIn(kontrol)
        musicPlayer.addListener(audioProcessor)

        universe = Universe(
                Array(height / cellSize) {
                    Array<Cell>(width / cellSize) { DeadCell }
                }
        )
        pixelFont = createFont("fonts/fff-forward.ttf", 24f, false)
        overlay = createGraphics(universe.width, universe.height, PConstants.P2D)

        semLogo = loadImage("images/semlogo.png").apply {
            resizeRatioAware(width = overlay.shorterDimension() / 2)
        }
        astralLogo = loadImage("images/astrallogo.png").apply {
            resizeRatioAware(width = (overlay.shorterDimension() / 1.5f).toInt())
        }

        beatCounter.addListener(OnKick, 1) {
            randomize(0.995f)
        }

        beatCounter.addListener(OnKick, 4) {
            targetZoom = random(1f, 1.2f)
        }

        randomize(0.30f)
    }

    /**
     * Draws cells into universe.
     * All white pixels will be alive cells.
     * All black pixels will be dead cells.
     * All other pixel values are ignored.
     */
    private fun drawOverlay() = with(overlay) {
        beginDraw()
        background(128f)

        overlayText?.let { text ->
            textFont(pixelFont)
            textAlign(CENTER, CENTER)
            textSize(24f)

            // Text stroke hack
            if (outlineEnabled) {
                for (xOff in -1..1) {
                    for (yOff in -1..1) {
                        fill(0f)
                        text(text, width / 2f + xOff, height / 2 - 24 / 2f + yOff)
                    }
                }
            }

            fill(255f)
            text(text, width / 2f, height / 2 - 24 / 2f)
        }

        overlayImage?.let { image ->
            translateCenter()
            image(image, -image.width / 2f, -image.height / 2f)
        }

        endDraw()
    }

    override fun draw() = with(sketch) {
        midiPlayer.update()

        hueStart = kontrol.knob1.midiRange(0f, 360f)
        hueEnd = kontrol.knob2.midiRange(0f, 360f)
        randomizeThresh = kontrol.slider1.midiRange(1f, 0f)
        stepMillis = kontrol.knob3.midiRange(40f, 120f).toInt()
        coolingFactor = kontrol.knob4.midiRange(0.10f, 0.99f)
        heatMapSaturation = kontrol.slider2.midiRange(0f, 100f)

        beatCounter.update()
        drawOverlay()
        background(0f, 0f, 10f)
        translateCenter()
        actualZoom = lerp(actualZoom, targetZoom, 0.20f)
        scale(actualZoom)

        if (millis() > nextRound) {
            nextRound = millis() + stepMillis
            universe.coolingFactor = coolingFactor
            universe.nextGeneration()
        }

        overlay.loadPixels()
        var brightness: Float
        for (y in 0 until overlay.pixelHeight) {
            for (x in 0 until overlay.width) {
                brightness = brightness(overlay.pixels[x + (y * overlay.pixelWidth)])
                when (brightness) {
                    100f -> universe.cells[y][x] = AliveCell
                    0f -> universe.cells[y][x] = DeadCell
                }
            }
        }

        for (y in 0 until universe.height) {
            for (x in 0 until universe.width) {

                val brightness = if (heatMapEnabled) {
                    universe.heatMap[y][x].remap(0f, 1f, 10f, 100f)
                } else {
                    if (universe.cells[y][x] is AliveCell) 100f else 0f
                }

                val color = color(
                        universe.heatMap[y][x].remap(1f, 0f, hueStart, hueEnd),
                        if (universe.cells[y][x] is AliveCell) 10f else heatMapSaturation,
                        brightness
                )

                noStroke()
                fill(color)
                rect(
                        x.toFloat() + (x * (cellSize - 1)) - width / 2f,
                        y.toFloat() + (y * (cellSize - 1)) - height / 2f,
                        cellSize.toFloat(),
                        cellSize.toFloat()
                )
            }
        }
    }

    override fun mouseClicked() = with(sketch) {
        val x = mouseX / cellSize
        val y = mouseY / cellSize
        universe.cells[y][x] = AliveCell
    }

    private fun randomize(threshold: Float) = with(sketch) {
        universe.cells.forEach {
            it.forEachIndexed { index, cell ->
                it[index] = if (random(1f) > constrain(threshold, 0f, 1f)) AliveCell else cell
            }
        }
    }
}