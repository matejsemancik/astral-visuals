package dev.matsem.astral.tools.galaxy.controls

import themidibus.MidiBus

class ToggleButton internal constructor(
        private val midiBus: MidiBus,
        val ch: Int,
        val cc: Int,
        private val defaultValue: Boolean = false
) : GalaxyControl {

    var isPressed = false
    private var onStateChanged: ((Boolean) -> Unit)? = null

    init {
        isPressed = defaultValue
        updatePhone()
    }

    fun attachListener(listener: (Boolean) -> Unit): ToggleButton {
        this.onStateChanged = listener
        return this
    }

    override fun controllerChange(channel: Int, control: Int, v: Int) {
        if (channel == ch && control == cc) {
            isPressed = v == 127
            onStateChanged?.invoke(isPressed)
        }
    }

    override fun update() = Unit

    override fun updatePhone() {
        midiBus.sendControllerChange(ch, cc, if (isPressed) 127 else 0)
    }
}