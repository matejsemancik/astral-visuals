package dev.matsem.astral.core.tools.osc.delegates

import dev.matsem.astral.core.tools.osc.OscHandler
import dev.matsem.astral.core.tools.osc.OscManager
import dev.matsem.astral.core.tools.osc.controls.OscToggleButton
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Delegated Boolean property for OSC toggle button represented with a single Float value, where 0f is turned off
 * and 1f is turned on.
 */
class OscToggleButtonDelegate(
    oscManager: OscManager,
    address: String,
    defaultValue: Boolean
) : ReadWriteProperty<OscHandler, Boolean> {

    private val button = OscToggleButton(oscManager, address, defaultValue)

    override fun getValue(thisRef: OscHandler, property: KProperty<*>): Boolean = button.value

    override fun setValue(thisRef: OscHandler, property: KProperty<*>, value: Boolean) {
        button.setValue(value)
    }
}