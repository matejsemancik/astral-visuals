package dev.matsem.astral.core.tools.extensions

import org.jbox2d.common.Vec2
import processing.core.PVector
import kotlin.math.absoluteValue

operator fun PVector.component1() = x
operator fun PVector.component2() = y
operator fun PVector.component3() = z

operator fun PVector.plusAssign(anotherVector: PVector) {
    add(anotherVector)
}

operator fun PVector.minusAssign(anotherVector: PVector) {
    sub(anotherVector)
}

fun PVector.toVec2() = Vec2(x, y)

fun PVector.isInRadius(threshold: Float): Boolean = x.absoluteValue <= threshold && y.absoluteValue <= threshold