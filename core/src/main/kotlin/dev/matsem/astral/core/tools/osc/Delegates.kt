package dev.matsem.astral.core.tools.osc

import dev.matsem.astral.core.tools.osc.delegates.LabelledOscFaderDelegate
import dev.matsem.astral.core.tools.osc.delegates.OscEncoderDelegate
import dev.matsem.astral.core.tools.osc.delegates.OscFaderDelegate
import dev.matsem.astral.core.tools.osc.delegates.OscLabelIndicatorDelegate
import dev.matsem.astral.core.tools.osc.delegates.OscLedIndicatorDelegate
import dev.matsem.astral.core.tools.osc.delegates.OscPushButtonDelegate
import dev.matsem.astral.core.tools.osc.delegates.OscToggleButtonDelegate
import dev.matsem.astral.core.tools.osc.delegates.OscXYPadDelegate
import processing.core.PVector

/**
 * Fancy constructor for [OscFaderDelegate] that can be used if sketch implements [OscHandler] interface.
 */
fun OscHandler.oscFaderDelegate(address: String, defaultValue: Float = 0f) =
    OscFaderDelegate(this.oscManager, address, defaultValue)

/**
 * Fancy constructor for [LabelledOscFaderDelegate] that can be used if sketch implements [OscHandler] interface.
 */
fun OscHandler.labelledOscFaderDelegate(address: String, label: String, defaultValue: Float = 0f) =
    LabelledOscFaderDelegate(this.oscManager, address, label, defaultValue)

/**
 * Fancy constructor for [OscToggleButtonDelegate] that can be used if sketch implements [OscHandler] interface.
 */
fun OscHandler.oscToggleButtonDelegate(address: String, defaultValue: Boolean = false) =
    OscToggleButtonDelegate(this.oscManager, address, defaultValue)

/**
 * Fancy constructor for [OscPushButtonDelegate] that can be used if sketch implements [OscHandler] interface.
 */
fun OscHandler.oscPushButtonDelegate(address: String, trigger: () -> Unit) =
    OscPushButtonDelegate(this.oscManager, address, trigger)

/**
 * Fancy constructor for [OscXYPadDelegate] that can be used if sketch implements [OscHandler] interface.
 */
fun OscHandler.oscXyPadDelegate(address: String, defaultValue: PVector = PVector(0f, 0f)) =
    OscXYPadDelegate(this.oscManager, address, defaultValue)

/**
 * Fancy constructor for [OscLedIndicatorDelegate] that can be used if sketch implements [OscHandler] interface.
 */
fun OscHandler.oscLedIndicatorDelegate(address: String, defaultValue: Float = 0f) =
    OscLedIndicatorDelegate(this.oscManager, address, defaultValue)

/**
 * Fancy constructor for [OscLabelIndicatorDelegate] that can be used if sketch implements [OscHandler] interface.
 */
fun OscHandler.oscLabelIndicatorDelegate(address: String, defaultValue: String = "---") =
    OscLabelIndicatorDelegate(this.oscManager, address, defaultValue)

/**
 * Fancy constructor for [OscEncoderDelegate] that can be used if sketch implements [OscHandler] interface.
 */
fun OscHandler.oscEncoderDelegate(
    address: String,
    defaultValue: Float = 0f,
    increment: Float = 1f,
    cw: ((Float) -> Unit)? = null,
    ccw: ((Float) -> Unit)? = null
) = OscEncoderDelegate(this.oscManager, address, defaultValue, increment, cw, ccw)