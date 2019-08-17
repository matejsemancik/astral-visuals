package dev.matsem.astral.tools.extensions

fun StringBuilder.newLine(): StringBuilder {
    this.apply { append("\n") }
    return this
}