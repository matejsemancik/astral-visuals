package dev.matsem.astral.playground.sketches

import ch.bildspur.postfx.builder.PostFX
import dev.matsem.astral.core.tools.animations.AnimationHandler
import dev.matsem.astral.core.tools.animations.radianSeconds
import dev.matsem.astral.core.tools.animations.saw
import dev.matsem.astral.core.tools.audio.AudioProcessor
import dev.matsem.astral.core.tools.extensions.colorModeHsb
import dev.matsem.astral.core.tools.extensions.longerDimension
import dev.matsem.astral.core.tools.extensions.pushPop
import dev.matsem.astral.core.tools.extensions.quantize
import dev.matsem.astral.core.tools.extensions.remap
import dev.matsem.astral.core.tools.extensions.translate
import dev.matsem.astral.core.tools.extensions.translateCenter
import dev.matsem.astral.core.tools.extensions.withAlpha
import dev.matsem.astral.core.tools.kontrol.KontrolF1
import dev.matsem.astral.core.tools.kontrol.onTriggerPad
import dev.matsem.astral.core.tools.shapes.ExtrusionCache
import org.koin.core.KoinComponent
import org.koin.core.inject
import processing.core.PApplet
import processing.core.PVector
import kotlin.random.Random.Default.nextBoolean

/**
 * Visual for SEM002 release by Afterlife and Dizztrickt.
 */
class StillJazzy : PApplet(), AnimationHandler, KoinComponent {

    private lateinit var fx: PostFX
    private val kontrol: KontrolF1 by inject()
    private val ec: ExtrusionCache by inject()
    private val audioProcessor: AudioProcessor by inject()

    lateinit var lines: List<Line>
    lateinit var spheres: List<Sphere>

    override fun provideMillis(): Int = millis()

    override fun settings() {
        size(1080, 1080, P3D)
    }

    override fun setup() {
        colorModeHsb()
        surface.setTitle("Still Jazzy")
        surface.setResizable(true)
        fx = PostFX(this)
        kontrol.connect()
        generateScene()

        kontrol.onTriggerPad(0, 0) {
            generateScene()
        }
    }

    private fun generateScene() {
        lines = List(1000) {
            Line(
                vector = PVector.random3D().mult(random(0f, longerDimension().toFloat())),
                strokeWeight = random(6f, 20f),
                length = random(0f, 200f),
                speed = random(10f, 30f),
                alpha = 0.08f
            )
        } + List(100) {
            Line(
                vector = PVector.random3D().mult(random(0f, longerDimension().toFloat())),
                strokeWeight = random(2f, 6f),
                length = random(0f, 200f),
                speed = random(5f, 20f),
                alpha = 1f
            )
        }

        spheres = List(10) {
            Sphere(
                vector = PVector.random3D().mult(random(0f, longerDimension() * 2f)),
                radius = random(40f, 120f),
                sphereDetail = random(3f, 6f).toInt(),
                strokeWeight = random(2f, 3f),
                rotationVector = PVector.random3D(),
                rotationSpeed = random(5f, 10f) * (if (nextBoolean()) 1f else -1f),
                audioRange = (20f..random(200f, 2000f))
            )
        } + List(100) {
            Sphere(
                vector = PVector.random3D().mult(random(0f, longerDimension() * 2f)),
                radius = random(20f, 40f),
                sphereDetail = random(1f, 3f).toInt(),
                strokeWeight = random(2f, 3f),
                rotationVector = PVector.random3D(),
                rotationSpeed = random(5f, 10f) * (if (nextBoolean()) 1f else -1f),
                audioRange = (2000f..random(3000f, 10000f))
            )
        }
    }

    override fun draw() {
        drawSketch()
    }

    private fun PApplet.drawSketch() {
        background(0x000000.withAlpha())
        directionalLight(0f, 0f, 100f, 0f, -1f, 0f)
        fill(0x111111.withAlpha())
        stroke(0xffffff.withAlpha())
        colorModeHsb()

        translateCenter()
        val noiseX =
            noise(millis() / 10000f).remap(0f, 1f, -PI * 0.1f, PI * 0.1f) + saw(1 / 30f).remap(0f, 1f, 0f, TWO_PI)
        val noiseY = noise(millis() / 10000f + 1f).remap(0f, 1f, PI * 0.1f, -PI * 0.1f) + PI / 2f
        rotateY(noiseX)
        rotateX(noiseY)

        lines.forEach {
            pushPop {
                strokeWeight(it.strokeWeight)
                stroke(0xffffff.withAlpha(it.alpha.remap(0f, 1f, 0f, 255f).toInt()))
                translate(0f, 0f, -1000f)
                translate(it.vector.x, it.vector.y, it.vector.z)
                translate(0f, 0f, saw(1f / it.speed) * 1000f)
                line(0f, 0f, 0f, 0f, 0f, 0f + it.length)
            }
        }

        spheres.forEach {
            pushPop {
                stroke(0xffffff.withAlpha())
                strokeWeight(it.strokeWeight)
                sphereDetail(it.sphereDetail)
                translate(it.vector)
                rotateX(radianSeconds(it.rotationSpeed).quantize(0.4f) * it.rotationVector.x)
                rotateY(radianSeconds(it.rotationSpeed).quantize(0.2f) * it.rotationVector.y)
                rotateZ(radianSeconds(it.rotationSpeed).quantize(0.1f) * it.rotationVector.z)
                sphere(it.radius + audioProcessor.getRange(it.audioRange))
            }
        }

        fx.render().apply {
            bloom(0.3f, 100, 10f)
            bloom(0.3f, 100, 10f)
            noise(0.1f, 0.1f)
            compose()
        }
    }

    override fun mouseClicked() {
        generateScene()
    }
}

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