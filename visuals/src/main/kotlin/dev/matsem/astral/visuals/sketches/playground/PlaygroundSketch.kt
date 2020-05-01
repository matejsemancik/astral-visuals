package dev.matsem.astral.visuals.sketches.playground

import dev.matsem.astral.core.tools.extensions.colorModeHsb
import org.koin.core.KoinComponent
import processing.core.PApplet
import processing.core.PConstants

// TODO to BaseSketch?
class PlaygroundSketch : PApplet(), KoinComponent {

    override fun settings() {
        size(1280, 720, PConstants.P3D)
    }

    override fun setup() {
        colorModeHsb()
    }

    override fun draw() = Unit
}