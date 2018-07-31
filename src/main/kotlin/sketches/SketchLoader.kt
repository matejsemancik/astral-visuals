package sketches

import processing.core.PApplet
import processing.core.PConstants
import processing.event.KeyEvent
import sketches.test.BlankSketch
import sketches.test.BlueSketch
import sketches.test.GreenSketch
import sketches.test.TestSketch

class SketchLoader : PApplet() {

    private var selector = '0'
    private lateinit var selectors: Map<Char, BaseSketch>

    lateinit var greenSketch: GreenSketch
    lateinit var blueSketch: BlueSketch
    lateinit var testSketch: TestSketch
    lateinit var blankSketch: BlankSketch

    override fun settings() {
        size(1280, 720, PConstants.P3D)
        smooth(4)
    }

    override fun setup() {
        blankSketch = BlankSketch(this)
        greenSketch = GreenSketch(this)
        blueSketch = BlueSketch(this)
        testSketch = TestSketch(this)

        blankSketch.setup()
        greenSketch.setup()
        blueSketch.setup()
        testSketch.setup()

        selectors = mapOf(
                '0' to blankSketch,
                '1' to greenSketch,
                '2' to blueSketch,
                '3' to testSketch
        )
    }

    override fun draw() {
        activeSketch().draw()
    }

    private fun activeSketch(): BaseSketch {
        return selectors.getOrDefault(selector, testSketch)
    }

    override fun keyPressed(event: KeyEvent?) {
        event?.let {
            if (selectors.keys.contains(it.key)) {
                selector = it.key
            } else {
                activeSketch().keyPressed(event)
            }
        }
    }
}