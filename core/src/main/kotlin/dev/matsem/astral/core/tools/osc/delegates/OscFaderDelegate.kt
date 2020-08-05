package dev.matsem.astral.core.tools.osc.delegates

import dev.matsem.astral.core.tools.osc.OscHandler
import dev.matsem.astral.core.tools.osc.OscManager
import oscP5.OscMessage
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Delegated Float property that connects to OSC control represented by a single Float value.
 */
class OscFaderDelegate(
    private val oscManager: OscManager,
    private val address: String,
    defaultValue: Float
) : ReadWriteProperty<OscHandler, Float> {

    private var latestValue = defaultValue

    init {
        oscManager.sendMessage(OscMessage(address).add(defaultValue))
        oscManager.addMessageListener { message ->
            if (message.checkAddrPattern(address) && message.checkTypetag("f")) {
                latestValue = message[0].floatValue()
            }
        }
    }

    override fun getValue(thisRef: OscHandler, property: KProperty<*>): Float = latestValue

    override fun setValue(thisRef: OscHandler, property: KProperty<*>, value: Float) {
        if (latestValue == value) {
            return
        }

        latestValue = value
        oscManager.sendMessage(OscMessage(address).add(value))
    }
}