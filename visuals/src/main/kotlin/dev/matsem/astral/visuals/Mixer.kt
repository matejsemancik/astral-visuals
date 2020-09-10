package dev.matsem.astral.visuals

import dev.matsem.astral.core.tools.extensions.mapp
import dev.matsem.astral.core.tools.extensions.withAlpha
import dev.matsem.astral.core.tools.osc.OscHandler
import dev.matsem.astral.core.tools.osc.OscManager
import dev.matsem.astral.core.tools.osc.controls.OscFader
import dev.matsem.astral.core.tools.osc.controls.OscToggleButton
import dev.matsem.astral.core.tools.osc.labelledOscFader
import dev.matsem.astral.core.tools.osc.listenableOscFader
import dev.matsem.astral.core.tools.osc.oscLabelIndicatorDelegate
import dev.matsem.astral.core.tools.osc.oscToggleButton
import dev.matsem.astral.core.tools.osc.oscToggleButtonDelegate
import dev.matsem.astral.visuals.layers.AttractorLayer
import dev.matsem.astral.visuals.layers.BackgroundLayer
import dev.matsem.astral.visuals.layers.BlobDetectionTerrainLayer
import dev.matsem.astral.visuals.layers.ConwayLayer
import dev.matsem.astral.visuals.layers.HexLayer
import dev.matsem.astral.visuals.layers.SphereLayer
import dev.matsem.astral.visuals.layers.TextOverlayLayer
import dev.matsem.astral.visuals.layers.debris.DebrisLayer
import dev.matsem.astral.visuals.layers.galaxy.GalaxyLayer
import dev.matsem.astral.visuals.layers.stars.StarsLayer
import org.koin.core.KoinComponent
import org.koin.core.inject
import processing.core.PApplet
import java.time.Duration
import java.time.LocalDateTime

data class Channel(
    val fader: OscFader,
    val layer: Layer,
    val autopilotEnable: OscToggleButton
)

class Mixer(override val oscManager: OscManager) : KoinComponent, OscHandler {

    companion object {
        const val AUTOPILOT_MIN_MS = 10L
        const val AUTOPILOT_MAX_MS = 3 * 60 * 1000L
    }

    private val backgroundLayer: BackgroundLayer by inject()
    private val starsLayer: StarsLayer by inject()
    private val attractorLayer: AttractorLayer by inject()
    private val blobDetectionLayer: BlobDetectionTerrainLayer by inject()
    private val conwayLayer: ConwayLayer by inject()
    private val textOverlayLayer: TextOverlayLayer by inject()
    private val galaxyLayer: GalaxyLayer by inject()
    private val sphereLayer: SphereLayer by inject()
    private val hexLayer: HexLayer by inject()
    private val debrisLayer: DebrisLayer by inject()

    private val channels = listOf(
        Channel(
            labelledOscFader(address = "/mix/ch/1/value", label = "conway", defaultValue = 0f),
            conwayLayer,
            oscToggleButton(address = "/mix/ch/1/autopilot/enable")
        ),
        Channel(
            labelledOscFader(address = "/mix/ch/2/value", label = "attract", defaultValue = 0f),
            attractorLayer,
            oscToggleButton(address = "/mix/ch/2/autopilot/enable")
        ),
        Channel(
            labelledOscFader(address = "/mix/ch/3/value", label = "stars", defaultValue = 1f),
            starsLayer,
            oscToggleButton(address = "/mix/ch/3/autopilot/enable")
        ),
        Channel(
            labelledOscFader(address = "/mix/ch/4/value", label = "blob", defaultValue = 0f),
            blobDetectionLayer,
            oscToggleButton(address = "/mix/ch/4/autopilot/enable")
        ),
        Channel(
            labelledOscFader(address = "/mix/ch/5/value", label = "galaxy", defaultValue = 0f),
            galaxyLayer,
            oscToggleButton(address = "/mix/ch/5/autopilot/enable")
        ),
        Channel(
            labelledOscFader(address = "/mix/ch/6/value", label = "sphere", defaultValue = 0f),
            sphereLayer,
            oscToggleButton(address = "/mix/ch/6/autopilot/enable")
        ),
        Channel(
            labelledOscFader(address = "/mix/ch/7/value", label = "hex", defaultValue = 0f),
            hexLayer,
            oscToggleButton(address = "/mix/ch/7/autopilot/enable")
        ),
        Channel(
            labelledOscFader(address = "/mix/ch/8/value", label = "hex", defaultValue = 1f),
            debrisLayer,
            oscToggleButton(address = "/mix/ch/8/autopilot/enable")
        ),
        Channel(
            labelledOscFader(address = "/mix/ch/10/value", label = "krest", defaultValue = 1f),
            textOverlayLayer,
            oscToggleButton(address = "/mix/ch/10/autopilot/enable")
        )
    )

    private val autopilotEnabledButton by oscToggleButtonDelegate("/mix/autopilot/enabled", defaultValue = false)
    private var autopilotIntervalLabel by oscLabelIndicatorDelegate("/mix/autopilot/interval/label", "--- s")
    private var autopilotRemainingLabel by oscLabelIndicatorDelegate("/mix/autopilot/interval/remaining", "--- s")
    private val autopilotIntervalFader = listenableOscFader("/mix/autopilot/interval", defaultValue = 0f) { newValue ->
        synchronized(lock) {
            autopilotDuration =
                Duration.ofMillis(newValue.mapp(AUTOPILOT_MIN_MS.toFloat(), AUTOPILOT_MAX_MS.toFloat()).toLong())
            autopilotNextSwitch = LocalDateTime.now() + autopilotDuration
            autopilotIntervalLabel = if (autopilotDuration.toMillis() < 1000L) {
                "${autopilotDuration.toMillis()} ms"
            } else {
                "${autopilotDuration.seconds} s"
            }
        }
    }
    private val lock = Any()
    private var autopilotDuration: Duration = Duration.ofMillis(AUTOPILOT_MIN_MS)
    private var autopilotNextSwitch: LocalDateTime = LocalDateTime.now()

    fun render(parent: PApplet) = with(parent) {
        autopilot(parent)

        backgroundLayer.update()
        tint(0xffffff.withAlpha())
        image(backgroundLayer.canvas, 0f, 0f)

        channels
            .forEach { (fader, layer, _) ->
                if (fader.value > 0f) {
                    layer.update()
                    tint(fader.value.oscAlpha())
                    image(layer.canvas, 0f, 0f)
                }
            }
    }

    private fun autopilot(parent: PApplet) {
        fun switchChannels() {
            channels.filter { (_, _, autopilot) -> autopilot.value }.forEach { (fader, _, _) -> fader.setValue(0f) }
            channels.filter { (_, _, autopilot) -> autopilot.value }.randomOrNull()?.fader?.setValue(1f)
        }

        fun updateOscController() {
            autopilotRemainingLabel = if (autopilotEnabledButton) {
                "${Duration.between(LocalDateTime.now(), autopilotNextSwitch).seconds} s"
            } else {
                "---"
            }
        }

        if (parent.frameCount % 20 == 0) {
            updateOscController()
        }

        synchronized(lock) {
            if (autopilotEnabledButton && LocalDateTime.now().isAfter(autopilotNextSwitch)) {
                autopilotNextSwitch = LocalDateTime.now() + autopilotDuration
                switchChannels()
            }
        }
    }

    private fun Float.oscAlpha() = 0xffffff.withAlpha(this.mapp(0f, 255f).toInt())
}