package dev.matsem.astral.sketches.galaxy

import dev.matsem.astral.sketches.BaseSketch
import dev.matsem.astral.sketches.SketchLoader
import dev.matsem.astral.tools.audio.AudioProcessor
import dev.matsem.astral.tools.audio.beatcounter.BeatCounter
import dev.matsem.astral.tools.audio.beatcounter.OnKick
import dev.matsem.astral.tools.audio.beatcounter.OnSnare
import dev.matsem.astral.tools.extensions.*
import dev.matsem.astral.tools.kontrol.KontrolF1
import dev.matsem.astral.tools.kontrol.onTogglePad
import dev.matsem.astral.tools.kontrol.onTriggerPad
import dev.matsem.astral.tools.logging.SketchLogger
import org.koin.core.KoinComponent
import org.koin.core.inject
import processing.core.PApplet.sin
import processing.core.PConstants
import processing.core.PImage
import processing.core.PVector

// TODO fg bg color
class GalaxySketch : BaseSketch(), KoinComponent {

    override val sketch: SketchLoader by inject()

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

    private val audioProcessor: AudioProcessor by inject()
    private val beatCounter: BeatCounter by inject()
    private val kontrol: KontrolF1 by inject()

    private val lock = Any()
    private val starField = mutableListOf<Star>()
    private val galaxy = mutableListOf<Star>()
    private val images = arrayOf(
            GalaxyImage(path = "images/galaxy1.png", pixelStep = 3, threshold = 60f),
            GalaxyImage(path = "images/galaxy2.png", pixelStep = 2, threshold = 50f)
    )

    private var bassGain: Float = 0f

    private var zoomQuantized: Boolean = false
    private var zoomOnBeat: Boolean = false
    private var zoomValue = 1f
    private var zoomMin = 1f
    private var zoomMax = 3f
    private var zoomHz = 1 / 60f
    private var zoomQuant = 0.005f

    private var randomDiameters: Boolean = false
    private var diameterFactor = 1f

    override fun onBecameActive() {
        kontrol.reset()

        kontrol.onTriggerPad(0, 0, 50) {
            if (it) {
                createGalaxy(images[0])
            }
        }

        kontrol.onTriggerPad(0, 1, 50) {
            if (it) {
                createGalaxy(images[1])
            }
        }

        kontrol.onTogglePad(0, 2, 0) {
            zoomQuantized = it
        }

        kontrol.onTogglePad(1, 2, 10) {
            zoomOnBeat = it
        }

        kontrol.onTogglePad(0, 3, 70) {
            randomDiameters = it
        }
    }

    override fun setup() = with(sketch) {
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
            if (randomDiameters) {
                randomizeDiameters()
            }
        }

        beatCounter.addListener(OnKick, 4) {
            if (zoomOnBeat) {
                zoomValue = random(zoomMin, zoomMax)
            }
        }
    }

    private fun createGalaxy(image: GalaxyImage) = with(sketch) {
        synchronized(lock) {
            galaxy.clear()
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
                        galaxy += Star(
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
        galaxy.forEach {
            it.diameter = generateDiameter()
        }
    }

    private fun generateDiameter(): Float = with(sketch) {
        return if (random(1f) > 0.92f) random(7f, 9f) else random(1f, 5f)
    }

    override fun draw() = with(sketch) {
        bassGain = kontrol.slider1.midiRange(1f)
        zoomMin = kontrol.knob1.midiRange(1f, 4f)
        zoomMax = kontrol.knob2.midiRange(2f, 4f)
        zoomHz = kontrol.slider3.midiRange(1 / 60f, 1 / 5f)
        zoomQuant = kontrol.knob3.midiRange(0.5f, 0.005f)
        diameterFactor = kontrol.knob4.midiRange(0.5f, 1.5f)

        beatCounter.update()
        background(30)

        noFill()
        stroke(0f, 0f, 100f)

        if (zoomQuantized) {
            zoomValue = sin(angularTimeHz(zoomHz)).mapSin(zoomMin, zoomMax).quantize(zoomQuant)
        }

        // Galaxy
        synchronized(lock) {
            galaxy.forEach {
                pushMatrix()
                translateCenter()
                scale(zoomValue)

                it.rotationExtra += audioProcessor.getRange(1000f..4000f).remap(0f, 100f, 0f, 0.02f) * it.randomFactor
                rotateX(-0.34f)
                rotateY((millis() - it.birth) * it.ySpeed + it.rotationExtra)
                rotateZ((millis() - it.birth) * it.zSpeed)

                strokeWeight(it.diameter * diameterFactor)
                val amp = audioProcessor.getRange(20f..200f) * random(-0.1f, 0.1f) * bassGain

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
        fill(0)
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