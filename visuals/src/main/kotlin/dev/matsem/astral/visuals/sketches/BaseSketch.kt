package dev.matsem.astral.visuals.sketches

import org.koin.core.KoinComponent
import processing.event.KeyEvent

abstract class BaseSketch : KoinComponent {

    abstract val sketch: SketchLoader

    var isInDebugMode = false

    abstract fun setup()

    open fun onBecameActive() = Unit

    abstract fun draw()

    open fun keyPressed(event: KeyEvent?) = Unit

    open fun mouseClicked() = Unit

    open fun mousePressed() = Unit

    val bgHue: Float get() = sketch.bgHuePot.value
    val bgSat: Float get() = sketch.bgSatPot.value
    val bgBrightness: Float get() = sketch.bgBriPot.value
    val bgColor: Int get() = sketch.color(bgHue, bgSat, bgBrightness)

    val fgHue: Float get() = sketch.fgHuePot.value
    val fgSat: Float get() = sketch.fgSatPot.value
    val fgBrightness: Float get() = sketch.fgBriPot.value
    val fgColor: Int get() = sketch.color(fgHue, fgSat, fgBrightness)

    val accentHue: Float get() = sketch.accentHuePot.value
    val accentSat: Float get() = sketch.accentSatPot.value
    val accentBrightness: Float get() = sketch.accentBriPot.value
    val accentColor: Int get() = sketch.color(accentHue, accentSat, accentBrightness)
}