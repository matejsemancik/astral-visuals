package dev.matsem.astral.core.tools.midi.delegate

/**
 * Delegates [Int] property to MIDI knob. The [Int] will have value in range 0..127.
 */
fun MidiBusOwner.midiKnob(channel: Int, cc: Int) = MidiKnobDelegate(this.midiBus, channel, cc)

/**
 * Delegates [Boolean] property to MIDI button.
 */
fun MidiBusOwner.midiButton(channel: Int, cc: Int, mode: ButtonMode = ButtonMode.TOGGLE) =
    MidiButtonDelegate(this.midiBus, channel, cc, mode)