package sketches.machina

import centerX
import centerY
import processing.core.PConstants
import processing.core.PImage
import sketches.BaseSketch
import sketches.SketchLoader
import tools.audio.AudioProcessor
import tools.galaxy.Galaxy

class MachinaSketch(
        sketch: SketchLoader,
        audioProcessor: AudioProcessor,
        galaxy: Galaxy
) : BaseSketch(
        sketch,
        audioProcessor,
        galaxy
) {

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