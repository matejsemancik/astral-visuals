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

        boxes.forEach {
            it.draw()
        }
    }

    override fun mousePressed() {
        boxes.add(
                Box(sketch, mouseX.toFloat(), mouseY.toFloat()).apply {
                    color = accentColor
                }
        )
    }
}