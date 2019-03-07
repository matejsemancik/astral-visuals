package dev.matsem.astral

import dev.matsem.astral.di.appModule
import dev.matsem.astral.sketches.SketchLoader
import org.koin.core.KoinComponent
import org.koin.core.context.startKoin
import org.koin.core.inject
import processing.core.PApplet

fun main(args: Array<String>) {
    startKoin {
        logger()
        modules(appModule)
    }

    App().start()
}

class App : KoinComponent {

    private val sketchLoader: SketchLoader by inject()

    fun start() {
        val processingArgs = arrayOf("Sketch")
        PApplet.runSketch(processingArgs, sketchLoader)
    }
}