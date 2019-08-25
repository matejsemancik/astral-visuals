package dev.matsem.astral.sketches.starfield

import dev.matsem.astral.tools.audio.AudioProcessor
import dev.matsem.astral.tools.extensions.midiRange
import dev.matsem.astral.tools.extensions.translateCenter
import dev.matsem.astral.tools.kontrol.KontrolF1
import org.koin.core.KoinComponent
import org.koin.core.inject
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PImage
import processing.core.PVector

class StarfieldSketch : PApplet(), KoinComponent {

    data class Star(
            val vec: PVector,
            val diameter: Float,
            val ySpeed: Float = 0f,
            val zSpeed: Float = 0f
    )

    private val audioProcessor: AudioProcessor by inject()
    private val kontrol: KontrolF1 by inject()

    private val starField = mutableListOf<Star>()
    private val galaxy = mutableListOf<Star>()

    private lateinit var galaxyImage: PImage

    override fun settings() {
        size(1280, 720, PConstants.P3D)
    }

    override fun setup() {
        hint(PConstants.ENABLE_DEPTH_SORT) // Displays loaded PImage with alpha channel
        colorMode(PConstants.HSB, 360f, 100f, 100f)
        galaxyImage = loadImage("images/galaxy-transparent-bw.png").apply { resize(640, 640) }
        galaxyImage.loadPixels()

        var pixelBrightness = 0f
        for (x in 0 until galaxyImage.width step 2) {
            for (y in 0 until galaxyImage.height step 2) {
                pixelBrightness = brightness(galaxyImage[x, y])
                if (pixelBrightness > 60f) {
                    galaxy += Star(
                            vec = PVector(
                                    x.toFloat() - galaxyImage.width / 2f,
                                    random(-4f, 4f),
                                    y.toFloat() - galaxyImage.height / 2f
                            ),
                            diameter = random(1f, 3f),
                            ySpeed = random(0.00002f, 0.000025f)
                    )
                }
            }
        }

        kontrol.apply {
            connect()
        }

        repeat(4000) {
            starField += Star(
                    vec = PVector.random3D().mult(random(0f, 2500f)),
                    diameter = if (random(1f) > 0.99f) random(6f, 10f) else random(1f, 4f),
                    ySpeed = random(0.00002f, 0.00005f),
                    zSpeed = random(-0.00001f, 0.00001f)
            )
        }
    }

    override fun draw() {
        background(0)

        noFill()
        stroke(0f, 0f, 100f)

        // Galaxy
        galaxy.forEach {
            pushMatrix()
            translateCenter()
            rotateX(kontrol.knob1.midiRange(0f, -PConstants.PI))
            rotateY(millis() * it.ySpeed)
            rotateZ(millis() * it.zSpeed)

            strokeWeight(it.diameter)
            point(it.vec.x, it.vec.y, it.vec.z)
            popMatrix()
        }

        // Stars
        starField.forEach {
            pushMatrix()
            translateCenter()
            rotateY(millis() * it.ySpeed)
            rotateZ(millis() * it.zSpeed)

            strokeWeight(it.diameter)
            point(it.vec.x, it.vec.y, it.vec.z)
            popMatrix()
        }
    }
}