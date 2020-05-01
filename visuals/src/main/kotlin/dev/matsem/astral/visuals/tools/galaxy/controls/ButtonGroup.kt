package dev.matsem.astral.visuals.tools.galaxy.controls

import themidibus.MidiBus

class ButtonGroup(
    private val midiBus: MidiBus,
    val ch: Int,
    val ccs: List<Int>,
    private val activeCCs: List<Int>
) : GalaxyControl {

    private val btns = mutableListOf<Boolean>()

    init {
        ccs.forEach { btns.add(activeCCs.contains(it)) }
        updatePhone()
    }

    fun switchToRandom() {
        val indices = btns.withIndex().map { it.index }
        indices.forEach { btns[it] = false }
        btns[indices.shuffled().first()] = true
        updatePhone()
    }

    fun activeButtonsIndices(exclusive: Boolean = true): List<Int> {
        val activeButtons = mutableListOf<Int>()
        btns.withIndex().forEach { if (it.value) activeButtons.add(it.index) }
        if (activeButtons.isEmpty() && exclusive) {
            activeButtons.add(0)
        }

        return activeButtons
    }

    override fun controllerChange(channel: Int, control: Int, v: Int) {
        if (ch == channel && ccs.contains(control)) {
            btns[ccs.indexOf(control)] = v == 127
        }
    }

    override fun update() = Unit

    override fun updatePhone() {
        for ((index, state) in btns.withIndex()) {
            midiBus.sendControllerChange(ch, ccs[index], if (state) 127 else 0)
        }
    }
}