package dev.matsem.astral.tools.kontrol

fun KontrolF1.onTriggerPad(
        y: Int,
        x: Int,
        midiHue: Int = 0,
        triggerFunc: () -> Unit
) {
    pad(y, x).apply {
        colorOn = Triple(midiHue, 127, 127)
        colorOff = Triple(midiHue, 127, 24)
        mode = Pad.Mode.TRIGGER
        setTriggerListener(triggerFunc)
    }
}

fun KontrolF1.onTogglePad(
        y: Int,
        x: Int,
        midiHue: Int = 0,
        toggleFunc: (Boolean) -> Unit
) {
    pad(y, x).apply {
        colorOn = Triple(midiHue, 127, 127)
        colorOff = Triple(midiHue, 127, 24)
        mode = Pad.Mode.TOGGLE
        setStateListener(toggleFunc)
    }
}