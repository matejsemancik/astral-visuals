@file:JvmName("Main")

package dev.matsem.astral

import dev.matsem.astral.di.appModule
import org.koin.core.context.startKoin

fun main(args: Array<String>) {
    args.forEach {
        println(it)
    }

    startKoin {
        logger()
        modules(appModule)
    }

    App().run()
}
