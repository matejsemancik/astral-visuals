package dev.matsem.astral.visuals

import dev.matsem.astral.core.ColorConfig
import dev.matsem.astral.core.tools.extensions.mapp
import dev.matsem.astral.core.tools.osc.OscHandler
import dev.matsem.astral.core.tools.osc.OscManager
import dev.matsem.astral.core.tools.osc.oscFader
import processing.core.PApplet

class Colorizer(
    private val parent: PApplet,
    override val oscManager: OscManager
) : OscHandler {

    private val fgHue by oscFader("/color/fg/hue", defaultValue = 0f)
    private val fgSaturation by oscFader("/color/fg/sat", defaultValue = 0f)
    private val fgBrightness by oscFader("/color/fg/bri", defaultValue = 1f)

    private val bgHue by oscFader("/color/bg/hue", defaultValue = 0f)
    private val bgSaturation by oscFader("/color/bg/sat", defaultValue = 0f)
    private val bgBrightness by oscFader("/color/bg/bri", defaultValue = 0f)

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
}