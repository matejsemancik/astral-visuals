package dev.matsem.astral.playground.sketches

import dev.matsem.astral.core.tools.extensions.colorModeHsb
import org.koin.core.KoinComponent
import processing.core.PApplet
import processing.core.PConstants

// Processing 4.0.1
// Can't load library: /Users/matsem/data/processing-proj/astral-visuals/natives/macosx-universal//gluegen_rt
// https://github.com/processing/processing4/issues/537
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