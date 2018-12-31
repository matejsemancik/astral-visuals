package sketches.boxes

import angularVelocity
import centerX
import centerY
import org.jbox2d.common.Vec2
import processing.core.PApplet
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
    lateinit var staticSphere: StaticSphere
    private val bodies = arrayListOf<Box>()
    val box2d = Box2DProcessing(sketch)

    override fun onBecameActive() {
        rectMode(PConstants.CENTER)
        sketch.ellipseMode(PConstants.RADIUS)
        sphereDetail(8)
    }

    override fun setup() {
        box2d.createWorld(Vec2(0f, 0f))
        box2d.setContinuousPhysics(true)
        staticSphere = StaticSphere(sketch, box2d, 0f, 0f).apply {
            fgColor = this@BoxesSketch.fgColor
            accentColor = this@BoxesSketch.accentColor
            radius = shorterDimension() / 4f
        }
    }

    var amp = 0f

    override fun draw() {
        if (sketch.keyPressed) {
            when (sketch.key) {
                'b' -> {
                    bodies.add(
                            Box(sketch, box2d, mouseX.toFloat() - width / 2f, mouseY.toFloat() - height / 2f).apply {
                                accentColor = this@BoxesSketch.accentColor
                                fgColor = this@BoxesSketch.fgColor
                                size = sketch.random(15f, 30f)
                            }
                    )
                }
            }
        }

        amp += audioProcessor.getRange((200f..300f)) * 1f
        amp *= 0.80f
        staticSphere.apply {
            radius = 100f + amp
            accentColor = this@BoxesSketch.accentColor
            fgColor = this@BoxesSketch.fgColor
        }

        background(bgColor)

        pushMatrix()
        translate(centerX(), centerY())
        rotateY(angularVelocity(16f))
        pushMatrix()
        rotateX(PApplet.radians(60f))
        staticSphere.draw()
        bodies.forEach {
            when {
                mousePressed -> it.attract(mouseX.toFloat() - width / 2f, mouseY.toFloat() - height / 2f, 20000f)
                sketch.keyPressed && sketch.key == 'f' -> it.boostOrbit(12000f)
                audioProcessor.beatDetect.isKick -> it.boostOrbit(12000f)
                else -> it.attract(staticSphere.x, staticSphere.y, 1000f)
            }

            it.accentColor = accentColor
            it.fgColor = fgColor
            it.draw()
        }

        popMatrix()
        popMatrix()

        box2d.step()
    }
}