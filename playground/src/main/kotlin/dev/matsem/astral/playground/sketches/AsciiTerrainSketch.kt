package dev.matsem.astral.playground.sketches

import ch.bildspur.postfx.builder.PostFX
import ddf.minim.ugens.Oscil
import ddf.minim.ugens.Sink
import ddf.minim.ugens.Waves
import dev.matsem.astral.core.tools.extensions.colorModeHsb
import dev.matsem.astral.core.tools.extensions.mapSin
import dev.matsem.astral.core.tools.extensions.remap
import dev.matsem.astral.core.tools.extensions.value
import org.koin.core.KoinComponent
import org.koin.core.inject
import processing.core.PApplet
import processing.core.PConstants

class AsciiTerrainSketch : PApplet(), KoinComponent {

    private val resolution = 8
    private val charPool = "SOULEXMACHINASYMBOL".toList()

    private val sink: Sink by inject()
    private val oscil1 by lazy { Oscil(1 / 5f, 1f, Waves.SINE).apply { patch(sink) } }
    private val oscil2 by lazy { Oscil(1 / 10f, 1f, Waves.SINE).apply { patch(sink) } }

    private val postFx: PostFX by lazy {
        PostFX(this)
    }

    override fun settings() {
        size(1920, 1080, PConstants.P2D)
    }

    override fun setup() {
        colorModeHsb()
        surface.setTitle("ASCII Playground")
        surface.setResizable(true)
    }

    override fun draw() {
        if (frameCount % 4 > 0) {
            return
        }
        background(0)
        textSize(resolution.toFloat())
//        val mouseX1 = map(mouseX.toFloat(), 0f, width.toFloat(), 0f, 1f)
//        val mouseY1 = map(mouseY.toFloat(), 0f, height.toFloat(), 0f, 1f)
        val mouseX1 = oscil2.value.mapSin(0.3f, 0.5f)
        val mouseY1 = oscil1.value.mapSin(0f, 1f)

        for (y in 0 until height step resolution) {
            for (x in 0 until width step resolution) {
                val char = if (random(0.2f, 0.8f) > mouseX1) {
                    val index = mouseY1.remap(0f, 1f, 0f, charPool.count().toFloat()).toInt()
                    charPool[index]
                } else {
                    charPool.random()
                }

                noStroke()
                fill(0f, 0f, 80f)

                val z = millis() / 1000f
                if (noise(x / 100f, y / 100f - millis() / 1000f, z) <= mouseX1) {
                    text(char, x.toFloat(), y.toFloat())
                }
            }
        }

        postFx.render()
            .rgbSplit(random(50f))
            .bloom(0.2f, 10, 0.2f)
            .compose()
    }
}