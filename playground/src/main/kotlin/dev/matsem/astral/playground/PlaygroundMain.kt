@file:JvmName("PlaygroundMain")

package dev.matsem.astral.playground

import dev.matsem.astral.core.di.coreModule
import dev.matsem.astral.playground.sketches.PlaygroundSketch
import org.koin.core.context.startKoin

fun main(args: Array<String>) {
    startKoin {
        modules(coreModule + playgroundModule { PlaygroundSketch() })
    }

    PlaygroundApp().run(args)
}