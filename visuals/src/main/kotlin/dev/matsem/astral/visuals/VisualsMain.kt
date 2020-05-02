@file:JvmName("VisualsMain")

package dev.matsem.astral.visuals

import dev.matsem.astral.core.di.coreModule
import dev.matsem.astral.visuals.di.visualsModule
import org.koin.core.context.startKoin

fun main(args: Array<String>) {
    startKoin {
        modules(visualsModule + coreModule)
    }

    AstralVisuals().run(args)
}
