package dev.matsem.astral.visuals

import dev.matsem.astral.visuals.sketches.SketchLoader
import org.koin.core.KoinComponent
import org.koin.core.inject
import processing.core.PApplet

class AstralVisuals : KoinComponent {

    private val sketch: SketchLoader by inject()

    fun run() {
        val processingArgs = arrayOf("Sketch")
        PApplet.runSketch(processingArgs, sketch)
    }
}