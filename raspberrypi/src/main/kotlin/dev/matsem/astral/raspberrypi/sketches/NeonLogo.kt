package dev.matsem.astral.raspberrypi.sketches

import ch.bildspur.postfx.builder.PostFX
import dev.matsem.astral.core.tools.animations.AnimationHandler
import dev.matsem.astral.core.tools.animations.radianSeconds
import dev.matsem.astral.core.tools.extensions.colorModeHsb
import dev.matsem.astral.core.tools.extensions.pushPop
import dev.matsem.astral.core.tools.extensions.quantize
import dev.matsem.astral.core.tools.extensions.shorterDimension
import dev.matsem.astral.core.tools.extensions.translateCenter
import dev.matsem.astral.core.tools.extensions.withAlpha
import extruder.extruder
import geomerative.RG
import geomerative.RShape
import org.koin.core.KoinComponent
import org.koin.core.inject
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PShape

/**
 * Uses geomerative library to convert the futuredlogo.svg into 2D shape. Extruder library is then used to extrude the
 * logo into 3D shape. Renders with oldskool playstation-one-like effect using PostFX shaders.
 */
class NeonLogo : PApplet(), AnimationHandler, KoinComponent {

    override fun provideMillis(): Int = millis()

    override fun settings() {
        fullScreen(PConstants.P3D)
    }

    private lateinit var fx: PostFX
    private val ex: extruder by inject()

    private val logoToScreenScale = 0.4f
    private val shapeDepth = 100
    private var sclOff = 0f
    private var rotYOff = 0f

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

        RG.init(this)
        RG.setPolygonizer(RG.UNIFORMSTEP)
        RG.setPolygonizerStep(1f)

        chunks = listOf("images/semlogo_vector_top.svg", "images/semlogo_vector_bot.svg")
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

        fx = PostFX(this)
    }

    override fun draw() {
        ortho()
        background(0)
        strokeWeight(6f)
        stroke(0x00ffc8.withAlpha())
        when {
            mouseX.toFloat() in (0f..width / 3f) -> noFill()
            mouseX.toFloat() in (width / 3f..width / 3f * 2f) -> fill(0xffffff.withAlpha(0))
            else -> fill(0)
        }

        if (random(0f, 10f).quantize(0.1f) == 0f) {
            sclOff = random(-0.1f, 0.1f)
            rotYOff = random(-PI * .2f, PI * .2f)
        }

        translateCenter()
        for (chunk in chunks) {
            pushPop {
                scale(
                    width / chunk.shapeWidth * logoToScreenScale + sclOff,
                    width / chunk.shapeWidth * logoToScreenScale + sclOff
                )

                rotateX(-PI * 0.1f)
                rotateY(radianSeconds(30f) + rotYOff)
                chunk.extrudedShape.forEach {
                    shape(it)
                }
            }
        }

        fx.render().apply {
            noise(0.2f, 0.1f)
            pixelate(shorterDimension() / 2.4f)
        }.compose()
    }
}