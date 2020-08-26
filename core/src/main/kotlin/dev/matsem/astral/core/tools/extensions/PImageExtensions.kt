package dev.matsem.astral.core.tools.extensions

import processing.core.PImage

fun PImage.resizeRatioAware(width: Int? = null, height: Int? = null) {
    when {
        width != null -> {
            val ratio = pixelWidth / pixelHeight.toFloat()
            resize(width, (width / ratio).toInt())
        }
        height != null -> {
            val ratio = pixelHeight / pixelHeight.toFloat()
            resize((height / ratio).toInt(), height)
        }
        else -> System.err.println("PImage resize: dimensions not specified")
    }
}

fun PImage.pixelAt(x: Int, y: Int) = pixels[y * width + x]