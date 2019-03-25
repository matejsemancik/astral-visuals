package dev.matsem.astral.sketches.boxes

import dev.matsem.astral.*
import dev.matsem.astral.sketches.BaseSketch
import dev.matsem.astral.sketches.SketchLoader
import dev.matsem.astral.sketches.polygonal.star.Starfield
import dev.matsem.astral.tools.audio.AudioProcessor
import dev.matsem.astral.tools.galaxy.Galaxy
import org.jbox2d.common.Vec2
import org.koin.core.inject
import processing.core.PApplet
import processing.core.PApplet.radians
import processing.core.PConstants
import shiffman.box2d.Box2DProcessing

class BoxesSketch : BaseSketch() {

    override val sketch: SketchLoader by inject()
    private val audioProcessor: AudioProcessor by inject()
    private val galaxy: Galaxy by inject()

    var starMotion = Starfield.Motion.ZOOMING

    val starSpeedPot = galaxy.createPot(6, 0, 0.5f, 5f, 0.5f).lerp(0.1f)
    val starCountPot = galaxy.createPot(6, 1, 0f, 1200f, 100f).lerp(0.005f)
    val starfieldRotationPot = galaxy.createPot(6, 2, 0f, 3f, 1f)
    val starBoostPot = galaxy.createPot(6, 3, 0f, 1f, 0.4f)
    val rotationZPot = galaxy.createPot(6, 8, -1f, 1f, 0f)
    val rotationZResetButton = galaxy.createPushButton(6, 9) {
        rotationZPot.reset()
    }

    val motionZoomBtn = galaxy.createPushButton(6, 4) { starMotion = Starfield.Motion.ZOOMING }
    val motionTranslateBWDBtn = galaxy.createPushButton(6, 5) { starMotion = Starfield.Motion.TRANSLATING_BACKWARD }
    val motionTranslateFWDBtn = galaxy.createPushButton(6, 6) { starMotion = Starfield.Motion.TRANSLATING_FORWARD }

    val addBoxBtn = galaxy.createToggleButton(6, 11)
    val removeBoxBtn = galaxy.createToggleButton(6, 10)

    val starfield1 = Starfield(sketch, 300).apply { motion = starMotion }
    val starfield2 = Starfield(sketch, 300).apply { motion = starMotion }

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
    var bassSum = 0f
    var starVz = 0f
    var starRotationZ = 0f

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

                'd' -> {
                    if (bodies.isNotEmpty()) {
                        val randomBox = bodies.random()
                        randomBox.destroy()
                        bodies.remove(randomBox)
                    }
                }
            }
        }

        if (addBoxBtn.isPressed) {
            bodies.add(
                    Box(sketch, box2d, random(-centerX(), centerX()), random(-centerY(), centerY())).apply {
                        accentColor = this@BoxesSketch.accentColor
                        fgColor = this@BoxesSketch.fgColor
                        size = sketch.random(15f, 30f)
                    }
            )
        } else if (removeBoxBtn.isPressed && bodies.isNotEmpty()) {
            val randomBox = bodies.random()
            randomBox.destroy()
            bodies.remove(randomBox)
        }

        amp += audioProcessor.getRange((200f..300f)) * 1f
        amp *= 0.80f

        bassSum += audioProcessor.getRange(0f..50f)
        bassSum *= 0.2f

        starVz += rotationZPot.value.threshold(0.05f) * 0.15f
        starVz *= 0.95f
        starRotationZ += starVz

        staticSphere.apply {
            radius = 100f + amp
            accentColor = this@BoxesSketch.accentColor
            fgColor = this@BoxesSketch.fgColor
        }

        background(bgColor)

        // Starfields
        starfield1.rotate(PApplet.map(bassSum, 0f, 50f, 0f, 0.04f * starfieldRotationPot.value) + radians(starRotationZ))
        starfield2.rotate(PApplet.map(bassSum, 0f, 50f, 0f, 0.08f * starfieldRotationPot.value) + radians(starRotationZ))

        starfield1.setCount(starCountPot.value.toInt())
        starfield2.setCount(starCountPot.value.toInt())
        starfield1.update(speed = (2 * starSpeedPot.value).toInt() + (bassSum * starBoostPot.value).toInt())
        starfield2.update(speed = (4 * starSpeedPot.value).toInt())
        starfield1.color = fgColor
        starfield2.color = fgColor
        starfield1.motion = starMotion
        starfield2.motion = starMotion
        starfield1.draw()
        starfield2.draw()

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