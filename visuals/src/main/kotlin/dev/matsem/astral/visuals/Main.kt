@file:JvmName("Main")

package dev.matsem.astral.visuals

import dev.matsem.astral.visuals.di.VisualsComponent
import org.koin.core.context.startKoin

fun main(args: Array<String>) {
    args.forEach {
        println(it)
    }

    startKoin {
        logger()
        modules(VisualsComponent.visualsModule)
    }

    AstralVisuals().run()
}
