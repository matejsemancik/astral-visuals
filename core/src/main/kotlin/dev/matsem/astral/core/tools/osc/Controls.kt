package dev.matsem.astral.core.tools.osc

import dev.matsem.astral.core.tools.osc.controls.LabelledOscFader
import dev.matsem.astral.core.tools.osc.controls.OscFader

/**
 * Fancy constructor for [OscFader] that can be used if sketch implements [OscHandler] interface.
 */
fun OscHandler.oscFader(address: String, defaultValue: Float = 0f) =
    OscFader(this.oscManager, address, defaultValue)

/**
 * Fancy constructor for [LabelledOscFader] that can be used if sketch implements [OscHandler] interface.
 */
fun OscHandler.labelledOscFader(address: String, label: String, defaultValue: Float = 0f) =
    LabelledOscFader(this.oscManager, address, label, defaultValue)