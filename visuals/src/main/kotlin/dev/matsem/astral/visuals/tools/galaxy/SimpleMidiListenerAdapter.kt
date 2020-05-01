package dev.matsem.astral.visuals.tools.galaxy

import themidibus.SimpleMidiListener

open class SimpleMidiListenerAdapter : SimpleMidiListener {
    override fun controllerChange(channel: Int, control: Int, value: Int) = Unit

    override fun noteOn(channel: Int, control: Int, value: Int) = Unit

    override fun noteOff(channel: Int, control: Int, value: Int) = Unit
}