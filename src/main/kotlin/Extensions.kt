import processing.core.PApplet

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

fun StringBuilder.newLine(): StringBuilder {
    this.apply { append("\n") }
    return this
}

fun PApplet.centerX() = this.width / 2f
fun PApplet.centerY() = this.height / 2f

fun Float.mapp(start: Float, end: Float): Float {
    return PApplet.map(this, 0f, 1f, start, end)
}

fun Int.midiRange(start: Float, end: Float): Float {
    return PApplet.map(this.toFloat(), 0f, 127f, start, end)
}

fun Int.midiRange(top: Float): Float {
    return this.midiRange(0f, top)
}