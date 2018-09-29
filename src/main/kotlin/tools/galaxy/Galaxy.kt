package tools.galaxy

import themidibus.MidiBus
import tools.galaxy.controls.Joystick
import tools.galaxy.controls.PushButton

class Galaxy {

    lateinit var midiBus: MidiBus
    var fader3 = 0
    var pot4 = 0
    var pot5 = 0

    fun connect() {
        midiBus = MidiBus(this, "TouchOSC Bridge", "TouchOSC Bridge")
        println("MidiBus connected")
    }

    fun createJoystick(channel: Int, ccX: Int, ccY: Int, ccTouch: Int): Joystick =
            Joystick(midiBus, channel, ccX, ccY, ccTouch)

    fun createPushButton(channel: Int, cc: Int, onPress: () -> Unit) =
            PushButton(midiBus, channel, cc, onPress)

    fun controllerChange(channel: Int, cc: Int, value: Int) {
        println("channel: $channel cc: $cc value: $value")

        if (channel == 0) {
            when (cc) {
                3 -> fader3 = value
                4 -> pot4 = value
                5 -> pot5 = value
            }
        }
    }
}