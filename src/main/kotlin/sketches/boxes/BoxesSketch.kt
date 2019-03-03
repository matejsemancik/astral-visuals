package sketches.boxes

import angularVelocity
import centerX
import centerY
import org.jbox2d.common.Vec2
import processing.core.PApplet
import processing.core.PApplet.radians
import processing.core.PApplet.sin
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
        rotateX(PApplet.map(sin(angularVelocity(30f)), -1f, 1f, radians(-180f), radians(180f)))
        staticSphere.draw()
        bodies.withIndex().forEach {
            when {
                mousePressed -> it.value.attract(mouseX.toFloat() - width / 2f, mouseY.toFloat() - height / 2f, 20000f)
                sketch.keyPressed && sketch.key == 'f' -> it.value.boostOrbit(12000f)
                audioProcessor.beatDetect.isKick -> it.value.attract(staticSphere.x, staticSphere.y, 10000f)
                else -> if (it.index % 2 == 0) it.value.boostOrbit(audioProcessor.getRange(20f..60f) * 100f)
            }

            it.value.attract(staticSphere.x, staticSphere.y, 1000f)

            it.value.accentColor = accentColor
            it.value.fgColor = fgColor
            it.value.draw()
        }

        popMatrix()
        popMatrix()

        box2d.step()
    }
}