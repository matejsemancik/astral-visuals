package sketches

import processing.core.PApplet
import processing.core.PConstants
import processing.event.KeyEvent
import sketches.blank.BlankSketch
import sketches.polygonal.PolygonalSketch
import sketches.terrain.TerrainSketch
import tools.audio.AudioProcessor
import tools.galaxy.Galaxy
import tools.galaxy.controls.Pot
import tools.galaxy.controls.PushButton
import tools.galaxy.controls.ToggleButton

class SketchLoader : PApplet() {

    // region shared resources

    private lateinit var audioProcessor: AudioProcessor
    private val galaxy: Galaxy = Galaxy()
    private lateinit var debugButton: ToggleButton
    private lateinit var gainPot: Pot

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

        gainPot = galaxy.createPot(15, 64, 0f, 5f, 1f)
        debugButton = galaxy.createToggleButton(15, 65, true)

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

        PushButton(galaxy.midiBus, 15, 1) { switchSketch('1') }
        PushButton(galaxy.midiBus, 15, 2) { switchSketch('2') }
    }

    override fun draw() {
        audioProcessor.gain = gainPot.value
        activeSketch().isInDebugMode = debugButton.isPressed
        activeSketch().draw()
    }

    private fun activeSketch(): BaseSketch {
        return sketches.getOrDefault(selector, blankSketch)
    }

    private fun switchSketch(num: Char) {
        selector = num
        activeSketch().onBecameActive()
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