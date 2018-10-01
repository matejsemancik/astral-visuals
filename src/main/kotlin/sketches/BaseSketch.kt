package sketches

import processing.core.PApplet
import processing.event.KeyEvent
import tools.audio.AudioProcessor
import tools.galaxy.Galaxy

abstract class BaseSketch(open val sketch: PApplet, audioProcess: AudioProcessor, galaxy: Galaxy) {

    abstract fun setup()

    abstract fun onBecameActive()

    abstract fun draw()

    open fun keyPressed(event: KeyEvent?) {

    }

    open fun mouseClicked() {

    }

    val width: Int get() = sketch.width

    val height: Int get() = sketch.height

    val frameRate: Float get() = sketch.frameRate

    val mouseX: Int get() = sketch.mouseX

    val mouseY: Int get() = sketch.mouseY

    fun pushMatrix() = sketch.pushMatrix()

    fun popMatrix() = sketch.popMatrix()

    fun translate(x: Float, y: Float) = sketch.translate(x, y)

    fun scale(s: Float) = sketch.scale(s)

    fun noStroke() = sketch.noStroke()

    fun stroke(p1: Float, p2: Float, p3: Float) = sketch.stroke(p1, p2, p3)

    fun noFill() = sketch.noFill()

    fun fill(p1: Float, p2: Float, p3: Float) = sketch.fill(p1, p2, p3)

    fun textSize(size: Float) = sketch.textSize(size)

    fun text(str: String, x: Float, y: Float) = sketch.text(str, x, y)

    fun rect(a: Float, b: Float, c: Float, d: Float) = sketch.rect(a, b, c, d)
}