package dev.matsem.astral.visuals

import ch.bildspur.postfx.builder.PostFX
import ch.bildspur.postfx.pass.NoisePass
import ch.bildspur.postfx.pass.PixelatePass
import ch.bildspur.postfx.pass.RGBSplitPass
import dev.matsem.astral.core.tools.extensions.colorModeHsb
import dev.matsem.astral.visuals.layers.BlobDetectionTerrain
import dev.matsem.astral.visuals.layers.TextOverlay
import org.koin.core.KoinComponent
import org.koin.core.inject
import processing.core.PApplet

class Mixer : PApplet(), KoinComponent {

    private val krestOverlay: TextOverlay by inject()
    private val blobDetection: BlobDetectionTerrain by inject()

    private val fx: PostFX by lazy { PostFX(this) }

    override fun settings() {
        size(1920, 1080, P2D)
    }

    override fun setup() {
        colorModeHsb()
        surface.setTitle("Astral Visuals")

        fx.preload(RGBSplitPass::class.java)
        fx.preload(NoisePass::class.java)
        fx.preload(PixelatePass::class.java)

        frameRate(24f)
    }

    override fun draw() {
        background(0)

        blobDetection.update()
        krestOverlay.update()

        image(blobDetection.canvas, 0f, 0f)
        image(krestOverlay.canvas, 0f, 0f)

        fx.render()
            .rgbSplit(20f)
            .noise(0.2f, 0.1f)
            .bloom(
                0.5f,
                (0.2f * 100).toInt(),
                0.4f * 100f
            )
            .compose()
    }
}