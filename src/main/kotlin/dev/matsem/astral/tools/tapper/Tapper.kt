package dev.matsem.astral.tools.tapper

import processing.core.PApplet

class Tapper(val sketch: PApplet) {

    var prev = 0
    var interval: Int = 1000
    var beats = 0

    private var listeners = mutableListOf<() -> Unit>()

    init {
        sketch.registerMethod("draw", this)
    }

    fun tap() {
        val current = sketch.millis()
        interval = current - prev

        prev = sketch.millis()
        beats = 0
    }

    fun doOnBeat(func: () -> Unit) {
        listeners.add(func)
    }

    fun draw() {
        if (sketch.millis() > prev + interval * beats) {
            beats++
            listeners.forEach { it() }
        }
    }
}