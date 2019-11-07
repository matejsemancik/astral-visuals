package dev.matsem.astral.sketches.oldskool

import dev.matsem.astral.sketches.BaseSketch
import dev.matsem.astral.sketches.SketchLoader
import dev.matsem.astral.tools.audio.AudioProcessor
import dev.matsem.astral.tools.audio.beatcounter.BeatCounter
import dev.matsem.astral.tools.audio.beatcounter.OnKick
import dev.matsem.astral.tools.automator.MidiAutomator
import dev.matsem.astral.tools.extensions.*
import dev.matsem.astral.tools.galaxy.Galaxy
import dev.matsem.astral.tools.kontrol.KontrolF1
import dev.matsem.astral.tools.kontrol.onTriggerPad
import dev.matsem.astral.tools.shapes.ExtrusionCache
import dev.matsem.astral.tools.tapper.Tapper
import dev.matsem.astral.tools.video.VideoPreparationTool
import org.koin.core.inject
import processing.core.PApplet.lerp
import processing.core.PApplet.radians
import processing.core.PConstants.PI
import processing.core.PConstants.TWO_PI
import processing.core.PVector

/**
 * Taking it oldskool with raw shapes
 */
class OldSkoolSketch : BaseSketch() {

    enum class ExpandMode {
        TAP, KICK
    }

    enum class StrokeMode {
        TAP, FREQ, STILL, KICK
    }

    // TODO buttons
    enum class PositionMode {
        SCATTER, CENTER, RADIAL
    }

    companion object {
        val TEXTS = arrayOf(
                "A T T E M P T",
                "M A T S E M",
                "S B U",
                "ROUGH : RESULT",
                "J O H N E Y",
                "K I D  K O D A M A",
                "S E B A",
                "A S T R A L"
        )
    }

    override val sketch: SketchLoader by inject()
    private val beatCounter: BeatCounter by inject()
    private val automator: MidiAutomator by inject()
    private val tapper: Tapper by inject()
    private val kontrol: KontrolF1 by inject()
    private val galaxy: Galaxy by inject()
    private val extrusionCache: ExtrusionCache by inject()
    private val audioProcessor: AudioProcessor by inject()
    private val preparationTool: VideoPreparationTool by inject()

    private val zMax = sketch.longerDimension() * 2f

    private val flyingSpeedSlider = galaxy.createPot(12, 4, 0.2f, 15f, 1f)
    private val deadZoneSlider = galaxy.createPot(12, 5, 0f, zMax / 2f, zMax / 4f)
    private val textAwareRotationZAccelSlider = galaxy.createPot(12, 6, -PI * 0.005f, PI * 0.005f, 0f)
    private val textAwareRotationResetBtn = galaxy.createPushButton(12, 7) {
        textAwareRotationZAccelSlider.reset()
    }
    private val sceneRotationButton = galaxy.createToggleButton(12, 27, true)

    private val expandModeButtons = galaxy.createButtonGroup(12, listOf(8, 9), listOf(9))
    private val expandAffectedPercentageSlider = galaxy.createPot(12, 10, 0f, 1f, 0.5f)
    private val expandScalePot = galaxy.createPot(12, 11, 0f, 2f, 1f)

    private val strokeModeButtons = galaxy.createButtonGroup(12, listOf(12, 13, 14, 26), listOf(14))
    private val strokeControlSlider = galaxy.createPot(12, 15, 0f, 4f, 2f)
    private val fillToggleButton = galaxy.createToggleButton(12, 16, false)

    private val textClearBtn = galaxy.createPushButton(12, 17) {
        clearText()
    }

    private val textButtons = galaxy.createPushButtonGroup(12, listOf(18, 19, 20, 21, 22, 23, 24, 25)) {
        addText(TEXTS[it])
    }

    private var expandMode: ExpandMode = ExpandMode.TAP
    private var sceneRotation = PVector(0f, 0f, 0f)
    private var targetSceneRotation = PVector(0f, 0f, 0f)
    private var textAwareRotationZ = 0f

    private var strokeWeight = 2f
    private var strokeFreq = 1f
    private var strokeMode = StrokeMode.STILL
    private var beatStrokeWeight = 2f

    private val flyingObjects = mutableListOf<FlyingObject>()
    private val lock = Any()

