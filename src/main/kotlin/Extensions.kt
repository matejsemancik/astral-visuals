/**
 * Created by matsem on 21/03/2018.
 */

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