package dev.matsem.astral.tools.kontrol

import dev.matsem.astral.tools.midi.MidiDevice
import dev.matsem.astral.tools.midi.MidiListener
import themidibus.MidiBus
import themidibus.SimpleMidiListener

class KontrolF1 : MidiDevice {

    companion object {
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
    }

    var knob1 = 64; private set
    var knob2 = 64; private set
    var knob3 = 64; private set
    var knob4 = 64; private set

    var slider1 = 0; private set
    var slider2 = 0; private set
    var slider3 = 0; private set
    var slider4 = 0; private set

    var encoder = 0; private set

    var pads = listOf(
            Pad(this, 10, 0, 0),
            Pad(this, 11, 1, 0),
            Pad(this, 12, 2, 0),
            Pad(this, 13, 3, 0),
            Pad(this, 14, 0, 1),
            Pad(this, 15, 1, 1),
            Pad(this, 16, 2, 1),
            Pad(this, 17, 3, 1),
            Pad(this, 18, 0, 2),
            Pad(this, 19, 1, 2),
            Pad(this, 20, 2, 2),
            Pad(this, 21, 3, 2),
            Pad(this, 22, 0, 3),
            Pad(this, 23, 1, 3),
            Pad(this, 24, 2, 3),
            Pad(this, 25, 3, 3)
    )
        private set

    var onEncoder: (Int) -> Unit = {}
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

            SLIDER1 -> slider1 = value
            SLIDER2 -> slider2 = value
            SLIDER3 -> slider3 = value
            SLIDER4 -> slider4 = value

            ENCODER -> {
                encoder = value
                onEncoder.invoke(value)
            }
        }

        // Buttons
        if (PADS.contains(cc)) {
            pads.first { it.cc == cc }.onStateChanged(value)
        }
    }

    fun pad(y: Int, x: Int) =
            if (y < 4 && x < 4) {
                pads.first { it.y == y && it.x == x }
            } else {
                pads[0]
            }

    fun onEncoder(onEncoder: (Int) -> Unit) {
        this.onEncoder = onEncoder
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