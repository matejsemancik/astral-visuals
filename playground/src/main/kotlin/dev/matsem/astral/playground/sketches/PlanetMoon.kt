package dev.matsem.astral.playground.sketches

import dev.matsem.astral.core.tools.extensions.colorModeHsb
import dev.matsem.astral.core.tools.extensions.draw
import dev.matsem.astral.core.tools.extensions.mapSin
import dev.matsem.astral.core.tools.extensions.pixelAt
import dev.matsem.astral.core.tools.extensions.pushPop
import dev.matsem.astral.core.tools.extensions.shorterDimension
import dev.matsem.astral.core.tools.extensions.translateCenter
import dev.matsem.astral.core.tools.extensions.withAlpha
import org.koin.core.component.KoinComponent
import processing.core.PApplet

class PlanetMoon : PApplet(), KoinComponent {

    private val resolution = 8
    private val characters = " .:-=+*#%@"
    private val step = 100f / characters.count() // Brightness step between LUT entries

    // Create lookup table (LUT) that contains a pairs of
    // IntRange (luminance) to Char (representing luminance)
    private val lut = characters
        .mapIndexed { index, char ->
            (0 + index * step..(step * index + step).coerceAtMost(100f)) to char
        }

    private val canvas by lazy { createGraphics(width / resolution, height / resolution, P3D) }
    private val colorStart by lazy { color(0f, 0f, 100f) }
    private val colorEnd by lazy { color(0f, 0f, 80f) }

    private val starfield by lazy {
        Array(canvas.height) {
            Array(canvas.width) {
                if (random(1f) > 0.95f) {
                    random(0f, 1f)
                } else {
                    null
                }
            }
        }
    }

    override fun settings() {
        size(1280, 720, P2D)
    }

    override fun setup() {
        colorModeHsb()
        surface.setTitle("ASCII Playground")
        surface.setResizable(true)
        frameRate(60f)
    }

    private fun drawCanvas() = canvas.draw {
        clear()
        ortho()
        colorModeHsb()
        directionalLight(
            0f,
            0f,
            100f,
            sin(frameCount / 600f),
            cos(frameCount / 400f),
            sin(frameCount / 300f).mapSin(-1f, 0f)
        )

        // Stars
        pushPop {
            translate(0f, 0f)
            translate((frameCount / 800f) % 10f, 0f)
            for (y in 0 until height) {
                for (x in 0 until width) {
                    val diameter = starfield[y][x]
                    diameter?.let {
                        noFill()
                        stroke(0xffffff.withAlpha())
                        strokeWeight(it)
                        point(x.toFloat(), y.toFloat())
                    }
                }
            }
        }

        // Planet
        pushPop {
            translateCenter()
            fill(0xffffff.withAlpha())
            noStroke()
            sphere(shorterDimension() / 4f)
        }

        // Moon
        pushPop {
            translateCenter()
            translate(
                sin(frameCount.toFloat() / 400f - PI / 2f) * shorterDimension() / 3f,
                sin(frameCount.toFloat() / 400f - PI / 2f) * shorterDimension() / 10f,
                cos(frameCount.toFloat() / 400f - PI / 2f) * shorterDimension() / 3f
            )
            fill(0xffffff.withAlpha())
            noStroke()
            sphere(shorterDimension() / 12f)
        }
    }

    override fun draw() {
        drawCanvas()
        pushPop {
            background(0f, 0f, 10f)
            textSize(resolution.toFloat())
            noStroke()

            canvas.loadPixels()

            for (y in 0 until canvas.height) {
                for (x in 0 until canvas.width) {
                    val bri = brightness(canvas.pixelAt(x, y))
                    val textColor = lerpColor(colorEnd, colorStart, bri / 100f)
                    val ascii = lut.first { it.first.contains(bri) }.second
                    fill(textColor)
                    text(ascii, x.toFloat() * resolution, y.toFloat() * resolution)
                }
            }
        }
    }
}