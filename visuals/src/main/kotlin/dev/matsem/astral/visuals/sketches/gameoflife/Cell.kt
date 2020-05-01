package dev.matsem.astral.visuals.sketches.gameoflife

sealed class Cell {

    companion object {
        const val CHARACTER_ALIVE = '*'
        const val CHARACTER_DEAD = '.'

        fun from(char: Char) = if (char == CHARACTER_ALIVE) AliveCell else DeadCell
    }

    abstract fun character(): Char
}

object AliveCell : Cell() {
    override fun character() = CHARACTER_ALIVE
}

object DeadCell : Cell() {
    override fun character() = CHARACTER_DEAD
}