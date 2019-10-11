package dev.matsem.astral.tools.galaxy

import dev.matsem.astral.tools.galaxy.controls.*
import dev.matsem.astral.tools.midi.MidiDevice
import dev.matsem.astral.tools.midi.MidiListener
import themidibus.MidiBus
import themidibus.SimpleMidiListener

/**
 * TouchOSC MIDI device
 */
class Galaxy : MidiDevice {

    lateinit var midiBus: MidiBus

    val controls = mutableListOf<GalaxyControl>()

    fun connect() {
        midiBus = MidiBus(this, "TouchOSC Bridge", "TouchOSC Bridge")
        println("MidiBus connected")
    }

    fun update() {
        controls.forEach { it.update() }
    }

    fun createJoystick(channel: Int, ccX: Int, ccY: Int, ccTouchXY: Int, ccZ: Int, ccTouchZ: Int, ccFeedbackEnabled: Int): Joystick =
            Joystick(midiBus, channel, ccX, ccY, ccTouchXY, ccZ, ccTouchZ, ccFeedbackEnabled).also { controls.add(it) }

    fun createPushButton(channel: Int, cc: Int, onPress: () -> Unit) =
            PushButton(channel, cc, onPress).also { controls.add(it) }

    fun createToggleButton(
            channel: Int,
            cc: Int,
            defaultValue: Boolean = false,
            listener: ((Boolean) -> Unit)? = null
    ) = ToggleButton(midiBus, channel, cc, defaultValue)
            .apply { listener?.let { addListener(it) } }
            .also { controls.add(it) }

    fun createPot(channel: Int, cc: Int, min: Float = 0f, max: Float = 1f, initialValue: Float = 0f) =
            Pot(midiBus, channel, cc, min, max, initialValue).also { controls.add(it) }

    fun createButtonGroup(channel: Int, ccs: List<Int>, activeCCs: List<Int>) =
            ButtonGroup(midiBus, channel, ccs, activeCCs).also { controls.add(it) }

    fun createEncoder(channel: Int, cc: Int, min: Int, max: Int, initialValue: Int = 0) =
            Encoder(midiBus, channel, cc, min, max, initialValue).also { controls.add(it) }

    fun controllerChange(channel: Int, cc: Int, value: Int) {
        controls.forEach { it.controllerChange(channel, cc, value) }
    }

    fun updatePhone() {
        controls.forEach { it.updatePhone() }
    }

    override fun plugIn(listener: MidiListener) {
        midiBus.addMidiListener(object : SimpleMidiListener {
            override fun controllerChange(p0: Int, p1: Int, p2: Int) {
                listener.controllerChange(p0, p1, p2)
            }

            override fun noteOn(p0: Int, p1: Int, p2: Int) {
                listener.noteOn(p0, p1, p2)
            }

            override fun noteOff(p0: Int, p1: Int, p2: Int) {
                listener.noteOff(p0, p1, p2)
            }
        })
    }

    override fun mockControlChange(channel: Int, control: Int, value: Int) {
        controls.forEach { it.controllerChange(channel, control, value) }
        midiBus.sendControllerChange(channel, control, value)
    }

    override fun mockNoteOn(channel: Int, control: Int, value: Int) = Unit

    override fun mockNoteOff(channel: Int, control: Int, value: Int) = Unit
}