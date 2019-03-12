package dev.matsem.astral

import dev.matsem.astral.di.appModule
import org.koin.core.context.startKoin

fun main(args: Array<String>) {
    startKoin {
        logger()
        modules(appModule)
    }

    App().run()
}
