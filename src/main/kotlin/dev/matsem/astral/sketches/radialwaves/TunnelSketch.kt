package dev.matsem.astral.sketches.radialwaves

import dev.matsem.astral.sketches.BaseSketch
import dev.matsem.astral.sketches.SketchLoader
import dev.matsem.astral.tools.audio.AudioProcessor
import dev.matsem.astral.tools.extensions.*
import dev.matsem.astral.tools.kontrol.KontrolF1
import org.koin.core.inject
import processing.core.PApplet

class TunnelSketch : BaseSketch() {

    override val sketch: SketchLoader by inject()
    private val kontrol: KontrolF1 by inject()
    private val audioProcessor: AudioProcessor by inject()

    override fun onBecameActive() = with(sketch) {
        ellipseMode(PApplet.CENTER)
    }

    override fun setup() = Unit

    override fun draw() = Unit
}