package dev.matsem.astral.visuals.layers

import dev.matsem.astral.core.ColorConfig
import dev.matsem.astral.core.Files
import dev.matsem.astral.core.tools.audio.beatcounter.BeatCounter
import dev.matsem.astral.core.tools.audio.beatcounter.OnKick
import dev.matsem.astral.core.tools.extensions.colorModeHsb
import dev.matsem.astral.core.tools.extensions.draw
import dev.matsem.astral.core.tools.extensions.midiRange
import dev.matsem.astral.core.tools.extensions.remap
import dev.matsem.astral.core.tools.extensions.resizeRatioAware
import dev.matsem.astral.core.tools.extensions.shorterDimension
import dev.matsem.astral.core.tools.extensions.translateCenter
import dev.matsem.astral.core.tools.galaxy.Galaxy
import dev.matsem.astral.core.tools.gameoflife.AliveCell
import dev.matsem.astral.core.tools.gameoflife.DeadCell
import dev.matsem.astral.core.tools.gameoflife.Universe
import dev.matsem.astral.core.tools.midi.MidiAutomator
import dev.matsem.astral.visuals.ColorHandler
import dev.matsem.astral.visuals.Colorizer
import dev.matsem.astral.visuals.Layer
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import processing.core.PApplet
import processing.core.PApplet.constrain
import processing.core.PConstants
import processing.core.PFont
import processing.core.PGraphics
import processing.core.PImage

class ConwayLayer : Layer(), KoinComponent, ColorHandler {

    override val parent: PApplet by inject()
    override val colorizer: Colorizer by inject()

    private val galaxy: Galaxy by inject()
    private val beatCounter: BeatCounter by inject()
    private val automator: MidiAutomator by inject()

    private var cellSize = 5
    private var nextRound = 0

    val universe = Universe(
        Array(canvas.height / cellSize) {
            Array(canvas.width / cellSize) { DeadCell }
        }
    )

    val overlay: PGraphics = parent.createGraphics(universe.width, universe.height, PConstants.P2D)
    val pixelFont: PFont = parent.createFont(Files.Font.FFF_FORWARD, 24f, false)

    private val semLogo: PImage = parent.loadImage("images/semlogo.png").apply {
        resizeRatioAware(width = overlay.shorterDimension() / 2)
    }

    private var overlayText: String? = null
    private var overlayImage: PImage? = null

    private var targetZoom = 1f
    private var actualZoom = 1f

    private val heatMapButton = galaxy.createToggleButton(channel = 11, cc = 4, defaultValue = false)
    private val heatHueStartPot =
        galaxy.createPot(channel = 11, cc = 5, min = 0f, max = ColorConfig.HUE_MAX, initialValue = 221.1f)
    private val heatHueEndPot =
        galaxy.createPot(channel = 11, cc = 6, min = 0f, max = ColorConfig.HUE_MAX, initialValue = 277.9f)
    private val heatCoolingFactorPot =
        galaxy.createPot(channel = 11, cc = 7, min = 0.1f, max = 0.99f, initialValue = 0.10f)

    private val randomizeThresholdSlider =
        galaxy.createPot(channel = 11, cc = 8, min = 0f, max = 1f, initialValue = 0.5f)
    private val simulationIntervalPot =
        galaxy.createPot(channel = 11, cc = 9, min = 40f, max = 120f, initialValue = 60f)
    private val randomizeButton = galaxy.createPushButton(channel = 11, cc = 10) {
        randomize(randomizeThresholdSlider.value)
    }

    private val heatMapSaturationSlider =
        galaxy.createPot(channel = 11, cc = 11, min = 0f, max = 100f, initialValue = 0f)
    private val outlineEnabledButton = galaxy.createToggleButton(channel = 11, cc = 12, defaultValue = true)

    private val overlayButtons = galaxy.createButtonGroup(11, listOf(13, 14, 15, 16, 17, 18, 19, 20, 21), listOf())
    private val overlaySizeSlider = galaxy.createPot(channel = 11, cc = 22, initialValue = 1f)

