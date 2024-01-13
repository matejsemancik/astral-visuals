package dev.matsem.astral.visuals.layers

import dev.matsem.astral.core.tools.audio.AudioProcessor
import dev.matsem.astral.core.tools.extensions.colorModeHsb
import dev.matsem.astral.core.tools.extensions.mapp
import dev.matsem.astral.core.tools.extensions.midiRange
import dev.matsem.astral.core.tools.extensions.remap
import dev.matsem.astral.core.tools.extensions.saw
import dev.matsem.astral.core.tools.extensions.shorterDimension
import dev.matsem.astral.core.tools.extensions.translateCenter
import dev.matsem.astral.core.tools.galaxy.Galaxy
import dev.matsem.astral.core.tools.midi.MidiAutomator
import dev.matsem.astral.visuals.ColorHandler
import dev.matsem.astral.visuals.Colorizer
import dev.matsem.astral.visuals.Layer
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PGraphics

class HexLayer : Layer(), KoinComponent, ColorHandler {
    override val parent: PApplet by inject()
    override val colorizer: Colorizer by inject()

    private val audioProcessor: AudioProcessor by inject()
    private val ex: extruder.extruder by inject()
    private val automator: MidiAutomator by inject()
    private val galaxy: Galaxy by inject()

    private val slider1 = galaxy.createPot(channel = 13, cc = 4)
    private val slider2 = galaxy.createPot(channel = 13, cc = 5)
    private val slider3 = galaxy.createPot(channel = 13, cc = 6)
    private val slider4 = galaxy.createPot(channel = 13, cc = 7)
    private val slider5 = galaxy.createPot(channel = 13, cc = 8)
    private val slider6 = galaxy.createPot(channel = 13, cc = 9)
    private val slider7 = galaxy.createPot(channel = 13, cc = 10)
    private val slider8 = galaxy.createPot(channel = 13, cc = 11)

    private val hexagon = with(canvas) {
        ex.extrude(
            createShape().apply {
                val radius = shorterDimension() / 2f
                val angle = PConstants.TWO_PI / 6

                beginShape()
                var a = 0f
                while (a < PConstants.TWO_PI) {
                    val sx = PApplet.cos(a) * radius
                    val sy = PApplet.sin(a) * radius
                    vertex(sx, sy)
                    a += angle
                }
                endShape(PConstants.CLOSE)
            },
            50,
            "box"
        )
    }

    init {
        automator.setupWithGalaxy(
            channel = 13,
            recordButtonCC = 0,
            playButtonCC = 1,
            loopButtonCC = 2,
            clearButtonCC = 3,
            channelFilter = null
        )
    }

    private var speed: Float = 0f
    private var strokew = 0f
    private var amplitude = 0f

    override fun PGraphics.draw() {
        colorModeHsb()
        clear()
        automator.update()

        amplitude += audioProcessor.getRange(100f..500f)
        amplitude *= 0.5f

        val targetStroke = slider1.rawValue.midiRange(1f, 10f)
        val targetSpeed = slider2.rawValue.midiRange(1 / 10f, 4f)
        val stepAplitudeMultiplier = slider6.rawValue.midiRange(0f, 1f)
        val step = slider3.rawValue.midiRange(100f, 400f).toInt() +
                amplitude.remap(0f, 100f, stepAplitudeMultiplier * -50f, stepAplitudeMultiplier * 50f).toInt()
        val flickerThresh = slider4.rawValue.midiRange(0f, 1f)
        val kickStrokeMultiplier = slider5.rawValue.midiRange(0f, 4f)

        if (audioProcessor.beatDetect.isKick) {
            strokew = targetStroke * kickStrokeMultiplier
        }

        strokew = PApplet.lerp(strokew, targetStroke, 0.1f)
        speed = PApplet.lerp(speed, targetSpeed, 0.1f)

        noFill()
        stroke(fgColor)
        strokeWeight(this@HexLayer.strokew)
        translateCenter()
        rotateZ(parent.millis() * slider7.value.mapp(-0.001f, 0.001f))

        polygon(amplitude, 6)

        for (z in 200 downTo -2200 step step) {
            val stroke = z.remap(0f, -2000f, strokew, strokew / 4f)
            val realZ = z + parent.saw(speed).mapp(0f, step.toFloat())
            if (parent.random(0f, 1f) > flickerThresh) {
                strokeWeight(stroke)
                pushMatrix()
                translate(0f, 0f, realZ)
                drawHexagon()
                popMatrix()
            }
        }
    }

    private fun drawHexagon() = with(canvas) {
        hexagon.forEachIndexed { index, shape ->
            shape.disableStyle()
            shape(shape)
        }
    }

    private fun polygon(radius: Float, nPoints: Int) = with(canvas) {
        val angle = PConstants.TWO_PI / nPoints
        beginShape()
        var a = 0f
        while (a < PConstants.TWO_PI) {
            val sx = PApplet.cos(a) * radius
            val sy = PApplet.sin(a) * radius
            vertex(sx, sy)
            a += angle
        }
        endShape(PConstants.CLOSE)
    }
}