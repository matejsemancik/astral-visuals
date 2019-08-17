package dev.matsem.astral.tools.extensions

fun Boolean.midiValue(): Int = if (this) 127 else 0