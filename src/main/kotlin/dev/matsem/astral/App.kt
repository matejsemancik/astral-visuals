package dev.matsem.astral

import dev.matsem.astral.sketches.starfield.StarfieldSketch
import org.koin.core.KoinComponent
import org.koin.core.inject
import processing.core.PApplet

class App : KoinComponent {

//    private val sketchLoader: SketchLoader by inject()
    private val sketch: StarfieldSketch by inject()

    fun run() {
        val processingArgs = arrayOf("Sketch")
        PApplet.runSketch(processingArgs, sketch)
    }
}