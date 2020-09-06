package dev.matsem.astral.visuals

import dev.matsem.astral.core.tools.extensions.colorModeHsb
import dev.matsem.astral.visuals.layers.AttractorLayer
import dev.matsem.astral.visuals.layers.BackgroundLayer
import dev.matsem.astral.visuals.layers.BlobDetectionTerrainLayer
import dev.matsem.astral.visuals.layers.TextOverlayLayer
import org.koin.core.KoinComponent
import org.koin.core.inject
import processing.core.PApplet

class Mixer : PApplet(), KoinComponent {

    private val effector: Effector by inject()

    private val backgroundLayer: BackgroundLayer by inject()
    private val attractorLayer: AttractorLayer by inject()
    private val blobDetection: BlobDetectionTerrainLayer by inject()
    private val krestOverlay: TextOverlayLayer by inject()

    override fun settings() {
        size(1920, 1080, P2D)
    }

    override fun setup() {
        colorModeHsb()
        surface.setTitle("Astral Visuals")
    }

    override fun draw() {
        background(0)

        backgroundLayer.update()
        attractorLayer.update()
        krestOverlay.update()

        image(backgroundLayer.canvas, 0f, 0f)
        image(attractorLayer.canvas, 0f, 0f)
        image(krestOverlay.canvas, 0f, 0f)

        effector.render()
    }
}