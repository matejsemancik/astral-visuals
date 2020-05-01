package dev.matsem.astral.visuals.tools.midi

import kotlinx.serialization.Serializable

@Serializable
data class MidiMessage(
    val type: MidiMessageType,
    val millis: Int,
    val frame: Int,
    val channel: Int,
    val control: Int,
    val value: Int
) {
    override fun toString(): String {
        return "$type @$millis ms, frame $frame, ch:$channel, control:$control, value:$value"
    }
}