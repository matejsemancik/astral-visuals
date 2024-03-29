package dev.matsem.astral.raspberrypi

import dev.matsem.astral.core.di.coreModule
import dev.matsem.astral.raspberrypi.sketches.NeonLogoFraktal
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import processing.core.PApplet

class RaspberryApp : KoinComponent {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            RaspberryApp().run(args)
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
            modules(coreModule + raspberryModule { NeonLogoFraktal() })
        }

        PApplet.runSketch(processingArgs + arrayOf("RaspberryVisuals"), sketch)
    }
}