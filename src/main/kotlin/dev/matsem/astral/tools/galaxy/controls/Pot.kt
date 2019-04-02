package dev.matsem.astral.tools.galaxy.controls

import dev.matsem.astral.random
import dev.matsem.astral.tools.galaxy.SimpleMidiListenerAdapter
import processing.core.PApplet
import themidibus.MidiBus

open class Pot internal constructor(
        private val midiBus: MidiBus,
        private val ch: Int,
        private val cc: Int,
        val min: Float = 0f,
        val max: Float = 1f,
        private val initialValue: Float = 0f
) : MidiControl() {

    var value = 0f
    var rawValue = 0
    private var lerp = 1f
    var lastUpdated = System.currentTimeMillis()

    init {
        if (min > max) {
            throw IllegalArgumentException("max:$max is greater than min:$min")
        }

        if ((min..max).contains(initialValue)) {
            value = initialValue
            rawValue = PApplet.map(initialValue, min, max, 0f, 127f).toInt()
            midiBus.sendControllerChange(ch, cc, rawValue)
        }

        midiBus.addMidiListener(object : SimpleMidiListenerAdapter() {
            override fun controllerChange(channel: Int, control: Int, v: Int) {
                if (channel == ch && control == cc) {
                    rawValue = v
                }
            }
        })
    }

    fun sendValue(value: Float) {
        rawValue = PApplet.map(value, min, max, 0f, 127f).toInt()
        val now = System.currentTimeMillis()
        if (now > lastUpdated + 1000) {
            lastUpdated = now
            sendClientUpdate()
        }
    }

    override fun onUpdate() {
        value = PApplet.lerp(value, PApplet.map(rawValue.toFloat(), 0f, 127f, min, max), lerp)
    }

    override fun sendClientUpdate() {
        midiBus.sendControllerChange(ch, cc, rawValue)
    }

    fun random() {
        rawValue = (0..127).random()
        sendClientUpdate()
    }

    fun reset() {
        value = initialValue
        rawValue = PApplet.map(initialValue, min, max, 0f, 127f).toInt()
        midiBus.sendControllerChange(ch, cc, rawValue)
    }

    fun lerp(lerp: Float) = apply {
        this.lerp = lerp
    }
}