package dev.matsem.astral.core.tools.galaxy.controls

interface GalaxyControl {

    /**
     * Notifies this control about incoming controller change message
     */
    fun controllerChange(channel: Int, control: Int, v: Int)

    /**
     * Called on each draw() in sketch
     */
    fun update()

    /**
     * Notifies this control that it should send it's state to phone app via MidiBus
     */
    fun updatePhone()
}