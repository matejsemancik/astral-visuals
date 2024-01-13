package dev.matsem.astral.playground.sketches

import ch.bildspur.postfx.builder.PostFX
import dev.matsem.astral.core.tools.animations.AnimationHandler
import dev.matsem.astral.core.tools.animations.radianSeconds
import dev.matsem.astral.core.tools.extensions.colorModeHsb
import dev.matsem.astral.core.tools.extensions.quantize
import dev.matsem.astral.core.tools.extensions.shorterDimension
import dev.matsem.astral.core.tools.extensions.translateCenter
import dev.matsem.astral.core.tools.extensions.withAlpha
import extruder.extruder
import geomerative.RG
import geomerative.RShape
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PShape
import kotlin.properties.Delegates

/**
 * Uses geomerative library to convert the futuredlogo.svg into 2D shape. Extruder library is then used to extrude the
 * logo into 3D shape. Renders with oldskool playstation-one-like effect using PostFX shaders.
 */
class FuturedLogo : PApplet(), AnimationHandler, KoinComponent {

    override fun provideMillis(): Int = millis()

    override fun settings() {
        size(640, 640, PConstants.P3D)
    }

    private lateinit var originalShape: RShape
    private lateinit var extrudedShape: Array<PShape>
    private lateinit var fx: PostFX
    private val ex: extruder by inject()

    private val logoToScreenScale = 0.4f
    private var shapeWidth by Delegates.notNull<Float>()
    private var shapeHeight by Delegates.notNull<Float>()
    private val shapeDepth = 100
    private var sclOff = 0f
    private var rotYOff = 0f

    override fun setup() {
        colorModeHsb()
        surface.setTitle("Futured")
        surface.setResizable(true)

        RG.init(this)
        originalShape = RG.loadShape("images/futuredlogo.svg")
        RG.setPolygonizer(RG.UNIFORMSTEP)
        RG.setPolygonizerStep(1f)
        val rshape = RG.polygonize(originalShape)

        shapeWidth = rshape.width
        shapeHeight = rshape.height

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
                it.translate(-shapeWidth / 2f, -shapeHeight / 2f, -shapeDepth / 2f)
            }
            .toList()
            .toTypedArray()

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
//        if (mouseX > width / 2f) {
//            noFill()
//        } else {
//            fill(0)
//        }

        if (random(0f, 10f).quantize(0.1f) == 0f) {
            sclOff = random(-0.1f, 0.1f)
            rotYOff = random(-PI * .2f, PI * .2f)
        }

        translateCenter()
        scale(
            width / shapeWidth * logoToScreenScale + sclOff,
            width / shapeWidth * logoToScreenScale + sclOff
        )

        rotateX(-PI * 0.1f)
        rotateY(radianSeconds(30f) + rotYOff)
        extrudedShape.forEach {
            shape(it)
        }

        fx.render().apply {
            bloom(0.3f, 100, 10f)
            bloom(0.3f, 100, 10f)
            noise(0.2f, 0.1f)
            pixelate(shorterDimension() / 2.4f)
        }.compose()
    }
}