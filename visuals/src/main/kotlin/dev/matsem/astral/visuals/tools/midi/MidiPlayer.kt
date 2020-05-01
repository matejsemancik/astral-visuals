package dev.matsem.astral.visuals.tools.midi

import processing.core.PApplet
import java.util.*
import kotlin.properties.Delegates

class MidiPlayer(private val sketch: PApplet) {

    private lateinit var device: MidiDevice
    private var stack: Stack<MidiMessage> = Stack()
    private var messages = listOf<MidiMessage>()
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

    var isLooping: Boolean = false
        private set

    fun plugIn(device: MidiDevice) {
        this.device = device
    }

    fun enqueue(messages: List<MidiMessage>) {
        this.messages = messages
    }

    fun play(loop: Boolean = false) {
        this.stack.clear()
        this.isLooping = loop

        messages.reversed().forEach { this.stack.add(it) }

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
        if (stack.isEmpty() || isPlaying.not()) {
            return
        }

        while (stack.peek().millis + millisOffset <= sketch.millis()) {
            val msg = stack.pop()
            when (msg.type) {
                MidiMessageType.CONTROLLER_CHANGE -> device.mockControlChange(msg.channel, msg.control, msg.value)
                MidiMessageType.NOTE_ON -> device.mockNoteOn(msg.channel, msg.control, msg.value)
                MidiMessageType.NOTE_OFF -> device.mockNoteOff(msg.channel, msg.control, msg.value)
            }

            if (stack.isEmpty()) {
                if (isLooping) {
                    play(loop = true)
                } else {
                    isPlaying = false
                    return
                }
            }
        }
    }

    /**
     * Plays MIDI messages synced with VideoExport
     */
    fun update(soundTime: Float) {
        if (stack.isEmpty() || isPlaying.not()) {
            return
        }

        while (stack.peek().millis <= soundTime) {
            val msg = stack.pop()
            when (msg.type) {
                MidiMessageType.CONTROLLER_CHANGE -> device.mockControlChange(msg.channel, msg.control, msg.value)
                MidiMessageType.NOTE_ON -> device.mockNoteOn(msg.channel, msg.control, msg.value)
                MidiMessageType.NOTE_OFF -> device.mockNoteOff(msg.channel, msg.control, msg.value)
            }

            if (stack.isEmpty()) {
                isPlaying = false
                return
            }
        }
    }
}