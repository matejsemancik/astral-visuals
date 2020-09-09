package dev.matsem.astral.core.tools.osc.controls

import dev.matsem.astral.core.tools.osc.OscManager
import oscP5.OscMessage

/**
 * [OscFader] but with String OSC value at original address + /label
 */
open class LabelledOscFader(
    private val oscManager: OscManager,
    private val address: String,
    private val defaultValue: Float,
    private val label: String
) : OscFader(oscManager, address, defaultValue) {

    init {
        oscManager.sendMessage(OscMessage("$address/label").add(label))
    }

    fun setLabel(value: String) {
        oscManager.sendMessage(OscMessage("$address/label").add(value))
    }
}