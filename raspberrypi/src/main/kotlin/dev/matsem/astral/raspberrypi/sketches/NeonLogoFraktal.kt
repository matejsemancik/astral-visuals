package dev.matsem.astral.raspberrypi.sketches

import ch.bildspur.postfx.builder.PostFX
import dev.matsem.astral.core.tools.animations.AnimationHandler
import dev.matsem.astral.core.tools.animations.radianSeconds
import dev.matsem.astral.core.tools.extensions.colorModeHsb
import dev.matsem.astral.core.tools.extensions.longerDimension
import dev.matsem.astral.core.tools.extensions.pushPop
import dev.matsem.astral.core.tools.extensions.shorterDimension
import dev.matsem.astral.core.tools.extensions.translateCenter
import dev.matsem.astral.core.tools.extensions.withAlpha
import extruder.extruder
import geomerative.RG
import geomerative.RShape
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PShape
import java.util.*

/**
 * The standalone raspberry pi sketch.
 *
 * Uses geomerative library to convert an SVG logo into 2D shape. Extruder library is then used to extrude the
 * logo into 3D shape. Renders with oldskool playstation-one-like effect using PostFX shaders.
 *
 * This sketch creates lineup.txt and render.properties files on your desktop. Use them to display text and modify render
 * settings of the scene. The files are watched and the sketch content will be updated upon modification of these files.
 */
class NeonLogoFraktal : PApplet(), AnimationHandler, KoinComponent {

    override fun provideMillis(): Int = millis()

    private lateinit var fx: PostFX
    private val ex: extruder by inject()
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    private var logoToScreenScale = 0.5f
    private val shapeDepth = 100
    private var sclOff = 0f
    private var rotationYPeriod = 30f
    private var rotationXPeriod = 60f
    private val fps = 24f
    private val renderStyles = arrayOf(
        RenderStyle(fillColor = 0xffffff, strokeColor = 0xff0000, strokeWeight = 20f),
        RenderStyle(fillColor = 0xff0000, strokeColor = 0xffffff, strokeWeight = 20f),
        RenderStyle(fillColor = 0x000000, strokeColor = 0xffffff, strokeWeight = 20f),
        RenderStyle(fillColor = null, strokeColor = 0xff0000, strokeWeight = 20f),
        RenderStyle(fillColor = 0xff0000, strokeColor = 0xffffff, strokeWeight = 20f)
    )
    private var renderStyle = renderStyles.first()

    private val props: Properties = Properties()
    private var renderStyleSwitchIntervalMs = 1000L
    private val fileReadIntervalMs = 5_000L

    private var cageRotationYPeriod = 120f
    private var cageRotationXPeriod = 200f

    companion object {
        const val PropsFile = "render.properties"
    }

    data class Chunk(
        val originalShape: RShape,
        val extrudedShape: List<PShape>,
        val shapeWidth: Float,
        val shapeHeight: Float
    )

    data class RenderStyle(
        val fillColor: Int?,
        val strokeColor: Int,
        val strokeWeight: Float
    )

    private lateinit var chunks: List<Chunk>
    override fun settings() {
//        fullScreen(PConstants.P3D, 2)
        size(1024, 768, PConstants.P3D)
    }

    override fun setup() {
        colorModeHsb()
        surface.setTitle("SEM_Party")
        surface.setResizable(true)
        surface.hideCursor()
        surface.setAlwaysOnTop(true)
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
                        .flatMap { ex.extrude(it, shapeDepth, "box").toList() }
                        .onEach {
                            it.enableStyle()
                            it.translate(-rshape.width / 2f, -rshape.height / 2f, -shapeDepth / 2f)
                        }
                        .toList()
                )
            }

        fx = PostFX(this)

        makeProps()
        coroutineScope.launch(Dispatchers.IO) {
            while (isActive) {
                props.load(desktopFile(PropsFile).inputStream())
                readProps(props)
                kotlinx.coroutines.delay(fileReadIntervalMs)
            }
        }
    }

    private fun makeProps() {
        val propsFile = desktopFile(PropsFile)
        if (propsFile.exists().not()) {
            propsFile.createNewFile()
            propsFile.writeText(
                """
                    visuals.logo.style.duration_ms=30000
                    visuals.logo.scale_ratio=0.50
                    visuals.logo.rotation.y.period_sec=12
                    visuals.logo.rotation.x.period_sec=53
                    visuals.cage.rotation.y.period_sec=120
                    visuals.cage.rotation.x.period_sec=200
                """.trimIndent()
            )
        }
    }

    private fun readProps(props: Properties) {
        renderStyleSwitchIntervalMs =
            props["visuals.logo.style.duration_ms"].toString().toLongOrNull() ?: 120_000L
        logoToScreenScale = props["visuals.logo.scale_ratio"].toString().toFloatOrNull() ?: 0.5f
        rotationYPeriod = props["visuals.logo.rotation.y.period_sec"].toString().toFloatOrNull() ?: 60f
        rotationXPeriod = props["visuals.logo.rotation.x.period_sec"].toString().toFloatOrNull() ?: 20f
        cageRotationYPeriod = props["visuals.cage.rotation.y.period_sec"].toString().toFloatOrNull() ?: 120f
        cageRotationXPeriod = props["visuals.cage.rotation.x.period_sec"].toString().toFloatOrNull() ?: 200f
    }

    override fun draw() {
        background(0)
        ortho()

        // region Update props

        if (millis() % renderStyleSwitchIntervalMs in 0 until 1000) {
            renderStyle = renderStyles.random()
        }

        // endregion

        // region Logo

        pushPop {
            translateCenter()

            val renderStyle = renderStyle

            for (chunk in chunks) {
                pushPop {
                    scale(
                        width / chunk.shapeWidth * logoToScreenScale + sclOff,
                        width / chunk.shapeWidth * logoToScreenScale + sclOff
                    )

                    rotateX(PI * 0.2f * sin(radianSeconds(rotationXPeriod)))
                    rotateY(radianSeconds(rotationYPeriod))
                    chunk.extrudedShape.forEach {
                        it.setFill(true)
                        if (renderStyle.fillColor != null) {
                            it.setFill(renderStyle.fillColor.withAlpha())
                        } else {
                            it.setFill(0x00000000)
                        }
                        it.setStroke(true)
                        it.setStroke(renderStyle.strokeColor.withAlpha())
                        it.setStrokeWeight(renderStyle.strokeWeight)
                        shape(it)
                    }
                }
            }
        }

        // endregion

        pushPop {
            translateCenter()
            pushPop {
                for(i in 0 until 6) {
                    rotateY(radianSeconds(cageRotationYPeriod))
                    noFill()
                    strokeWeight(10f)
                    stroke(0xff0000.withAlpha())
                    circle(0f, 0f, longerDimension() * 1.2f)
                }
            }

            pushPop {
                for(i in 0 until 6) {
                    rotateX(radianSeconds(cageRotationXPeriod))
                    noFill()
                    strokeWeight(2f)
                    stroke(0xff0000.withAlpha())
                    circle(0f, 0f, longerDimension() * 1.5f)
                }
            }
        }

        fx.render().apply {
            pixelate(shorterDimension() / 2.4f)
        }.compose()
    }
}