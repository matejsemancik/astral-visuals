package dev.matsem.astral.tools.midi

interface MidiDevice {

    fun plugIn(listener: MidiListener)

    fun mockControlChange(channel: Int, control: Int, value: Int)

    fun mockNoteOn(channel: Int, control: Int, value: Int)

    fun mockNoteOff(channel: Int, control: Int, value: Int)
}