package dev.matsem.astral.playground.sketches

import ch.bildspur.postfx.builder.PostFX
import dev.matsem.astral.core.tools.animations.AnimationHandler
import dev.matsem.astral.core.tools.animations.radianHz
import dev.matsem.astral.core.tools.animations.radianSeconds
import dev.matsem.astral.core.tools.animations.saw
import dev.matsem.astral.core.tools.audio.AudioProcessor
import dev.matsem.astral.core.tools.extensions.colorModeHsb
import dev.matsem.astral.core.tools.extensions.mapp
import dev.matsem.astral.core.tools.extensions.pushPop
import dev.matsem.astral.core.tools.extensions.quantize
import dev.matsem.astral.core.tools.extensions.remap
import dev.matsem.astral.core.tools.extensions.translateCenter
import dev.matsem.astral.core.tools.extensions.withAlpha
import dev.matsem.astral.core.tools.shapes.ExtrusionCache
import dev.matsem.astral.core.tools.videoexport.VideoExporter
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PVector

/**
 * Visual for SEM001 release: Proximity / 40W by Johney.
 * https://semfree.bandcamp.com/album/proximity-40w
 *
 * Was also used to generate artwork for the first version of Soul Ex Machina slipmats.
 */
class Proximity : PApplet(), KoinComponent, AnimationHandler {

    private val ec: ExtrusionCache by inject()
    private val audioProcessor: AudioProcessor by inject()
    private val videoExporter: VideoExporter by inject()

    override fun provideMillis(): Int {
        return videoExporter.videoMillis()
    }

    private lateinit var fx: PostFX

    sealed class ExportConfig(
        val width: Int,
        val height: Int,
        val numX: Int,
        val numY: Int,
        val numZ: Int,
        val depth: Float
    ) {
        object Landscape : ExportConfig(1920, 1080,6, 5, 6, 1920f)
        object Portrait : ExportConfig(1080, 1920,5, 7, 5, 1920f)
        object Square : ExportConfig(1080, 1080, 5, 5, 5, 1080f)
    }

    val exportConfig = ExportConfig.Square
    val fixedFrameRate = 24f
    val dryRun = true

    val numX = exportConfig.numX
    val numY = exportConfig.numY
    val numZ = exportConfig.numZ
    val depth = exportConfig.depth

    var scale = 1f
    var targetScale = 1f
    var yRotationPhi = 0f
    var targetYRotationPhi = 0f

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
        size(exportConfig.width, exportConfig.height, PConstants.P3D)
    }

    override fun setup() {
        // Use the same seed for consistency between multiple exports
        randomSeed(420L)
        noiseSeed(420L)

        fx = PostFX(this)
        colorModeHsb()

        videoExporter.prepare(
            audioFilePath = "music/proximity-clip.wav",
            movieFps = fixedFrameRate,
            audioGain = 2f,
            dryRun = dryRun
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

        targetScale = saw(1 / 10f).mapp(1f, 1.5f)
        scale = lerp(scale, targetScale, 0.2f)
        targetYRotationPhi = radianSeconds(30f).quantize(PConstants.PI)
        yRotationPhi = lerp(yRotationPhi, targetYRotationPhi, 0.08f)

        scale(scale)
        rotateY(radianSeconds(60f) + yRotationPhi)

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
                    audioProcessor.getRange(20f..100f).remap(0f, 50f, 0.05f, 0.13f),
                    0.4f
                )
                rgbSplit(20f)
            }.compose()
    }
}