package dev.matsem.astral.playground.sketches

import ch.bildspur.postfx.builder.PostFX
import ddf.minim.ugens.Oscil
import ddf.minim.ugens.Sink
import ddf.minim.ugens.Waves
import dev.matsem.astral.core.tools.extensions.*
import dev.matsem.astral.core.tools.osc.*
import dev.matsem.astral.core.tools.shapes.ExtrusionCache
import org.koin.core.KoinComponent
import org.koin.core.inject
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PVector
import processing.event.MouseEvent

class PlaygroundSketch : PApplet(), KoinComponent, OscHandler {

    private val ec: ExtrusionCache by inject()
    private val sink: Sink by inject()

    private lateinit var fx: PostFX
    private lateinit var oscil: Oscil
    override val oscManager: OscManager by lazy {
        OscManager(sketch = this, inputPort = 7001, outputIp = "192.168.1.11", outputPort = 7001)
    }

    private var fader1: Float by oscFader("/1/fader1", defaultValue = 0.5f)
    private var fader2: Float by oscFader("/1/fader2")
    private var knob1: Float by oscKnob("/1/rotary1")
    private var knob2: Float by oscKnob("/1/rotary2", defaultValue = 0.5f)
    private var toggle1: Boolean by oscToggleButton("/1/toggle1", defaultValue = false)
    private val push1: Boolean by oscPushButton("/1/push1") {
        println("Trigger!")
    }
    private var xy1: PVector by oscXyPad("/1/xy1", defaultValue = PVector(0.5f, 0.5f))
    private var led1: Float by oscLedIndicator("/1/led1")
    private var label1: String by oscLabelIndicator("/1/label1")
    private val encoder1: Float by oscEncoder(
        "/1/encoder1",
        defaultValue = 100f,
        increment = 1f,
        cw = { println("-> $it") },
        ccw = { println("<- $it") })

    val numX = 5
    val numY = 7

    data class Rotator(
        val velocity: PVector,
        val speed: Float,
        val offsetX: Float,
        val offsetY: Float,
        val scale: Float
    )

    val rotators = Array(numX) {
        Array(numY) {
            Rotator(
                velocity = PVector(random(1f), random(1f), random(1f)),
                speed = random(-0.0002f, 0.0002f),
                offsetX = random(-20f, 20f),
                offsetY = random(-20f, 20f),
                scale = random(0.4f, 0.7f)
            )
        }
    }

    override fun settings() {
        size(720, 1280, PConstants.P3D)
    }

    override fun setup() {
        surface.setResizable(true)
        fx = PostFX(this)
        colorModeHsb()
        ortho()
        frameRate(30f)

        oscil = Oscil(1 / 15f, 1f, Waves.SAW).apply { patch(sink) }
    }

    override fun draw() {
        led1 = fader2
        label1 = fader2.toString().take(5)

        val bgColor = 0x0f0f0f.withAlpha()
        val fgColor = 0xfca503.withAlpha()
        background(bgColor)
        fill(bgColor)
        stroke(fgColor)
        strokeWeight(random(2f, 3f))

        translateCenter()
        scale(oscil.value.mapSin(1f, 0.9f))

        for (x in 0 until numX) {
            for (y in 0 until numY) {
                pushPop {
                    val centerX = x * width / numX + rotators[x][y].offsetX - width / 2f
                    val centerY = y * height / numY + rotators[x][y].offsetY - height / 2f
                    translate(
                        centerX + width / (numX * 2f),
                        centerY + height / (numY * 2f)
                    )
                    scale(rotators[x][y].scale)
                    rotate(
                        PConstants.PI * rotators[x][y].speed * millis(),
                        rotators[x][y].velocity.x,
                        rotators[x][y].velocity.y,
                        rotators[x][y].velocity.z
                    )
                    for (shape in ec.semLogo) {
                        shape.disableStyle()
                        shape(shape)
                    }
                }
            }
        }

        fx.render()
            .apply {
                bloom(0.5f, 40, 40f)
                if (frameCount % 120 in (0..10)) {
                    rgbSplit(random(50f))
                }
            }.compose()
    }

    override fun mouseClicked(event: MouseEvent?) {
        when (event?.button) {
            PConstants.LEFT -> redraw()
        }
    }
}