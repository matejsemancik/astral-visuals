package dev.matsem.astral.visuals.layers.galaxy

import dev.matsem.astral.core.tools.audio.AudioProcessor
import dev.matsem.astral.core.tools.audio.beatcounter.BeatCounter
import dev.matsem.astral.core.tools.audio.beatcounter.OnKick
import dev.matsem.astral.core.tools.audio.beatcounter.OnSnare
import dev.matsem.astral.core.tools.extensions.angularTimeHz
import dev.matsem.astral.core.tools.extensions.constrain
import dev.matsem.astral.core.tools.extensions.longerDimension
import dev.matsem.astral.core.tools.extensions.mapSin
import dev.matsem.astral.core.tools.extensions.quantize
import dev.matsem.astral.core.tools.extensions.remap
import dev.matsem.astral.core.tools.extensions.translateCenter
import dev.matsem.astral.core.tools.galaxy.Galaxy
import dev.matsem.astral.core.tools.midi.MidiAutomator
import dev.matsem.astral.core.tools.osc.OscHandler
import dev.matsem.astral.core.tools.osc.OscManager
import dev.matsem.astral.core.tools.osc.oscToggleButtonDelegate
import dev.matsem.astral.visuals.ColorHandler
import dev.matsem.astral.visuals.Colorizer
import dev.matsem.astral.visuals.Layer
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import processing.core.PApplet
import processing.core.PApplet.lerp
import processing.core.PApplet.sin
import processing.core.PConstants
import processing.core.PGraphics
import processing.core.PImage
import processing.core.PVector

class GalaxyLayer : Layer(), KoinComponent, ColorHandler, OscHandler {

    data class Star(
        val vec: PVector,
        var targetVec: PVector,
        var diameter: Float,
        val ySpeed: Float = 0f,
        val zSpeed: Float = 0f,
        val randomFactor: Float = 0f,
        val birth: Int = 0,
        var rotationExtra: Float = 0f
    )

    override val parent: PApplet by inject()
    override val colorizer: Colorizer by inject()
    override val oscManager: OscManager by inject()

    private val audioProcessor: AudioProcessor by inject()
    private val beatCounter: BeatCounter by inject()
    private val galaxy: Galaxy by inject()
    private val automator: MidiAutomator by inject()

    private val lock = Any()
    private val starField = mutableListOf<Star>()
    private val galaxyStars = mutableListOf<Star>()
    private val images = arrayOf(
        GalaxyImage(path = "images/galaxy1.png", pixelStep = 3, threshold = 60f),
        GalaxyImage(path = "images/galaxy2.png", pixelStep = 2, threshold = 50f),
        GalaxyImage(path = "images/galaxy1.png", pixelStep = 3, threshold = 50f),
        GalaxyImage(path = "images/galaxy2.png", pixelStep = 2, threshold = 40f),
        GalaxyImage(path = "images/semlogo.png", pixelStep = 3, threshold = 50f)
    )

    // region remote control

    private val galaxyImageButtons =
        galaxy.createPushButtonGroup(10, listOf(4, 5, 6, 7, 24)) {
            createGalaxy(images[it])
        }

    private val zoomQuantizeButton = galaxy.createToggleButton(channel = 10, cc = 8, defaultValue = false)
    private val zoomOnBeatButton = galaxy.createToggleButton(channel = 10, cc = 9, defaultValue = false)
    private val zoomQuantSlider = galaxy.createPot(channel = 10, cc = 10, min = 0.005f, max = 0.5f, initialValue = 0.5f)
    private val zoomHzSlider =
        galaxy.createPot(channel = 10, cc = 11, min = 1 / 60f, max = 1 / 5f, initialValue = 1 / 60f)
    private val zoomMinSlider = galaxy.createPot(channel = 10, cc = 12, min = 1f, max = 4f, initialValue = 1f)
    private val zoomMaxSlider = galaxy.createPot(channel = 10, cc = 13, min = 1f, max = 4f, initialValue = 2f)
    private var joystick = galaxy.createJoystick(
        channel = 10,
        ccX = 17,
        ccY = 18,
        ccTouchXY = 19,
        ccZ = 20,
        ccTouchZ = 21,
        ccFeedbackEnabled = 22
    )

    private var rotationResetButton = galaxy.createPushButton(channel = 10, cc = 23) {
        rotX = 0f
        rotY = 0f
        rotZ = 0f
    }

