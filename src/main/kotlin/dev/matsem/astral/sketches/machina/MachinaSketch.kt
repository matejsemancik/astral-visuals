package dev.matsem.astral.sketches.machina

import dev.matsem.astral.sketches.BaseSketch
import dev.matsem.astral.sketches.SketchLoader
import dev.matsem.astral.tools.extensions.centerX
import dev.matsem.astral.tools.extensions.centerY
import org.koin.core.inject
import processing.core.PConstants
import processing.core.PImage

class MachinaSketch : BaseSketch() {

    override val sketch: SketchLoader by inject()

    lateinit var semlogo: PImage
    val scale = 80
    val widthRatio = 0.1f

    override fun onBecameActive() {
        rectMode(PConstants.CENTER)
        sketch.imageMode(PConstants.CENTER)
    }

    override fun setup() {
        semlogo = sketch.loadImage("semlogo.png")
    }

    override fun draw() {
        background(bgColor)

        pushMatrix()
        translate(centerX(), centerY())
        sketch.tint(accentColor)
        sketch.image(semlogo, 0f, 0f, semlogo.pixelWidth / 2f, semlogo.pixelHeight / 2f)
        popMatrix()

        pushMatrix()
        translate(centerX(), centerY())
        rotateZ(millis() * PConstants.TWO_PI / (1000f * 32f))
        noStroke()
        fill(fgColor)

        for (i in -width until width step width / scale) {
            rect(
                    i.toFloat(),
                    0f,
                    width / scale * widthRatio,
                    height.toFloat() * 2f
            )
        }
        popMatrix()
    }

}