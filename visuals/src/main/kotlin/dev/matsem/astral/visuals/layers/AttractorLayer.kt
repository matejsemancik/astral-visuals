package dev.matsem.astral.visuals.layers

import dev.matsem.astral.core.ColorConfig
import dev.matsem.astral.core.tools.audio.AudioProcessor
import dev.matsem.astral.core.tools.extensions.angularTimeS
import dev.matsem.astral.core.tools.extensions.mapSin
import dev.matsem.astral.core.tools.extensions.mapp
import dev.matsem.astral.core.tools.extensions.remap
import dev.matsem.astral.core.tools.extensions.saw
import dev.matsem.astral.core.tools.extensions.translateCenter
import dev.matsem.astral.core.tools.osc.OscHandler
import dev.matsem.astral.core.tools.osc.OscManager
import dev.matsem.astral.core.tools.osc.oscFader
import dev.matsem.astral.core.tools.osc.oscPushButton
import dev.matsem.astral.core.tools.osc.oscToggleButton
import dev.matsem.astral.visuals.ColorHandler
import dev.matsem.astral.visuals.Colorizer
import dev.matsem.astral.visuals.Layer
import org.koin.core.KoinComponent
import org.koin.core.inject
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PGraphics
import processing.core.PVector

class AttractorLayer : Layer(), KoinComponent, OscHandler, ColorHandler {

    companion object {
        const val ITERATIONS_MAX = 50000
    }

    override val parent: PApplet by inject()
    override val oscManager: OscManager by inject()
    override val colorizer: Colorizer by inject()
    val audioProcessor: AudioProcessor by inject()

    private val iterationCount by oscFader("/attractor/iterations", defaultValue = 0.5f)
    private val scale by oscFader("/attractor/scale", defaultValue = 0.4f)
    private val stretchX by oscFader("/attractor/stretchX", defaultValue = 0.5f)
    private val stabilize by oscToggleButton("/attractor/stabilize", defaultValue = true)
    private val dotSize by oscFader("/attractor/dotSize", defaultValue = 0f)
    private val bgAlpha by oscFader("/attractor/bgAlpha", defaultValue = 1f)
    private val debug by oscToggleButton("/attractor/debug", defaultValue = true)

    private var sliderA by oscFader("/attractor/a/value", defaultValue = 0.8f)
    private var potA0 by oscFader("/attractor/a/speed", defaultValue = 0f)
    private var potA1 by oscFader("/attractor/a/amt", defaultValue = 0f)
    private var potA2 by oscFader("/attractor/a/audio", defaultValue = 0f)

    private var sliderB by oscFader("/attractor/b/value", defaultValue = 0.5f)
    private var potB0 by oscFader("/attractor/b/speed", defaultValue = 0f)
    private var potB1 by oscFader("/attractor/b/amt", defaultValue = 0f)
    private var potB2 by oscFader("/attractor/b/audio", defaultValue = 0f)

    private var sliderC by oscFader("/attractor/c/value", defaultValue = 0.6f)
    private var potC0 by oscFader("/attractor/c/speed", defaultValue = 0f)
    private var potC1 by oscFader("/attractor/c/amt", defaultValue = 0f)
    private var potC2 by oscFader("/attractor/c/audio", defaultValue = 0f)

    private var sliderD by oscFader("/attractor/d/value", defaultValue = 0.4f)
    private var potD0 by oscFader("/attractor/d/speed", defaultValue = 0f)
    private var potD1 by oscFader("/attractor/d/amt", defaultValue = 0f)
    private var potD2 by oscFader("/attractor/d/audio", defaultValue = 0f)

    private var sliderABeat by oscToggleButton("/attractor/a/value/beat", defaultValue = false)
    private var potA0Beat by oscToggleButton("/attractor/a/speed/beat", defaultValue = false)
    private var potA1Beat by oscToggleButton("/attractor/a/amt/beat", defaultValue = false)
    private var potA2Beat by oscToggleButton("/attractor/a/audio/beat", defaultValue = false)

