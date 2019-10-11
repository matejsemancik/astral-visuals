package dev.matsem.astral.tools.midi

import processing.core.PApplet
import java.util.*
import kotlin.properties.Delegates

class MidiPlayer(private val sketch: PApplet) {

    private lateinit var device: MidiDevice
    private var messages: Stack<MidiMessage> = Stack()
    private var frameOffset: Int = 0
    private var millisOffset: Int = 0

    private var onPlayStart: (() -> Unit)? = null
    private var onPlayStop: (() -> Unit)? = null

    var isPlaying: Boolean by Delegates.observable(false) { _, _, isPlaying ->
        if (isPlaying) {
            onPlayStart?.invoke()
        } else {
            onPlayStop?.invoke()
        }
    }
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

    fun doOnPlay(func: () -> Unit) {
        onPlayStart = func
    }

    fun doOnStop(func: () -> Unit) {
        onPlayStop = func
    }

    fun update() {
        if (messages.isEmpty() || isPlaying.not()) {
            return
        }

        while (messages.peek().millis + millisOffset <= sketch.millis()) {
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