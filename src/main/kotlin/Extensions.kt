import org.jbox2d.common.Vec2
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PVector
import sketches.BaseSketch
import java.util.*
import kotlin.math.absoluteValue

fun Float.toRad(): Float {
    if (this < 0f) {
        return (Math.PI * 2 - Math.abs(this)).toFloat()
    } else {
        return this
    }
}

fun Float.containToRange(lower: Float, upper: Float): Float {
    if (this < lower) {
        return lower
    }

    if (this > upper) {
        return upper
    }

    return this
}

fun Float.threshold(threshold: Float): Float {
    return if (this.absoluteValue > threshold) {
        this
    } else {
        0f
    }
}

fun StringBuilder.newLine(): StringBuilder {
    this.apply { append("\n") }
    return this
}

fun PApplet.centerX() = this.width / 2f
fun PApplet.centerY() = this.height / 2f

fun BaseSketch.centerX() = this.width / 2f
fun BaseSketch.centerY() = this.height / 2f

fun Float.mapp(start: Float, end: Float): Float {
    return PApplet.map(this, 0f, 1f, start, end)
}

fun Int.midiRange(start: Float, end: Float): Float {
    return PApplet.map(this.toFloat(), 0f, 127f, start, end)
}

fun Int.midiRange(top: Float): Float {
    return this.midiRange(0f, top)
}

fun Float.toMidi(low: Float, high: Float): Int = PApplet.map(this, low, high, 0f, 127f).toInt()

fun Boolean.midiValue(): Int = if (this) 127 else 0

fun BaseSketch.shorterDimension(): Int {
    return if (width < height) {
        width
    } else {
        height
    }
}

fun BaseSketch.longerDimension(): Int {
    return if (width > height) {
        width
    } else {
        height
    }
}

fun BaseSketch.angularVelocity(seconds: Float): Float {
    return millis() / 1000f * PConstants.TWO_PI / seconds
}

fun Float.remap(start1: Float, end1: Float, start2: Float, end2: Float): Float =
        PApplet.map(this, start1, end1, start2, end2)

fun Int.remap(start1: Float, end1: Float, start2: Float, end2: Float): Float =
        PApplet.map(this.toFloat(), start1, end1, start2, end2)

fun IntRange.random() =
        Random().nextInt((endInclusive + 1) - start) + start

fun PVector.toVec2() = Vec2(x, y)

fun Vec2.toPVector() = PVector(x, y)

fun Float.quantize(step: Float): Float = (this / step).toInt() * step