package dev.matsem.astral.core.tools.midi.delegate

import themidibus.MidiBus
import themidibus.SimpleMidiListener
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class MidiKnobDelegate(
    bus: MidiBus,
    private val channel: Int,
    private val cc: Int
) : ReadOnlyProperty<Any, Int> {

    private var latestValue: Int = 0

    init {
        bus.addMidiListener(
            object : SimpleMidiListener {
                override fun controllerChange(channel: Int, cc: Int, value: Int) {
                    if (channel == this@MidiKnobDelegate.channel && cc == this@MidiKnobDelegate.cc) {
                        latestValue = value
                    }
                }

                override fun noteOn(p0: Int, p1: Int, p2: Int) = Unit
                override fun noteOff(p0: Int, p1: Int, p2: Int) = Unit
            }
        )
    }

    override fun getValue(thisRef: Any, property: KProperty<*>): Int {
        return latestValue
    }
}