package dev.matsem.astral.core.tools.extensions

fun StringBuilder.newLine(): StringBuilder {
    this.apply { append("\n") }
    return this
}