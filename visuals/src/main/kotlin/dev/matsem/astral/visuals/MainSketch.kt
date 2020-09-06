package dev.matsem.astral.visuals

import dev.matsem.astral.core.tools.extensions.colorModeHsb
import org.koin.core.KoinComponent
import org.koin.core.inject
import processing.core.PApplet
import processing.core.PConstants

class MainSketch : PApplet(), KoinComponent {

    private val mixer: Mixer by inject()
    private val effector: Effector by inject()

    override fun settings() {
        size(1920, 1080, PConstants.P2D)
    }

    override fun setup() {
        colorModeHsb()
        surface.setTitle("Astral Visuals")
    }

    override fun draw() {
        mixer.render(this)
        effector.render()
    }
}