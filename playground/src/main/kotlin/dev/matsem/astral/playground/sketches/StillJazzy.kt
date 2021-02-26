package dev.matsem.astral.playground.sketches

import ch.bildspur.postfx.builder.PostFX
import dev.matsem.astral.core.tools.animations.AnimationHandler
import dev.matsem.astral.core.tools.animations.radianSeconds
import dev.matsem.astral.core.tools.animations.saw
import dev.matsem.astral.core.tools.audio.AudioProcessor
import dev.matsem.astral.core.tools.audio.beatcounter.BeatCounter
import dev.matsem.astral.core.tools.audio.beatcounter.OnKick
import dev.matsem.astral.core.tools.audio.beatcounter.OnSnare
import dev.matsem.astral.core.tools.extensions.colorModeHsb
import dev.matsem.astral.core.tools.extensions.longerDimension
import dev.matsem.astral.core.tools.extensions.mapp
import dev.matsem.astral.core.tools.extensions.pushPop
import dev.matsem.astral.core.tools.extensions.quantize
import dev.matsem.astral.core.tools.extensions.remap
import dev.matsem.astral.core.tools.extensions.shorterDimension
import dev.matsem.astral.core.tools.extensions.translate
import dev.matsem.astral.core.tools.extensions.translateCenter
import dev.matsem.astral.core.tools.extensions.withAlpha
import dev.matsem.astral.core.tools.kontrol.KontrolF1
import dev.matsem.astral.core.tools.kontrol.onTriggerPad
import dev.matsem.astral.core.tools.shapes.ExtrusionCache
import dev.matsem.astral.core.tools.videoexport.VideoExporter
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject
import processing.core.PApplet
import processing.core.PShape
import processing.core.PVector
import kotlin.random.Random.Default.nextBoolean

/**
 * Visual for SEM002 release by Afterlife and Dizztrickt.
 */
class StillJazzy : PApplet(), AnimationHandler, KoinComponent {

    sealed class ExportConfig(val fileName: String, val width: Int, val height: Int) {
        object Square : ExportConfig("sem002_loop_sqr.mp4", 1080, 1080)
        object Landscape : ExportConfig("sem002_loop_land.mp4", 1920, 1080)
        object Portrait : ExportConfig("sem002_loop_port.mp4", 1080, 1920)
    }

    private val exportConfig = ExportConfig.Portrait

    private val coroutineScope = GlobalScope

    private lateinit var fx: PostFX
    private val kontrol: KontrolF1 by inject()
    private val audioProcessor: AudioProcessor by inject()
    private val beatCounter: BeatCounter by inject()
    private val videoExporter: VideoExporter by inject()
    private val ec: ExtrusionCache by inject()

    lateinit var lines: List<Line>
    lateinit var spheres: List<Sphere>
    lateinit var logo: Logo

    private var psThreshold = 10f
    private var psWindow = 1f
    private var psLerpSpeed = 0.8f
    private var yRotationOffsetTarget = 0f
    private var yRotationOffset = 0f
    private var yRotationOffsetLerpSpeed = 0.9f

    override fun provideMillis(): Int = videoExporter.videoMillis()

    override fun settings() {
        size(exportConfig.width, exportConfig.height, P3D)
    }

    override fun setup() {
        colorModeHsb()
        surface.setTitle("Still Jazzy")
        surface.setResizable(true)
        fx = PostFX(this)
        kontrol.connect()
        val seed = 424L // With SEM Logo
//        val seed = 420L // Without SEM logo
        noiseSeed(seed)
        randomSeed(seed)
        generateScene()

        kontrol.onTriggerPad(0, 0) { glitchSequence() }
        kontrol.onTriggerPad(0, 1) { twist() }
        beatCounter.addListener(OnKick, 5) { glitchSequence() }
        beatCounter.addListener(OnSnare, 4) { twist() }

        videoExporter.prepare(
            audioFilePath = "music/sem002-clip.mp3",
            outputVideoFileName = exportConfig.fileName,
            movieFps = 24f,
            audioGain = 0.5f,
            dryRun = false
        ) {
            drawSketch()
        }
    }

    private fun glitch() {
        psThreshold = random(10f, 90f)
        psWindow = random(100f, longerDimension().toFloat())
        psLerpSpeed = random(0.5f, 0.90f)
    }

    private fun glitchSequence() = coroutineScope.launch {
        coroutineScope.launch {
            glitch()
            kotlinx.coroutines.delay(100L)
            glitch()
            kotlinx.coroutines.delay(200L)
            glitch()
        }
    }

    private fun twist() {
        yRotationOffsetLerpSpeed = 0.9f
        yRotationOffsetTarget = random(0f, TWO_PI * 2f)
    }

