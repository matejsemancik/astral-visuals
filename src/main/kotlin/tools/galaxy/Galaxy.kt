package tools.galaxy

import themidibus.MidiBus
import tools.galaxy.controls.Joystick
import tools.galaxy.controls.Pot
import tools.galaxy.controls.PushButton
import tools.galaxy.controls.ToggleButton

class Galaxy {

    lateinit var midiBus: MidiBus

    fun connect() {
        midiBus = MidiBus(this, "TouchOSC Bridge", "TouchOSC Bridge")
        println("MidiBus connected")
    }

    fun createJoystick(channel: Int, ccX: Int, ccY: Int, ccTouch: Int): Joystick =
            Joystick(midiBus, channel, ccX, ccY, ccTouch)

    fun createPushButton(channel: Int, cc: Int, onPress: () -> Unit) =
            PushButton(midiBus, channel, cc, onPress)

    fun createToggleButton(channel: Int, cc: Int, defaultValue: Boolean = false) =
            ToggleButton(midiBus, channel, cc, defaultValue)

    fun createPot(channel: Int, cc: Int, min: Float = 0f, max: Float = 1f, initialValue: Float = 0f) =
            Pot(midiBus, channel, cc, min, max, initialValue)

    fun controllerChange(channel: Int, cc: Int, value: Int) {
        println("channel: $channel cc: $cc value: $value")
    }
}