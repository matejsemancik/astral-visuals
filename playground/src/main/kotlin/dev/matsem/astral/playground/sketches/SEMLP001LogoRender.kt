package dev.matsem.astral.playground.sketches

import ch.bildspur.postfx.builder.PostFX
import dev.matsem.astral.core.tools.animations.AnimationHandler
import dev.matsem.astral.core.tools.animations.radianSeconds
import dev.matsem.astral.core.tools.extensions.colorModeHsb
import dev.matsem.astral.core.tools.extensions.pushPop
import dev.matsem.astral.core.tools.extensions.shorterDimension
import dev.matsem.astral.core.tools.extensions.translateCenter
import org.koin.core.KoinComponent
import org.openrndr.extra.easing.easeCubicInOut
import org.openrndr.extra.easing.easeSineInOut
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PShape

/**
 * Raw render of SEM Logo that was used for PR of first-ever SEMLP release event.
 */
class SEMLP001LogoRender : PApplet(), AnimationHandler, KoinComponent {

    companion object {
        private const val Fps = 24f
        private const val ExportMode = false
        private const val ExportDurationSec = 30
        private const val LogoFull = "3d/semlogo_full.obj"
        private const val TextRotationIntervalSec = 20f
        private const val LogoRotationIntervalSec = 6f
    }

    private lateinit var fx: PostFX
    private lateinit var logoInner: PShape
    private lateinit var logoOuter: PShape

    private var exportMillis: Int = 0
    override fun provideMillis(): Int {
        return if (ExportMode) {
            exportMillis
        } else {
            millis()
        }
    }

    override fun settings() {
        size(1080, 1080, PConstants.P3D)
    }

    override fun setup() {
        colorModeHsb()
        surface.setTitle("SEMLP001 Logo Render")
        if (!ExportMode) {
            frameRate(Fps)
        }

        logoInner = loadShape(LogoFull).apply {
            // Model normalize
            translate(-getWidth() / 2f, 0f, getWidth() / 2f)
            rotateX(radians(-90f))
            scale(this@SEMLP001LogoRender.width / getWidth())
            scale(1f, 1f, 2f)

            // Removes outer text portion of 3D model
            while (childCount > 28) {
                removeChild(28)
            }

            setFill(color(0f, 0f, 100f))
        }

        logoOuter = loadShape(LogoFull).apply {
            // Model normalize
            translate(-getWidth() / 2f, 0f, getWidth() / 2f)
            rotateX(radians(-90f))
            scale(this@SEMLP001LogoRender.width / getWidth())

            // Removes inner logo portion of 3D model
            repeat(28) {
                removeChild(0)
            }

            setFill(color(0f, 0f, 100f))
        }

        fx = PostFX(this)
    }

    override fun draw() {
        drawSketch()
    }

    private fun PApplet.drawSketch() {
        ortho()
        background(0f, 0f, 0f, 0f)

        // Lighting experiments
//        ambientLight(0f, 0f, 100f)
//        directionalLight(20f, 0f, 100f, 0f, 0f, -1f)
//        spotLight(128f, 100f, 100f, width/2f, height/2f, 400f, 0f, 0f, -1f, PI, 2f)
//        pointLight(0f, 0f, 60f, 0f, height / 2f, -200f)
//        pointLight(0f, 0f, 60f, widthF, height / 2f, -200f)
//        pointLight(0f, 0f, 100f, width / 2f, height / 2f, 400f)

        pointLight(0f, 0f, 100f, width / 2f, height / 2f, 0f)
        directionalLight(0f, 0f, 80f, 1f, 0f, -0.5f)
//        noLights()

        translateCenter()
        scale(0.8f)

        val rotationInner = easeCubicInOut(
            t = radianSeconds(LogoRotationIntervalSec).toDouble(),
            b = 0.0,
            c = TWO_PI.toDouble(),
            d = TWO_PI.toDouble()
        ).toFloat()

        val rotationOuter = easeSineInOut(
            t = radianSeconds(LogoRotationIntervalSec).toDouble(),
            b = 0.0,
            c = TWO_PI.toDouble(),
            d = TWO_PI.toDouble()
        ).toFloat()

        pushPop {
            rotateY(rotationInner)
            pushPop {
                rotateY(-radians(30f))
                rotateX(radians(20f))
                shape(logoInner)
            }
        }

        pushPop {
            rotateY(rotationOuter)
            pushPop {
                rotateY(-radians(30f))
                rotateX(radians(20f))
                rotateZ(-radianSeconds(TextRotationIntervalSec))
                shape(logoOuter)
            }
        }

        fx.render().apply {
            if (!mousePressed) {
                rgbSplit(80f)
                grayScale()
                bloom(0.5f, 80, 100f)
                noise(0.2f, 0.1f)
                pixelate(shorterDimension() / 1.5f)
            }
        }.compose()

        if (ExportMode) {
            saveFrame("data/export/########.tif")
            exportMillis += (1000 / Fps).toInt()
            if (provideMillis() > ExportDurationSec * 1000) {
                exit()
            }
        }
    }
}