    private var pulseEnabledButton = galaxy.createToggleButton(channel = 10, cc = 35, defaultValue = false)
    private var galaxyPulseZoomExtraSlider =
        galaxy.createPot(channel = 10, cc = 32, min = 0f, max = 1f, initialValue = 0.2f)
    private var starfieldPulseZoomExtraSlider =
        galaxy.createPot(channel = 10, cc = 33, min = 0f, max = 1f, initialValue = 0.25f)
    private var pulseSpeedSlider = galaxy.createPot(channel = 10, cc = 34, min = 1f, max = 10f, initialValue = 8f)

    private var zoomValue = 1f
    private var targetZoomValue = 1f
    private var galaxyPulseZoomExtra = 0f
    private var starfieldPulseZoomExtra = 0f
    private var pulsarStartAt = 0
    private var rotX = 0f
    private var rotY = 0f
    private var rotZ = 0f
    private var highs = 0f

    private val randomDiametersButton = galaxy.createToggleButton(channel = 10, cc = 14, defaultValue = false)
    private val starDiameterSlider = galaxy.createPot(channel = 10, cc = 15, min = 0.2f, max = 1.5f, initialValue = 1f)

    private val bassGainSlider = galaxy.createPot(channel = 10, cc = 16, min = 0f, max = 2f)
    var shuffleStarsEnabled by oscToggleButtonDelegate("/galaxy/shuffleEnabled", defaultValue = true)

    // endregion

    init {
        automator.setupWithGalaxy(
            channel = 10,
            recordButtonCC = 0,
            playButtonCC = 1,
            loopButtonCC = 2,
            clearButtonCC = 3,
            channelFilter = null
        )

        // Create galaxy from image
        createGalaxy(images[0])

        // Create starfield
        repeat(4000) {
            val vec = PVector.random3D().mult(parent.random(0f, 2500f))
            starField += Star(
                vec = vec,
                targetVec = vec,
                diameter = if (parent.random(1f) > 0.99f) {
                    parent.random(6f, 10f)
                } else {
                    parent.random(1f, 4f)
                },
                ySpeed = parent.random(0.00002f, 0.00005f),
                zSpeed = parent.random(-0.00001f, 0.00001f),
                birth = parent.millis(),
                randomFactor = if (parent.random(1f) > 0.50f) parent.random(0.2f, 1f) else 0f
            )
        }

        beatCounter.addListener(OnSnare, 1) {
            if (randomDiametersButton.isPressed) {
                randomizeDiameters()
            }
        }

        beatCounter.addListener(OnKick, 4) {
            if (zoomOnBeatButton.isPressed) {
                targetZoomValue = parent.random(zoomMinSlider.value, zoomMaxSlider.value)
            }
        }

        beatCounter.addListener(OnKick, 2) {
            if (pulseEnabledButton.isPressed) {
                galaxyPulseZoomExtra = galaxyPulseZoomExtraSlider.value
                starfieldPulseZoomExtra = starfieldPulseZoomExtraSlider.value
                pulsarStartAt = parent.millis()
            }
        }

        beatCounter.addListener(OnSnare, 16) {
            if (shuffleStarsEnabled) {
                starField
                    .forEach { it.targetVec = PVector.random3D().mult(parent.random(0f, 2500f)) }
            }
        }
    }

    private fun createGalaxy(image: GalaxyImage) = with(parent) {
        synchronized(lock) {
            galaxyStars.clear()
            val galaxyImage: PImage = loadImage(image.path).apply {
                val ratio = pixelWidth / pixelHeight.toFloat()
                resize(720, (720 / ratio).toInt())
            }

            galaxyImage.loadPixels()
            var pixelBrightness: Float
            for (x in 0 until galaxyImage.width step image.pixelStep) {
                for (y in 0 until galaxyImage.height step image.pixelStep) {
                    pixelBrightness = brightness(galaxyImage[x, y])
                    if (pixelBrightness > image.threshold) {
                        val vec = PVector(
                            x.toFloat() - galaxyImage.width / 2f,
                            random(-4f, 4f),
                            y.toFloat() - galaxyImage.height / 2f
                        )
                        galaxyStars += Star(
                            vec = vec,
                            targetVec = vec,
                            diameter = generateDiameter(),
                            ySpeed = random(PConstants.TWO_PI * 6e-6f, PConstants.TWO_PI * 8e-6f),
                            birth = millis(),
                            randomFactor = random(0.01f, 0.1f)
                        )
                    }
                }
            }
        }
    }

