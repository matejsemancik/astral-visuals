package dev.matsem.astral.sketches.oldskool

import dev.matsem.astral.sketches.BaseSketch
import dev.matsem.astral.sketches.SketchLoader
import dev.matsem.astral.tools.audio.beatcounter.BeatCounter
import dev.matsem.astral.tools.audio.beatcounter.OnKick
import dev.matsem.astral.tools.extensions.longerDimension
import dev.matsem.astral.tools.extensions.rotate
import dev.matsem.astral.tools.extensions.shorterDimension
import dev.matsem.astral.tools.extensions.translateCenter
import dev.matsem.astral.tools.kontrol.KontrolF1
import dev.matsem.astral.tools.kontrol.onTriggerPad
import dev.matsem.astral.tools.shapes.ExtrusionCache
import dev.matsem.astral.tools.tapper.Tapper
import org.koin.core.inject
import processing.core.PApplet.radians
import processing.core.PVector
import kotlin.math.absoluteValue

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
    private var logoGenerationThreshold = 0.90f
    private var resizeOnBeatScaleMin = 0.7f
    private var resizeOnBeatScaleMax = 1.1f
    private var expandOnBeatScale = 1.4f
    private var flyingSpeed = 1f
    private var strokeWeight = 4f

    private val flyingObjects = mutableListOf<FlyingObject>()

    private fun newObject(): FlyingObject = with(sketch) {
        if (random(1f) > logoGenerationThreshold) {
            return SemLogo(
                    cache = extrusionCache,
                    position = PVector.random3D().mult(random(longerDimension().toFloat())),
                    rotation = PVector(0f, 0f, 0f),
                    rotationVector = PVector(random(-1e-2f, 1e-2f), random(-1e-2f, 1e-2f), random(-1e-2f, 1e-2f)),
                    size = 0f,
                    targetSize = random(10f, 20f)
            )
        } else {
            return Box(
                    position = PVector.random3D().mult(random(longerDimension().toFloat())),
                    rotation = PVector(0f, 0f, 0f),
                    rotationVector = PVector(random(-1e-2f, 1e-2f), random(-1e-2f, 1e-2f), random(-1e-2f, 1e-2f)),
                    size = 0f,
                    targetSize = random(10f, 20f)
            )
        }
    }

    private fun replaceObjects() {
        var lostObjects = 0

        flyingObjects.removeIf {
            it.update(flyingSpeed)
            val isDead = it.position.z > deadZone
            lostObjects += if (isDead) 1 else 0
            return@removeIf isDead
        }

        repeat(lostObjects) {
            // Do not generate objects in straight center (less than 20 around middle)
            var obj: FlyingObject
            do {
                obj = newObject()
            } while (obj.position.x.absoluteValue < 20 && obj.position.y.absoluteValue < 20)

            flyingObjects.add(obj)
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
            flyingObjects.shuffled().take(flyingObjects.size / 10).forEach {
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
        beatCounter.update()

        background(bgColor)
        translateCenter()
        sceneRotation = PVector.lerp(sceneRotation, targetSceneRotation, 0.001f)
        rotate(sceneRotation)

        replaceObjects()

        flyingObjects.forEach {
            it.fillColor = null
            it.strokeWeight = strokeWeight
            it.strokeColor = fgColor
            it.draw(this)
        }
    }
}