package dev.matsem.astral.core.tools.galaxy.controls

import themidibus.MidiBus

class PushButtonGroup(
    private val midiBus: MidiBus,
    val ch: Int,
    val ccs: List<Int>,
    val listener: (Int) -> Unit
) : GalaxyControl {


    override fun controllerChange(channel: Int, control: Int, v: Int) {
        if (ch == channel && ccs.contains(control) && v == 127) {
            listener(ccs.indexOf(control))
        }
    }

    override fun update() = Unit

    override fun updatePhone() = Unit
}