package dev.matsem.astral

import dev.matsem.astral.sketches.SketchLoader
import org.koin.core.KoinComponent
import org.koin.core.inject
import processing.core.PApplet

class App : KoinComponent {

    private val sketch: SketchLoader by inject()

    fun run() {
        val processingArgs = arrayOf("Sketch")
        PApplet.runSketch(processingArgs, sketch)
    }
}