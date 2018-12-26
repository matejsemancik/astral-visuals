package sketches.boxes

import org.jbox2d.common.Vec2
import processing.core.PConstants
import shiffman.box2d.Box2DProcessing
import sketches.BaseSketch
import sketches.SketchLoader
import tools.audio.AudioProcessor
import tools.galaxy.Galaxy

class BoxesSketch(
        override val sketch: SketchLoader,
        val audioProcessor: AudioProcessor,
        val galaxy: Galaxy
) : BaseSketch(
        sketch,
        audioProcessor,
        galaxy
) {
    lateinit var boundary: Boundary
    val boxes = arrayListOf<Box>()
    val box2d = Box2DProcessing(sketch)

    override fun onBecameActive() {
        rectMode(PConstants.CORNER)
    }

    override fun setup() {
        box2d.createWorld(Vec2(0f, -9.81f * box2d.scaleFactor))
        boundary = Boundary(sketch, box2d)
    }

    override fun draw() {
        box2d.step()

        if (sketch.keyPressed && sketch.key == 'a') {
            boxes.add(
                    Box(sketch, box2d, mouseX.toFloat(), mouseY.toFloat()).apply {
                        color = accentColor
                        size = sketch.random(20f, 30f)
                    }
            )
        }

        background(bgColor)

        pushMatrix()
        noStroke()
        fill(fgColor)
        val w = width.toFloat() / audioProcessor.fft.avgSize()
        for (i in 0 until audioProcessor.fft.avgSize()) {
            val start = i * w
            val end = (i + 1) * w

            rect(i * w, height.toFloat(), w, -audioProcessor.fft.getAvg(i))
            boxes
                    .filter {
                        val pixelCoords = box2d.coordWorldToPixels(it.body.worldCenter)
                        return@filter (start..end).contains(pixelCoords.x)
                    }
                    .forEach {
                        val pixelCoords = box2d.coordWorldToPixels(it.body.worldCenter)
                        it.attract(pixelCoords.x, height - audioProcessor.fft.getAvg(i), 5000f)
//                        it.applyForce(Vec2(0f, audioProcessor.fft.getAvg(i)))
                    }
        }
        popMatrix()

        boxes.forEach {
            if (mousePressed) {
                it.attract(mouseX.toFloat(), mouseY.toFloat(), 1000f)
            }

            it.draw()
        }
    }
}