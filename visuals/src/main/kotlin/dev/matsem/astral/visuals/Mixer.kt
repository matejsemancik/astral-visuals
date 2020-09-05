package dev.matsem.astral.visuals

import dev.matsem.astral.core.tools.extensions.colorModeHsb
import dev.matsem.astral.visuals.layers.BlobDetectionTerrain
import dev.matsem.astral.visuals.layers.TextOverlay
import org.koin.core.KoinComponent
import org.koin.core.inject
import processing.core.PApplet

class Mixer : PApplet(), KoinComponent {

    private val effector: Effector by inject()

    private val krestOverlay: TextOverlay by inject()
    private val blobDetection: BlobDetectionTerrain by inject()

    override fun settings() {
        size(1920, 1080, P2D)
    }

    override fun setup() {
        colorModeHsb()
        surface.setTitle("Astral Visuals")

        frameRate(24f)
    }

    override fun draw() {
        background(0)

        blobDetection.update()
        krestOverlay.update()

        image(blobDetection.canvas, 0f, 0f)
        image(krestOverlay.canvas, 0f, 0f)
        effector.render()
    }
}