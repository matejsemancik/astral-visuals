package dev.matsem.astral.visuals

import ch.bildspur.postfx.builder.PostFX
import ch.bildspur.postfx.pass.BloomPass
import ch.bildspur.postfx.pass.ChromaticAberrationPass
import ch.bildspur.postfx.pass.NoisePass
import ch.bildspur.postfx.pass.PixelatePass
import ch.bildspur.postfx.pass.RGBSplitPass
import dev.matsem.astral.core.tools.extensions.mapp
import dev.matsem.astral.core.tools.osc.OscHandler
import dev.matsem.astral.core.tools.osc.OscManager
import dev.matsem.astral.core.tools.osc.oscFaderDelegate
import dev.matsem.astral.core.tools.osc.oscToggleButtonDelegate
import processing.core.PApplet

class Effector(
    private val parent: PApplet,
    override val oscManager: OscManager
) : OscHandler {

    private val fx = PostFX(parent)

    private val rgbSplitEnabled by oscToggleButtonDelegate("/fx/rgbSplit/enabled")
    private val rgbSplitDelta by oscFaderDelegate("/fx/rgbSplit/delta", defaultValue = 0.5f)

    private val pixelateEnabled by oscToggleButtonDelegate("/fx/pixelate/enabled")
    private val pixelateAmount by oscFaderDelegate("/fx/pixelate/amount", defaultValue = 0.8f)

    private val bloomEnabled by oscToggleButtonDelegate("/fx/bloom/enabled")
    private val bloomThreshold by oscFaderDelegate("/fx/bloom/threshold", defaultValue = 0.5f)
    private val bloomSize by oscFaderDelegate("/fx/bloom/size", defaultValue = 0.5f)
    private val bloomSigma by oscFaderDelegate("/fx/bloom/sigma", defaultValue = 0.8f)

    private val chromaticAberrationEnabled by oscToggleButtonDelegate("/fx/aberration/enabled")

    init {
        fx.apply {
            preload(RGBSplitPass::class.java)
            preload(PixelatePass::class.java)
            preload(NoisePass::class.java)
            preload(BloomPass::class.java)
            preload(ChromaticAberrationPass::class.java)
        }
    }

    fun render() {
        fx.render()
            .apply {
                if (rgbSplitEnabled) {
                    rgbSplit(rgbSplitDelta.mapp(0f, 200f))
                }
                if (pixelateEnabled) {
                    pixelate(pixelateAmount.mapp(0f, 1000f))
                }
                if (bloomEnabled) {
                    bloom(
                        bloomThreshold.mapp(0f, 1f),
                        bloomSize.mapp(0f, 80f).toInt(),
                        bloomSigma.mapp(0f, 30f)
                    )
                }
                if (chromaticAberrationEnabled) {
                    chromaticAberration()
                }
            }
            .compose()
    }
}