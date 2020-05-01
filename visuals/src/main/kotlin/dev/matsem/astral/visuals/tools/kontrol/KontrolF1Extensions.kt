package dev.matsem.astral.visuals.tools.kontrol

fun KontrolF1.onTriggerPad(
    y: Int,
    x: Int,
    midiHue: Int = 0,
    shift: Boolean = false,
    triggerFunc: () -> Unit
) {
    pad(y, x, shift).apply {
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
    shift: Boolean = false,
    toggleFunc: (Boolean) -> Unit
) {
    pad(y, x, shift).apply {
        colorOn = Triple(midiHue, 127, 127)
        colorOff = Triple(midiHue, 127, 24)
        mode = Pad.Mode.TOGGLE
        setStateListener(toggleFunc)
    }
}