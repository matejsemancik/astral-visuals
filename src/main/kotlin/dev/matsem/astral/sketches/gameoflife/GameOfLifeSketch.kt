package dev.matsem.astral.sketches.gameoflife

import dev.matsem.astral.midiRange
import dev.matsem.astral.remap
import dev.matsem.astral.tools.audio.beatcounter.BeatCounter
import dev.matsem.astral.tools.audio.beatcounter.OnSnare
import dev.matsem.astral.tools.kontrol.KontrolF1
import dev.matsem.astral.tools.kontrol.onTogglePad
import dev.matsem.astral.tools.kontrol.onTriggerPad
import org.koin.core.KoinComponent
import org.koin.core.inject
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PFont
import processing.core.PGraphics

// TODO convert to BaseSketch
class GameOfLifeSketch : PApplet(), KoinComponent {

    private val kontrol: KontrolF1 by inject()
    private val beatCounter: BeatCounter by inject()

    private var cellSize = 5

    private var speedMillis = 60
    private var nextRound = 0

    lateinit var universe: Universe
    lateinit var overlay: PGraphics
    lateinit var pixelFont: PFont

    private var overlayText: String? = null
    private var hueStart: Float = 160f
    private var hueEnd: Float = 220f
    private var heatMapEnabled: Boolean = false
    private var randomizeThresh: Float = 1f

    override fun settings() {
        size(1280, 720, PConstants.P3D)
    }

    override fun setup() {
        kontrol.connect()

        kontrol.onTogglePad(0, 0, midiHue = 64) { heatMapEnabled = it }
        kontrol.onTriggerPad(3, 0, midiHue = 100) { overlayText = if (it) "astral" else null }
        kontrol.onTriggerPad(3, 1, midiHue = 100) { overlayText = if (it) "soul ex\nmachina" else null }
        kontrol.onTriggerPad(3, 2, midiHue = 100) { overlayText = if (it) "16/11" else null }
        kontrol.onTriggerPad(3, 3, midiHue = 80) { if (it) randomize(randomizeThresh) }

        colorMode(PConstants.HSB, 360f, 100f, 100f)
        rectMode(PConstants.CORNER)

        universe = Universe(Array(height / cellSize) { Array<Cell>(width / cellSize) { DeadCell } })
        pixelFont = createFont("fonts/fff-forward.ttf", 24f, false)
        overlay = createGraphics(universe.width, universe.height, PConstants.P2D)

        beatCounter.addListener(OnSnare, 2) {
            randomize(0.996f)
        }
    }

    fun drawOverlay() = with(overlay) {
        beginDraw()
        noStroke()
        background(0f)

        overlayText?.let { text ->
            fill(255f)
            textFont(pixelFont)
            textAlign(CENTER, CENTER)
            textSize(24f)
            text(text, width / 2f, height / 2 - 24 / 2f)
        }

        endDraw()
    }

    override fun draw() {
        hueStart = kontrol.knob1.midiRange(0f, 360f)
        hueEnd = kontrol.knob2.midiRange(0f, 360f)
        randomizeThresh = kontrol.slider1.midiRange(1f, 0f)

        beatCounter.update()
        drawOverlay()
        background(0f, 0f, 10f)

        if (millis() > nextRound) {
            nextRound = millis() + speedMillis
            universe.nextGeneration()
        }

        overlay.loadPixels()
        for (y in 0 until overlay.pixelHeight) {
            for (x in 0 until overlay.width) {
                if (brightness(overlay.pixels[x + (y * overlay.pixelWidth)]) > 0) {
                    universe.cells[y][x] = AliveCell
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
                        if (universe.cells[y][x] is AliveCell) 10f else 100f,
                        brightness
                )

                noStroke()
                fill(color)
                rect(x.toFloat() + (x * (cellSize - 1)), y.toFloat() + (y * (cellSize - 1)), cellSize.toFloat(), cellSize.toFloat())
            }
        }
    }

    override fun mouseClicked() {
        val x = mouseX / cellSize
        val y = mouseY / cellSize
        universe.cells[y][x] = AliveCell
    }

    private fun randomize(threshold: Float) {
        universe.cells.forEach {
            it.forEachIndexed { index, cell ->
                it[index] = if (random(1f) > constrain(threshold, 0f, 1f)) AliveCell else cell
            }
        }
    }
}