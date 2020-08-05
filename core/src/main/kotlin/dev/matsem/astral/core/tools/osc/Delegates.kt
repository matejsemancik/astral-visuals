package dev.matsem.astral.core.tools.osc

import dev.matsem.astral.core.tools.osc.delegates.OscFaderDelegate
import dev.matsem.astral.core.tools.osc.delegates.OscPushButtonDelegate
import dev.matsem.astral.core.tools.osc.delegates.OscToggleButtonDelegate

/**
 * Fancy constructor for [OscFaderDelegate] that can be used if sketch implements [OscHandler] interface.
 */
fun OscHandler.oscFader(address: String, defaultValue: Float = 0f) =
    OscFaderDelegate(this.oscManager, address, defaultValue)

/**
 * Just an alias of [oscFader] for different naming convention.
 */
fun OscHandler.oscKnob(address: String, defaultValue: Float = 0f) =
    OscFaderDelegate(this.oscManager, address, defaultValue)

/**
 * Fancy constructor for [OscToggleButtonDelegate] that can be used if sketch implements [OscHandler] interface.
 */
fun OscHandler.oscToggleButton(address: String, defaultValue: Boolean = false) =
    OscToggleButtonDelegate(this.oscManager, address, defaultValue)

/**
 * Fancy constructor for [OscPushButtonDelegate] that can be used if sketch iplements [OscHandler] interface.
 */
fun OscHandler.oscPushButton(address: String, trigger: () -> Unit) =
    OscPushButtonDelegate(this.oscManager, address, trigger)