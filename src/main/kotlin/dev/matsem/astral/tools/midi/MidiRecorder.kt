package dev.matsem.astral.tools.midi

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import processing.core.PApplet

class MidiRecorder(private val sketch: PApplet) {

    private val json = Json(JsonConfiguration.Stable)

    private val messages = mutableListOf<MidiMessage>()
    private var frameOffset = 0
    private var millisOffset = 0

    var debugLogging = false
    var isRecording = false
        private set

    fun plugIn(device: MidiDevice) {
        device.plugIn(object : MidiListener {
            override fun controllerChange(channel: Int, control: Int, value: Int) {
                if (debugLogging) {
                    println("MidiRecorder controllerChange: chan $channel, control $control, value $value")
                }

                if (isRecording) {
                    messages += MidiMessage(
                            type = MidiMessageType.CONTROLLER_CHANGE,
                            millis = sketch.millis() - millisOffset,
                            frame = sketch.frameCount - frameOffset,
                            channel = channel,
                            control = control,
                            value = value
                    )
                }
            }

            override fun noteOn(channel: Int, control: Int, value: Int) {
                if (debugLogging) {
                    println("MidiRecorder noteOn: chan $channel, control $control, value $value")
                }

                if (isRecording) {
                    messages += MidiMessage(
                            type = MidiMessageType.NOTE_ON,
                            millis = sketch.millis() - millisOffset,
                            frame = sketch.frameCount - frameOffset,
                            channel = channel,
                            control = control,
                            value = value
                    )
                }
            }

            override fun noteOff(channel: Int, control: Int, value: Int) {
                if (debugLogging) {
                    println("MidiRecorder noteOff: chan $channel, control $control, value $value")
                }

                if (isRecording) {
                    messages += MidiMessage(
                            type = MidiMessageType.NOTE_OFF,
                            millis = sketch.millis() - millisOffset,
                            frame = sketch.frameCount - frameOffset,
                            channel = channel,
                            control = control,
                            value = value
                    )
                }
            }
        })
    }

    fun startRecording() {
        frameOffset = sketch.frameCount
        millisOffset = sketch.millis()
        isRecording = true
    }

    fun stopRecording() {
        isRecording = false

        println("Recorded messages:")
        messages.sortBy { it.millis }
        messages.forEach { println(it) }
    }

    fun getMessages(excludedCCs: List<Int> = listOf()) = messages
            .toList()
            .filterNot { excludedCCs.contains(it.control) }
}