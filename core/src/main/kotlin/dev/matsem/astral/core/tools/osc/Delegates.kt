package dev.matsem.astral.core.tools.osc

import dev.matsem.astral.core.tools.osc.delegates.OscFaderDelegate

/**
 * Fancy constructor for [OscFaderDelegate], that can be used if sketch implements [OscHandler] interface.
 */
fun OscHandler.oscFader(address: String, defaultValue: Float = 0f) =
    OscFaderDelegate(this.oscManager, address, defaultValue)