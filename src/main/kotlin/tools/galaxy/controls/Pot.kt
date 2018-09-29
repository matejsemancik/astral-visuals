package tools.galaxy.controls

import midiRange
import processing.core.PApplet
import themidibus.MidiBus
import tools.galaxy.SimpleMidiListenerAdapter

open class Pot internal constructor(
        private val midiBus: MidiBus,
        private val ch: Int,
        private val cc: Int,
        private val min: Float = 0f,
        private val max: Float = 1f,
        private val initialValue: Float = 0f
) {

    var value = 0f

    init {
        if (min > max) {
            throw IllegalArgumentException("max:$max is greater than min:$min")
        }

        if ((min..max).contains(initialValue)) {
            value = initialValue
            midiBus.sendControllerChange(ch, cc, PApplet.map(initialValue, min, max, 0f, 127f).toInt())
        }

        midiBus.addMidiListener(object : SimpleMidiListenerAdapter() {
            override fun controllerChange(channel: Int, control: Int, v: Int) {
                if (channel == ch && control == cc) {
                    value = v.midiRange(min, max)
                }
            }
        })
    }
}