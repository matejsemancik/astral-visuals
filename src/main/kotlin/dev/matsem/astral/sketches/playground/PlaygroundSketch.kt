package dev.matsem.astral.sketches.playground

import org.koin.core.KoinComponent
import processing.core.PApplet
import processing.core.PConstants

// TODO to BaseSketch?
class PlaygroundSketch : PApplet(), KoinComponent {

    override fun settings() {
        size(1280, 720, PConstants.P3D)
    }

    override fun setup() {
        colorMode(PConstants.HSB, 360f, 100f, 100f)
    }

    override fun draw() = Unit
}