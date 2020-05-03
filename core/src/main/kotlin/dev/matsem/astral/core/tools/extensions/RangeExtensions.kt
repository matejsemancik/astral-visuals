package dev.matsem.astral.core.tools.extensions

/**
 * Splits Float range into [count] ranges
 */
fun ClosedFloatingPointRange<Float>.split(count: Int) = mutableListOf<ClosedFloatingPointRange<Float>>().apply {
    val step = (endInclusive - start) / count
    repeat(count) { i ->
        val l = start + i * step
        val h = l + step
        add(l..h)
    }
}