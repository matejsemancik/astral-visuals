package sketches.boxes

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
    val boxes = arrayListOf<Box>()

    override fun setup() {

    }

    override fun onBecameActive() {

    }

    override fun draw() {
        background(bgColor)
        noFill()
        stroke(accentColor)
        strokeWeight(3f)

        boxes.forEach {
            pushMatrix()
            translate(it.x, it.y)
            sketch.box(it.size)
            popMatrix()
        }
    }

    override fun mouseClicked() {
        boxes.add(Box(mouseX.toFloat(), mouseY.toFloat()))
    }
}