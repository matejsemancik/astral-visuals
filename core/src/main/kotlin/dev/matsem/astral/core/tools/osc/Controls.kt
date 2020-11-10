package dev.matsem.astral.core.tools.osc

import dev.matsem.astral.core.tools.osc.controls.LabelledOscFader
import dev.matsem.astral.core.tools.osc.controls.ListenableOscFader
import dev.matsem.astral.core.tools.osc.controls.OscFader
import dev.matsem.astral.core.tools.osc.controls.OscToggleButton

/**
 * Fancy constructor for [OscFader] that can be used if sketch implements [OscHandler] interface.
 */
fun OscHandler.oscFader(address: String, defaultValue: Float = 0f) =
    OscFader(this.oscManager, address, defaultValue)

/**
 * Fancy constructor for [ListenableOscFader] that can be used if sketch implements [OscHandler] interface.
 */
fun OscHandler.listenableOscFader(address: String, defaultValue: Float = 0f, onChanged: (Float) -> Unit) =
    ListenableOscFader(this.oscManager, address, defaultValue, onChanged)

/**
 * Fancy constructor for [LabelledOscFader] that can be used if sketch implements [OscHandler] interface.
 */
fun OscHandler.labelledOscFader(address: String, label: String, defaultValue: Float = 0f) =
    LabelledOscFader(this.oscManager, address, label, defaultValue)

/**
 * Fancy constructor for [OscToggleButton] that can be used if sketch implements [OscHandler] interface.
 */
fun OscHandler.oscToggleButton(address: String, defaultValue: Boolean = false) =
    OscToggleButton(this.oscManager, address, defaultValue)