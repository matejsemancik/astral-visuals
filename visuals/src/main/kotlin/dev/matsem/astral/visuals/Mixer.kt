package dev.matsem.astral.visuals

import dev.matsem.astral.core.tools.extensions.mapp
import dev.matsem.astral.core.tools.extensions.withAlpha
import dev.matsem.astral.core.tools.osc.OscHandler
import dev.matsem.astral.core.tools.osc.OscManager
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

//    private val conway by oscFader("/mix/ch/1/value", defaultValue = 0f)
//    private val attractor by oscFader("/mix/ch/2/value", defaultValue = 0f)
//    private val stars by oscFader("/mix/ch/3/value", defaultValue = 1f)
//    private val blob by oscFader("/mix/ch/4/value", defaultValue = 0f)
//    private val galaxy by oscFader("/mix/ch/5/value", defaultValue = 0f)
//    private val sphere by oscFader("/mix/ch/6/value", defaultValue = 1f)
//    private val krest by oscFader("/mix/ch/10/value", defaultValue = 1f)

//    val channels = mapOf(
//       conwayLayer to
//    )

    fun render(parent: PApplet) = with(parent) {
        backgroundLayer.update()
        tint(0xffffff.withAlpha())
        image(backgroundLayer.canvas, 0f, 0f)

//        channels.forEachIndexed { index, oscFader ->
//            val sin = PApplet.sin(parent.millis() / 1000f + (0.1f * index)).mapSin(0f, 1f)
//            oscFader.setValue(sin)
//            oscFader.setLabel(sin.toString().take(4))
//        }

//        if (conway > 0f) {
//            conwayLayer.update()
//            tint(conway.oscAlpha())
//            image(conwayLayer.canvas, 0f, 0f)
//        }
//
//        if (attractor > 0f) {
//            attractorLayer.update()
//            tint(attractor.oscAlpha())
//            image(attractorLayer.canvas, 0f, 0f)
//        }
//
//        if (stars > 0f) {
//            starsLayer.update()
//            tint(stars.oscAlpha())
//            image(starsLayer.canvas, 0f, 0f)
//        }
//
//        if (blob > 0f) {
//            blobDetectionLayer.update()
//            tint(blob.oscAlpha())
//            image(blobDetectionLayer.canvas, 0f, 0f)
//        }
//
//        if (galaxy > 0f) {
//            galaxyLayer.update()
//            tint(galaxy.oscAlpha())
//            image(galaxyLayer.canvas, 0f, 0f)
//        }
//
//        if (sphere > 0f) {
//            sphereLayer.update()
//            tint(sphere.oscAlpha())
//            image(sphereLayer.canvas, 0f, 0f)
//        }
//
//        if (krest > 0f) {
//            textOverlayLayer.update()
//            tint(krest.oscAlpha())
//            image(textOverlayLayer.canvas, 0f, 0f)
//        }
    }

    private fun Float.oscAlpha() = 0xffffff.withAlpha(this.mapp(0f, 255f).toInt())
}