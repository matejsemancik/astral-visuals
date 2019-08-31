package dev.matsem.astral.sketches.starfield

import dev.matsem.astral.sketches.BaseSketch
import dev.matsem.astral.sketches.SketchLoader
import dev.matsem.astral.tools.audio.AudioProcessor
import dev.matsem.astral.tools.audio.beatcounter.BeatCounter
import dev.matsem.astral.tools.extensions.constrain
import dev.matsem.astral.tools.extensions.remap
import dev.matsem.astral.tools.extensions.translateCenter
import dev.matsem.astral.tools.kontrol.KontrolF1
import org.koin.core.KoinComponent
import org.koin.core.inject
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PImage
import processing.core.PVector

class StarfieldSketch : BaseSketch(), KoinComponent {

    override val sketch: SketchLoader by inject()

    data class Star(
            val vec: PVector,
            val diameter: Float,
            val ySpeed: Float = 0f,
            val zSpeed: Float = 0f,
            val randomFactor: Float = 0f,
            var rotationExtra: Float = 0f
    )

    private val audioProcessor: AudioProcessor by inject()
    private val beatCounter: BeatCounter by inject()
    private val kontrol: KontrolF1 by inject()

    private val starField = mutableListOf<Star>()
    private val galaxy = mutableListOf<Star>()

    override fun setup() = with(sketch) {
        // Create galaxy from image
        val galaxyImage: PImage = loadImage("images/galaxy-transparent-bw.png").apply { resize(720, 720) }
        galaxyImage.loadPixels()
        var pixelBrightness: Float
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
                            diameter = random(1f, 4f),
                            ySpeed = random(PConstants.TWO_PI * 6e-6f, PConstants.TWO_PI * 8e-6f),
                            randomFactor = random(0.01f, 0.1f)
                    )
                }
            }
        }

        // Create starfield
        repeat(2500) {
            starField += Star(
                    vec = PVector.random3D().mult(random(0f, 2500f)),
                    diameter = if (random(1f) > 0.99f) random(6f, 10f) else random(1f, 4f),
                    ySpeed = random(0.00002f, 0.00005f),
                    zSpeed = random(-0.00001f, 0.00001f),
                    randomFactor = if (random(1f) > 0.50f) random(0.2f, 1f) else 0f
            )
        }
    }

    override fun draw() = with(sketch) {
        beatCounter.update()
        background(30)

        noFill()
        stroke(0f, 0f, 100f)

        // Galaxy
        galaxy.forEach {
            pushMatrix()
            translateCenter()
            it.rotationExtra += audioProcessor.getRange(1000f..4000f).remap(0f, 100f, 0f, 0.02f) * it.randomFactor
//            rotateX(kontrol.knob1.midiRange(PConstants.PI / 4f, -PConstants.PI / 4f))
            rotateX(-0.34f)
            rotateY(millis() * it.ySpeed + it.rotationExtra)
            rotateZ(millis() * it.zSpeed)

            strokeWeight(it.diameter)
            val wave = PApplet.sin(millis() / 1000f + it.vec.x / 40f)
            val waveAmplitude = 2f
            point(it.vec.x, it.vec.y + wave * waveAmplitude, it.vec.z)
            popMatrix()
        }

        // Stars
        starField
                .shuffled()
                .take(audioProcessor
                        .getRange(20f..60f)
                        .remap(0f, 400f, starField.size.toFloat(), starField.size.toFloat() / 2f).toInt()
                        .constrain(high = starField.size - 1)
                )
                .forEach {
                    pushMatrix()
                    translateCenter()
                    it.rotationExtra += audioProcessor.getRange(2500f..16000f).remap(0f, 100f, 0f, 0.2f) * it.randomFactor
                    rotateY(millis() * it.ySpeed + it.rotationExtra)
                    rotateZ(millis() * it.zSpeed)

                    strokeWeight(it.diameter)
                    point(it.vec.x, it.vec.y, it.vec.z)
                    popMatrix()
                }

        // Black hole
        pushMatrix()
        translateCenter()
        noStroke()
        fill(0)
        ellipseMode(PConstants.CENTER)
        beginShape()
        sphere(25f)
        endShape()
        popMatrix()
    }
}