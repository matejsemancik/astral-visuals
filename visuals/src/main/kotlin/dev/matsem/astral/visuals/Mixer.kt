package dev.matsem.astral.visuals

import ch.bildspur.postfx.builder.PostFX
import dev.matsem.astral.core.tools.extensions.colorModeHsb
import dev.matsem.astral.visuals.layers.Countdown
import org.koin.core.KoinComponent
import org.koin.core.inject
import processing.core.PApplet

class Mixer : PApplet(), KoinComponent {

    private val krestOverlay: Countdown by inject()
    private val fx: PostFX by lazy { PostFX(this) }

    override fun settings() {
        size(1920, 1080, P2D)
    }

    override fun setup() {
        colorModeHsb()
        surface.setTitle("Astral Visuals")
    }

    override fun draw() {
        background(0)

        krestOverlay.update()
        image(krestOverlay.canvas, 0f, 0f)

//        fx.render()
//            .rgbSplit(20f)
//            .compose()
    }
}