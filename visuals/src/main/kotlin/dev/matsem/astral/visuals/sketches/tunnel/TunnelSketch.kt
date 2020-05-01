package dev.matsem.astral.visuals.sketches.tunnel

import dev.matsem.astral.core.tools.extensions.mapp
import dev.matsem.astral.core.tools.extensions.midiRange
import dev.matsem.astral.core.tools.extensions.remap
import dev.matsem.astral.core.tools.extensions.saw
import dev.matsem.astral.core.tools.extensions.shorterDimension
import dev.matsem.astral.core.tools.extensions.translateCenter
import dev.matsem.astral.visuals.sketches.BaseSketch
import dev.matsem.astral.visuals.sketches.SketchLoader
import dev.matsem.astral.visuals.tools.audio.AudioProcessor
import dev.matsem.astral.visuals.tools.automator.MidiAutomator
import dev.matsem.astral.visuals.tools.galaxy.Galaxy
import org.koin.core.inject
import processing.core.PApplet.cos
import processing.core.PApplet.lerp
import processing.core.PApplet.sin
import processing.core.PConstants.CLOSE
import processing.core.PConstants.TWO_PI

class TunnelSketch : BaseSketch() {

    override val sketch: SketchLoader by inject()
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

    private val hexagon = with(sketch) {
        ex.extrude(
            createShape().apply {
                val radius = shorterDimension() / 2f
                val angle = TWO_PI / 6

                beginShape()
                var a = 0f
                while (a < TWO_PI) {
                    val sx = cos(a) * radius
                    val sy = sin(a) * radius
                    vertex(sx, sy)
                    a += angle
                }
                endShape(CLOSE)
            },
            50,
            "box"
        )
    }

    override fun onBecameActive() = Unit

    override fun setup() = with(sketch) {
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
    private var stroke = 0f
    private var amplitude = 0f

    override fun draw() = with(sketch) {
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
            stroke = targetStroke * kickStrokeMultiplier
        }

        stroke = lerp(stroke, targetStroke, 0.1f)
        speed = lerp(speed, targetSpeed, 0.1f)

        background(bgColor)
        noFill()
        stroke(fgColor)
        strokeWeight(stroke)
        translateCenter()

        polygon(amplitude, 6)

        for (z in 200 downTo -2200 step step) {
            val stroke = z.remap(0f, -2000f, stroke, stroke / 4f)
            val realZ = z + saw(speed).mapp(0f, step.toFloat())
            if (random(0f, 1f) > flickerThresh) {
                strokeWeight(stroke)
                pushMatrix()
                translate(0f, 0f, realZ)
                drawHexagon()
                popMatrix()
            }
        }
    }

    private fun drawHexagon() = with(sketch) {
        hexagon.forEachIndexed { index, shape ->
            shape.disableStyle()
            shape(shape)
        }
    }

    private fun polygon(radius: Float, nPoints: Int) = with(sketch) {
        val angle = TWO_PI / nPoints
        beginShape()
        var a = 0f
        while (a < TWO_PI) {
            val sx = cos(a) * radius
            val sy = sin(a) * radius
            vertex(sx, sy)
            a += angle
        }
        endShape(CLOSE)
    }
}