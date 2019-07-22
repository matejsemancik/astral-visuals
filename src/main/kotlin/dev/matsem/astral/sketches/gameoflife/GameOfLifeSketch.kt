package dev.matsem.astral.sketches.gameoflife

import dev.matsem.astral.remap
import dev.matsem.astral.tools.audio.beatcounter.BeatCounter
import dev.matsem.astral.tools.audio.beatcounter.OnKick
import dev.matsem.astral.tools.kontrol.KontrolF1
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

    override fun settings() {
        size(1280, 720, PConstants.P3D)
    }

    override fun setup() {
        kontrol.connect()

        colorMode(PConstants.HSB, 360f, 100f, 100f)
        rectMode(PConstants.CORNER)

        universe = Universe(Array(height / cellSize) { Array<Cell>(width / cellSize) { DeadCell } })

        beatCounter.addListener(OnKick, 2) {
            randomize(0.999f)
        }

        pixelFont = createFont("fonts/fff-forward.ttf", 24f, false)
        stamp = createGraphics(universe.width, universe.height, PConstants.P2D)
        stamp.beginDraw()
        stamp.noStroke()
        stamp.background(0f)
        stamp.fill(255f)
        stamp.textFont(pixelFont)
        stamp.textSize(24f)
        val w = stamp.textWidth("astral")
        stamp.text("astral", stamp.width / 2 - w / 2, stamp.height / 2 - 24 / 2f)
        stamp.endDraw()
    }

    override fun draw() {
        beatCounter.update()
        background(0f, 0f, 10f)

        if (millis() > nextRound) {
            nextRound = millis() + delayMillis
            universe.nextGeneration()
        }

        if (mousePressed) {
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
                val color = color(
                        universe.heatMap[y][x].remap(0f, 1f, 128f, 0f),
                        if (universe.cells[y][x] is AliveCell) 10f else 100f,
                        universe.heatMap[y][x].remap(0f, 1f, 10f, 100f)
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