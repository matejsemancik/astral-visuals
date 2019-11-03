package dev.matsem.astral.sketches.oldskool

import dev.matsem.astral.sketches.BaseSketch
import dev.matsem.astral.sketches.SketchLoader
import dev.matsem.astral.tools.audio.beatcounter.BeatCounter
import dev.matsem.astral.tools.audio.beatcounter.OnKick
import dev.matsem.astral.tools.extensions.*
import dev.matsem.astral.tools.kontrol.KontrolF1
import dev.matsem.astral.tools.kontrol.onTogglePad
import dev.matsem.astral.tools.kontrol.onTriggerPad
import dev.matsem.astral.tools.shapes.ExtrusionCache
import dev.matsem.astral.tools.tapper.Tapper
import org.koin.core.inject
import processing.core.PApplet.radians
import processing.core.PConstants.PI
import processing.core.PVector

/**
 * Taking it oldskool with raw shapes
 */
class OldSkoolSketch : BaseSketch() {

    enum class BeatMode {
        TAP, KICK
    }

    enum class StrokeMode {
        STILL, TAP, FREQ
    }

    override val sketch: SketchLoader by inject()
    private val beatCounter: BeatCounter by inject()
    private val tapper: Tapper by inject()
    private val kontrol: KontrolF1 by inject()
    private val extrusionCache: ExtrusionCache by inject()

    private var sceneRotation = PVector(0f, 0f, 0f)
    private var targetSceneRotation = PVector(0f, 0f, 0f)

    private var deadZone = sketch.shorterDimension().toFloat()
    private var expandOnBeatScale = 1.4f
    private var flyingSpeed = 1f
    private var affectedBeatPercentage = 0.5f
    private var beatMode: BeatMode = BeatMode.TAP
    private var strokeWeight = 2f
    private var strokeFreq = 1f
    private var strokeMode = StrokeMode.STILL
    private var bgFill = false
    private var textAwareRotationZ = 0f
    private var textAwareRotationZAccel = 0f

    private val flyingObjects = mutableListOf<FlyingObject>()
    private val lock = Any()

    private fun newObject(): FlyingObject = with(sketch) {
        val random = random(1f)

        return when {
            random < 0.2f -> SemLogo(
                    cache = extrusionCache,
                    position = newRandomPosition(allowCenter = false),
                    rotation = PVector(0f, 0f, 0f),
                    rotationVector = PVector(random(-1e-2f, 1e-2f), random(-1e-2f, 1e-2f), random(-1e-2f, 1e-2f)),
                    size = 0f,
                    targetSize = random(10f, 20f)
            )
            else -> Box(
                    position = newRandomPosition(allowCenter = false),
                    rotation = PVector(0f, 0f, 0f),
                    rotationVector = PVector(random(-1e-2f, 1e-2f), random(-1e-2f, 1e-2f), random(-1e-2f, 1e-2f)),
                    size = 0f,
                    targetSize = random(10f, 20f)
            )
        }
    }

    private fun addText(text: String) = with(sketch) {
        synchronized(lock) {
            flyingObjects += Text(
                    text = text,
                    cache = extrusionCache,
                    position = newRandomPosition().apply {
                        x = 0f
                        y = 0f
                    },
                    rotation = PVector(0f, 0f, 0f),
                    rotationVector = PVector(random(0.001f), random(0.001f), random(0.001f)),
                    size = 0f,
                    targetSize = random(20f, 40f)
            )
        }
    }

    private fun newRandomPosition(allowCenter: Boolean = true): PVector = with(sketch) {
        if (allowCenter) {
            return PVector.random3D().mult(random(longerDimension().toFloat()))
        }

        var newPosition: PVector
        do {
            newPosition = PVector.random3D().mult(random(longerDimension().toFloat()))
        } while (newPosition.isInRadius(20f))

        return newPosition
    }

    private fun resetObject(flyingObject: FlyingObject) = with(sketch) {
        val newPosition: PVector = when (flyingObject) {
            is Text -> newRandomPosition().apply {
                x = 0f
                y = 0f
            }
            else -> newRandomPosition(allowCenter = false)
        }

        flyingObject.position = newPosition
        flyingObject.rotation = PVector(0f, 0f, 0f)
        flyingObject.size = 0f
        flyingObject.targetSize = random(10f, 20f)
    }

    private fun updateObjects() {
        flyingObjects.forEach {
            if (it.position.z > deadZone - it.size * 2f) {
                it.targetSize = 0f
            }

            it.update(flyingSpeed)
            val isDead = it.position.z > deadZone
            if (isDead) {
                resetObject(it)
            }
        }
    }