    private var sliderBBeat by oscToggleButton("/attractor/b/value/beat", defaultValue = false)
    private var potB0Beat by oscToggleButton("/attractor/b/speed/beat", defaultValue = false)
    private var potB1Beat by oscToggleButton("/attractor/b/amt/beat", defaultValue = false)
    private var potB2Beat by oscToggleButton("/attractor/b/audio/beat", defaultValue = false)

    private var sliderCBeat by oscToggleButton("/attractor/c/value/beat", defaultValue = false)
    private var potC0Beat by oscToggleButton("/attractor/c/speed/beat", defaultValue = false)
    private var potC1Beat by oscToggleButton("/attractor/c/amt/beat", defaultValue = false)
    private var potC2Beat by oscToggleButton("/attractor/c/audio/beat", defaultValue = false)

    private var sliderDBeat by oscToggleButton("/attractor/d/value/beat", defaultValue = false)
    private var potD0Beat by oscToggleButton("/attractor/d/speed/beat", defaultValue = false)
    private var potD1Beat by oscToggleButton("/attractor/d/amt/beat", defaultValue = false)
    private var potD2Beat by oscToggleButton("/attractor/d/audio/beat", defaultValue = false)

    private val randomizeTrigger by oscPushButton("/attractor/randomize") {
        sliderA = parent.random(0f, 1f)
        potA0 = parent.random(0f, 1f)
        potA1 = parent.random(0f, 1f)
        potA2 = parent.random(0f, 1f)
        potB0 = parent.random(0f, 1f)
        potB1 = parent.random(0f, 1f)
        potB2 = parent.random(0f, 1f)
        potC0 = parent.random(0f, 1f)
        potC1 = parent.random(0f, 1f)
        potC2 = parent.random(0f, 1f)
        sliderD = parent.random(0f, 1f)
        potD0 = parent.random(0f, 1f)
        potD1 = parent.random(0f, 1f)
        potD2 = parent.random(0f, 1f)
    }

    private val beatDivide1 by oscToggleButton("/attractor/beatDivide/1/1", defaultValue = true)
    private val beatDivide2 by oscToggleButton("/attractor/beatDivide/1/2", defaultValue = false)
    private val beatDivide4 by oscToggleButton("/attractor/beatDivide/1/3", defaultValue = false)
    private val beatDivide8 by oscToggleButton("/attractor/beatDivide/1/4", defaultValue = false)
    private val beatDivide16 by oscToggleButton("/attractor/beatDivide/1/5", defaultValue = false)
    private val beatDivide32 by oscToggleButton("/attractor/beatDivide/1/6", defaultValue = false)

    private var rightmost = Float.MIN_VALUE
    private var leftmost = Float.MAX_VALUE
    private var topmost = Float.MAX_VALUE
    private var bottommost = Float.MIN_VALUE
    private val deJongPoints = Array(ITERATIONS_MAX) { PVector() }
    private var numBeats = 0

    private fun deJongAttractor(a: Float, b: Float, c: Float, d: Float) {
        var px = 0f
        var py = 0f

        for (i in 0 until iterationCount.mapp(0f, ITERATIONS_MAX.toFloat()).toInt()) {
            val x = PApplet.sin(a * py) - PApplet.cos(b * px)
            val y = PApplet.sin(c * px) - PApplet.cos(d * py)

            px = x
            py = y

            deJongPoints[i] = PVector(x, y)
        }
    }

