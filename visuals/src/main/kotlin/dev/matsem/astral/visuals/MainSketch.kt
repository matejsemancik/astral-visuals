package dev.matsem.astral.visuals

import dev.matsem.astral.core.tools.extensions.colorModeHsb
import dev.matsem.astral.core.tools.galaxy.Galaxy
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import processing.core.PApplet
import processing.core.PConstants

class MainSketch : PApplet(), KoinComponent {

    private val mixer: Mixer by inject()
    private val effector: Effector by inject()
    private val galaxy: Galaxy by inject()
    private val engineRoom: EngineRoom by inject()

    override fun settings() {
        size(1920, 1080, PConstants.P2D)
    }

    override fun setup() {
        colorModeHsb()
        surface.setTitle("Astral Visuals")
        galaxy.connect()
        engineRoom
    }

    override fun draw() {
        mixer.render(this)
        effector.render()
    }
}