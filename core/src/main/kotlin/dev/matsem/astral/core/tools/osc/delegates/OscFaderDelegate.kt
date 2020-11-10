package dev.matsem.astral.core.tools.osc.delegates

import dev.matsem.astral.core.tools.osc.OscHandler
import dev.matsem.astral.core.tools.osc.OscManager
import dev.matsem.astral.core.tools.osc.controls.OscFader
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Delegated Float property that connects to OSC control represented by a single Float value.
 */
class OscFaderDelegate(
    oscManager: OscManager,
    address: String,
    defaultValue: Float
) : ReadWriteProperty<OscHandler, Float> {

    private val fader = OscFader(oscManager, address, defaultValue)

    override fun getValue(thisRef: OscHandler, property: KProperty<*>): Float = fader.value

    override fun setValue(thisRef: OscHandler, property: KProperty<*>, value: Float) {
        fader.setValue(value)
    }
}