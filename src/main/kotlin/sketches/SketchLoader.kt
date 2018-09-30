package sketches

import processing.core.PApplet
import processing.core.PConstants
import processing.event.KeyEvent
import sketches.blank.BlankSketch

class SketchLoader : PApplet() {

    private var selector = '0'
    private lateinit var selectors: Map<Char, BaseSketch>

    lateinit var blankSketch: BlankSketch

    override fun settings() {
        size(1280, 720, PConstants.P3D)
        smooth(4)
    }

    override fun setup() {
        blankSketch = BlankSketch(this)

        blankSketch.setup()

        selectors = mapOf(
                '0' to blankSketch
                )

        activeSketch().onBecameActive()
    }

    override fun draw() {
        activeSketch().draw()
    }

    private fun activeSketch(): BaseSketch {
        return selectors.getOrDefault(selector, blankSketch)
    }

    override fun keyPressed(event: KeyEvent?) {
        event?.let {
            if (selectors.keys.contains(it.key)) {
                selector = it.key
                activeSketch().onBecameActive()
            } else {
                activeSketch().keyPressed(event)
            }
        }
    }
}