package dev.matsem.astral.visuals.tools.audio.beatcounter

class BeatListener(val type: Type, val modulo: Int) {
    var lastBeat: Int = 0
}

sealed class Type

object OnKick : Type()

object OnSnare : Type()

object OnHat : Type()
