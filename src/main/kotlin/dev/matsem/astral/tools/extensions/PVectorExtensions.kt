package dev.matsem.astral.tools.extensions

import org.jbox2d.common.Vec2
import processing.core.PVector

fun PVector.toVec2() = Vec2(x, y)

operator fun PVector.plusAssign(anotherVector: PVector) {
    add(anotherVector)
}

operator fun PVector.minusAssign(anotherVector: PVector) {
    sub(anotherVector)
}