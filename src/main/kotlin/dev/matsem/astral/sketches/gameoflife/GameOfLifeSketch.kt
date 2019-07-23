package dev.matsem.astral.sketches.gameoflife

import dev.matsem.astral.midiRange
import dev.matsem.astral.remap
import dev.matsem.astral.tools.audio.beatcounter.BeatCounter
import dev.matsem.astral.tools.audio.beatcounter.OnKick
import dev.matsem.astral.tools.audio.beatcounter.OnSnare
import dev.matsem.astral.tools.kontrol.KontrolF1
import dev.matsem.astral.tools.kontrol.Pad
import org.koin.core.KoinComponent
import org.koin.core.inject
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PFont
import processing.core.PGraphics

class GameOfLifeSketch : PApplet(), KoinComponent {

    private val kontrol: KontrolF1 by inject()
    private val beatCounter: BeatCounter by inject()

    private var cellSize = 5

    private var delayMillis = 60
    private var nextRound = 0

    lateinit var universe: Universe
    lateinit var stamp: PGraphics
    lateinit var pixelFont: PFont

    private var stampCount: Int = 0

    private var hueStart: Float = 160f
    private var hueEnd: Float = 220f
    private var heatMapEnabled: Boolean = false

    override fun settings() {
        size(1280, 720, PConstants.P3D)
    }

    override fun setup() {
        kontrol.connect()

        kontrol.pad(0, 0).apply {
            colorOn = Triple(64, 127, 127)
            colorOff = Triple(64, 127, 0)
            mode = Pad.Mode.TOGGLE
            setStateListener { heatMapEnabled = it }
        }

        colorMode(PConstants.HSB, 360f, 100f, 100f)
        rectMode(PConstants.CORNER)

        universe = Universe(Array(height / cellSize) { Array<Cell>(width / cellSize) { DeadCell } })

        beatCounter.addListener(OnKick, 16) {
            stampCount = 0
        }

        beatCounter.addListener(OnSnare, 8) {
            randomize(0.990f)
        }

        pixelFont = createFont("fonts/fff-forward.ttf", 24f, false)
        val text = "astral"
        stamp = createGraphics(universe.width, universe.height, PConstants.P2D)
        stamp.beginDraw()
        stamp.noStroke()
        stamp.background(0f)
        stamp.fill(255f)
        stamp.textFont(pixelFont)
        stamp.textAlign(CENTER, CENTER)
        stamp.textSize(24f)
        stamp.text(text, stamp.width / 2f, stamp.height / 2 - 24 / 2f)
        stamp.endDraw()
    }

    override fun draw() {
        hueStart = kontrol.knob1.midiRange(0f, 360f)
        hueEnd = kontrol.knob2.midiRange(0f, 360f)

        beatCounter.update()
        background(0f, 0f, 10f)

        if (millis() > nextRound) {
            nextRound = millis() + delayMillis
            universe.nextGeneration()
            stampCount++
        }

        if (stampCount < 20) {
            stamp.loadPixels()
            for (y in 0 until stamp.pixelHeight) {
                for (x in 0 until stamp.width) {
                    if (brightness(stamp.pixels[x + (y * stamp.pixelWidth)]) > 0) {
                        universe.cells[y][x] = AliveCell
                    }
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
        randomize(0.990f)
    }

    private fun randomize(threshold: Float) {
        universe.cells.forEach {
            it.forEachIndexed { index, cell ->
                it[index] = if (random(1f) > constrain(threshold, 0f, 1f)) AliveCell else cell
            }
        }
    }
}