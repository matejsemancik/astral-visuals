package dev.matsem.astral.core.tools.osc.delegates

import dev.matsem.astral.core.tools.osc.OscHandler
import dev.matsem.astral.core.tools.osc.OscManager
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Delegated Boolean property for OSC push button represented with a single Float value, where 0f is turned off
 * and 1f is turned on.
 * Triggers button push by calling [trigger] lambda when button is pressed down.
 * Also, current push button status is stored in this Boolean property.
 */
class OscPushButtonDelegate(
    private val oscManager: OscManager,
    private val address: String,
    private val trigger: () -> Unit
) : ReadOnlyProperty<OscHandler, Boolean> {

    private var latestValue = false

    init {
        oscManager.addMessageListener { message ->
            if (message.checkAddrPattern(address) && message.checkTypetag("f")) {
                val isPressed = message[0].floatValue() == 1f
                latestValue = isPressed
                if (isPressed) {
                    trigger()
                }
            }
        }
    }

    override fun getValue(thisRef: OscHandler, property: KProperty<*>): Boolean = latestValue
}