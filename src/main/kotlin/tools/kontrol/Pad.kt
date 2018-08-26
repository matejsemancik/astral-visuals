package tools.kontrol

import processing.core.PVector

class Pad constructor(private val kontrol: KontrolF1, val cc: Int, val x: Int, val y: Int) {

    enum class Mode {
        TOGGLE,
        TRIGGER
    }

    private var mode = Mode.TOGGLE
    private var rawState = 0

    var state = false; private set
    var colorOn = PVector(0f, 127f, 127f); private set
    var colorOff = PVector(0f, 127f, 0f); private set

    fun onStateChanged(value: Int) {
        rawState = value
        invalidate()
    }

    fun setMode(mode: Mode) {
        this.mode = mode
        invalidate()
    }

    private fun invalidate() {
        when (mode) {
            Mode.TOGGLE -> {
                if (rawState == 127) {
                    state = !state
                }
            }

            Mode.TRIGGER -> {
                state = rawState == 127
            }
        }

        if (state) ledOn() else ledOff()
    }

    fun ledOn() {
        kontrol.sendHSV(cc, colorOn.x.toInt(), colorOn.y.toInt(), colorOn.z.toInt())
    }

    fun ledOff() {
        kontrol.sendHSV(cc, colorOff.x.toInt(), colorOff.y.toInt(), colorOff.z.toInt())
    }

    fun setColorOn(hue: Int, sat: Int, bri: Int) {
        colorOn = PVector(hue.toFloat(), sat.toFloat(), bri.toFloat())
        invalidate()
    }

    fun setColorOff(hue: Int, sat: Int, bri: Int) {
        colorOff = PVector(hue.toFloat(), sat.toFloat(), bri.toFloat())
        invalidate()
    }
}
