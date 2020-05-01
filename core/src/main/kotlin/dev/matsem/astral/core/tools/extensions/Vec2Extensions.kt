package dev.matsem.astral.core.tools.extensions

import org.jbox2d.common.Vec2
import processing.core.PVector

fun Vec2.toPVector() = PVector(x, y)