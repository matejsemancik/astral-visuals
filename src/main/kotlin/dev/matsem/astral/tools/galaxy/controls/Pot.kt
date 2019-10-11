package dev.matsem.astral.tools.galaxy.controls

import processing.core.PApplet
import themidibus.MidiBus

class Pot internal constructor(
        private val midiBus: MidiBus,
        private val ch: Int,
        private val cc: Int,
        val min: Float = 0f,
        val max: Float = 1f,
        private val initialValue: Float = 0f
) : GalaxyControl {

    var value = 0f
    var rawValue = 0
    private var lerp = 1f

    init {
        if (min > max) {
            throw IllegalArgumentException("max:$max is greater than min:$min")
        }

        if ((min..max).contains(initialValue)) {
            value = initialValue
            rawValue = PApplet.map(initialValue, min, max, 0f, 127f).toInt()
            midiBus.sendControllerChange(ch, cc, rawValue)
        }
    }

    fun random(maxRaw: Int = 127) {
        rawValue = (0..maxRaw).random()
        updatePhone()
    }

    fun reset() {
        value = initialValue
        rawValue = PApplet.map(initialValue, min, max, 0f, 127f).toInt()
        midiBus.sendControllerChange(ch, cc, rawValue)
    }

    fun lerp(lerp: Float) = apply {
        this.lerp = lerp
    }

    override fun controllerChange(channel: Int, control: Int, v: Int) {
        if (channel == ch && control == cc) {
            rawValue = v
        }
    }

    override fun update() {
        value = PApplet.lerp(value, PApplet.map(rawValue.toFloat(), 0f, 127f, min, max), lerp)
    }

    override fun updatePhone() {
        midiBus.sendControllerChange(ch, cc, rawValue)
    }
}