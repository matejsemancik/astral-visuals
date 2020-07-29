package dev.matsem.astral.playground.sketches

import ch.bildspur.postfx.builder.PostFX
import dev.matsem.astral.core.tools.extensions.colorModeHsb
import dev.matsem.astral.core.tools.extensions.pushPop
import dev.matsem.astral.core.tools.extensions.withAlpha
import dev.matsem.astral.core.tools.kontrol.KontrolF1
import dev.matsem.astral.core.tools.shapes.ExtrusionCache
import org.koin.core.KoinComponent
import org.koin.core.inject
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PVector
import processing.event.MouseEvent

/**
 * Thing from http://piterpasma.nl/articles/rotating
 */
class PlaygroundSketch : PApplet(), KoinComponent {

    private val kontrol: KontrolF1 by inject()
    private val ec: ExtrusionCache by inject()
    private lateinit var fx: PostFX

    val numX = 4
    val numY = 4

    override fun settings() {
        size(720, 720, PConstants.P3D)
    }

    override fun setup() {
        fx = PostFX(this)
        colorModeHsb()
        kontrol.connect()
        ortho()
        noLoop()
    }

    override fun draw() {
        val bgColor = 0x0f0f0f.withAlpha()
//        val fgColor = 0xebab34.withAlpha()
        val fgColor = 0xffffff.withAlpha()
        background(bgColor)
        fill(bgColor)
        stroke(fgColor)
        strokeWeight(random(2f, 3f))

        for (x in 0 until width step width / numX) {
            for (y in 0 until height step height / numY) {
                pushPop {
                    val randomOffsetX = random(-1f, 1f)
                    val randomOffsetY = random(-1f, 1f)
                    translate(
                        x.toFloat() + width / (numX * 2) + randomOffsetX,
                        y.toFloat() + height / (numY * 2 + randomOffsetY)
                    )
                    scale(random(0.6f, 0.65f))
                    rotate(random(PConstants.PI * 2f), random(1f), random(1f), random(1f))
                    for (shape in ec.semLogo) {
                        shape.disableStyle()
                        shape(shape)
                    }
                }
            }
        }

        fx.render()
            .bloom(0.5f, 20, 40f)
            .rgbSplit(random(50f))
            .compose()

//        saveFrame("data/output/semlogo-###.png")
    }

    override fun mouseClicked(event: MouseEvent?) {
        when(event?.button) {
            PConstants.LEFT -> redraw()
        }
    }
}