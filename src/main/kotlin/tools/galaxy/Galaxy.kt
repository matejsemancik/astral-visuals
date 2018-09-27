package tools.galaxy

import themidibus.MidiBus

class Galaxy {

    lateinit var midiBus: MidiBus
    lateinit var joystick: Joystick
    var fader3 = 0
    var pot4 = 0
    var pot5 = 0

    fun connect() {
        midiBus = MidiBus(this, "TouchOSC Bridge", "TouchOSC Bridge")
        joystick = Joystick(midiBus, 0, 0, 1, 2).apply { flipped = true }
        println("MidiBus connected")
    }

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