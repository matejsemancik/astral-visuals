package dev.matsem.astral.tools.galaxy

import dev.matsem.astral.tools.galaxy.controls.*
import themidibus.MidiBus

class Galaxy {

    lateinit var midiBus: MidiBus

    val controls = mutableListOf<MidiControl>()

    fun connect() {
        midiBus = MidiBus(this, "TouchOSC Bridge", "TouchOSC Bridge")
        println("MidiBus connected")
    }

    fun update() {
        controls.forEach { it.onUpdate() }
    }

    fun createJoystick(channel: Int, ccX: Int, ccY: Int, ccTouchXY: Int, ccZ: Int, ccTouchZ: Int, ccFeedbackEnabled: Int): Joystick =
            Joystick(midiBus, channel, ccX, ccY, ccTouchXY, ccZ, ccTouchZ, ccFeedbackEnabled).also { controls.add(it) }

    fun createPushButton(channel: Int, cc: Int, onPress: () -> Unit) =
            PushButton(midiBus, channel, cc, onPress).also { controls.add(it) }

    fun createToggleButton(channel: Int, cc: Int, defaultValue: Boolean = false) =
            ToggleButton(midiBus, channel, cc, defaultValue).also { controls.add(it) }

    fun createPot(channel: Int, cc: Int, min: Float = 0f, max: Float = 1f, initialValue: Float = 0f) =
            Pot(midiBus, channel, cc, min, max, initialValue).also { controls.add(it) }

    fun createButtonGroup(channel: Int, ccs: List<Int>, activeCCs: List<Int>) =
            ButtonGroup(midiBus, channel, ccs, activeCCs).also { controls.add(it) }

    fun createEncoder(channel: Int, cc: Int, min: Int, max: Int, initialValue: Int = 0) =
            Encoder(midiBus, channel, cc, min, max, initialValue)

    fun controllerChange(channel: Int, cc: Int, value: Int) {
//        println("channel: $channel cc: $cc value: $value")
    }

    fun sendClientUpdates() {
        controls.forEach { it.sendClientUpdate() }
    }
}