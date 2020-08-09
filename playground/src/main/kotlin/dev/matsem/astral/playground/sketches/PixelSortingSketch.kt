package dev.matsem.astral.playground.sketches

import dev.matsem.astral.core.tools.osc.OscHandler
import dev.matsem.astral.core.tools.osc.OscManager
import dev.matsem.astral.core.tools.osc.oscFader
import dev.matsem.astral.core.tools.pixelsort.PixelSorter
import org.koin.core.KoinComponent
import org.koin.core.inject
import processing.core.PApplet
import processing.core.PConstants

class PixelSortingSketch : PApplet(), KoinComponent, OscHandler {

    val pixelSorter: PixelSorter by inject()
    val originalImg by lazy { loadImage("lenna.png").apply { loadPixels() } }
    override val oscManager by lazy { OscManager(this, 7001, "192.168.1.11", 7001) }

    val threshold by oscFader("/play/fader1")

    override fun settings() {
        size(1, 1, PConstants.P2D)
    }

    override fun setup() {
        surface.setTitle("Pixel Sorter")
        surface.setResizable(true)
        surface.setSize(originalImg.width, originalImg.height)
        image(originalImg, 0f, 0f)
    }

    override fun draw() {
        val newImg = originalImg.copy()
        newImg.pixels = pixelSorter.sortRows(originalImg.pixels, originalImg.height) { pixelsRow ->
            val thresh = threshold * 255f
            val startIndex = pixelsRow.withIndex().firstOrNull { brightness(it.value) < thresh }?.index ?: 0
            val endIndex = pixelsRow.withIndex().lastOrNull { brightness(it.value) < thresh }?.index ?: 0

            pixelsRow
                .toIntArray()
                .apply {
                    sort(startIndex, endIndex)
                }
                .toList()
        }

        newImg.updatePixels()
        image(newImg, 0f, 0f)
    }
}