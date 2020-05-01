package dev.matsem.astral.visuals.tools.kontrol

import dev.matsem.astral.visuals.tools.midi.MidiDevice
import dev.matsem.astral.visuals.tools.midi.MidiListener
import themidibus.MidiBus
import themidibus.SimpleMidiListener

class KontrolF1 : MidiDevice {

    companion object {
        val SHIFT_CC_OFFSET = 41

        const val KNOB1 = 2
        const val KNOB2 = 3
        const val KNOB3 = 4
        const val KNOB4 = 5

        const val SLIDER1 = 6
        const val SLIDER2 = 7
        const val SLIDER3 = 8
        const val SLIDER4 = 9

        const val ENCODER = 41
        const val ENCODER_DISPLAY = 41

        val PADS = listOf(10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25)
        val SHIFT_PADS = PADS.map { it + SHIFT_CC_OFFSET }
    }

    var knob1 = 64
        private set
    var knob2 = 64
        private set
    var knob3 = 64
        private set
    var knob4 = 64
        private set
    var knob1Shift = 64
        private set
    var knob2Shift = 64
        private set
    var knob3Shift = 64
        private set
    var knob4Shift = 64
        private set

    var slider1 = 0
        private set
    var slider2 = 0
        private set
    var slider3 = 0
        private set
    var slider4 = 0
        private set
    var slider1Shift = 0
        private set
    var slider2Shift = 0
        private set
    var slider3Shift = 0
        private set
    var slider4Shift = 0
        private set

    var encoder = 0; private set

    private val pads = listOf(
        Pad(kontrol = this, cc = 10, x = 0, y = 0),
        Pad(kontrol = this, cc = 11, x = 1, y = 0),
        Pad(kontrol = this, cc = 12, x = 2, y = 0),
        Pad(kontrol = this, cc = 13, x = 3, y = 0),
        Pad(kontrol = this, cc = 14, x = 0, y = 1),
        Pad(kontrol = this, cc = 15, x = 1, y = 1),
        Pad(kontrol = this, cc = 16, x = 2, y = 1),
        Pad(kontrol = this, cc = 17, x = 3, y = 1),
        Pad(kontrol = this, cc = 18, x = 0, y = 2),
        Pad(kontrol = this, cc = 19, x = 1, y = 2),
        Pad(kontrol = this, cc = 20, x = 2, y = 2),
        Pad(kontrol = this, cc = 21, x = 3, y = 2),
        Pad(kontrol = this, cc = 22, x = 0, y = 3),
        Pad(kontrol = this, cc = 23, x = 1, y = 3),
        Pad(kontrol = this, cc = 24, x = 2, y = 3),
        Pad(kontrol = this, cc = 25, x = 3, y = 3)
    )

    private val shiftPads = pads.map { Pad(kontrol = this, cc = it.cc + SHIFT_CC_OFFSET, x = it.x, y = it.y) }

    lateinit var midibus: MidiBus

    fun connect() {
        midibus = MidiBus(this, "Traktor Kontrol F1 - 1 Input", "Traktor Kontrol F1 - 1 Output")
        reset()
    }

    fun reset() {
        pads.forEach { it.onStateChanged(0) }
    }

    // MidiBus override
    fun controllerChange(channel: Int, cc: Int, value: Int) {

        // Analogue knobs, sliders and encoder
        when (cc) {
            KNOB1 -> knob1 = value
            KNOB2 -> knob2 = value
            KNOB3 -> knob3 = value
            KNOB4 -> knob4 = value
            KNOB1 + SHIFT_CC_OFFSET -> knob1Shift = value
            KNOB2 + SHIFT_CC_OFFSET -> knob2Shift = value
            KNOB3 + SHIFT_CC_OFFSET -> knob3Shift = value
            KNOB4 + SHIFT_CC_OFFSET -> knob4Shift = value

            SLIDER1 -> slider1 = value
            SLIDER2 -> slider2 = value
            SLIDER3 -> slider3 = value
            SLIDER4 -> slider4 = value
            SLIDER1 + SHIFT_CC_OFFSET -> slider1Shift = value
            SLIDER2 + SHIFT_CC_OFFSET -> slider2Shift = value
            SLIDER3 + SHIFT_CC_OFFSET -> slider3Shift = value
            SLIDER4 + SHIFT_CC_OFFSET -> slider4Shift = value

            ENCODER -> {
                encoder = value
            }
        }

        // Buttons
        if (PADS.contains(cc)) {
            pads.first { it.cc == cc }.onStateChanged(value)
        }

        if (SHIFT_PADS.contains(cc)) {
            shiftPads.first { it.cc == cc }.onStateChanged(value)
        }
    }

    fun pad(y: Int, x: Int, shift: Boolean = false): Pad {
        val collection = if (shift) shiftPads else pads
        return if (y < 4 && x < 4) {
            collection.first { it.y == y && it.x == x }
        } else {
            collection[0]
        }
    }

    fun sendHSV(cc: Int, hue: Int, sat: Int, brightness: Int) {
        midibus.sendControllerChange(0, cc, hue) // Hue
        midibus.sendControllerChange(1, cc, sat) // Saturation
        midibus.sendControllerChange(2, cc, brightness) // Brightness
    }

    // region MidiDevice

    override fun plugIn(listener: MidiListener) {
        midibus.addMidiListener(object : SimpleMidiListener {
            override fun controllerChange(p0: Int, p1: Int, p2: Int) = listener.controllerChange(p0, p1, p2)

            override fun noteOn(p0: Int, p1: Int, p2: Int) = listener.noteOn(p0, p1, p2)

            override fun noteOff(p0: Int, p1: Int, p2: Int) = listener.noteOff(p0, p1, p2)
        })
    }

    override fun mockControlChange(channel: Int, control: Int, value: Int) {
        controllerChange(channel, control, value)
    }

    override fun mockNoteOff(channel: Int, control: Int, value: Int) = Unit

    override fun mockNoteOn(channel: Int, control: Int, value: Int) = Unit

    // endregion
}