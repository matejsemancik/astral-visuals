package dev.matsem.astral.core.tools.pixelsort

class PixelSorter {

    /**
     * Sorts input [pixels] buffer of [PGraphics] with specified [height] using [sortingFunction].
     * [sortingFunction] receives the list of pixels in one row and returns the sorted list of pixels.
     */
    fun sortRows(pixels: IntArray, height: Int, sortingFunction: (List<Int>) -> List<Int>): IntArray {
        return pixels
            .withIndex()
            .groupBy { (index, _) -> index / height } // divide into list per row
            .values
            .map { it.map { it.value } }
            .map {
                sortingFunction(it)
            }
            .flatten()
            .toIntArray()
    }
}