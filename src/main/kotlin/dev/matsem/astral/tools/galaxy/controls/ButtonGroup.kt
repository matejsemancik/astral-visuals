package dev.matsem.astral.tools.galaxy.controls

import dev.matsem.astral.tools.galaxy.SimpleMidiListenerAdapter
import themidibus.MidiBus

class ButtonGroup(
        private val midiBus: MidiBus,
        val ch: Int,
        val ccs: List<Int>,
        private val activeCCs: List<Int>
) : MidiControl() {

    private val btns = mutableListOf<Boolean>()

    init {
        ccs.forEach { btns.add(activeCCs.contains(it)) }
        sendClientUpdate()

        midiBus.addMidiListener(object : SimpleMidiListenerAdapter() {
            override fun controllerChange(channel: Int, control: Int, v: Int) {
                if (ch == channel && ccs.contains(control)) {
                    btns[ccs.indexOf(control)] = v == 127
                }
            }
        })
    }

    override fun sendClientUpdate() {
        for ((index, state) in btns.withIndex()) {
            midiBus.sendControllerChange(ch, ccs[index], if (state) 127 else 0)
        }
    }

    fun switchToRandom() {
        val indices = btns.withIndex().map { it.index }
        indices.forEach { btns[it] = false }
        btns[indices.shuffled().first()] = true
        sendClientUpdate()
    }

    fun activeButtonsIndices(exclusive: Boolean = true): List<Int> {
        val activeButtons = mutableListOf<Int>()
        btns.withIndex().forEach { if (it.value) activeButtons.add(it.index) }
        if (activeButtons.isEmpty() && exclusive) {
            activeButtons.add(0)
        }

        return activeButtons
    }
}