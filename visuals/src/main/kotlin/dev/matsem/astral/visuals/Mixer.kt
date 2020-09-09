package dev.matsem.astral.visuals

import dev.matsem.astral.core.tools.extensions.mapp
import dev.matsem.astral.core.tools.extensions.withAlpha
import dev.matsem.astral.core.tools.osc.OscHandler
import dev.matsem.astral.core.tools.osc.OscManager
import dev.matsem.astral.core.tools.osc.controls.OscFader
import dev.matsem.astral.core.tools.osc.labelledOscFader
import dev.matsem.astral.visuals.layers.AttractorLayer
import dev.matsem.astral.visuals.layers.BackgroundLayer
import dev.matsem.astral.visuals.layers.BlobDetectionTerrainLayer
import dev.matsem.astral.visuals.layers.ConwayLayer
import dev.matsem.astral.visuals.layers.SphereLayer
import dev.matsem.astral.visuals.layers.TextOverlayLayer
import dev.matsem.astral.visuals.layers.galaxy.GalaxyLayer
import dev.matsem.astral.visuals.layers.stars.StarsLayer
import org.koin.core.KoinComponent
import org.koin.core.inject
import processing.core.PApplet

class Mixer(override val oscManager: OscManager) : KoinComponent, OscHandler {

    private val backgroundLayer: BackgroundLayer by inject()
    private val starsLayer: StarsLayer by inject()
    private val attractorLayer: AttractorLayer by inject()
    private val blobDetectionLayer: BlobDetectionTerrainLayer by inject()
    private val conwayLayer: ConwayLayer by inject()
    private val textOverlayLayer: TextOverlayLayer by inject()
    private val galaxyLayer: GalaxyLayer by inject()
    private val sphereLayer: SphereLayer by inject()

    private val channels = listOf<Pair<OscFader, Layer>>(
        labelledOscFader(address = "/mix/ch/1/value", label = "conway", defaultValue = 0f) to conwayLayer,
        labelledOscFader(address = "/mix/ch/2/value", label = "attract", defaultValue = 0f) to attractorLayer,
        labelledOscFader(address = "/mix/ch/3/value", label = "stars", defaultValue = 1f) to starsLayer,
        labelledOscFader(address = "/mix/ch/4/value", label = "blob", defaultValue = 0f) to blobDetectionLayer,
        labelledOscFader(address = "/mix/ch/5/value", label = "galaxy", defaultValue = 0f) to galaxyLayer,
        labelledOscFader(address = "/mix/ch/6/value", label = "sphere", defaultValue = 1f) to sphereLayer,
        labelledOscFader(address = "/mix/ch/10/value", label = "krest", defaultValue = 1f) to textOverlayLayer
    )

    fun render(parent: PApplet) = with(parent) {
        backgroundLayer.update()
        tint(0xffffff.withAlpha())
        image(backgroundLayer.canvas, 0f, 0f)

        channels
            .forEach { (fader, layer) ->
                if (fader.value > 0f) {
                    layer.update()
                    tint(fader.value.oscAlpha())
                    image(layer.canvas, 0f, 0f)
                }
            }
    }

    private fun Float.oscAlpha() = 0xffffff.withAlpha(this.mapp(0f, 255f).toInt())
}