    init {
        automator.setupWithGalaxy(
            channel = 11,
            recordButtonCC = 0,
            playButtonCC = 1,
            loopButtonCC = 2,
            clearButtonCC = 3,
            channelFilter = null
        )

        beatCounter.addListener(OnKick, 1) {
            randomize(0.995f)
        }

        beatCounter.addListener(OnKick, 4) {
            targetZoom = parent.random(1f, 1.2f)
        }
    }

    /**
     * Draws cells into universe.
     * All white pixels will be alive cells.
     * All black pixels will be dead cells.
     * All other pixel values are ignored.
     */
    private fun drawOverlay() = with(overlay) {
        draw {
            background(128f)

            overlayText = when (overlayButtons.activeButtonsIndices(exclusive = false).firstOrNull()) {
                0 -> "ATTEMPT"
                1 -> "JOHNEY"
                2 -> "WZ"
                3 -> "MATSEM"
                4 -> "ROUGH:\nRESULT"
                5 -> "S B U"
                6 -> "DANIEL\nWEIRDONE"
                7 -> "SYMBOL LP"
                else -> null
            }

            overlayImage = when (overlayButtons.activeButtonsIndices(exclusive = false).firstOrNull()) {
                8 -> semLogo
                else -> null
            }

            overlayText?.let { text ->
                textFont(pixelFont)
                textAlign(PConstants.CENTER, PConstants.BOTTOM)
                textSize(overlaySizeSlider.rawValue.midiRange(12f, 32f))

                // Text stroke hack
                if (outlineEnabledButton.isPressed) {
                    for (xOff in -1..1) {
                        for (yOff in -1..1) {
                            fill(0f)
                            text(text, width / 2f + xOff, height * 0.8f + yOff)
                        }
                    }
                }

                fill(255f)
                text(text, width / 2f, height * 0.8f)
            }

            overlayImage?.let { image ->
                pushMatrix()
                translateCenter()
                scale(overlaySizeSlider.rawValue.midiRange(0.8f, 1.4f))
                image(image, -image.width / 2f, -image.height / 2f)
                popMatrix()
            }
        }
    }

    override fun PGraphics.draw() {
        clear()
        colorModeHsb()
        automator.update()
        rectMode(PApplet.CORNER)

        drawOverlay()
        translateCenter()
        actualZoom = PApplet.lerp(actualZoom, targetZoom, 0.20f)
        scale(actualZoom)

        if (parent.millis() > nextRound) {
            nextRound = parent.millis() + simulationIntervalPot.value.toInt()
            universe.coolingFactor = heatCoolingFactorPot.value
            universe.nextGeneration()
        }

        overlay.loadPixels()
        for (y in 0 until overlay.pixelHeight) {
            for (x in 0 until overlay.width) {
                when (brightness(overlay.pixels[x + (y * overlay.pixelWidth)])) {
                    100f -> universe.cells[y][x] = AliveCell
                    0f -> universe.cells[y][x] = DeadCell
                }
            }
        }

        for (y in 0 until universe.height) {
            for (x in 0 until universe.width) {

                if (heatMapButton.isPressed) {
                    val color = color(
                        universe.heatMap[y][x].remap(1f, 0f, heatHueStartPot.value, heatHueEndPot.value),
                        if (universe.cells[y][x] is AliveCell) 10f else heatMapSaturationSlider.value,
                        universe.heatMap[y][x].remap(0f, 1f, 10f, 100f)
                    )

                    noStroke()
                    fill(color)
                    rect(
                        x.toFloat() + (x * (cellSize - 1)) - width / 2f,
                        y.toFloat() + (y * (cellSize - 1)) - height / 2f,
                        cellSize.toFloat(),
                        cellSize.toFloat()
                    )
                } else {
                    if (universe.cells[y][x] is AliveCell) {
                        noStroke()
                        fill(fgColor)
                        rect(
                            x.toFloat() + (x * (cellSize - 1)) - width / 2f,
                            y.toFloat() + (y * (cellSize - 1)) - height / 2f,
                            cellSize.toFloat(),
                            cellSize.toFloat()
                        )
                    }
                }
            }
        }
    }

    private fun randomize(threshold: Float) {
        universe.cells.forEach {
            it.forEachIndexed { index, cell ->
                it[index] = if (parent.random(1f) > constrain(threshold, 0f, 1f)) AliveCell else cell
            }
        }
    }
}