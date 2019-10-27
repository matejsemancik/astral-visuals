package dev.matsem.astral.sketches.oldskool

import dev.matsem.astral.sketches.BaseSketch
import dev.matsem.astral.sketches.SketchLoader
import dev.matsem.astral.tools.audio.beatcounter.BeatCounter
import dev.matsem.astral.tools.audio.beatcounter.OnKick
import dev.matsem.astral.tools.extensions.*
import org.koin.core.inject
import processing.core.PApplet
import processing.core.PApplet.radians
import processing.core.PVector
import kotlin.math.absoluteValue

class OldSkoolSketch : BaseSketch() {

    private val beatCounter: BeatCounter by inject()

    class Box(
            var position: PVector,
            val rotation: PVector,
            val rotationVector: PVector,
            var size: Float,
            var targetSize: Float
    ) {
        fun update() {
            position += PVector(0f, 0f, 1f)
            rotation += rotationVector
            size = PApplet.lerp(size, targetSize, 0.1f)
        }
    }

    private var sceneRotation = PVector(0f, 0f, 0f)
    private var targetSceneRotation = PVector(0f, 0f, 0f)

    override val sketch: SketchLoader by inject()
    private val boxes = mutableListOf<Box>()
    private var deadZone = sketch.shorterDimension()

    private fun newBox(): Box = with(sketch) {
        return Box(
                position = PVector.random3D().mult(random(longerDimension().toFloat())),
                rotation = PVector(0f, 0f, 0f),
                rotationVector = PVector(random(-1e-2f, 1e-2f), random(-1e-2f, 1e-2f), random(-1e-2f, 1e-2f)),
                size = 0f,
                targetSize = random(10f, 20f)
        )
    }

    override fun setup() = with(sketch) {
        repeat(500) { boxes.add(newBox()) }

        // TODO tapper!
        beatCounter.addListener(OnKick, 1) {
            boxes.shuffled().take(boxes.size / 10).forEach {
                // TODO tweakable!
                it.targetSize = random(it.size * 0.7f, it.size * 1.1f)
                it.size *= 1.4f
            }
        }

        beatCounter.addListener(OnKick, 32) {
            // TODO tweakable!
            if (random(1f) > 0.5f) {
                targetSceneRotation = PVector(0f, 0f, random(-radians(90f), radians(90f)))
            } else {
                targetSceneRotation = PVector.random3D()
            }
        }
    }

    override fun draw() = with(sketch) {
        beatCounter.update()

        background(bgColor)
        translateCenter()
        sceneRotation = PVector.lerp(sceneRotation, targetSceneRotation, 0.001f)
        rotate(sceneRotation)

        var lostBoxes = 0

        boxes.removeIf {
            it.update()
            val isDead = it.position.z > deadZone
            lostBoxes += if (isDead) 1 else 0
            return@removeIf isDead
        }

        repeat(lostBoxes) {
            // Do not generate boxes in straight center (less than 20 around middle)
            var box: Box
            do {
                box = newBox()
            } while (box.position.x.absoluteValue < 20 && box.position.y.absoluteValue < 20)

            boxes.add(box)
        }

        boxes.forEach {
            noFill()
            stroke(fgColor)
            strokeWeight(4f)
            pushMatrix()
            translate(it.position)
            rotate(it.rotation)
            box(it.size)
            popMatrix()
        }
    }
}