package dev.matsem.astral.visuals.sketches.attractor

import dev.matsem.astral.visuals.Config
import dev.matsem.astral.visuals.sketches.BaseSketch
import dev.matsem.astral.visuals.sketches.SketchLoader
import dev.matsem.astral.visuals.tools.audio.AudioProcessor
import dev.matsem.astral.visuals.tools.automator.MidiAutomator
import dev.matsem.astral.core.tools.extensions.*
import dev.matsem.astral.visuals.tools.galaxy.Galaxy
import dev.matsem.astral.visuals.tools.galaxy.controls.Pot
import dev.matsem.astral.visuals.tools.kontrol.KontrolF1
import org.koin.core.inject
import processing.core.PApplet.*
import processing.core.PConstants
import processing.core.PVector

class AttractorSketch : BaseSketch() {

    companion object {
        const val ITERATIONS_MAX = 50000
    }

    override val sketch: SketchLoader by inject()
    val audioProcessor: AudioProcessor by inject()
    val galaxy: Galaxy by inject()
    private val automator: MidiAutomator by inject()
    private val kontrol: KontrolF1 by inject()

    private var rightmost = Float.MIN_VALUE
    private var leftmost = Float.MAX_VALUE
    private var topmost = Float.MAX_VALUE
    private var bottommost = Float.MIN_VALUE
    private val deJongPoints = Array(ITERATIONS_MAX) { PVector() }

    private val iterationCountSlider = galaxy.createPot(5, 0, 100f, ITERATIONS_MAX.toFloat(), 25000f)
    private val scaleSlider = galaxy.createPot(5, 1, 1f, 600f, 250f)
    private val stretchSlider = galaxy.createPot(5, 6, 0.5f, 2f, 1f)
    private val stabilizeBtn = galaxy.createToggleButton(5, 7, true)
    private val beatBtn = galaxy.createToggleButton(5, 8, false)
    private val randomizeButton = galaxy.createPushButton(5, 21) {
        randomPots.forEach { it.random() }
    }

    private val randomPots = mutableListOf<Pot>()

    private val sliderA = galaxy.createPot(5, 2, -10f, 10f, 4f).also { randomPots.add(it) }
    private val sliderB = galaxy.createPot(5, 3, -2f, 2f, 0.54f)
    private val sliderC = galaxy.createPot(5, 4, -2f, 2f, 0.40f)
    private val sliderD = galaxy.createPot(5, 5, -10f, 10f, -2.43f).also { randomPots.add(it) }

    private val potA0 = galaxy.createPot(5, 9).also { randomPots.add(it) }
    private val potA1 = galaxy.createPot(5, 10).also { randomPots.add(it) }
    private val potA2 = galaxy.createPot(5, 11).also { randomPots.add(it) }

    private val potB0 = galaxy.createPot(5, 12).also { randomPots.add(it) }
    private val potB1 = galaxy.createPot(5, 13).also { randomPots.add(it) }
    private val potB2 = galaxy.createPot(5, 14).also { randomPots.add(it) }

    private val potC0 = galaxy.createPot(5, 15).also { randomPots.add(it) }
    private val potC1 = galaxy.createPot(5, 16).also { randomPots.add(it) }
    private val potC2 = galaxy.createPot(5, 17).also { randomPots.add(it) }

    private val potD0 = galaxy.createPot(5, 18).also { randomPots.add(it) }
    private val potD1 = galaxy.createPot(5, 19).also { randomPots.add(it) }
    private val potD2 = galaxy.createPot(5, 20).also { randomPots.add(it) }

    private var numBeats = 0
    private val beatDividerBtns = galaxy.createButtonGroup(5, (38..43).toList(), listOf(38))
    private val beatBtns = galaxy.createButtonGroup(5, (22..37).toList(), listOf())
    private val beatControls = listOf(
        sliderA, sliderB, sliderC, sliderD,
        potA0, potB0, potC0, potD0,
        potA1, potB1, potC1, potD1,
        potA2, potB2, potC2, potD2
    )

    private val dotSizePot = galaxy.createPot(5, 44, 1f, 5f, 1f)
    private val bgAlphaPot = galaxy.createPot(5, 49, 0f, Config.Color.ALPHA_MAX, Config.Color.ALPHA_MAX)

