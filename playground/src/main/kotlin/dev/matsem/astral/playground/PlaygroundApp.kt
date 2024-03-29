package dev.matsem.astral.playground

import dev.matsem.astral.core.di.coreModule
import dev.matsem.astral.playground.sketches.SEMLP001LogoRender
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import processing.core.PApplet

class PlaygroundApp : KoinComponent {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            PlaygroundApp().run(args)
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
            modules(coreModule + playgroundModule { SEMLP001LogoRender() })
        }

        PApplet.runSketch(processingArgs + arrayOf("ProcessingPlayground"), sketch)
    }
}