    private fun newObject(): FlyingObject = with(sketch) {
        val random = random(1f)

        return when {
            random < 0.4f -> SemLogo(
                    cache = extrusionCache,
                    position = newRandomPosition(PositionMode.RADIAL),
                    rotation = PVector(0f, 0f, 0f),
                    rotationVector = PVector(random(-1e-2f, 1e-2f), random(-1e-2f, 1e-2f), random(-1e-2f, 1e-2f)),
                    size = 0f,
                    targetSize = random(10f, 20f)
            )
            else -> Box(
                    position = newRandomPosition(PositionMode.RADIAL),
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
                    position = newRandomPosition(PositionMode.CENTER).apply {
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

    private fun clearText() {
        synchronized(lock) {
            flyingObjects.removeIf { it is Text }
        }
    }

    private fun newRandomPosition(mode: PositionMode = PositionMode.SCATTER): PVector = with(sketch) {
        return when (mode) {
            PositionMode.SCATTER -> {
                var newPosition: PVector
                do {
                    newPosition = PVector.random3D().mult(zMax)
                } while (newPosition.isInRadius(20f))

                newPosition
            }

            PositionMode.CENTER -> {
                PVector.random3D().mult(zMax).apply {
                    x = 0f
                    y = 0f
                }
            }

            PositionMode.RADIAL -> {
                val angle = random(TWO_PI)
                val scatter = shorterDimension() / 6f
                val radius = shorterDimension() / 4f + random(-scatter, scatter)
                val z = random(zMax)
                PVector.fromAngle(angle)
                        .normalize()
                        .mult(radius)
                        .apply { this.z = z }
            }
        }
    }

    private fun resetObject(flyingObject: FlyingObject) = with(sketch) {
        val newPosition: PVector = when (flyingObject) {
            is Text -> newRandomPosition(PositionMode.CENTER).apply {
                x = 0f
                y = 0f
            }
            else -> newRandomPosition(PositionMode.RADIAL)
        }

        flyingObject.position = newPosition
        flyingObject.rotation = PVector(0f, 0f, 0f)
        flyingObject.size = 0f
        flyingObject.targetSize = random(10f, 20f)
    }

    private fun updateObjects() {
        flyingObjects.forEach {
            if (it.position.z > deadZoneSlider.value - it.size * 2f) {
                it.targetSize = 0f
            }

            it.update(flyingSpeedSlider.value)
            val isDead = it.position.z > deadZoneSlider.value
            if (isDead) {
                resetObject(it)
            }
        }
    }

    override fun setup() = with(sketch) {
        automator.setupWithGalaxy(
                channel = 12,
                recordButtonCC = 0,
                playButtonCC = 1,
                loopButtonCC = 2,
                clearButtonCC = 3,
                channelFilter = null
        )

        kontrol.onTriggerPad(0, 0, midiHue = 0) {
            tapper.tap()
        }

        tapper.doOnBeat {
            if (expandMode == ExpandMode.TAP) {
                flyingObjects.shuffled().take((flyingObjects.size * expandAffectedPercentageSlider.value).toInt()).forEach {
                    it.size *= expandScalePot.value
                }
            }
        }

        beatCounter.addListener(OnKick, 1) {
            if (expandMode == ExpandMode.KICK) {
                flyingObjects.shuffled().take((flyingObjects.size * expandAffectedPercentageSlider.value).toInt()).forEach {
                    it.size *= expandScalePot.value
                }
            }
        }

        beatCounter.addListener(OnKick, 1) {
            beatStrokeWeight = strokeWeight * 6f
        }

        beatCounter.addListener(OnKick, 24) {
            if (sceneRotationButton.isPressed) {
                if (random(1f) > 0.9f) {
                    targetSceneRotation = PVector(0f, 0f, random(-radians(90f), radians(90f)))
                } else {
                    targetSceneRotation = PVector.random3D()
                }
            }
        }

        repeat(500) { flyingObjects.add(newObject()) }
    }

    override fun draw() = with(sketch) {
        beatCounter.update()
        automator.update()

        textAwareRotationZ += textAwareRotationZAccelSlider.value
        expandMode = ExpandMode.values()[expandModeButtons.activeButtonsIndices(true).first()]
        strokeMode = StrokeMode.values()[strokeModeButtons.activeButtonsIndices(true).first()]
        strokeWeight = strokeControlSlider.rawValue.midiRange(0f, 4f)
        strokeFreq = strokeControlSlider.rawValue.midiRange(0.1f, 30f)
        beatStrokeWeight = lerp(beatStrokeWeight, strokeWeight, 0.4f)
        if (sceneRotationButton.isPressed.not()) {
            targetSceneRotation = PVector(0f, 0f, 0f)
        }
        sceneRotation = PVector.lerp(sceneRotation, targetSceneRotation, 0.001f)

        background(bgColor)
        translateCenter()
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
                        it.size += audioProcessor.getRange(20f..200f) * 0.02f
                        if (fillToggleButton.isPressed) {
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
                    StrokeMode.KICK -> beatStrokeWeight
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