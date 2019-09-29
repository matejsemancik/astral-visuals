package dev.matsem.astral.tools.midi

sealed class MidiMessageType

object ControllerChange : MidiMessageType()

object NoteOn : MidiMessageType()

object NoteOff : MidiMessageType()