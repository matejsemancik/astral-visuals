package dev.matsem.astral.core.tools.osc.delegates

import dev.matsem.astral.core.tools.osc.OscHandler
import dev.matsem.astral.core.tools.osc.OscManager
import oscP5.OscMessage
import processing.core.PVector
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Delegated [PVector] property that connects to OSC control represented by two [Float] values.
 * Suited for TouchOSC's XY Pad control.
 */
class OscXYPadDelegate(
    private val oscManager: OscManager,
    private val address: String,
    defaultValue: PVector
) : ReadWriteProperty<OscHandler, PVector> {

    private var latestValue = defaultValue

    init {
        oscManager.sendMessage(OscMessage(address).add(defaultValue.x).add(defaultValue.y))
        oscManager.addMessageListener { message ->
            if (message.checkAddrPattern(address) && message.checkTypetag("ff")) {
                latestValue = PVector(message[0].floatValue(), message[1].floatValue())
            }
        }
    }

    override fun getValue(thisRef: OscHandler, property: KProperty<*>): PVector = latestValue

    override fun setValue(thisRef: OscHandler, property: KProperty<*>, value: PVector) {
        latestValue = value
        oscManager.sendMessage(OscMessage(address).add(value.x).add(value.y))
    }
}