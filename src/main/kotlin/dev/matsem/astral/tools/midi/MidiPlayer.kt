package dev.matsem.astral.tools.midi

import processing.core.PApplet
import java.util.*

class MidiPlayer(private val sketch: PApplet) {

    private lateinit var device: MidiDevice
    private var messages: Stack<MidiMessage> = Stack()
    private var frameOffset: Int = 0

    var isPlaying = false
        private set

    fun plugIn(device: MidiDevice) {
        this.device = device
    }

    fun enqueue(messages: List<MidiMessage>) {
        this.messages.clear()
        messages.reversed().forEach { this.messages.add(it) }
    }

    fun play() {
        frameOffset = sketch.frameCount
        isPlaying = true
    }

    fun stop() {
        isPlaying = false
    }

    fun update() {
        if (messages.isEmpty() || isPlaying.not()) {
            return
        }

        while(messages.peek().frame + frameOffset <= sketch.frameCount) {
            val msg = messages.pop()
            when (msg.type) {
                ControllerChange -> device.mockControlChange(msg.channel, msg.control, msg.value)
                NoteOn -> device.mockNoteOn(msg.channel, msg.control, msg.value)
                NoteOff -> device.mockNoteOff(msg.channel, msg.control, msg.value)
            }

            if (messages.isEmpty()) {
                isPlaying = false
                return
            }
        }
    }
}