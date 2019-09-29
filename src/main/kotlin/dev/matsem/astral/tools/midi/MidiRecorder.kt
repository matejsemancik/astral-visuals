package dev.matsem.astral.tools.midi

import processing.core.PApplet

class MidiRecorder(private val sketch: PApplet) {

    var debugLogging = false
    var isRecording = false
        private set

    private val messages = mutableListOf<MidiMessage>()
    private var frameOffset = 0
    private var millisOffset = 0

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

    fun getMessages() = messages.toList()
}