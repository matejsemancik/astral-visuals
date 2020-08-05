package dev.matsem.astral.core.tools.osc

import dev.matsem.astral.core.tools.osc.delegates.*
import processing.core.PVector

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
 * Fancy constructor for [OscPushButtonDelegate] that can be used if sketch implements [OscHandler] interface.
 */
fun OscHandler.oscPushButton(address: String, trigger: () -> Unit) =
    OscPushButtonDelegate(this.oscManager, address, trigger)

/**
 * Fancy constructor for [OscXYPadDelegate] that can be used if sketch implements [OscHandler] interface.
 */
fun OscHandler.oscXyPad(address: String, defaultValue: PVector = PVector(0f, 0f)) =
    OscXYPadDelegate(this.oscManager, address, defaultValue)

/**
 * Fancy constructor for [OscLedIndicatorDelegate] that can be used if sketch implements [OscHandler] interface.
 */
fun OscHandler.oscLedIndicator(address: String, defaultValue: Float = 0f) =
    OscLedIndicatorDelegate(this.oscManager, address, defaultValue)

/**
 * Fancy constructor for [OscLabelIndicatorDelegate] that can be used if sketch implements [OscHandler] interface.
 */
fun OscHandler.oscLabelIndicator(address: String, defaultValue: String = "---") =
    OscLabelIndicatorDelegate(this.oscManager, address, defaultValue)

/**
 * Fancy constructor for [OscEncoderDelegate] that can be used if sketch implements [OscHandler] interface.
 */
fun OscHandler.oscEncoder(
    address: String,
    defaultValue: Float = 0f,
    increment: Float = 1f,
    cw: ((Float) -> Unit)? = null,
    ccw: ((Float) -> Unit)? = null
) = OscEncoderDelegate(this.oscManager, address, defaultValue, increment, cw, ccw)