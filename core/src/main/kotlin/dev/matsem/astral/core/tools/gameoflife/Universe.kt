package dev.matsem.astral.core.tools.gameoflife

class Universe(
    var cells: Array<Array<Cell>>
) {

    val width: Int
        get() = cells[0].size

    val height: Int
        get() = cells.size

    val heatMap: Array<Array<Float>> = Array(height) { y ->
        Array(width) { x ->
            if (cells[y][x] is AliveCell) 1f else 0f
        }
    }

    var coolingFactor = 0.90f

    fun nextGeneration() {
        val nextGeneration = Array(height) { Array<Cell>(width) { DeadCell } }

        for (y in 0 until height) {
            for (x in 0 until width) {
                val neighborCount = neighborCount(y, x)

                nextGeneration[y][x] = when {
                    cells[y][x] is AliveCell && neighborCount in 2..3 -> AliveCell
                    cells[y][x] is DeadCell && neighborCount == 3 -> AliveCell
                    else -> DeadCell
                }

                heatMap[y][x] = when {
                    cells[y][x] is AliveCell -> 1f
                    else -> heatMap[y][x] * coolingFactor
                }
            }
        }

        cells = nextGeneration
    }

    private fun neighborCount(atY: Int, atX: Int): Int {
        var count = 0
        for (y in (atY - 1)..(atY + 1)) {
            for (x in (atX - 1)..(atX + 1)) {
                // If out of bounds
                if (y < 0 || x < 0 || y >= height || x >= width) {
                    continue
                }

                // If this cell
                if (y == atY && x == atX) {
                    continue
                }

                if (cells[y][x] is AliveCell) {
                    count++
                }
            }
        }

        return count
    }

    override fun toString(): String {
        return cells.joinToString(
            separator = "\n",
            transform = { row ->
                row.joinToString(
                    separator = "",
                    transform = { cell ->
                        "${cell.character()}"
                    }
                )
            }
        )
    }
}