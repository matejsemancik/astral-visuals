package dev.matsem.astral.visuals

import dev.matsem.astral.core.tools.audio.AudioProcessor
import dev.matsem.astral.core.tools.extensions.mapp
import dev.matsem.astral.core.tools.osc.OscHandler
import dev.matsem.astral.core.tools.osc.OscManager
import dev.matsem.astral.core.tools.osc.oscFaderDelegate
import dev.matsem.astral.core.tools.osc.oscPushButtonDelegate
import dev.matsem.astral.visuals.tools.tapper.Tapper
import processing.core.PApplet

class EngineRoom(
    private val parent: PApplet,
    private val audioProcessor: AudioProcessor,
    private val tapper: Tapper,
    private val colorizer: Colorizer,
    override val oscManager: OscManager
) : OscHandler {

    val audioGain by oscFaderDelegate("/system/audio/gain", defaultValue = 0f)
    val oscResendTrigger by oscPushButtonDelegate("/system/osc/resend") {

    }
    val colorResetTrigger by oscPushButtonDelegate("/system/color/reset") {
        colorizer.reset()
    }
    val tapperTapTrigger by oscPushButtonDelegate("/system/tapper/tap") {
        tapper.tap()
    }

    init {
        parent.registerMethod("draw", this)
    }

    fun draw() {
        audioProcessor.gain = audioGain.mapp(1f, 5f)
    }
}