package dev.matsem.astral.playground

import dev.matsem.astral.core.di.coreModule
import dev.matsem.astral.playground.sketches.Blank
import org.koin.core.KoinComponent
import org.koin.core.context.startKoin
import org.koin.core.inject
import org.koin.core.logger.Level
import processing.core.PApplet

class PlaygroundApp : KoinComponent {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            // https://github.com/processing/processing4/issues/537 This somehow works but does not solve the native lib issue with other processing libraries
            // System.setProperty("java.library.path", "/Applications/Processing.app/Contents/Java/core/library/macos-x86_64")
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
            modules(coreModule + playgroundModule { Blank() })
        }

        PApplet.runSketch(processingArgs + arrayOf("ProcessingPlayground"), sketch)
    }
}