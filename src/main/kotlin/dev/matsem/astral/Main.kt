package dev.matsem.astral
import dev.matsem.astral.sketches.SketchLoader
import processing.core.PApplet

fun main(args: Array<String>) {
    val processingArgs = arrayOf("Sketch")
    val sketch = SketchLoader()
    PApplet.runSketch(processingArgs, sketch)
}