package dev.matsem.astral

import dev.matsem.astral.sketches.gameoflife.GameOfLifeSketch
import org.koin.core.KoinComponent
import processing.core.PApplet

class App : KoinComponent {

//    private val sketch: SketchLoader by inject()
    private val sketch = GameOfLifeSketch()

    fun run() {
        val processingArgs = arrayOf("Sketch")
        PApplet.runSketch(processingArgs, sketch)
    }
}