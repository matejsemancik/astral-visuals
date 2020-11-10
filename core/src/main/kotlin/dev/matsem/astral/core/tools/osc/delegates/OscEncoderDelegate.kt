package dev.matsem.astral.core.tools.osc.delegates

import dev.matsem.astral.core.tools.osc.OscHandler
import dev.matsem.astral.core.tools.osc.OscManager
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Delegated [Float] property for OSC rotary encoder represented by a single [Float] value.
 * Suited for TouchOSC Encoder control. Encoder needs to be configured to send either 0f on CCW turn, or 1f on CW turn.
 * This [Float] property contains current encoder value which is incremented or decremented on each encoder turn by
 * specified [increment].
 * When turned clockwise, the [cw] lambda is triggered with new encoder value.
 * When turned counter clockwise, the [ccw] lambda is triggered with new encoder value.
 */
class OscEncoderDelegate(
    oscManager: OscManager,
    private val address: String,
    defaultValue: Float,
    private val increment: Float,
    private val cw: ((Float) -> Unit)?,
    private val ccw: ((Float) -> Unit)?
) : ReadOnlyProperty<OscHandler, Float> {

    private var currentValue = defaultValue

    init {
        oscManager.addMessageListener { message ->
            if (message.checkAddrPattern(address) && message.checkTypetag("f")) {
                when (message[0].floatValue()) {
                    0f -> {
                        currentValue -= increment
                        ccw?.invoke(currentValue)
                    }
                    1f -> {
                        currentValue += increment
                        cw?.invoke(currentValue)
                    }
                }
            }
        }
    }

    override fun getValue(thisRef: OscHandler, property: KProperty<*>): Float = currentValue
}