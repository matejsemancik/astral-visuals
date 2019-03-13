package dev.matsem.astral.sketches

import org.koin.core.KoinComponent
import processing.core.PApplet
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

    val width: Int get() = sketch.width

    val height: Int get() = sketch.height

    val frameRate: Float get() = sketch.frameRate

    val mouseX: Int get() = sketch.mouseX

    val mouseY: Int get() = sketch.mouseY

    val pmouseX: Int get() = sketch.pmouseX

    val pmouseY: Int get() = sketch.pmouseY

    val mousePressed: Boolean get() = sketch.mousePressed

    fun millis() = sketch.millis()

    fun pushMatrix() = sketch.pushMatrix()

    fun popMatrix() = sketch.popMatrix()

    fun translate(x: Float, y: Float) = sketch.translate(x, y)

    fun translate(x: Float, y: Float, z: Float) = sketch.translate(x, y, z)

    fun scale(s: Float) = sketch.scale(s)

    fun rotateX(rot: Float) = sketch.rotateX(rot)

    fun rotateY(rot: Float) = sketch.rotateY(rot)

    fun rotateZ(rot: Float) = sketch.rotateZ(rot)

    fun background(p1: Float, p2: Float, p3: Float) = sketch.background(p1, p2, p3)

    fun background(rgb: Int) = sketch.background(rgb)

    fun noStroke() = sketch.noStroke()

    fun stroke(p1: Float, p2: Float, p3: Float) = sketch.stroke(p1, p2, p3)

    fun stroke(rgb: Int) = sketch.stroke(rgb)

    fun strokeWeight(weight: Float) = sketch.strokeWeight(weight)

    fun noFill() = sketch.noFill()

    fun fill(p1: Float, p2: Float, p3: Float) = sketch.fill(p1, p2, p3)

    fun fill(rgb: Int) = sketch.fill(rgb)

    fun textSize(size: Float) = sketch.textSize(size)

    fun text(str: String, x: Float, y: Float) = sketch.text(str, x, y)

    fun rect(a: Float, b: Float, c: Float, d: Float) = sketch.rect(a, b, c, d)

    fun rectMode(mode: Int) = sketch.rectMode(mode)

    fun noise(x: Float) = sketch.noise(x)

    fun noise(x: Float, y: Float) = sketch.noise(x, y)

    fun noise(x: Float, y: Float, z: Float) = sketch.noise(x, y, z)

    fun beginShape(kind: Int) = sketch.beginShape(kind)

    fun endShape() = sketch.endShape()

    fun vertex(x: Float, y: Float, z: Float) = sketch.vertex(x, y, z)

    fun sphereDetail(detail: Int) = sketch.sphereDetail(detail)

    fun sphere(radius: Float) = sketch.sphere(radius)

    fun color(v1: Int, v2: Int, v3: Int) = sketch.color(v1, v2, v3)

    fun point(x: Float, y: Float, z: Float) = sketch.point(x, y, z)

    fun point(x: Float, y: Float) = sketch.point(x, y)

    fun sin(t: Float) = PApplet.sin(t)

    fun cos(t: Float) = PApplet.cos(t)

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