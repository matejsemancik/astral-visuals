package sketches

import processing.core.PApplet
import processing.core.PConstants
import processing.event.KeyEvent
import sketches.blank.BlankSketch
import sketches.polygonal.PolygonalSketch
import sketches.terrain.TerrainSketch
import tools.audio.AudioProcessor
import tools.galaxy.Galaxy

class SketchLoader : PApplet() {

    // region shared resources

    private lateinit var audioProcessor: AudioProcessor
    private val galaxy: Galaxy = Galaxy()

    // endregion

    lateinit var blankSketch: BaseSketch
    var selector = '1'
    val sketches = mutableMapOf<Char, BaseSketch>()

    override fun settings() {
        size(1280, 720, PConstants.P3D)
        smooth(4)
    }

    override fun setup() {
        colorMode(PConstants.HSB, 360f, 100f, 100f)

        galaxy.connect()
        audioProcessor = AudioProcessor(this)

        blankSketch = BlankSketch(this, audioProcessor, galaxy)

        sketches.apply {
            put('0', blankSketch)
            put('1', PolygonalSketch(this@SketchLoader, audioProcessor, galaxy))
            put('2', TerrainSketch(this@SketchLoader, audioProcessor, galaxy))
        }

        sketches.forEach { key, sketch ->
            sketch.setup()
        }

        activeSketch().onBecameActive()
    }

    override fun draw() {
        activeSketch().draw()
    }

    private fun activeSketch(): BaseSketch {
        return sketches.getOrDefault(selector, blankSketch)
    }

    override fun keyPressed(event: KeyEvent?) {
        event?.let {
            if (sketches.keys.contains(it.key)) {
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