package dev.matsem.astral.raspberrypi

import org.koin.dsl.bind
import org.koin.dsl.module
import processing.core.PApplet

fun raspberryModule(providePApplet: () -> PApplet) = module {
    single { providePApplet() } bind PApplet::class
}