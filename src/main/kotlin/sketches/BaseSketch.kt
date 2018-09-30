package sketches

import processing.core.PApplet
import processing.event.KeyEvent

abstract class BaseSketch(open val sketch: PApplet) {

    abstract fun setup()

    abstract fun onBecameActive()

    abstract fun draw()

    open fun keyPressed(event: KeyEvent?) {

    }
}