    private fun generateScene() {
        lines = List(1000) {
            Line(
                vector = PVector.random3D().mult(random(0f, shorterDimension().toFloat())),
                strokeWeight = random(6f, 20f),
                length = random(0f, 200f),
                speed = random(10f, 30f),
                alpha = 0.08f
            )
        } + List(100) {
            Line(
                vector = PVector.random3D().mult(random(0f, shorterDimension().toFloat())),
                strokeWeight = random(2f, 6f),
                length = random(0f, 200f),
                speed = random(5f, 20f),
                alpha = 1f
            )
        }

        spheres = List(10) {
            Sphere(
                vector = PVector.random3D().mult(random(0f, shorterDimension() * 2f)),
                radius = random(40f, 120f),
                sphereDetail = random(3f, 6f).toInt(),
                strokeWeight = random(2f, 3f),
                rotationVector = PVector.random3D(),
                rotationSpeed = random(5f, 10f) * (if (nextBoolean()) 1f else -1f),
                audioRange = (20f..random(200f, 2000f))
            )
        } + List(100) {
            Sphere(
                vector = PVector.random3D().mult(random(0f, shorterDimension() * 2f)),
                radius = random(20f, 40f),
                sphereDetail = random(1f, 3f).toInt(),
                strokeWeight = random(2f, 3f),
                rotationVector = PVector.random3D(),
                rotationSpeed = random(5f, 10f) * (if (nextBoolean()) 1f else -1f),
                audioRange = (2000f..random(3000f, 10000f))
            )
        }

        logo = Logo(
            ec.semLogo.toList()
        )
    }

    override fun draw() = Unit

    private fun PApplet.drawSketch() {
        background(0x000000.withAlpha())
        directionalLight(0f, 0f, 100f, 0f, -1f, 0f)
        colorModeHsb()

        translateCenter()
        yRotationOffset = lerp(yRotationOffset, yRotationOffsetTarget, yRotationOffsetLerpSpeed)
        val sceneRotationY = noise(millis() / 10000f).mapp(-PI * 0.1f, PI * 0.1f) +
                saw(fHz = 1 / 30f).mapp(0f, TWO_PI) +
                yRotationOffset

        val sceneRotationX = noise(millis() / 10000f + 1f).mapp(PI * 0.1f, -PI * 0.1f) + PI / 2f

        rotateY(sceneRotationY)
        rotateX(sceneRotationX)

        lines.forEach {
            pushPop {
                strokeWeight(it.strokeWeight)
                stroke(0xffffff.withAlpha(it.alpha.remap(0f, 1f, 0f, 255f).toInt()))
                translate(0f, 0f, -800f)
                translate(it.vector.x, it.vector.y, it.vector.z)
                translate(0f, 0f, saw(1f / it.speed) * 1000f)
                line(0f, 0f, 0f, 0f, 0f, 0f + it.length)
            }
        }

        spheres.forEach {
            pushPop {
                fill(0x111111.withAlpha())
                stroke(0xffffff.withAlpha())
                strokeWeight(it.strokeWeight)
                sphereDetail(it.sphereDetail)
                translate(it.vector)
                rotateX(radianSeconds(it.rotationSpeed).quantize(0.3f) * it.rotationVector.x)
                rotateY(radianSeconds(it.rotationSpeed).quantize(0.2f) * it.rotationVector.y)
                rotateZ(radianSeconds(it.rotationSpeed).quantize(0.1f) * it.rotationVector.z)
                sphere(it.radius + audioProcessor.getRange(it.audioRange))
            }
        }

        pushPop {
            scale(2f)
            rotateX(PI / 2f)
            noFill()
            stroke(0xffffff.withAlpha())
            strokeWeight(8f)
            for (shape in logo.shapes) {
                shape.disableStyle()
                shape(shape)
            }
        }

        fx.render().apply {
            bloom(0.3f, 100, 10f)
            bloom(0.3f, 100, 10f)
            noise(0.1f, 0.1f)
        }.compose()

        // region Pixel sorting

        psWindow = lerp(psWindow, 1f, psLerpSpeed)

        loadPixels()
        var nextIndex = 0
        for (i in pixels.indices) {
            if (i < nextIndex) {
                continue
            }
            val brightness = brightness(pixels[i])
            if (brightness > psThreshold) {
                nextIndex = (i + psWindow.toInt()).coerceAtMost(pixels.size)
                pixels.sort(fromIndex = i, toIndex = nextIndex - 1)
            }
        }
        updatePixels()

        // endregion
    }
}

data class Logo(
    val shapes: List<PShape>
)

data class Line(
    val vector: PVector,
    val strokeWeight: Float,
    val length: Float,
    val speed: Float,
    val alpha: Float
)

data class Sphere(
    val vector: PVector,
    val radius: Float,
    val sphereDetail: Int,
    val strokeWeight: Float,
    val rotationVector: PVector,
    val rotationSpeed: Float,
    val audioRange: ClosedFloatingPointRange<Float>
)