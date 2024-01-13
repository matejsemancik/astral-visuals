package dev.matsem.astral.playground.sketches

import dev.matsem.astral.core.tools.extensions.colorModeHsb
import org.koin.core.component.KoinComponent
import processing.core.PApplet
import processing.core.PConstants

class Blank : PApplet(), KoinComponent {

    override fun settings() {
        size(420, 420, PConstants.P2D)
    }

    override fun setup() {
        colorModeHsb()
    }

    override fun draw() {
        background(0f, 100f, 100f)
    }
}