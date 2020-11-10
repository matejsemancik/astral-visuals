package dev.matsem.astral.visuals

import dev.matsem.astral.core.di.coreModule
import dev.matsem.astral.visuals.di.visualsModule
import org.koin.core.KoinComponent
import org.koin.core.context.startKoin
import org.koin.core.inject
import org.koin.core.logger.Level
import processing.core.PApplet

class VisualsApp : KoinComponent {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            VisualsApp().run(args)
        }
    }

    private val sketch: PApplet by inject()

    /**
     * Launches PApplet with specified arguments. Be sure to include --sketch-path argument for proper data
     * folder resolution (dir containing your Processing data folder),
     * @see https://processing.github.io/processing-javadocs/core/
     */
    fun run(processingArgs: Array<String>) {
        startKoin {
            printLogger(Level.ERROR)
            modules(coreModule + visualsModule { MainSketch() })
        }

        PApplet.runSketch(processingArgs + arrayOf("AstralVisuals"), sketch)
    }
}