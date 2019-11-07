package dev.matsem.astral.tools.extensions

import org.jbox2d.common.Vec2
import processing.core.PVector
import kotlin.math.absoluteValue

fun PVector.toVec2() = Vec2(x, y)

fun PVector.isInRadius(threshold: Float): Boolean = x.absoluteValue <= threshold && y.absoluteValue <= threshold

operator fun PVector.plusAssign(anotherVector: PVector) {
    add(anotherVector)
}

operator fun PVector.minusAssign(anotherVector: PVector) {
    sub(anotherVector)
}