package dev.matsem.astral.raspberrypi.sketches

import ch.bildspur.postfx.builder.PostFX
import dev.matsem.astral.core.Files
import dev.matsem.astral.core.tools.animations.AnimationHandler
import dev.matsem.astral.core.tools.animations.radianSeconds
import dev.matsem.astral.core.tools.extensions.colorModeHsb
import dev.matsem.astral.core.tools.extensions.draw
import dev.matsem.astral.core.tools.extensions.heightF
import dev.matsem.astral.core.tools.extensions.pushPop
import dev.matsem.astral.core.tools.extensions.shorterDimension
import dev.matsem.astral.core.tools.extensions.translate
import dev.matsem.astral.core.tools.extensions.translateCenter
import dev.matsem.astral.core.tools.extensions.widthF
import dev.matsem.astral.core.tools.extensions.withAlpha
import extruder.extruder
import geomerative.RG
import geomerative.RShape
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PFont
import processing.core.PGraphics
import processing.core.PShape
import processing.core.PVector

/**
 * Uses geomerative library to convert the futuredlogo.svg into 2D shape. Extruder library is then used to extrude the
 * logo into 3D shape. Renders with oldskool playstation-one-like effect using PostFX shaders.
 */
class NeonLogo : PApplet(), AnimationHandler, KoinComponent {

    override fun provideMillis(): Int = millis()

    override fun settings() {
//        fullScreen(PConstants.P3D)
        size(1024, 768, PConstants.P3D)
    }

    private lateinit var fx: PostFX
    private val ex: extruder by inject()
    private lateinit var font: PFont
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    private val logoToScreenScale = 0.35f
    private val shapeDepth = 100
    private var sclOff = 0f
    private var rotYOff = 0f
    private val starCount = 3000
    private val fps = 15f
    private lateinit var starsCanvas: PGraphics
    private lateinit var stars: List<PVector>

    private val logoPosition = PVector(0f, 0f)
    private val logoPositionTarget = PVector(0f, 0f)
    private val textPosition = PVector(0f, 0f)
    private val textPositionTarget = PVector(0f, 0f)
    private var lerpSpeed = 0.2f

    private val mainColor = 0x00ffc8.withAlpha()

    data class Chunk(
        val originalShape: RShape,
        val extrudedShape: List<PShape>,
        val shapeWidth: Float,
        val shapeHeight: Float
    )

    private lateinit var chunks: List<Chunk>
    override fun setup() {
        colorModeHsb()
        surface.setTitle("Futured")
        surface.setResizable(true)
        surface.hideCursor()
        frameRate(fps)

        RG.init(this)
        RG.setPolygonizer(RG.UNIFORMSTEP)
        RG.setPolygonizerStep(1f)

        chunks = listOf("images/semlogo_top.svg", "images/semlogo_bottom.svg")
            .map { uri -> RG.loadShape(uri) }
            .map { rshape -> RG.polygonize(rshape) }
            .map { rshape ->
                Chunk(
                    originalShape = rshape,
                    shapeWidth = rshape.width,
                    shapeHeight = rshape.height,
                    extrudedShape = rshape.children
                        .asSequence()
                        .map { it.points }
                        .map { points ->
                            createShape().apply {
                                beginShape()
                                for (point in points) {
                                    vertex(point.x, point.y)
                                }
                                endShape(PApplet.CLOSE)
                            }
                        }
                        .flatMap { ex.extrude(it, 100, "box").toList() }
                        .onEach {
                            it.disableStyle()
                            it.translate(-rshape.width / 2f, -rshape.height / 2f, -shapeDepth / 2f)
                        }
                        .toList()
                )
            }

        starsCanvas = createGraphics(width, height, PConstants.P3D)
        stars = generateSequence {
            PVector(
                random(-width.toFloat(), width.toFloat()),
                random(-width.toFloat(), width.toFloat()),
                random(-width.toFloat(), width.toFloat())
            )
        }.take(starCount).toList()

        font = createFont(Files.Font.JETBRAINS_MONO, height / 20f, false)
        fx = PostFX(this)

        coroutineScope.launch {
            while (isActive) {
                logoPositionTarget.set(0f, 0f)
                textPositionTarget.set(widthF, 0f)
                kotlinx.coroutines.delay(30_000L)

                logoPositionTarget.set(-widthF * 0.4f, 0f)
                textPositionTarget.set(-widthF * 0.2f, 0f)
                kotlinx.coroutines.delay(30_000L)
            }
        }
    }

    override fun draw() {
        background(0)

        // Update props

        stars.forEach {
            it.z += 2f
            if (it.z > width) {
                it.z = random(-width.toFloat(), 0f)
            }
        }

        logoPosition.lerp(logoPositionTarget, lerpSpeed)
        textPosition.lerp(textPositionTarget, lerpSpeed)

        // endregion

        // region Starfield

        starsCanvas.draw {
            fill(0x000000.withAlpha(32))
            rect(0f, 0f, widthF, heightF)

            pushPop {
                noStroke()
                fill(mainColor.withAlpha(128))
                translateCenter()

                stars.forEach {
                    pushPop {
                        translate(it.x, it.y, it.z)
                        circle(0f, 0f, 3f)
                    }
                }
            }
        }

        pushPop {
            translate(0f, 0f, -400f)
            image(starsCanvas, 0f, 0f)
        }

        // endregion

        ortho()

        // region Logo

        pushPop {
            translateCenter()
            translate(logoPosition)
            strokeWeight(10f)
            stroke(0)
            fill(mainColor)

            for (chunk in chunks) {
                pushPop {
                    scale(
                        width / chunk.shapeWidth * logoToScreenScale + sclOff,
                        width / chunk.shapeWidth * logoToScreenScale + sclOff
                    )

                    rotateX(PI * 0.1f * sin(radianSeconds(60f)))
                    rotateY(-radianSeconds(30f) + rotYOff)
                    chunk.extrudedShape.forEach {
                        shape(it)
                    }
                }
            }
        }

        // endregion

        // region Info text

        val text = """
            07:00pm matsem
            08:30pm attempt
            10:00pm SEBA
            11:30pm NEED FOR MIRRORS
            01:00am sbu & rough:result
        """.trimIndent()

        pushPop {
            translateCenter()
            translate(textPosition)
            textFont(font)
            textAlign(LEFT, CENTER)
            noStroke()
            fill(mainColor)
            text(text, 0f, 0f)
        }

        // endregion

        fx.render().apply {
            noise(0.3f, 0.1f)
            pixelate(shorterDimension() / 2.4f)
        }.compose()
    }
}