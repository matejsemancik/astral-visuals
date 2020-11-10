package dev.matsem.astral.core.tools.midi.delegate

import themidibus.MidiBus

interface MidiBusOwner {
    val midiBus: MidiBus
}