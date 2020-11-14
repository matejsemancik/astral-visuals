package dev.matsem.astral.playground.sketches

import ch.bildspur.postfx.builder.PostFX
import dev.matsem.astral.core.tools.audio.AudioProcessor
import dev.matsem.astral.core.tools.extensions.angularTimeS
import dev.matsem.astral.core.tools.extensions.colorModeHsb
import dev.matsem.astral.core.tools.extensions.draw
import dev.matsem.astral.core.tools.extensions.pixelAt
import dev.matsem.astral.core.tools.extensions.pushPop
import dev.matsem.astral.core.tools.extensions.remap
import dev.matsem.astral.core.tools.extensions.translateCenter
import dev.matsem.astral.core.tools.extensions.withAlpha
import dev.matsem.astral.core.tools.osc.OscHandler
import dev.matsem.astral.core.tools.osc.OscManager
import dev.matsem.astral.core.tools.shapes.ExtrusionCache
import org.koin.core.KoinComponent
import org.koin.core.inject
import processing.core.PApplet
import processing.core.PConstants

class Proximity : PApplet(), KoinComponent, OscHandler {

    override val oscManager: OscManager by inject()
    private val extrusionCache: ExtrusionCache by inject()
    private val audioProcessor: AudioProcessor by inject()
    private lateinit var fx: PostFX

    private val resolution = 4
    private val characters = " .:-=+*#%@"
    private val step = 100f / characters.count() // Brightness step between LUT entries
    // Create lookup table (LUT) that contains a pairs of
    // IntRange (luminance) to Char (representing luminance)
    private val lut = characters
        .mapIndexed { index, char ->
            (0 + index * step..(step * index + step).coerceAtMost(100f)) to char
        }
    private val canvas by lazy { createGraphics(width / resolution, height / resolution, P3D) }

    override fun settings() {
        size(1280, 720, PConstants.P3D)
    }

    override fun setup() {
        colorModeHsb()
        fx = PostFX(this)
    }

    override fun draw() = drawApplet()

    private fun PApplet.drawApplet() {
        drawCanvas()
        image(canvas, 0f, 0f)

        // Draws canvas with ASCII effect
        pushPop {
            background(0f, 0f, 10f)
            textSize(resolution.toFloat())
            noStroke()

            canvas.loadPixels()

            for (y in 0 until canvas.height) {
                for (x in 0 until canvas.width) {
                    val bri = brightness(canvas.pixelAt(x, y))
                    val textColor = 0xffffff.withAlpha()
                    val ascii = lut.first { it.first.contains(bri) }.second
                    fill(textColor)
                    text(ascii, x.toFloat() * resolution, y.toFloat() * resolution)
                }
            }
        }

        fx.render()
            .noise(
                audioProcessor.getRange(20f..100f).remap(0f, 50f, 0.05f, 0.1f),
                0.4f)
            .rgbSplit(20f)
            .compose()
    }

    // Draws raw content onto canvas
    private fun drawCanvas() = canvas.draw {
        clear()
        colorModeHsb()

        background(0)
        fill(0xffffff.withAlpha())
        strokeWeight(2f)
        stroke(0x000000.withAlpha())

        lights()
        translateCenter()
        rotateY(angularTimeS(12f))
        pushPop {
            scale(1f / resolution)
            scale(
                audioProcessor.getRange(80f..100f).remap(0f, 50f, 1f, 2f).coerceIn(1f, 2f)
            )
            pushPop {
                translate(0f, 0f, 50f)
                extrusionCache.getText("JOHNEY", 50).forEach {
                    shape(it)
                }
            }
            pushPop {
                translate(0f, 0f, -50f)
                scale(-1f)
                extrusionCache.semLogo.forEach {
                    shape(it)
                }
            }
        }
    }
}