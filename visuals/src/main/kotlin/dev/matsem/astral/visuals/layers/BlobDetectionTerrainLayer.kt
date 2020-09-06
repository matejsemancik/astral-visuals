package dev.matsem.astral.visuals.layers

import blobDetection.BlobDetection
import blobDetection.EdgeVertex
import ddf.minim.ugens.Oscil
import ddf.minim.ugens.Sink
import ddf.minim.ugens.Waves
import dev.matsem.astral.core.tools.audio.beatcounter.BeatCounter
import dev.matsem.astral.core.tools.audio.beatcounter.OnKick
import dev.matsem.astral.core.tools.extensions.colorModeHsb
import dev.matsem.astral.core.tools.extensions.draw
import dev.matsem.astral.core.tools.extensions.mapSin
import dev.matsem.astral.core.tools.extensions.mapp
import dev.matsem.astral.core.tools.extensions.pushPop
import dev.matsem.astral.core.tools.extensions.translateCenter
import dev.matsem.astral.core.tools.extensions.value
import dev.matsem.astral.core.tools.osc.OscHandler
import dev.matsem.astral.core.tools.osc.OscManager
import dev.matsem.astral.core.tools.osc.oscKnob
import dev.matsem.astral.core.tools.osc.oscPushButton
import dev.matsem.astral.core.tools.osc.oscToggleButton
import dev.matsem.astral.visuals.ColorHandler
import dev.matsem.astral.visuals.Colorizer
import dev.matsem.astral.visuals.Layer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.inject
import peasy.PeasyCam
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PGraphics
import kotlin.math.absoluteValue

class BlobDetectionTerrainLayer : Layer(), KoinComponent, CoroutineScope, OscHandler, ColorHandler {

    override val renderer: String = PConstants.P3D

    override val parent: PApplet by inject()
    override val oscManager: OscManager by inject()
    override val colorizer: Colorizer by inject()

    private val sink: Sink by inject()
    private val beatCounter: BeatCounter by inject()

    override val coroutineContext = Dispatchers.Default
    val camRotations = arrayOf(
        doubleArrayOf(-0.0, 0.0, -0.0, 800.0),
        doubleArrayOf(-0.797263, 0.5013783, -0.4376617, 984.4043796914573),
        doubleArrayOf(-2.7533004, 0.42473003, -2.9553256, 1028.1625192272145),
        doubleArrayOf(-0.09054168, -0.00491144, -3.0944278, 1028.1625192272145),
        doubleArrayOf(-2.8729408, -1.2027321, 1.8468192, 260.5817143545596),
        doubleArrayOf(-0.43025014, -0.078433305, -2.7281766, 546.8965049754636),
        doubleArrayOf(-1.959376, 7.200048E-4, -3.132293, 922.4871887350347)
    )

    private val cam = PeasyCam(parent, canvas, 720.0).apply {
        wheelScale = 0.01
        lookAt(canvas.width / 2.0, canvas.height / 2.0, 0.0)
    }

    private var oscilFreq by oscKnob("/blob/oscil/freq", defaultValue = 0.0f)
    private var elevScale by oscKnob("/blob/elevation/scale", defaultValue = 1f)
    private var noiseScl by oscKnob("/blob/noise/scale", defaultValue = 0.5f)
    private var noiseAuto by oscToggleButton("/blob/noise/auto", defaultValue = false)
    private var flicker by oscKnob("/blob/flicker", defaultValue = 0.5f)
    private var flickerAuto by oscToggleButton("/blob/flicker/auto", defaultValue = false)
    private var speed by oscKnob("/blob/speed", defaultValue = 0.5f)

    private val newCamAuto by oscToggleButton("/blob/cam/auto", defaultValue = false)
    private val newCamTrigger by oscPushButton("/blob/cam/new") { switchCam() }

    private val levels = 25
    private var elevation = 600f
    private var noiseScale = 0.1f
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

    init {
        beatCounter.addListener(OnKick, 16) {
            if (newCamAuto) {
                switchCam()
            }
        }

        beatCounter.addListener(OnKick, 8) {
            if (noiseAuto) {
                noiseScl = parent.random(0f, 0.8f)
            }
            if (flickerAuto) {
                flicker = parent.random(0.2f, 0.8f)
            }
        }
    }

    override fun PGraphics.draw() {
        clear()
        colorModeHsb()

        oscil.frequency.lastValue = oscilFreq.mapp(0f, 0.5f)
        elevation = oscil.value.mapSin(-1f, 1f) * elevScale.mapp(0f, 600f)
        generateMap()
        computeBlobs()
        pushPop {
            translateCenter()
            blobDetectors.forEachIndexed { i, detector ->
                pushPop {
                    noFill()
                    strokeWeight(2f)
                    stroke(colorizer.fgColor)
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

    private fun switchCam() {
        val rot = camRotations.random()
        cam.setRotations(rot[0], rot[1], rot[2])
        cam.setDistance(rot[3], 1000L)
    }
}