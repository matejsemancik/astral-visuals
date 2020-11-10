package dev.matsem.astral.core.tools.extensions

import peasy.PeasyCam

fun PeasyCam.drawHUD(block: () -> Unit) {
    beginHUD()
    block()
    endHUD()
}