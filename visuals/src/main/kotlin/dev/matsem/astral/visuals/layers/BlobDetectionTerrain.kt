package dev.matsem.astral.visuals.layers

import blobDetection.BlobDetection
import blobDetection.EdgeVertex
import ddf.minim.ugens.Oscil
import ddf.minim.ugens.Sink
import ddf.minim.ugens.Waves
import dev.matsem.astral.core.tools.extensions.*
import dev.matsem.astral.core.tools.osc.OscHandler
import dev.matsem.astral.core.tools.osc.OscManager
import dev.matsem.astral.core.tools.osc.oscFader
import dev.matsem.astral.core.tools.osc.oscKnob
import dev.matsem.astral.visuals.Layer
import kotlinx.coroutines.*
import org.koin.core.KoinComponent
import org.koin.core.inject
import peasy.PeasyCam
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PGraphics
import kotlin.math.absoluteValue

class BlobDetectionTerrain : Layer(), KoinComponent, CoroutineScope, OscHandler {

    override val parent: PApplet by inject()
    override val renderer: String = PConstants.P3D

    override val coroutineContext = Dispatchers.Default
    override val oscManager: OscManager by lazy {
        OscManager(parent, 7001, "192.168.1.11", 7001) // TODO inject
    }

    private val sink: Sink by inject()
    private val cam = PeasyCam(parent, canvas, 720.0).apply {
        wheelScale = 0.01
        lookAt(canvas.width / 2.0, canvas.height / 2.0, 0.0)
    }

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
    private val map = parent.createGraphics(canvas.width / 10, canvas.height / 10, PConstants.P2D)

    private val blobDetectors: Array<BlobDetection> =
        Array(levels) {
            BlobDetection(map.width, map.height).apply {
                setThreshold(it.toFloat() / levels)
            }
        }

    private val oscil: Oscil by lazy {
        Oscil(1f / 10f, 1f, Waves.SINE).apply { patch(sink) }
    }

    override fun PGraphics.draw() {
        colorModeHsb()

        oscil.frequency.lastValue = oscilFreq.mapp(0f, 0.5f)
        elevation = oscil.value.mapSin(0f, 1f) * elevScale.mapp(0f, 300f)
        generateMap()
        computeBlobs()
        background(0)
        pushPop {
            translateCenter()
            blobDetectors.forEachIndexed { i, detector ->
                pushPop {
                    noFill()
                    strokeWeight(2f)
                    stroke(0xffffff.withAlpha())
                    translate(0f, 0f, elevation / levels * i)
                    drawContours(detector)
                }
            }
        }
    }

    private fun drawContours(detector: BlobDetection) = with(canvas) {
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
                        parent.noise(
                            x * noiseScale,
                            y * noiseScale - parent.millis() * speed.mapp(0f, 0.001f),
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
}