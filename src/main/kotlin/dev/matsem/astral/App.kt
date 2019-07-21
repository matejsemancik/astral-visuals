package dev.matsem.astral

import dev.matsem.astral.sketches.SketchLoader
import dev.matsem.astral.sketches.gameoflife.GameOfLifeSketch
import org.koin.core.KoinComponent
import org.koin.core.inject
import processing.core.PApplet

class App : KoinComponent {

    private val sketchLoader: SketchLoader by inject()

    fun run() {
        val processingArgs = arrayOf("Sketch")
//        PApplet.runSketch(processingArgs, sketchLoader)
        PApplet.runSketch(processingArgs, GameOfLifeSketch())
    }
}