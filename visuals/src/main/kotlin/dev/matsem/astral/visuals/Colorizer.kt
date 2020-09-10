package dev.matsem.astral.visuals

import dev.matsem.astral.core.ColorConfig
import dev.matsem.astral.core.tools.extensions.mapp
import dev.matsem.astral.core.tools.osc.OscHandler
import dev.matsem.astral.core.tools.osc.OscManager
import dev.matsem.astral.core.tools.osc.oscFaderDelegate
import processing.core.PApplet

class Colorizer(
    private val parent: PApplet,
    override val oscManager: OscManager
) : OscHandler {

    private var fgHue by oscFaderDelegate("/color/fg/hue", defaultValue = 0f)
    private var fgSaturation by oscFaderDelegate("/color/fg/sat", defaultValue = 0f)
    private var fgBrightness by oscFaderDelegate("/color/fg/bri", defaultValue = 1f)

    private var bgHue by oscFaderDelegate("/color/bg/hue", defaultValue = 0f)
    private var bgSaturation by oscFaderDelegate("/color/bg/sat", defaultValue = 0f)
    private var bgBrightness by oscFaderDelegate("/color/bg/bri", defaultValue = 0f)

    private val inputFgColor
        get() = parent.color(
            fgHue.mapp(0f, ColorConfig.HUE_MAX),
            fgSaturation.mapp(0f, ColorConfig.SATURATION_MAX),
            fgBrightness.mapp(0f, ColorConfig.BRIGHTNESS_MAX),
            ColorConfig.ALPHA_MAX
        )


    private val inputBgColor
        get() = parent.color(
            bgHue.mapp(0f, ColorConfig.HUE_MAX),
            bgSaturation.mapp(0f, ColorConfig.SATURATION_MAX),
            bgBrightness.mapp(0f, ColorConfig.BRIGHTNESS_MAX),
            ColorConfig.ALPHA_MAX
        )

    var fgColor = 0
    var bgColor = 0

    init {
        parent.registerMethod("draw", this)
    }

    fun draw() {
        fgColor = parent.lerpColor(fgColor, inputFgColor, 0.1f)
        bgColor = parent.lerpColor(bgColor, inputBgColor, 0.1f)
    }

    fun reset() {
        fgHue = 0f
        fgSaturation = 0f
        fgBrightness = 1f

        bgHue = 0f
        bgSaturation = 0f
        bgBrightness = 0f
    }
}