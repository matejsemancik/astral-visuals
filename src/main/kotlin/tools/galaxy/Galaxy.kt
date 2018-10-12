package tools.galaxy

import themidibus.MidiBus
import tools.galaxy.controls.*

class Galaxy {

    lateinit var midiBus: MidiBus

    lateinit var pot1: Pot
    lateinit var pot2: Pot
    lateinit var pot3: Pot

    val controls = mutableListOf<MidiControl>()

    fun connect() {
        midiBus = MidiBus(this, "TouchOSC Bridge", "TouchOSC Bridge")
        println("MidiBus connected")

        pot1 = Pot(midiBus, 1, 1)
        pot2 = Pot(midiBus, 1, 2)
        pot3 = Pot(midiBus, 1, 3)
    }

    fun update() {
        controls.forEach { it.onUpdate() }
    }

    fun createJoystick(channel: Int, ccX: Int, ccY: Int, ccTouch: Int): Joystick =
            Joystick(midiBus, channel, ccX, ccY, ccTouch).also { controls.add(it) }

    fun createPushButton(channel: Int, cc: Int, onPress: () -> Unit) =
            PushButton(midiBus, channel, cc, onPress).also { controls.add(it) }

    fun createToggleButton(channel: Int, cc: Int, defaultValue: Boolean = false) =
            ToggleButton(midiBus, channel, cc, defaultValue).also { controls.add(it) }

    fun createPot(channel: Int, cc: Int, min: Float = 0f, max: Float = 1f, initialValue: Float = 0f) =
            Pot(midiBus, channel, cc, min, max, initialValue).also { controls.add(it) }

    fun controllerChange(channel: Int, cc: Int, value: Int) {
        println("channel: $channel cc: $cc value: $value")
    }
}