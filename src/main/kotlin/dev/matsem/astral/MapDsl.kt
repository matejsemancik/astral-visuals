package dev.matsem.astral

import processing.core.PApplet

data class FromRange(
        val input: Float,
        val inRange: ClosedFloatingPointRange<Float>
)

infix fun Float.fromRange(range: ClosedFloatingPointRange<Float>): FromRange {
    return FromRange(this, range)
}

infix fun FromRange.toRange(outRange: ClosedFloatingPointRange<Float>): Float {
    return PApplet.map(input, inRange.start, inRange.endInclusive, outRange.start, outRange.endInclusive)
}