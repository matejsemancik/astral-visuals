package dev.matsem.astral.core.tools.shapes


import dev.matsem.astral.core.Files
import extruder.extruder
import geomerative.RG
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PShape

class ExtrusionCache(
    private val sketch: PApplet,
    private val ex: extruder
) {

    init {
        RG.init(sketch)
    }

    val semLogo: Array<PShape> by lazy {
        val upper = sketch.createShape().apply {
            beginShape()
            vertex(0f, 0f)
            vertex(96f, 0f)
            vertex(58f, 25f)
            vertex(28f, 25f)
            vertex(28f, 44f)
            vertex(0f, 60f)
            endShape(PConstants.CLOSE)
        }

        val lower = sketch.createShape().apply {
            beginShape()
            vertex(4f, 96f)
            vertex(42f, 71f)
            vertex(73f, 71f)
            vertex(73f, 52f)
            vertex(100f, 35f)
            vertex(100f, 96f)
            endShape(PConstants.CLOSE)
        }

        val depth = 50
        mutableListOf<PShape>().apply {
            this += ex.extrude(upper, depth, "box")
            this += ex.extrude(lower, depth, "box")
            forEach { it.translate(-50f, -50f, -depth / 2f) }
        }.toTypedArray()
    }

    private val textCache = mutableMapOf<String, Array<PShape>>()

    fun getText(text: String, fontSize: Int = 48, depth: Int = 20): Array<PShape> {
        return textCache.getOrPut(text) {

            val rShape = RG.getText(text, Files.Font.FFF_FORWARD, fontSize, PApplet.CENTER)

            RG.setPolygonizer(RG.UNIFORMSTEP)
            RG.setPolygonizerStep(1f)

            val letters = rShape.children

            letters
                .asSequence()
                .map { it.points }
                .map { points ->
                    sketch.createShape().apply {
                        beginShape()
                        for (point in points) {
                            vertex(point.x, point.y)
                        }
                        endShape(PApplet.CLOSE)
                    }
                }
                .flatMap { ex.extrude(it, depth, "box").toList() }
                .onEach { it.translate(0f, fontSize / 2f, -depth / 2f) }
                .toList()
                .toTypedArray()
        }
    }
}