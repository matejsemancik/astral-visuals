package dev.matsem.astral.core.tools.midi.delegate

import themidibus.MidiBus
import themidibus.SimpleMidiListener
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

enum class ButtonMode {
    TOGGLE, GATE
}

class MidiButtonDelegate(
    bus: MidiBus,
    private val channel: Int,
    private val cc: Int,
    private val mode: ButtonMode
) : ReadOnlyProperty<Any, Boolean> {

    private var latestValue: Boolean = false

    init {
        bus.addMidiListener(
            object : SimpleMidiListener {
                override fun controllerChange(channel: Int, cc: Int, value: Int) {
                    if (channel == this@MidiButtonDelegate.channel && cc == this@MidiButtonDelegate.cc) {
                        handleControllerChange(value)
                    }
                }

                override fun noteOn(p0: Int, p1: Int, p2: Int) = Unit

                override fun noteOff(p0: Int, p1: Int, p2: Int) = Unit
            }
        )
    }

    override fun getValue(thisRef: Any, property: KProperty<*>) = latestValue

    private fun handleControllerChange(value: Int) {
        when (mode) {
            ButtonMode.TOGGLE -> if (value == 127) {
                latestValue = !latestValue
            }
            ButtonMode.GATE -> latestValue = value == 127
        }
    }
}