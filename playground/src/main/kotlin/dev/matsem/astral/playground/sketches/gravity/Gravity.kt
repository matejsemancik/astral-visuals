package dev.matsem.astral.playground.sketches.gravity

import dev.matsem.astral.core.tools.animations.AnimationHandler
import dev.matsem.astral.core.tools.animations.radianSeconds
import dev.matsem.astral.core.tools.audio.AudioProcessor
import dev.matsem.astral.core.tools.extensions.centerX
import dev.matsem.astral.core.tools.extensions.centerY
import dev.matsem.astral.core.tools.extensions.shorterDimension
import dev.matsem.astral.core.tools.extensions.threshold
import dev.matsem.astral.core.tools.extensions.withAlpha
import dev.matsem.astral.core.tools.galaxy.Galaxy
import dev.matsem.astral.core.tools.midi.MidiAutomator
import org.koin.core.KoinComponent
import org.koin.core.inject
import processing.core.PApplet
import shiffman.box2d.Box2DProcessing

class Gravity : PApplet(), KoinComponent, AnimationHandler {

    override fun provideMillis(): Int = millis()

    private val audioProcessor: AudioProcessor by inject()
    private val galaxy: Galaxy by inject()
    private val box2d: Box2DProcessing by inject()
    private val automator: MidiAutomator by inject()

    private val starSpeedPot = galaxy.createPot(6, 0, 0.5f, 5f, 0.5f).lerp(0.1f)
    private val starCountPot = galaxy.createPot(6, 1, 0f, 1200f, 300f).lerp(0.005f)
    private val starfieldRotationPot = galaxy.createPot(6, 2, 0f, 3f, 1f)
    private val starBoostPot = galaxy.createPot(6, 3, 0f, 1f, 0.4f)
    private val rotationZPot = galaxy.createPot(6, 8, -1f, 1f, 0f)

    private val rotationZResetButton = galaxy.createPushButton(6, 9) { rotationZPot.reset() }

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

    private val transButton = galaxy.createToggleButton(6, 33, false)
    private val transXPeriodSlider = galaxy.createPot(6, 34, 15f, 120f, 30f).lerp(0.01f)
    private val transYPeriodSlider = galaxy.createPot(6, 35, 15f, 120f, 30f).lerp(0.01f)

    private val staticSphere = StaticSphere(this, box2d, 0f, 0f).apply {
        fgColor = 0xffffff.withAlpha()
        accentColor = 0xffffff.withAlpha()
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

    private var transX = 0f
    private var transY = 0f

    override fun setup() {
        automator.setupWithGalaxy(
            channel = 6,
            recordButtonCC = 29,
            playButtonCC = 30,
            loopButtonCC = 31,
            clearButtonCC = 32,
            channelFilter = null
        )
    }

    private fun addBox() = bodies.add(
        Box(this, box2d, random(-centerX(), centerX()), random(-centerY(), centerY())).apply {
            accentColor = 0xffffff.withAlpha()
            fgColor = 0xffffff.withAlpha()
            size = random(15f, 30f)
        }
    )

    private fun destroyBox() {
        val randomBox = bodies.random()
        randomBox.destroy()
        bodies.remove(randomBox)
    }

    override fun draw() {
        automator.update()
        if (keyPressed) {
            when (key) {
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

        background(0x000000.withAlpha())

        rotX += joystick.y * .04f
        rotY += joystick.x * .04f
        rotZ += joystick.z * .04f

        if (transButton.isPressed) {
            transX = sin(radianSeconds(transXPeriodSlider.value)) * shorterDimension() / 3f
            transY = cos(radianSeconds(transYPeriodSlider.value)) * shorterDimension() / 6f
        } else {
            transX = 0f
            transY = 0f
        }

        if (rotationAutoBtn.isPressed) {
            pushMatrix()
            translate(centerX() + transX, centerY() + transY)
            rotateY(radianSeconds(16f))
            pushMatrix()
            rotateX(PApplet.map(sin(radianSeconds(30f)), -1f, 1f, radians(-180f), radians(180f)))
            pushMatrix()
            rotateZ(0f)
        } else {
            pushMatrix()
            translate(centerX() + transX, centerY() + transY)
            rotateY(rotY)
            pushMatrix()
            rotateX(rotX)
            pushMatrix()
            rotateZ(rotZ)
        }

        staticSphere.apply {
            radius = baseDiameterPot.value + amp
            accentColor = 0xffffff.withAlpha()
            fgColor = 0xffffff.withAlpha()
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
                audioProcessor.beatDetect.isKick -> it.value.attract(
                    staticSphere.x,
                    staticSphere.y,
                    beatAttractPot.value
                )
                else -> if (it.index % 2 == 0) it.value.boostOrbit(audioProcessor.getRange(20f..60f) * bassBoostPot.value)
            }

            it.value.attract(staticSphere.x, staticSphere.y, sphereGravityPot.value)

            it.value.accentColor = 0xffffff.withAlpha()
            it.value.fgColor = 0xffffff.withAlpha()
            it.value.draw()
        }

        popMatrix()
        popMatrix()
        popMatrix()

        box2d.step()
    }

    override fun mouseClicked() {
        addBox()
    }
}