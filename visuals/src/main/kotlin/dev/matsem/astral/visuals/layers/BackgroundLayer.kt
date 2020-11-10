package dev.matsem.astral.visuals.layers

import dev.matsem.astral.visuals.ColorHandler
import dev.matsem.astral.visuals.Colorizer
import dev.matsem.astral.visuals.Layer
import org.koin.core.KoinComponent
import org.koin.core.inject
import processing.core.PApplet
import processing.core.PGraphics

class BackgroundLayer : Layer(), ColorHandler, KoinComponent {

    override val parent: PApplet by inject()
    override val colorizer: Colorizer by inject()

    override fun PGraphics.draw() {
        clear()
        background(colorizer.bgColor)
    }
}