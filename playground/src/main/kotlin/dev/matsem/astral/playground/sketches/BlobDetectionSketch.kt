package dev.matsem.astral.playground.sketches

import blobDetection.BlobDetection
import blobDetection.EdgeVertex
import ch.bildspur.postfx.builder.PostFX
import ch.bildspur.postfx.pass.NoisePass
import ch.bildspur.postfx.pass.PixelatePass
import ddf.minim.ugens.Oscil
import ddf.minim.ugens.Sink
import ddf.minim.ugens.Waves
import dev.matsem.astral.core.tools.extensions.*
import dev.matsem.astral.core.tools.osc.OscHandler
import dev.matsem.astral.core.tools.osc.OscManager
import dev.matsem.astral.core.tools.osc.oscFader
import dev.matsem.astral.core.tools.osc.oscKnob
import kotlinx.coroutines.*
import org.koin.core.KoinComponent
import org.koin.core.inject
import peasy.PeasyCam
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PGraphics
import java.io.File
import kotlin.math.absoluteValue

class BlobDetectionSketch : PApplet(), KoinComponent, CoroutineScope, OscHandler {

    override val coroutineContext = Dispatchers.Default
    override val oscManager: OscManager by lazy {
        OscManager(this, 7001, "192.168.1.11", 7001)
    }

    private val sink: Sink by inject()
    private lateinit var cam: PeasyCam

    private var oscilFreq by oscKnob("/play/fader1", 0.5f)
    private var elevScale by oscKnob("/play/fader2", 0.5f)
    private var noiseScl by oscKnob("/play/fader3", 0.5f)
    private var flicker by oscKnob("/play/fader4", 0.5f)
    private var speed by oscKnob("/play/fader5", 0.5f)
    private var bloomThresh by oscFader("/play/fader6", 0.5f)
    private var bloomSize by oscFader("/play/fader7", 0.2f)
    private var bloomSigma by oscFader("/play/fader8", 0.4f)

    private val levels = 25
    private var elevation = 200f
    private var noiseScale = 0f
    private lateinit var map: PGraphics

    private lateinit var blobDetectors: Array<BlobDetection>
    private val oscil: Oscil by lazy {
        Oscil(1f / 10f, 1f, Waves.SINE).apply { patch(sink) }
    }
    private lateinit var fx: PostFX

    override fun settings() {
        size(1280, 720, PConstants.P3D)
    }

    override fun setup() {
        surface.setResizable(true)
        colorModeHsb()

        cam = PeasyCam(this, 720.0)
        cam.wheelScale = 0.01
        cam.lookAt(width / 2.0, height / 2.0, 0.0)

        map = createGraphics(width / 10, height / 5, PConstants.P2D)
        blobDetectors =
            Array(levels) {
                BlobDetection(map.width, map.height).apply {
                    setThreshold(it.toFloat() / levels)
                }
            }

        prepareFx()
    }

    private fun prepareFx() {
        fx = PostFX(this)
        fx.preload(NoisePass::class.java)
        fx.preload(PixelatePass::class.java)
    }

    override fun draw() {
        oscil.frequency.lastValue = oscilFreq.mapp(0f, 0.5f)
        elevation = oscil.value.mapSin(0f, 1f) * elevScale.mapp(0f, 300f)
        generateMap()
        computeBlobs()
        background(0x18214d.withAlpha())
        pushPop {
            translateCenter()
            blobDetectors.forEachIndexed { i, detector ->
                pushPop {
                    noFill()
                    strokeWeight(2f)
                    val color = lerpColor(
                        color(200f, 100f, 100f),
                        color(200f, 100f, 80f),
                        i / levels.toFloat()
                    )
                    stroke(color)
                    translate(0f, 0f, elevation / levels * i)
                    drawContours(detector)
                }
            }
        }

        cam.drawHUD {
            fx.render().bloom(
                bloomThresh,
                (bloomSize * 100).toInt(),
                bloomSigma * 100f
            ).compose()
            pushPop {
                text("$frameRate", 10f, 20f)
                image(map, 10f, 30f)
            }
        }
    }

    private fun drawContours(detector: BlobDetection) {
        var edgeA: EdgeVertex
        var edgeB: EdgeVertex

        for (n in 0 until detector.blobNb) {
            val blob = detector.blob[n] ?: return
            if (blob.edgeNb > 3) {
                val firstEdge = blob.getEdgeVertexA(0)
                val lastEdge = blob.getEdgeVertexB(blob.edgeNb - 1)
                val threshold = flicker.mapp(0f, 1f)
                if ((firstEdge.x - lastEdge.x).absoluteValue > threshold || (firstEdge.y - lastEdge.y).absoluteValue > threshold) {
                    return
                }
                for (i in 0 until blob.edgeNb) {
                    edgeA = blob.getEdgeVertexA(i) ?: return
                    edgeB = blob.getEdgeVertexB(i) ?: return
                    line(
                        edgeA.x * map.width * 10f - map.width * 10f / 2f,
                        edgeA.y * map.height * 10f - map.height * 10f / 2f,
                        0f,
                        edgeB.x * map.width * 10f - map.width * 10f / 2f,
                        edgeB.y * map.height * 10f - map.height * 10f / 2f,
                        0f
                    )
                }
            }
        }
    }

    private fun generateMap() = with(map) {
        noiseScale = noiseScl.mapp(0.1f, 0.00001f)
        draw {
            colorModeHsb()
            clear()
            loadPixels()
            for (y in 0 until height) {
                for (x in 0 until width) {
                    pixels[x + (y * width)] = color(
                        0f,
                        0f,
                        noise(
                            x * noiseScale,
                            y * noiseScale - millis() * speed.mapp(0f, 0.001f),
                            0f
                        ) * 100f
                    )
                }
            }
            updatePixels()
        }
    }

    private fun computeBlobs() {
        blobDetectors.map {
            async { computeBlobs(it) }
        }.also {
            runBlocking {
                it.forEach { it.await() }
            }
        }
    }

    private suspend fun computeBlobs(detector: BlobDetection) {
        withContext(Dispatchers.IO) {
            detector.computeBlobs(map.pixels)
        }
    }

    /**
     * Sketch data path override. It's wrong when using local Processing installation core jars.
     * Sketch folder path cannot be passed as an argument, does not play well with DI.
     */
    override fun dataPath(where: String): String {
        return System.getProperty("user.dir") + "/data/" + where
    }

    /**
     * Sketch data path override. It's wrong when using local Processing installation core jars.
     * Sketch folder path cannot be passed as an argument, does not play well with DI.
     */
    override fun dataFile(where: String): File {
        return File(System.getProperty("user.dir") + "/data/" + where)
    }
}