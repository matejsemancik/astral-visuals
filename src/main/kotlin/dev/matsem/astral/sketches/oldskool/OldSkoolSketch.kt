package dev.matsem.astral.sketches.oldskool

import dev.matsem.astral.sketches.BaseSketch
import dev.matsem.astral.sketches.SketchLoader
import dev.matsem.astral.tools.audio.beatcounter.BeatCounter
import dev.matsem.astral.tools.audio.beatcounter.OnKick
import dev.matsem.astral.tools.extensions.*
import dev.matsem.astral.tools.kontrol.KontrolF1
import dev.matsem.astral.tools.kontrol.onTriggerPad
import dev.matsem.astral.tools.shapes.ExtrusionCache
import dev.matsem.astral.tools.tapper.Tapper
import org.koin.core.inject
import processing.core.PApplet.radians
import processing.core.PVector

/**
 * Taking it oldskool with raw shapes
 */
class OldSkoolSketch : BaseSketch() {

    override val sketch: SketchLoader by inject()
    private val beatCounter: BeatCounter by inject()
    private val tapper: Tapper by inject()
    private val kontrol: KontrolF1 by inject()
    private val extrusionCache: ExtrusionCache by inject()

    private var sceneRotation = PVector(0f, 0f, 0f)
    private var targetSceneRotation = PVector(0f, 0f, 0f)

    private var deadZone = sketch.shorterDimension()
    private var resizeOnBeatScaleMin = 0.7f
    private var resizeOnBeatScaleMax = 1.1f
    private var expandOnBeatScale = 1.4f
    private var flyingSpeed = 1f
    private var strokeWeight = 4f
    private var affectedBeatPercentage = 0.5f

    private val flyingObjects = mutableListOf<FlyingObject>()

    private fun newObject(): FlyingObject = with(sketch) {
        val obj = if (random(1f) > 0.9f) {
            SemLogo(
                    cache = extrusionCache,
                    position = PVector.random3D().mult(random(longerDimension().toFloat())),
                    rotation = PVector(0f, 0f, 0f),
                    rotationVector = PVector(random(-1e-2f, 1e-2f), random(-1e-2f, 1e-2f), random(-1e-2f, 1e-2f)),
                    size = 0f,
                    targetSize = random(10f, 20f)
            )
        } else {
            Box(
                    position = PVector.random3D().mult(random(longerDimension().toFloat())),
                    rotation = PVector(0f, 0f, 0f),
                    rotationVector = PVector(random(-1e-2f, 1e-2f), random(-1e-2f, 1e-2f), random(-1e-2f, 1e-2f)),
                    size = 0f,
                    targetSize = random(10f, 20f)
            )
        }

        // Do not generate objects in straight center (less than 20 around middle)
        if (obj.position.isInRadius(20f)) {
            return@with newObject()
        } else {
            obj
        }
    }

    private fun resetObject(flyingObject: FlyingObject) = with(sketch) {
        var newPosition: PVector
        do {
            newPosition = PVector.random3D().mult(random(longerDimension().toFloat()))
        } while (newPosition.isInRadius(20f))

        flyingObject.position = newPosition
        flyingObject.rotation = PVector(0f, 0f, 0f)
        flyingObject.rotationVector = PVector(random(-1e-2f, 1e-2f), random(-1e-2f, 1e-2f), random(-1e-2f, 1e-2f))
        flyingObject.size = 0f
        flyingObject.targetSize = random(10f, 20f)
    }

    private fun updateObjects() {
        flyingObjects.forEach {
            it.update(flyingSpeed)
            val isDead = it.position.z > deadZone
            if (isDead) {
                resetObject(it)
            }
        }
    }

    override fun setup() = with(sketch) {

        kontrol.onTriggerPad(0, 0, midiHue = 0) {
            if (it) {
                tapper.tap()
            }
        }

        repeat(500) { flyingObjects.add(newObject()) }

        tapper.doOnBeat {
            flyingObjects.shuffled().take((flyingObjects.size * affectedBeatPercentage).toInt()).forEach {
                it.targetSize = random(it.size * resizeOnBeatScaleMin, it.size * resizeOnBeatScaleMax)
                it.size *= expandOnBeatScale
            }
        }

        beatCounter.addListener(OnKick, 32) {
            if (random(1f) > 0.5f) {
                targetSceneRotation = PVector(0f, 0f, random(-radians(90f), radians(90f)))
            } else {
                targetSceneRotation = PVector.random3D()
            }
        }
    }

    override fun draw() = with(sketch) {
        flyingSpeed = kontrol.slider1.midiRange(1f, 2f)
        affectedBeatPercentage = kontrol.slider2.midiRange(0f, 1f)
        expandOnBeatScale = kontrol.knob1.midiRange(0.5f, 1.5f)
        resizeOnBeatScaleMin = kontrol.knob2.midiRange(0.7f, 1.3f)
        resizeOnBeatScaleMax = kontrol.knob3.midiRange(0.7f, 1.3f)

        beatCounter.update()

        background(bgColor)
        translateCenter()
        sceneRotation = PVector.lerp(sceneRotation, targetSceneRotation, 0.001f)
        rotate(sceneRotation)

        updateObjects()

        flyingObjects.forEach {
            it.fillColor = null
            it.strokeWeight = strokeWeight
            it.strokeColor = fgColor
            it.draw(this)
        }
    }
}