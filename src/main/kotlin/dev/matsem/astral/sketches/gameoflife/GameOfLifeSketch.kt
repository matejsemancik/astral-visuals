package dev.matsem.astral.sketches.gameoflife

import dev.matsem.astral.remap
import dev.matsem.astral.tools.audio.beatcounter.BeatCounter
import dev.matsem.astral.tools.audio.beatcounter.OnKick
import dev.matsem.astral.tools.kontrol.KontrolF1
import org.koin.core.KoinComponent
import org.koin.core.inject
import processing.core.PApplet
import processing.core.PConstants

class GameOfLifeSketch : PApplet(), KoinComponent {

    private val kontrol: KontrolF1 by inject()
    private val beatCounter: BeatCounter by inject()

    private var cellSize = 5

    private var delayMillis = 60
    private var nextRound = 0

    lateinit var universe: Universe

    override fun settings() {
        size(1280, 720, PConstants.P3D)
    }

    override fun setup() {
        kontrol.connect()

        colorMode(PConstants.HSB, 360f, 100f, 100f)
        rectMode(PConstants.CORNER)

        universe = Universe(Array(height / cellSize) { Array<Cell>(width / cellSize) { DeadCell } })

        beatCounter.addListener(OnKick, 4) {
            randomize(0.999f)
        }
    }

    override fun draw() {
        beatCounter.update()
        background(0f, 0f, 10f)

        if (millis() > nextRound) {
            nextRound = millis() + delayMillis
            universe.nextGeneration()
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