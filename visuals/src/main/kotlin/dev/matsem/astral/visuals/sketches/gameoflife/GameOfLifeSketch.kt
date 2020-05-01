package dev.matsem.astral.visuals.sketches.gameoflife

import dev.matsem.astral.core.tools.ColorConfig
import dev.matsem.astral.core.tools.extensions.midiRange
import dev.matsem.astral.core.tools.extensions.remap
import dev.matsem.astral.core.tools.extensions.resizeRatioAware
import dev.matsem.astral.core.tools.extensions.shorterDimension
import dev.matsem.astral.core.tools.extensions.translateCenter
import dev.matsem.astral.visuals.Config
import dev.matsem.astral.visuals.Files
import dev.matsem.astral.visuals.sketches.BaseSketch
import dev.matsem.astral.visuals.sketches.SketchLoader
import dev.matsem.astral.visuals.tools.audio.beatcounter.BeatCounter
import dev.matsem.astral.visuals.tools.audio.beatcounter.OnKick
import dev.matsem.astral.visuals.tools.automator.MidiAutomator
import dev.matsem.astral.visuals.tools.galaxy.Galaxy
import org.koin.core.inject
import processing.core.PApplet.constrain
import processing.core.PApplet.lerp
import processing.core.PConstants
import processing.core.PConstants.CENTER
import processing.core.PFont
import processing.core.PGraphics
import processing.core.PImage

// ideas:
// - color inversion
// - optimization: do not create new array for each new universe generation
class GameOfLifeSketch : BaseSketch() {

    override val sketch: SketchLoader by inject()

    private val galaxy: Galaxy by inject()
    private val beatCounter: BeatCounter by inject()
    private val automator: MidiAutomator by inject()

    private var cellSize = 5
    private var nextRound = 0

    lateinit var universe: Universe
    lateinit var overlay: PGraphics
    lateinit var pixelFont: PFont

    private lateinit var semLogo: PImage
    private lateinit var astralLogo: PImage

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

    override fun onBecameActive() = with(sketch) {
        rectMode(PConstants.CORNER)
    }

    override fun setup() = with(sketch) {
        automator.setupWithGalaxy(
            channel = 11,
            recordButtonCC = 0,
            playButtonCC = 1,
            loopButtonCC = 2,
            clearButtonCC = 3,
            channelFilter = null
        )

        universe = Universe(
            Array(height / cellSize) {
                Array<Cell>(width / cellSize) { DeadCell }
            }
        )
        pixelFont = createFont(Files.Font.FFF_FORWARD, 24f, false)
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

        overlayText = when (overlayButtons.activeButtonsIndices(exclusive = false).firstOrNull()) {
            0 -> "ATTEMPT"
            1 -> "JOHNEY"
            2 -> "KID\nKODAMA"
            3 -> "MATSEM"
            4 -> "ROUGH:\nRESULT"
            5 -> "SBU"
            6 -> "SEBA"
            else -> null
        }

        overlayImage = when (overlayButtons.activeButtonsIndices(exclusive = false).firstOrNull()) {
            7 -> semLogo
            8 -> astralLogo
            else -> null
        }

        overlayText?.let { text ->
            textFont(pixelFont)
            textAlign(CENTER, CENTER)
            textSize(overlaySizeSlider.rawValue.midiRange(12f, 24f))

            // Text stroke hack
            if (outlineEnabledButton.isPressed) {
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
            pushMatrix()
            translateCenter()
            scale(overlaySizeSlider.rawValue.midiRange(0.8f, 1.4f))
            image(image, -image.width / 2f, -image.height / 2f)
            popMatrix()
        }

        endDraw()
    }

    override fun draw() = with(sketch) {
        automator.update()
        beatCounter.update()

        drawOverlay()
        background(0f, 0f, 10f)
        translateCenter()
        actualZoom = lerp(actualZoom, targetZoom, 0.20f)
        scale(actualZoom)

        if (millis() > nextRound) {
            nextRound = millis() + simulationIntervalPot.value.toInt()
            universe.coolingFactor = heatCoolingFactorPot.value
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

                val brightness = if (heatMapButton.isPressed) {
                    universe.heatMap[y][x].remap(0f, 1f, 10f, 100f)
                } else {
                    if (universe.cells[y][x] is AliveCell) 100f else 0f
                }

                val color = if (heatMapButton.isPressed) {
                    color(
                        universe.heatMap[y][x].remap(1f, 0f, heatHueStartPot.value, heatHueEndPot.value),
                        if (universe.cells[y][x] is AliveCell) 10f else heatMapSaturationSlider.value,
                        brightness
                    )
                } else {
                    if (universe.cells[y][x] is AliveCell) {
                        fgColor
                    } else {
                        bgColor
                    }
                }

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