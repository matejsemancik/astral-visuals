package dev.matsem.astral.core.tools.galaxy.controls

import themidibus.MidiBus

class Encoder(
    private val midiBus: MidiBus,
    private val ch: Int,
    private val cc: Int,
    private val min: Int = 0,
    private val max: Int = 1,
    private val initialValue: Int = 0
) : GalaxyControl {

    var value = initialValue

    init {
        if (min > max) {
            throw IllegalArgumentException("max:$max is greater than min:$min")
        }

        if ((min..max).contains(initialValue).not()) {
            throw IllegalArgumentException("initial value in not in min - max range")
        }
    }

    fun random() {
        value = (min..max).random()
    }

    override fun controllerChange(channel: Int, control: Int, v: Int) {
        if (channel == ch && control == cc) {
            when (v) {
                127 -> if (value <= max) value++
                else -> if (value >= min) value--
            }
        }
    }

    override fun update() = Unit

    override fun updatePhone() = Unit
}