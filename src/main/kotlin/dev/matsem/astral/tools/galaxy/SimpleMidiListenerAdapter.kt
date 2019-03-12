package dev.matsem.astral.tools.galaxy

import themidibus.SimpleMidiListener

open class SimpleMidiListenerAdapter : SimpleMidiListener {
    override fun controllerChange(channel: Int, control: Int, v: Int) = Unit

    override fun noteOn(channel: Int, control: Int, v: Int) = Unit

    override fun noteOff(channel: Int, control: Int, v: Int) = Unit
}