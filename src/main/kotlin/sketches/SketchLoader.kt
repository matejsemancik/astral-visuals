package sketches

import processing.core.PApplet
import processing.core.PConstants
import processing.event.KeyEvent
import sketches.blank.BlankSketch
import sketches.polygonal.PolygonalSketch
import tools.audio.AudioProcessor
import tools.galaxy.Galaxy

class SketchLoader : PApplet() {

    private var selector = '0'
    private lateinit var selectors: Map<Char, BaseSketch>

    // region sketches

    lateinit var blankSketch: BlankSketch
    lateinit var polygonalSketch: PolygonalSketch

    // endregion

    // region shared resources

    private lateinit var audioProcessor: AudioProcessor
    private val galaxy: Galaxy = Galaxy()

    // endregion

    override fun settings() {
        size(1280, 720, PConstants.P3D)
        smooth(4)
    }

    override fun setup() {
        colorMode(PConstants.HSB, 360f, 100f, 100f)

        galaxy.connect()
        audioProcessor = AudioProcessor(this)

        blankSketch = BlankSketch(this, audioProcessor, galaxy)
        polygonalSketch = PolygonalSketch(this, audioProcessor, galaxy)

        blankSketch.setup()
        polygonalSketch.setup()

        selectors = mapOf(
                '0' to blankSketch,
                '1' to polygonalSketch
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

    override fun mouseClicked() {
        activeSketch().mouseClicked()
    }
}