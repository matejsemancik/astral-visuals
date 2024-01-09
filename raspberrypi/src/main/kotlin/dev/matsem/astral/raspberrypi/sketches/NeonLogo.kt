package dev.matsem.astral.raspberrypi.sketches

import ch.bildspur.postfx.builder.PostFX
import dev.matsem.astral.core.Files
import dev.matsem.astral.core.tools.animations.AnimationHandler
import dev.matsem.astral.core.tools.animations.radianSeconds
import dev.matsem.astral.core.tools.extensions.*
import extruder.extruder
import geomerative.RG
import geomerative.RShape
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject
import processing.core.*
import java.io.File
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
class NeonLogo : PApplet(), AnimationHandler, KoinComponent {

    override fun provideMillis(): Int = millis()

    private lateinit var fx: PostFX
    private val ex: extruder by inject()
    private lateinit var font: PFont
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private lateinit var textFile: File
    private lateinit var displayedText: String

    private val logoToScreenScale = 0.35f
    private val shapeDepth = 100
    private var sclOff = 0f
    private var rotYOff = 0f
    private val starCount = 3000
    private val fps = 20f
    private lateinit var starsCanvas: PGraphics
    private lateinit var stars: List<PVector>
    private val logoPosition = PVector(0f, 0f)
    private val logoPositionTarget = PVector(0f, 0f)
    private val textPosition = PVector(0f, 0f)
    private val textPositionTarget = PVector(0f, 0f)
    private var lerpSpeed = 0.2f
    private val renderStyles = arrayOf(
        RenderStyle(fillColor = 0x00ffc8, strokeColor = 0x000000, strokeWeight = 10f),
        RenderStyle(fillColor = 0x000000, strokeColor = 0x00ffc8, strokeWeight = 10f),
        RenderStyle(fillColor = null, strokeColor = 0x00ffc8, strokeWeight = 10f)
    )
    private var renderStyle = renderStyles.first()

    private val props: Properties = Properties()
    private var logoActiveIntervalMs = 1000L
    private var textActiveIntervalMs = 1000L
    private var renderStyleSwitchIntervalMs = 1000L
    private val fileReadIntervalMs = 5_000L

    companion object {
        const val LineupFile = "lineup.txt"
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
        fullScreen(PConstants.P3D)
//        size(1024, 768, PConstants.P3D)
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
                        .flatMap { ex.extrude(it, 100, "box").toList() }
                        .onEach {
                            it.enableStyle()
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
        textFile = desktopFile(LineupFile)
        fx = PostFX(this)

        makeFiles()

        coroutineScope.launch {
            while (isActive) {
                logoPositionTarget.set(0f, 0f)
                textPositionTarget.set(widthF, 0f)
                kotlinx.coroutines.delay(logoActiveIntervalMs)

                logoPositionTarget.set(-widthF * 0.4f, 0f)
                textPositionTarget.set(-widthF * 0.15f, 0f)
                kotlinx.coroutines.delay(textActiveIntervalMs)
            }
        }

        coroutineScope.launch(Dispatchers.IO) {
            while (isActive) {
                displayedText = textFile.readText().trimIndent()

                props.load(desktopFile(PropsFile).inputStream())
                logoActiveIntervalMs = props["visuals.logo.duration_ms"].toString().toLongOrNull() ?: 35_000L
                textActiveIntervalMs = props["visuals.text.duration_ms"].toString().toLongOrNull() ?: 60_000L
                renderStyleSwitchIntervalMs =
                    props["visuals.logo.style.duration_ms"].toString().toLongOrNull() ?: 120_000L

                kotlinx.coroutines.delay(fileReadIntervalMs)
            }
        }
    }

    private fun makeFiles() {
        val textFile = desktopFile(LineupFile)
        val propsFile = desktopFile(PropsFile)
        if (textFile.exists().not()) {
            textFile.createNewFile()
            textFile.writeText(
                """
                    Edit ~/Desktop/lineup.txt bruv
                """.trimIndent()
            )
        }

        if (propsFile.exists().not()) {
            propsFile.createNewFile()
            propsFile.writeText(
                """
                    visuals.logo.style.duration_ms=5000
                    visuals.logo.duration_ms=5000
                    visuals.text.duration_ms=5000
                """.trimIndent()
            )
        }
    }

    override fun draw() {
        background(0)
        ortho()

        // Update props

        stars.forEach {
            it.z += 2f
            if (it.z > width) {
                it.z = random(-width.toFloat(), 0f)
            }
        }

        logoPosition.lerp(logoPositionTarget, lerpSpeed)
        textPosition.lerp(textPositionTarget, lerpSpeed)

        if (millis() % renderStyleSwitchIntervalMs in 0 until 1000) {
            renderStyle = renderStyles.random()
        }

        // endregion

        // region Starfield

        starsCanvas.draw {
            fill(0x000000.withAlpha(32))
            rect(0f, 0f, widthF, heightF)

            pushPop {
                noStroke()
                fill(0x00ffc8.withAlpha(128))
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

        // region Logo

        pushPop {
            translateCenter()
            translate(logoPosition)

            val renderStyle = renderStyle

            for (chunk in chunks) {
                pushPop {
                    scale(
                        width / chunk.shapeWidth * logoToScreenScale + sclOff,
                        width / chunk.shapeWidth * logoToScreenScale + sclOff
                    )

                    rotateX(PI * 0.1f * sin(radianSeconds(60f)))
                    rotateY(-radianSeconds(30f) + rotYOff)
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

        // region Info text

        pushPop {
            translateCenter()
            translate(textPosition)
            textFont(font)
            textAlign(LEFT, CENTER)
            noStroke()
            fill(0x00ffc8.withAlpha())
            text(displayedText, 0f, 0f)
        }

        // endregion

        fx.render().apply {
            noise(0.3f, 0.1f)
            pixelate(shorterDimension() / 2.4f)
        }.compose()
    }
}