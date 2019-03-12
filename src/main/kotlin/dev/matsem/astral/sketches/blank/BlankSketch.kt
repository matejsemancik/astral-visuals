package dev.matsem.astral.sketches.blank

import dev.matsem.astral.sketches.BaseSketch
import dev.matsem.astral.sketches.SketchLoader
import org.koin.core.inject

class BlankSketch : BaseSketch() {

    override val sketch: SketchLoader by inject()

    override fun setup() {
        // nothing
    }

    override fun onBecameActive() {

    }

    override fun draw() {
        sketch.background(0)
    }
}