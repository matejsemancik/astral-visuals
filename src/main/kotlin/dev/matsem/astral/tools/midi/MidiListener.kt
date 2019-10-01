package dev.matsem.astral.tools.midi

interface MidiListener {
    fun controllerChange(channel: Int, control: Int, value: Int)

    fun noteOn(channel: Int, control: Int, value: Int)

    fun noteOff(channel: Int, control: Int, value: Int)
}