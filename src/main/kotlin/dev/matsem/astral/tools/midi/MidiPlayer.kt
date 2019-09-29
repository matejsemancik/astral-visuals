package dev.matsem.astral.tools.midi

import processing.core.PApplet
import java.util.*

class MidiPlayer(private val sketch: PApplet) {

    private lateinit var device: MidiDevice
    private var messages: Stack<MidiMessage> = Stack()
    private var frameOffset: Int = 0
    private var millisOffset: Int = 0

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
        millisOffset = sketch.millis()
        isPlaying = true
    }

    fun stop() {
        isPlaying = false
    }

    fun update() {
        if (messages.isEmpty() || isPlaying.not()) {
            return
        }

        while (messages.peek().frame + frameOffset <= sketch.frameCount) {
            val msg = messages.pop()
            when (msg.type) {
                MidiMessageType.CONTROLLER_CHANGE -> device.mockControlChange(msg.channel, msg.control, msg.value)
                MidiMessageType.NOTE_ON -> device.mockNoteOn(msg.channel, msg.control, msg.value)
                MidiMessageType.NOTE_OFF -> device.mockNoteOff(msg.channel, msg.control, msg.value)
            }

            if (messages.isEmpty()) {
                isPlaying = false
                return
            }
        }
    }

    /**
     * Plays MIDI messages synced with VideoExport
     */
    fun update(soundTime: Float) {
        if (messages.isEmpty() || isPlaying.not()) {
            return
        }

        while (messages.peek().millis <= soundTime) {
            val msg = messages.pop()
            when (msg.type) {
                MidiMessageType.CONTROLLER_CHANGE -> device.mockControlChange(msg.channel, msg.control, msg.value)
                MidiMessageType.NOTE_ON -> device.mockNoteOn(msg.channel, msg.control, msg.value)
                MidiMessageType.NOTE_OFF -> device.mockNoteOff(msg.channel, msg.control, msg.value)
            }

            if (messages.isEmpty()) {
                isPlaying = false
                return
            }
        }
    }
}