    private fun randomizeDiameters() = synchronized(lock) {
        galaxyStars.forEach {
            it.diameter = generateDiameter()
        }
    }

    private fun generateDiameter(): Float = with(parent) {
        return if (random(1f) > 0.92f) random(7f, 9f) else random(1f, 5f)
    }

    override fun PGraphics.draw() {
        clear()
        ellipseMode(PConstants.RADIUS)

        highs += audioProcessor.getRange((4000f..8000f))
        highs *= 0.5f
        zoomValue = lerp(zoomValue, targetZoomValue, 0.2f)

        galaxyPulseZoomExtra = lerp(galaxyPulseZoomExtra, 0f, 0.5f)
        starfieldPulseZoomExtra = lerp(starfieldPulseZoomExtra, 0f, 0.5f)

        automator.update()

        val diameterFactor = starDiameterSlider.value
        rotX += joystick.x * 0.01f
        rotY += joystick.y * 0.01f
        rotZ += joystick.z * 0.01f

        translateCenter()
        rotateX(rotX)
        rotateY(rotY)
        rotateZ(rotZ)

        noFill()
        stroke(fgColor)

        if (zoomQuantizeButton.isPressed) {
            targetZoomValue = sin(parent.angularTimeHz(zoomHzSlider.value))
                .mapSin(zoomMinSlider.value, zoomMaxSlider.value)
                .quantize(zoomQuantSlider.value)
        }

        // Galaxy
        synchronized(lock) {
            galaxyStars.forEach {
                pushMatrix()
                scale(zoomValue + galaxyPulseZoomExtra)

                it.rotationExtra += audioProcessor.getRange(1000f..4000f)
                    .remap(0f, 100f, 0f, 0.02f) * it.randomFactor

                rotateX(-0.34f)
                rotateY((parent.millis() - it.birth) * it.ySpeed + it.rotationExtra)
                rotateZ((parent.millis() - it.birth) * it.zSpeed)

                strokeWeight(it.diameter * diameterFactor)
                val amp = audioProcessor.getRange(20f..200f) * parent.random(-0.1f, 0.1f) * bassGainSlider.value

                val v = it.vec
                point(v.x, v.y + amp, v.z)
                popMatrix()
            }
        }

        // Stars
        starField
            .shuffled()
            .take(
                audioProcessor
                    .getRange(20f..60f)
                    .remap(0f, 400f, starField.size.toFloat(), starField.size.toFloat() / 2f).toInt()
                    .constrain(low = 0, high = starField.size - 1)
            )
            .forEach {
                pushMatrix()
                scale(zoomValue - starfieldPulseZoomExtra)
                it.vec.lerp(it.targetVec, 0.04f)
                it.rotationExtra += audioProcessor.getRange(2500f..16000f).remap(0f, 100f, 0f, 0.2f) * it.randomFactor
                rotateY(parent.millis() * it.ySpeed + it.rotationExtra)
                rotateZ(parent.millis() * it.zSpeed)

                strokeWeight(it.diameter * diameterFactor)
                point(it.vec.x, it.vec.y, it.vec.z)
                popMatrix()
            }

        // Black hole
        pushMatrix()
        scale(zoomValue)
        noStroke()
        fill(bgColor)
        ellipseMode(PConstants.CENTER)
        beginShape()
        sphere(25f + highs)
        endShape()
        popMatrix()

        // Pulsar
        pushMatrix()
        scale(zoomValue)
        noFill()
        stroke(fgColor)

        // Pulsar - ellipse
        pushMatrix()
        rotateX(PConstants.PI / 2f - 0.34f)
        for (i in 0 until 3) {
            val radius = ((parent.millis() - pulsarStartAt) * pulseSpeedSlider.value) * i * 0.5f
            val weight = radius
                .remap(0f, longerDimension().toFloat() * 2f, starDiameterSlider.value * 4f, 0f)
                .constrain(0f, starDiameterSlider.value * 4f)

            strokeWeight(weight)
            ellipse(0f, 0f, radius - i * 100f, radius - i * 100f)
        }
        popMatrix()
        popMatrix()
    }
}