package dev.matsem.astral.sketches.blank

import dev.matsem.astral.sketches.BaseSketch
import dev.matsem.astral.sketches.SketchLoader
import org.koin.core.inject

class BlankSketch : BaseSketch() {

    override val sketch: SketchLoader by inject()

    override fun setup() = Unit

    override fun draw() = with(sketch) {
        background(0)
    }
}