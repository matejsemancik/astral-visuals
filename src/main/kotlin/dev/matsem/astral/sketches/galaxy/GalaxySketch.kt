package dev.matsem.astral.sketches.galaxy

import dev.matsem.astral.sketches.BaseSketch
import dev.matsem.astral.sketches.SketchLoader
import dev.matsem.astral.tools.audio.AudioProcessor
import dev.matsem.astral.tools.audio.beatcounter.BeatCounter
import dev.matsem.astral.tools.audio.beatcounter.OnKick
import dev.matsem.astral.tools.audio.beatcounter.OnSnare
import dev.matsem.astral.tools.automator.MidiAutomator
import dev.matsem.astral.tools.extensions.*
import dev.matsem.astral.tools.galaxy.Galaxy
import dev.matsem.astral.tools.logging.SketchLogger
import org.koin.core.KoinComponent
import org.koin.core.inject
import processing.core.PApplet.sin
import processing.core.PConstants
import processing.core.PImage
import processing.core.PVector

class GalaxySketch : BaseSketch(), KoinComponent {

    data class Star(
            val vec: PVector,
            var diameter: Float,
            val ySpeed: Float = 0f,
            val zSpeed: Float = 0f,
            val randomFactor: Float = 0f,
            val birth: Int = 0,
            var rotationExtra: Float = 0f
    )

    private val logger = SketchLogger.Builder()
            .withResolution()
            .withFps()
            .build()

    override val sketch: SketchLoader by inject()
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
            GalaxyImage(path = "images/galaxy2.png", pixelStep = 2, threshold = 40f)
    )

    // region remote control

    private val galaxyImageButtons = galaxy.createPushButtonGroup(10, listOf(4, 5, 6, 7)) {
        createGalaxy(images[it])
    }

    private val zoomQuantizeButton = galaxy.createToggleButton(channel = 10, cc = 8, defaultValue = false)
    private val zoomOnBeatButton = galaxy.createToggleButton(channel = 10, cc = 9, defaultValue = false)
    private val zoomQuantSlider = galaxy.createPot(channel = 10, cc = 10, min = 0.005f, max = 0.5f, initialValue = 0.5f)
    private val zoomHzSlider = galaxy.createPot(channel = 10, cc = 11, min = 1 / 60f, max = 1 / 5f, initialValue = 1 / 60f)
    private val zoomMinSlider = galaxy.createPot(channel = 10, cc = 12, min = 1f, max = 4f, initialValue = 1f)
    private val zoomMaxSlider = galaxy.createPot(channel = 10, cc = 13, min = 1f, max = 4f, initialValue = 2f)
    private var zoomValue = 1f

    private val randomDiametersButton = galaxy.createToggleButton(channel = 10, cc = 14, defaultValue = false)
    private val starDiameterSlider = galaxy.createPot(channel = 10, cc = 15, min = 0.5f, max = 1.5f, initialValue = 1f)

    private val bassGainSlider = galaxy.createPot(channel = 10, cc = 16, min = 0f, max = 1f)

    // endregion

    override fun onBecameActive() = Unit

    override fun setup() = with(sketch) {
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
        repeat(2000) {
            starField += Star(
                    vec = PVector.random3D().mult(random(0f, 2500f)),
                    diameter = if (random(1f) > 0.99f) random(6f, 10f) else random(1f, 4f),
                    ySpeed = random(0.00002f, 0.00005f),
                    zSpeed = random(-0.00001f, 0.00001f),
                    birth = millis(),
                    randomFactor = if (random(1f) > 0.50f) random(0.2f, 1f) else 0f
            )
        }

        beatCounter.addListener(OnSnare, 1) {
            if (randomDiametersButton.isPressed) {
                randomizeDiameters()
            }
        }

        beatCounter.addListener(OnKick, 4) {
            if (zoomOnBeatButton.isPressed) {
                zoomValue = random(zoomMinSlider.value, zoomMaxSlider.value)
            }
        }
    }

    private fun createGalaxy(image: GalaxyImage) = with(sketch) {
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
                        galaxyStars += Star(
                                vec = PVector(
                                        x.toFloat() - galaxyImage.width / 2f,
                                        random(-4f, 4f),
                                        y.toFloat() - galaxyImage.height / 2f
                                ),
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

    private fun generateDiameter(): Float = with(sketch) {
        return if (random(1f) > 0.92f) random(7f, 9f) else random(1f, 5f)
    }

    override fun draw() = with(sketch) {
        automator.update()
        val diameterFactor = starDiameterSlider.value

        beatCounter.update()
        background(bgColor)

        noFill()
        stroke(fgColor)

        if (zoomQuantizeButton.isPressed) {
            zoomValue = sin(angularTimeHz(zoomHzSlider.value))
                    .mapSin(zoomMinSlider.value, zoomMaxSlider.value)
                    .quantize(zoomQuantSlider.value)
        }

        // Galaxy
        synchronized(lock) {
            galaxyStars.forEach {
                pushMatrix()
                translateCenter()
                scale(zoomValue)

                it.rotationExtra += audioProcessor.getRange(1000f..4000f).remap(0f, 100f, 0f, 0.02f) * it.randomFactor
                rotateX(-0.34f)
                rotateY((millis() - it.birth) * it.ySpeed + it.rotationExtra)
                rotateZ((millis() - it.birth) * it.zSpeed)

                strokeWeight(it.diameter * diameterFactor)
                val amp = audioProcessor.getRange(20f..200f) * random(-0.1f, 0.1f) * bassGainSlider.value

                val v = it.vec
                point(v.x, v.y + amp, v.z)
                popMatrix()
            }
        }

        // Stars
        starField
                .shuffled()
                .take(audioProcessor
                        .getRange(20f..60f)
                        .remap(0f, 400f, starField.size.toFloat(), starField.size.toFloat() / 2f).toInt()
                        .constrain(low = 0, high = starField.size - 1)
                )
                .forEach {
                    pushMatrix()
                    translateCenter()
                    scale(zoomValue)
                    it.rotationExtra += audioProcessor.getRange(2500f..16000f).remap(0f, 100f, 0f, 0.2f) * it.randomFactor
                    rotateY(millis() * it.ySpeed + it.rotationExtra)
                    rotateZ(millis() * it.zSpeed)

                    strokeWeight(it.diameter * diameterFactor)
                    point(it.vec.x, it.vec.y, it.vec.z)
                    popMatrix()
                }

        // Black hole
        pushMatrix()
        translateCenter()
        scale(zoomValue)
        noStroke()
        fill(bgColor)
        ellipseMode(PConstants.CENTER)
        beginShape()
        sphere(25f)
        endShape()
        popMatrix()

        // Debug
        if (isInDebugMode) {
            logger.draw(this)
        }
    }
}