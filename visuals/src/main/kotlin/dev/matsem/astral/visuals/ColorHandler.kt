package dev.matsem.astral.visuals

interface ColorHandler {
    val colorizer: Colorizer

    val fgColor
        get() = colorizer.fgColor

    val bgColor
        get() = colorizer.bgColor
}