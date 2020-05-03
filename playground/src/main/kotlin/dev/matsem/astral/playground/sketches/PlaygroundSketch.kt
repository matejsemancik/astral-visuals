package dev.matsem.astral.playground.sketches

import dev.matsem.astral.core.tools.extensions.colorModeHsb
import processing.core.PApplet
import processing.core.PConstants

class PlaygroundSketch : PApplet() {

    override fun settings() {
        size(1280, 720, PConstants.P3D)
    }

    override fun setup() {
        colorModeHsb()
    }

    override fun draw() {
        background(100f, 100f, 100f)
    }
}