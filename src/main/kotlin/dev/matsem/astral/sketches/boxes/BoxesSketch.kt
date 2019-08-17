package dev.matsem.astral.sketches.boxes

import dev.matsem.astral.sketches.BaseSketch
import dev.matsem.astral.sketches.SketchLoader
import dev.matsem.astral.sketches.polygonal.star.Starfield
import dev.matsem.astral.tools.audio.AudioProcessor
import dev.matsem.astral.tools.extensions.*
import dev.matsem.astral.tools.galaxy.Galaxy
import org.koin.core.inject
import processing.core.PApplet
import processing.core.PApplet.radians
import shiffman.box2d.Box2DProcessing

class BoxesSketch : BaseSketch() {

    override val sketch: SketchLoader by inject()
    private val audioProcessor: AudioProcessor by inject()
    private val galaxy: Galaxy by inject()
    private val box2d: Box2DProcessing by inject()

    var starMotion = Starfield.Motion.ZOOMING

    private val starSpeedPot = galaxy.createPot(6, 0, 0.5f, 5f, 0.5f).lerp(0.1f)
    private val starCountPot = galaxy.createPot(6, 1, 0f, 1200f, 300f).lerp(0.005f)
    private val starfieldRotationPot = galaxy.createPot(6, 2, 0f, 3f, 1f)
    private val starBoostPot = galaxy.createPot(6, 3, 0f, 1f, 0.4f)
    private val rotationZPot = galaxy.createPot(6, 8, -1f, 1f, 0f)

    private val rotationZResetButton = galaxy.createPushButton(6, 9) { rotationZPot.reset() }
    private val motionZoomBtn = galaxy.createPushButton(6, 4) { starMotion = Starfield.Motion.ZOOMING }
    private val motionTranslateBWDBtn = galaxy.createPushButton(6, 5) { starMotion = Starfield.Motion.TRANSLATING_BACKWARD }
    private val motionTranslateFWDBtn = galaxy.createPushButton(6, 6) { starMotion = Starfield.Motion.TRANSLATING_FORWARD }

    private val addBoxBtn = galaxy.createToggleButton(6, 11)
    private val removeBoxBtn = galaxy.createToggleButton(6, 10)

    private val manualBoostOrbitBtn = galaxy.createToggleButton(6, 12)
    private val manualBoostOrbitForcePot = galaxy.createPot(6, 13, 0f, 20000f, 12000f)
    private val beatAttractPot = galaxy.createPot(6, 14, 0f, 40000f, 10000f)
    private val bassBoostPot = galaxy.createPot(6, 15, 0f, 500f, 100f)

    private val sphereDetailPot = galaxy.createPot(6, 16, 4f, 32f, 8f)
    private val sphereGravityPot = galaxy.createPot(6, 17, 0f, 2000f, 1000f)

    private val joystick = galaxy.createJoystick(6, 18, 19, 20, 21, 22, 23).flipped()
    private val rotationAutoBtn = galaxy.createToggleButton(6, 24, true)

    private val wireframesBtn = galaxy.createToggleButton(6, 25)

    private val baseDiameterPot = galaxy.createPot(6, 26, 50f, 200f, 100f)
    private val ampGainPot = galaxy.createPot(6, 27, 0.2f, 8f, 1f)
    private val startFreqPot = galaxy.createPot(6, 28, 100f, 1000f, 200f)

    private val starfield1 = Starfield(sketch, 300).apply { motion = starMotion }
    private val starfield2 = Starfield(sketch, 300).apply { motion = starMotion }

    private val staticSphere = StaticSphere(sketch, box2d, 0f, 0f).apply {
        fgColor = this@BoxesSketch.fgColor
        accentColor = this@BoxesSketch.accentColor
        radius = shorterDimension() / 4f
    }

    private val bodies = arrayListOf<Box>()

    var amp = 0f
    var bassSum = 0f
    var starVz = 0f
    var starRotationZ = 0f

    var rotX = 0f
    var rotY = 0f
    var rotZ = 0f

    override fun onBecameActive() {
        sphereDetail(sphereDetailPot.value.toInt())
    }

    override fun setup() = Unit

    private fun addBox() {
        bodies.add(
                Box(sketch, box2d, random(-centerX(), centerX()), random(-centerY(), centerY())).apply {
                    accentColor = this@BoxesSketch.accentColor
                    fgColor = this@BoxesSketch.fgColor
                    size = sketch.random(15f, 30f)
                }
        )
    }

    private fun destroyBox() {
        val randomBox = bodies.random()
        randomBox.destroy()
        bodies.remove(randomBox)
    }

    override fun draw() {
        if (sketch.keyPressed) {
            when (sketch.key) {
                'b' -> {
                    addBox()
                }

                'd' -> {
                    destroyBox()
                }
            }
        }

        if (addBoxBtn.isPressed) {
            addBox()
        } else if (removeBoxBtn.isPressed && bodies.isNotEmpty()) {
            destroyBox()
        }

        val range = startFreqPot.value..startFreqPot.value + 100f
        amp += audioProcessor.getRange(range) * ampGainPot.value
        amp *= 0.80f

        bassSum += audioProcessor.getRange(0f..50f)
        bassSum *= 0.2f

        starVz += rotationZPot.value.threshold(0.05f) * 0.15f
        starVz *= 0.95f
        starRotationZ += starVz

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

        rotX += joystick.y * .04f
        rotY += joystick.x * .04f
        rotZ += joystick.z * .04f

        if (rotationAutoBtn.isPressed) {
            pushMatrix()
            translate(centerX(), centerY())
            rotateY(angularVelocity(16f))
            pushMatrix()
            rotateX(PApplet.map(sin(angularVelocity(30f)), -1f, 1f, radians(-180f), radians(180f)))
            pushMatrix()
            rotateZ(0f)
        } else {
            pushMatrix()
            translate(centerX(), centerY())
            rotateY(rotY)
            pushMatrix()
            rotateX(rotX)
            pushMatrix()
            rotateZ(rotZ)
        }

        staticSphere.apply {
            radius = baseDiameterPot.value + amp
            accentColor = this@BoxesSketch.accentColor
            fgColor = this@BoxesSketch.fgColor
        }

        sphereDetail(sphereDetailPot.value.toInt())
        staticSphere.draw()

        if (audioProcessor.beatDetect.isSnare && wireframesBtn.isPressed) {
            bodies.forEach { it.hasFill = !it.hasFill }
            staticSphere.hasFill = !staticSphere.hasFill
        }

        bodies.withIndex().forEach {
            when {
                manualBoostOrbitBtn.isPressed -> it.value.boostOrbit(manualBoostOrbitForcePot.value)
                audioProcessor.beatDetect.isKick -> it.value.attract(staticSphere.x, staticSphere.y, beatAttractPot.value)
                else -> if (it.index % 2 == 0) it.value.boostOrbit(audioProcessor.getRange(20f..60f) * bassBoostPot.value)
            }

            it.value.attract(staticSphere.x, staticSphere.y, sphereGravityPot.value)

            it.value.accentColor = accentColor
            it.value.fgColor = fgColor
            it.value.draw()
        }

        popMatrix()
        popMatrix()
        popMatrix()

        box2d.step()
    }
}