    override fun setup() {
        automator.setupWithGalaxy(
            channel = 5,
            recordButtonCC = 45,
            playButtonCC = 46,
            loopButtonCC = 47,
            clearButtonCC = 48,
            channelFilter = null
        )
    }

    override fun onBecameActive() = with(sketch) {
        rectMode(PConstants.CORNER)
    }

    override fun draw() = with(sketch) {
        automator.update()
        fill(bgColor, bgAlphaPot.value)
        noStroke()
        rect(0f, 0f, width.toFloat(), height.toFloat())
        strokeWeight(dotSizePot.value)

        if (audioProcessor.beatDetect.isKick) {
            numBeats++

            if (numBeats % pow(2f, beatDividerBtns.activeButtonsIndices().first().toFloat()).toInt() == 0) {
                beatControls.withIndex().forEach { controlWithIndex ->
                    if (beatBtns.activeButtonsIndices(exclusive = false).contains(controlWithIndex.index)) {
                        controlWithIndex.value.random()
                    }
                }
            }
        }

        val a = sliderA.value +
                saw(potA0.value.mapp(1 / 100f, 1 / 10f)).mapp(sliderA.min, sliderA.max) * potA1.value +
                (audioProcessor.getRange(20f..100f) / 100f) * potA2.value.mapp(0f, 2f)

        val b = sliderB.value +
                sin(angularTimeS(potB0.value.mapp(100f, 10f))).mapSin(sliderB.min, sliderB.max) * potB1.value +
                (audioProcessor.getRange(600f..800f) / 1000f) * potB2.value.mapp(0f, 2f)

        val c = sliderC.value +
                sin(angularTimeS(potC0.value.mapp(100f, 10f))).mapSin(sliderC.min, sliderC.max) * potC1.value +
                (audioProcessor.getRange(200f..600f) / 1000f) * potC2.value.mapp(0f, 2f)

        val d = sliderD.value +
                saw(potD0.value.mapp(1 / 100f, 1 / 10f)).mapp(sliderD.min, sliderD.max) * potD1.value +
                (audioProcessor.getRange(800f..1200f) / 100f) * potD2.value.mapp(0f, 2f)

        deJongAttractor(a, b, c, d)

        deJongPoints
            .withIndex()
            .filter { it.index < iterationCountSlider.value.toInt() }
            .map { pt -> PVector(pt.value.x * scaleSlider.value, pt.value.y * scaleSlider.value) }
            .forEach {
                if (it.x > rightmost) rightmost = it.x
                if (it.x < leftmost) leftmost = it.x
                if (it.y < topmost) topmost = it.y
                if (it.y > bottommost) bottommost = it.y
            }

        val deJongWidth = rightmost - leftmost
        val deJongHeight = bottommost - topmost

        translateCenter()
        stroke(fgColor)
        fill(fgColor)

        // DEBUG
        if (isInDebugMode) {
            line(rightmost, -height / 8f, rightmost, height / 8f)
            line(leftmost, -height / 8f, leftmost, height / 8f)
            line(-width / 8f, topmost, width / 8f, topmost)
            line(-width / 8f, bottommost, width / 8f, bottommost)
        }

        for (i in 0 until iterationCountSlider.value.toInt()) {
            val pt = deJongPoints[i]
            if (stabilizeBtn.isPressed) {
                point(
                    (pt.x * scaleSlider.value).remap(
                        leftmost,
                        rightmost,
                        -deJongWidth / 2f * stretchSlider.value,
                        deJongWidth / 2f * stretchSlider.value
                    ),
                    (pt.y * scaleSlider.value).remap(
                        topmost,
                        bottommost,
                        -deJongHeight / 2f,
                        deJongHeight / 2f
                    )
                )
            } else {
                point(pt.x * scaleSlider.value, pt.y * scaleSlider.value)
            }
        }

        rightmost = Float.MIN_VALUE
        leftmost = Float.MAX_VALUE
        topmost = Float.MAX_VALUE
        bottommost = Float.MIN_VALUE
    }

    private fun deJongAttractor(a: Float, b: Float, c: Float, d: Float) {
        var px = 0f
        var py = 0f

        for (i in 0 until iterationCountSlider.value.toInt()) {
            val x = sin(a * py) - cos(b * px)
            val y = sin(c * px) - cos(d * py)

            px = x
            py = y

            deJongPoints[i] = PVector(x, y)
        }
    }
}