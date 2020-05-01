package dev.matsem.astral.visuals

import dev.matsem.astral.visuals.sketches.SketchLoader
import org.koin.core.KoinComponent
import org.koin.core.inject
import processing.core.PApplet

class AstralVisuals : KoinComponent {

    private val sketch: SketchLoader by inject()

    /**
     * Creates PApplet with specified arguments. Be sure to include --sketch-location argument for proper data
     * folder resolution (dir containing your data/ folder),
     * @see https://processing.github.io/processing-javadocs/core/
     */
    fun run(processingArgs: Array<String>) {
        println("processing args: ${processingArgs.joinToString { it }}")
        val args = processingArgs + arrayOf("SketchLoader")
        PApplet.runSketch(args, sketch)
    }
}