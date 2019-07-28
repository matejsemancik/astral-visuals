package dev.matsem.astral.tools.kontrol

class Pad constructor(private val kontrol: KontrolF1, val cc: Int, val x: Int, val y: Int) {

    enum class Mode {
        TOGGLE,
        TRIGGER
    }

    private var rawState = 0
    var state = false; private set

    private var onTrigger: (() -> Unit)? = null
    private var onStateChanged: ((state: Boolean) -> Unit)? = null

    var mode = Mode.TOGGLE
        set(value) {
            field = value
            invalidate()
        }

    var colorOn = Triple(0, 127, 127)
        set(value) {
            field = value
            colorOff = value.copy(third = 20)
        }

    var colorOff = Triple(0, 127, 0)
        set(value) {
            field = value
            invalidate()
        }

    fun onStateChanged(value: Int) {
        rawState = value

        when (mode) {
            Mode.TOGGLE -> {
                if (rawState == 127) {
                    state = !state
                    onStateChanged?.invoke(state)
                }
            }

            Mode.TRIGGER -> {
                state = rawState == 127
                onStateChanged?.invoke(state)
                if (state) {
                    onTrigger?.invoke()
                }
            }
        }

        invalidate()
    }

    fun setTriggerListener(onTrigger: () -> Unit) {
        this.onTrigger = onTrigger
    }

    fun setStateListener(onStateChanged: (state: Boolean) -> Unit) {
        this.onStateChanged = onStateChanged
    }

    private fun invalidate() {
        if (state) ledOn() else ledOff()
    }

    fun ledOn() {
        kontrol.sendHSV(cc, colorOn.first, colorOn.second, colorOn.third)
    }

    fun ledOff() {
        kontrol.sendHSV(cc, colorOff.first, colorOff.second, colorOff.third)
    }
}
