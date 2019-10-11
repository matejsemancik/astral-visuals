package dev.matsem.astral.tools.galaxy.controls

class PushButton internal constructor(
        val ch: Int,
        val cc: Int,
        private val onPress: () -> Unit
) : GalaxyControl {

    override fun controllerChange(channel: Int, control: Int, v: Int) {
        if (channel == ch && control == cc && v == 127) {
            onPress.invoke()
        }
    }

    override fun update() = Unit

    override fun updatePhone() = Unit
}