    override fun setup() = with(sketch) {
        kontrol.onTriggerPad(0, 0, midiHue = 0) {
            tapper.tap()
        }

        kontrol.onTriggerPad(1, 0, midiHue = 10) {
            beatMode = BeatMode.TAP
        }

        kontrol.onTriggerPad(1, 1, midiHue = 10) {
            beatMode = BeatMode.KICK
        }

        kontrol.onTriggerPad(2, 0, midiHue = 30) {
            strokeMode = StrokeMode.TAP
        }

        kontrol.onTriggerPad(2, 1, midiHue = 30) {
            strokeMode = StrokeMode.FREQ
        }

        kontrol.onTriggerPad(2, 2, midiHue = 30) {
            strokeMode = StrokeMode.STILL
        }

        kontrol.onTogglePad(2, 3, midiHue = 40) {
            bgFill = it
        }

        kontrol.onTriggerPad(3, 0, midiHue = 60) {
            synchronized(lock) {
                flyingObjects.removeIf { it is Text }
            }
        }

        kontrol.onTriggerPad(3, 1, midiHue = 70) {
            addText("SEMTV")
        }

        kontrol.onTriggerPad(0, 1, midiHue = 70) {
            addText("A T T E M P T")
        }

        kontrol.onTriggerPad(0, 2, midiHue = 70) {
            addText("M A T S E M")
        }

        kontrol.onTriggerPad(0, 3, midiHue = 70) {
            addText("S B U")
        }

        kontrol.onTriggerPad(1, 2, midiHue = 70) {
            addText("ROUGH : RESULT")
        }

        kontrol.onTriggerPad(1, 3, midiHue = 70) {
            addText("J O H N E Y")
        }

        tapper.doOnBeat {
            if (beatMode == BeatMode.TAP) {
                flyingObjects.shuffled().take((flyingObjects.size * affectedBeatPercentage).toInt()).forEach {
                    it.size *= expandOnBeatScale
                }
            }
        }

        beatCounter.addListener(OnKick, 1) {
            if (beatMode == BeatMode.KICK) {
                flyingObjects.shuffled().take((flyingObjects.size * affectedBeatPercentage).toInt()).forEach {
                    it.size *= expandOnBeatScale
                }
            }
        }

        beatCounter.addListener(OnKick, 24) {
            if (random(1f) > 0.9f) {
                targetSceneRotation = PVector(0f, 0f, random(-radians(90f), radians(90f)))
            } else {
                targetSceneRotation = PVector.random3D()
            }
        }

        repeat(500) { flyingObjects.add(newObject()) }
    }

    override fun draw() = with(sketch) {
        flyingSpeed = kontrol.slider1.midiRange(0.2f, 4f)
        affectedBeatPercentage = kontrol.slider2.midiRange(0f, 1f)
        expandOnBeatScale = kontrol.knob1.midiRange(0f, 2f)
        strokeWeight = kontrol.knob2.midiRange(0f, 4f)
        strokeFreq = kontrol.knob2.midiRange(0.1f, 30f)
        deadZone = kontrol.slider3.midiRange(0f, shorterDimension().toFloat())
        textAwareRotationZAccel = kontrol.slider4.midiRange(-PI * 0.005f, PI * 0.005f)
        textAwareRotationZ += textAwareRotationZAccel

        beatCounter.update()

        background(bgColor)
        translateCenter()
        sceneRotation = PVector.lerp(sceneRotation, targetSceneRotation, 0.001f)
        rotate(sceneRotation)
        rotateZ(textAwareRotationZ)

        synchronized(lock) {
            updateObjects()

            flyingObjects.forEach {
                when (it) {
                    is Text -> {
                        fill(bgColor)
                        stroke(fgColor)
                    }
                    else -> {
                        if (bgFill) {
                            fill(bgColor)
                        } else {
                            noFill()
                        }

                        stroke(fgColor)
                    }
                }

                // constrain to 0.001f - bug with missing stroke in PShapes once stroke is set to 0
                val strokeW = when (strokeMode) {
                    StrokeMode.STILL -> strokeWeight
                    StrokeMode.FREQ -> saw(strokeFreq).mapp(0f, 4f)
                    StrokeMode.TAP -> saw(1000f / tapper.interval, tapper.prev).mapp(0f, strokeWeight)
                }.constrain(low = 0.001f)

                strokeWeight(strokeW)

                if (it is Text) {
                    pushMatrix()
                    rotateZ(-textAwareRotationZ)
                    it.draw(this)
                    popMatrix()
                } else {
                    it.draw(this)
                }
            }
        }
    }
}