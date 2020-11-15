package dev.matsem.astral.playground.sketches

import ch.bildspur.postfx.builder.PostFX
import dev.matsem.astral.core.tools.animations.AnimationHandler
import dev.matsem.astral.core.tools.animations.radianHz
import dev.matsem.astral.core.tools.animations.saw
import dev.matsem.astral.core.tools.audio.AudioProcessor
import dev.matsem.astral.core.tools.extensions.colorModeHsb
import dev.matsem.astral.core.tools.extensions.mapp
import dev.matsem.astral.core.tools.extensions.pushPop
import dev.matsem.astral.core.tools.extensions.remap
import dev.matsem.astral.core.tools.extensions.translateCenter
import dev.matsem.astral.core.tools.extensions.withAlpha
import dev.matsem.astral.core.tools.shapes.ExtrusionCache
import dev.matsem.astral.core.tools.videoexport.VideoExporter
import org.koin.core.KoinComponent
import org.koin.core.inject
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PVector

class Proximity : PApplet(), KoinComponent, AnimationHandler {

    private val ec: ExtrusionCache by inject()
    private val audioProcessor: AudioProcessor by inject()
    private val videoExporter: VideoExporter by inject()

    override fun provideMillis(): Int {
        return videoExporter.videoMillis()
    }

    private lateinit var fx: PostFX

    val fixedFrameRate = 24f
    val numX = 5
    val numY = 5
    val numZ = 5
    val depth = 1080f

    data class Rotator(
        val rootationDirection: PVector,
        val rotationFrequency: Float,
        val rotationPhi: Float,
        val offsetX: Float,
        val offsetY: Float,
        val offsetZ: Float,
        val scale: Float,
        val freqRange: ClosedFloatingPointRange<Float>,
        val freqAmplitude: Float
    )

    val rotators: Array<Array<Array<Rotator>>> by lazy {
        Array(numZ) {
            Array(numX) {
                Array(numY) {
                    val freqStart = random(20f, 1000f)
                    val freqBandWidth = random(100f, 500f)
                    Rotator(
                        rootationDirection = PVector(random(1f), random(1f), random(1f)),
                        rotationFrequency = random(-0.1f, 0.1f),
                        rotationPhi = random(0f, 2 * PConstants.PI),
                        offsetX = random(-100f, 100f),
                        offsetY = random(-50f, 50f),
                        offsetZ = random(-100f, 100f),
                        scale = random(0.4f, 0.7f),
                        freqRange = (freqStart..freqStart + freqBandWidth),
                        freqAmplitude = 1.5f
                    )
                }
            }
        }
    }

    override fun settings() {
        size(1080, 1080, PConstants.P3D)
    }

    override fun setup() {
        randomSeed(420L)
        noiseSeed(420L)
        fx = PostFX(this)
        colorModeHsb()

        videoExporter.prepare(
            audioFilePath = "music/001clip02.wav",
            movieFps = fixedFrameRate,
            audioGain = 1f,
            dryRun = true
        ) {
            drawSketch()
        }
    }

    override fun draw() = Unit

    private fun PApplet.drawSketch() {
        val bgColor = 0x0f0f0f.withAlpha()
        val fgColor = 0xffffff.withAlpha()
        background(bgColor)
        fill(bgColor)
        stroke(fgColor)
        strokeWeight(random(2f, 3f))

        translateCenter()
        scale(saw(1 / 5f).mapp(1.2f, 1f))
        rotateY(frameCount / 500f)

        for (z in 0 until numZ) {
            for (x in 0 until numX) {
                for (y in 0 until numY) {
                    val rotator = rotators[z][x][y]
                    pushPop {
                        val centerZ = z * depth / numZ + rotator.offsetZ - depth / 2f
                        val centerX = x * width / numX + rotator.offsetX - width / 2f
                        val centerY = y * height / numY + rotator.offsetY - height / 2f
                        translate(
                            centerX + width / (numX * 2f),
                            centerY + height / (numY * 2f),
                            centerZ + depth / (numZ * 2f)
                        )
                        scale(
                            rotator.scale * audioProcessor.getRange(rotator.freqRange)
                                .remap(0f, 50f, 1f, rotator.freqAmplitude)
                        )

                        rotate(
                            radianHz(rotator.rotationFrequency) + rotator.rotationPhi,
                            rotator.rootationDirection.x,
                            rotator.rootationDirection.y,
                            rotator.rootationDirection.z
                        )
                        for (shape in ec.semLogo) {
                            shape.disableStyle()
                            shape(shape)
                        }
                    }
                }
            }
        }

        fx.render()
            .apply {
                bloom(0.5f, 40, 40f)
                if (frameCount % 120 in (0..10)) {
                    rgbSplit(random(50f))
                }
                noise(
                    audioProcessor.getRange(20f..100f).remap(0f, 50f, 0.05f, 0.09f),
                    0.4f
                )
                rgbSplit(20f)
            }.compose()
    }
}