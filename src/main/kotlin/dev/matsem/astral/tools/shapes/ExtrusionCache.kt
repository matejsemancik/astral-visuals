package dev.matsem.astral.tools.shapes

import extruder.extruder
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PShape

class ExtrusionCache(
        sketch: PApplet,
        ex: extruder
) {
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

        mutableListOf<PShape>().apply {
            this += ex.extrude(upper, 40, "box")
            this += ex.extrude(lower, 40, "box")
            forEach { it.translate(-50f, -50f) }
        }.toTypedArray()
    }
}