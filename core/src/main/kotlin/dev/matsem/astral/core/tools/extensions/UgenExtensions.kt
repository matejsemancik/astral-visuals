package dev.matsem.astral.core.tools.extensions

import ddf.minim.UGen

inline val UGen.value: Float get() = this.lastValues.firstOrNull() ?: 0f