    override fun PGraphics.draw() {
        rectMode(PConstants.CORNER)
        fill(bgColor, bgAlpha.mapp(0f, ColorConfig.ALPHA_MAX))
        noStroke()
        rect(0f, 0f, width.toFloat(), height.toFloat())
        strokeWeight(dotSize.mapp(1f, 5f))

        if (audioProcessor.beatDetect.isKick) {
            numBeats++

            val modulo = when {
                beatDivide1 -> 1
                beatDivide2 -> 2
                beatDivide4 -> 4
                beatDivide8 -> 8
                beatDivide16 -> 16
                beatDivide32 -> 32
                else -> 1
            }

            if (numBeats % modulo == 0) {
                if (sliderABeat) {
                    sliderA = parent.random(0f, 1f)
                }
                if (potA0Beat) {
                    potA0 = parent.random(0f, 1f)
                }
                if (potA1Beat) {
                    potA1 = parent.random(0f, 1f)
                }
                if (potA2Beat) {
                    potA2 = parent.random(0f, 1f)
                }

                if (sliderBBeat) {
                    sliderB = parent.random(0f, 1f)
                }
                if (potB0Beat) {
                    potB0 = parent.random(0f, 1f)
                }
                if (potB1Beat) {
                    potB1 = parent.random(0f, 1f)
                }
                if (potB2Beat) {
                    potB2 = parent.random(0f, 1f)
                }

                if (sliderCBeat) {
                    sliderC = parent.random(0f, 1f)
                }
                if (potC0Beat) {
                    potC0 = parent.random(0f, 1f)
                }
                if (potC1Beat) {
                    potC1 = parent.random(0f, 1f)
                }
                if (potC2Beat) {
                    potC2 = parent.random(0f, 1f)
                }

                if (sliderDBeat) {
                    sliderD = parent.random(0f, 1f)
                }
                if (potD0Beat) {
                    potD0 = parent.random(0f, 1f)
                }
                if (potD1Beat) {
                    potD1 = parent.random(0f, 1f)
                }
                if (potD2Beat) {
                    potD2 = parent.random(0f, 1f)
                }
            }
        }

        val a = sliderA.mapp(-10f, 10f) +
                parent.saw(potA0.mapp(1 / 100f, 1 / 10f)).mapp(-10f, 10f) * potA1 +
                (audioProcessor.getRange(20f..100f) / 100f) * potA2.mapp(0f, 2f)

        val b = sliderB.mapp(-2f, 2f) +
                PApplet.sin(parent.angularTimeS(potB0.mapp(100f, 10f)))
                    .mapSin(-2f, 2f) * potB1 +
                (audioProcessor.getRange(600f..800f) / 1000f) * potB2.mapp(0f, 2f)

        val c = sliderC.mapp(-2f, 2f) +
                PApplet.sin(parent.angularTimeS(potC0.mapp(100f, 10f)))
                    .mapSin(-2f, 2f) * potC1 +
                (audioProcessor.getRange(200f..600f) / 1000f) * potC2.mapp(0f, 2f)

        val d = sliderD.mapp(-10f, 10f) +
                parent.saw(potD0.mapp(1 / 100f, 1 / 10f)).mapp(-10f, 10f) * potD1 +
                (audioProcessor.getRange(800f..1200f) / 100f) * potD2.mapp(0f, 2f)

        deJongAttractor(a, b, c, d)

        deJongPoints
            .withIndex()
            .filter { it.index < iterationCount.mapp(0f, ITERATIONS_MAX.toFloat()).toInt() }
            .map { pt -> PVector(pt.value.x * scale.mapp(1f, 600f), pt.value.y * scale.mapp(1f, 600f)) }
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

        if (debug) {
            line(rightmost, -height / 8f, rightmost, height / 8f)
            line(leftmost, -height / 8f, leftmost, height / 8f)
            line(-width / 8f, topmost, width / 8f, topmost)
            line(-width / 8f, bottommost, width / 8f, bottommost)
        }

        for (i in 0 until iterationCount.mapp(0f, ITERATIONS_MAX.toFloat()).toInt()) {
            val pt = deJongPoints[i]
            if (stabilize) {
                point(
                    (pt.x * scale.mapp(1f, 600f)).remap(
                        leftmost,
                        rightmost,
                        -deJongWidth / 2f * stretchX.mapp(0.5f, 2f),
                        deJongWidth / 2f * stretchX.mapp(0.5f, 2f)
                    ),
                    (pt.y * scale.mapp(1f, 600f)).remap(
                        topmost,
                        bottommost,
                        -deJongHeight / 2f,
                        deJongHeight / 2f
                    )
                )
            } else {
                point(pt.x * scale.mapp(1f, 600f), pt.y * scale.mapp(1f, 600f))
            }
        }

        rightmost = Float.MIN_VALUE
        leftmost = Float.MAX_VALUE
        topmost = Float.MAX_VALUE
        bottommost = Float.MIN_VALUE
    }
}