package dev.matsem.astral.tools.logging

import dev.matsem.astral.tools.extensions.newLine
import processing.core.PApplet

class SketchLogger private constructor(
        private val withResolution: Boolean,
        private val withFps: Boolean,
        private val textSize: Float
) {
    class Builder() {

        private var mWithResolution = false
        private var mWithFps = false
        private var mTextSize = 14f

        fun withResolution() = apply { mWithResolution = true }

        fun withFps() = apply { mWithFps = true }

        fun withTextSize(size: Float) = apply { mTextSize = size }

        fun build(): SketchLogger {
            return SketchLogger(
                    withResolution = mWithResolution,
                    withFps = mWithFps,
                    textSize = mTextSize
            )
        }
    }

    fun draw(sketch: PApplet, color: Int = sketch.color(255)) = with(sketch) {
        val str = StringBuilder()
                .apply {
                    if (withResolution) {
                        append("resolution: ${width}x$height").newLine()
                    }

                    if (withFps) {
                        append("fps: $frameRate").newLine()
                    }

                }
                .toString()

        noStroke()
        fill(color)
        textSize(textSize)
        text(str, 12f, 24f)
    }
}