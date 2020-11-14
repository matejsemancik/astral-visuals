package dev.matsem.astral.playground.sketches

import ch.bildspur.postfx.builder.PostFX
import ddf.minim.ugens.Oscil
import ddf.minim.ugens.Sink
import ddf.minim.ugens.Waves
import dev.matsem.astral.core.tools.audio.AudioProcessor
import dev.matsem.astral.core.tools.extensions.angularTimeS
import dev.matsem.astral.core.tools.extensions.colorModeHsb
import dev.matsem.astral.core.tools.extensions.mapSin
import dev.matsem.astral.core.tools.extensions.pushPop
import dev.matsem.astral.core.tools.extensions.remap
import dev.matsem.astral.core.tools.extensions.translateCenter
import dev.matsem.astral.core.tools.extensions.value
import dev.matsem.astral.core.tools.extensions.withAlpha
import dev.matsem.astral.core.tools.osc.OscHandler
import dev.matsem.astral.core.tools.osc.OscManager
import dev.matsem.astral.core.tools.shapes.ExtrusionCache
import org.koin.core.KoinComponent
import org.koin.core.inject
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PVector

class PlaygroundSketch : PApplet(), KoinComponent, OscHandler {

    private val ec: ExtrusionCache by inject()
    private val sink: Sink by inject()
    private val audioProcessor: AudioProcessor by inject()

    private lateinit var fx: PostFX
    private lateinit var oscil: Oscil
    override val oscManager: OscManager by lazy {
        OscManager(sketch = this, inputPort = 7001, outputIp = "192.168.1.11", outputPort = 7001)
    }

    val numX = 5
    val numY = 4
    val numZ = 5
    val depth = 1280f

    data class Rotator(
        val velocity: PVector,
        val speed: Float,
        val offsetX: Float,
        val offsetY: Float,
        val offsetZ: Float,
        val scale: Float
    )

    val rotators = Array(numZ) {
        Array(numX) {
            Array(numY) {
                Rotator(
                    velocity = PVector(random(1f), random(1f), random(1f)),
                    speed = random(-0.0002f, 0.0002f),
                    offsetX = random(-100f, 100f),
                    offsetY = random(-50f, 50f),
                    offsetZ = random(-20f, 20f),
                    scale = random(0.4f, 0.7f)
                )
            }
        }
    }

    override fun settings() {
        size(1280, 720, PConstants.P3D)
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
        val bgColor = 0x0f0f0f.withAlpha()
        val fgColor = 0xffffff.withAlpha()
        background(bgColor)
        fill(bgColor)
        stroke(fgColor)
        strokeWeight(random(2f, 3f))

        translateCenter()
        scale(oscil.value.mapSin(1.5f, 1f))
        rotateY(angularTimeS(60f))

        for (z in 0 until numZ) {
            for (x in 0 until numX) {
                for (y in 0 until numY) {
                    val rotator = rotators[z][x][y]
                    pushPop {
                        val centerZ = z * depth / numZ + rotator.offsetZ - depth / 2f
                        val centerX = x * width / numX + rotator.offsetX - width / 2f
                        val centerY = y * height / numY + rotator.offsetY - height / 2f
                        translate(
                            centerX + width / (numX * 2f),
                            centerY + height / (numY * 2f),
                            centerZ + depth / (numZ * 2f)
                        )
                        scale(rotator.scale)
                        rotate(
                            PConstants.PI * rotator.speed * millis(),
                            rotator.velocity.x,
                            rotator.velocity.y,
                            rotator.velocity.z
                        )
                        for (shape in ec.semLogo) {
                            shape.disableStyle()
                            shape(shape)
                        }
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
                noise(
                    audioProcessor.getRange(20f..100f).remap(0f, 50f, 0.05f, 0.08f),
                    0.4f
                )
                rgbSplit(20f)
            }.compose()
    }
}