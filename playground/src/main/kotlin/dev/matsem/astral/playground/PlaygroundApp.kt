package dev.matsem.astral.playground

import org.koin.core.KoinComponent
import org.koin.core.inject
import processing.core.PApplet

class PlaygroundApp : KoinComponent {

    private val sketch: PApplet by inject()

    /**
     * Launches PApplet with specified arguments. Be sure to include --sketch-location argument for proper data
     * folder resolution (dir containing your data/ folder),
     * @see https://processing.github.io/processing-javadocs/core/
     */
    fun run(processingArgs: Array<String>) {
        PApplet.runSketch(processingArgs + arrayOf("ProcessingPlayground"), sketch)
    }
}