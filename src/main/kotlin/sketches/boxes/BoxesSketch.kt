package sketches.boxes

import centerX
import centerY
import org.jbox2d.common.Vec2
import processing.core.PConstants
import shiffman.box2d.Box2DProcessing
import shorterDimension
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
    lateinit var staticSphere: StaticSphere
    private val bodies = arrayListOf<DynamicBody>()
    val box2d = Box2DProcessing(sketch)

    override fun onBecameActive() {
        rectMode(PConstants.CENTER)
        sketch.ellipseMode(PConstants.RADIUS)
        sphereDetail(8)
    }

    override fun setup() {
        box2d.createWorld(Vec2(0f, 0f))
        box2d.setContinuousPhysics(true)
//        boundary = Boundary(sketch, box2d)
        staticSphere = StaticSphere(sketch, box2d, centerX(), centerY()).apply {
            color = accentColor
            radius = shorterDimension() / 4f
        }
    }

    var amp = 0f

    override fun draw() {
        if (sketch.keyPressed) {
            when (sketch.key) {
                'b' -> {
                    bodies.add(
                            Box(sketch, box2d, mouseX.toFloat(), mouseY.toFloat()).apply {
                                color = accentColor
                                size = sketch.random(15f, 25f)
                            }
                    )
                }
            }
        }

        amp += audioProcessor.getRange((20f..40f)) * 0.6f
        amp *= 0.45f
        staticSphere.radius = 100f + amp
        staticSphere.color = accentColor

        background(bgColor)
        staticSphere.draw()

        bodies.forEach {
            if (mousePressed) {
                it.attract(mouseX.toFloat(), mouseY.toFloat(), 20000f)
            }

            it.attract(staticSphere.x, staticSphere.y, 4000f)

            it.color = accentColor
            it.draw()
        }

        box2d.step()
    }
}