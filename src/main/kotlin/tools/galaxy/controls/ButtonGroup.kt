package tools.galaxy.controls

import themidibus.MidiBus
import tools.galaxy.SimpleMidiListenerAdapter

class ButtonGroup(
        private val midiBus: MidiBus,
        val ch: Int,
        val ccs: List<Int>,
        val activeCCs: List<Int>
) : MidiControl() {

    val btns = mutableListOf<Boolean>()

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

    fun activeButtons(): List<Int> {
        val activeButtons = mutableListOf<Int>()
        btns.withIndex().forEach { if (it.value) activeButtons.add(it.index) }
        if (activeButtons.isEmpty()) {
            activeButtons.add(0)
        }

        return activeButtons
    }
}