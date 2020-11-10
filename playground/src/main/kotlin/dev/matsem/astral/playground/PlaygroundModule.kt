package dev.matsem.astral.playground

import org.koin.dsl.bind
import org.koin.dsl.module
import processing.core.PApplet

fun playgroundModule(providePApplet: () -> PApplet) = module {
    single { providePApplet() } bind PApplet::class
}