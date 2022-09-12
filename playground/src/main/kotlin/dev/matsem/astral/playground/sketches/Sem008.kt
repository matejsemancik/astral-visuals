package dev.matsem.astral.playground.sketches

import ch.bildspur.postfx.builder.PostFX
import dev.matsem.astral.core.tools.animations.AnimationHandler
import dev.matsem.astral.core.tools.animations.radianSeconds
import dev.matsem.astral.core.tools.animations.saw
import dev.matsem.astral.core.tools.audio.beatcounter.BeatCounter
import dev.matsem.astral.core.tools.audio.beatcounter.OnKick
import dev.matsem.astral.core.tools.audio.beatcounter.OnSnare
import dev.matsem.astral.core.tools.extensions.centerX
import dev.matsem.astral.core.tools.extensions.colorModeHsb
import dev.matsem.astral.core.tools.extensions.mapSin
import dev.matsem.astral.core.tools.extensions.mapp
import dev.matsem.astral.core.tools.extensions.pushPop
import dev.matsem.astral.core.tools.extensions.quantize
import dev.matsem.astral.core.tools.extensions.shorterDimension
import dev.matsem.astral.core.tools.extensions.translateCenter
import dev.matsem.astral.core.tools.videoexport.VideoExporter
import org.koin.core.KoinComponent
import org.koin.core.inject
import processing.core.PApplet

/**
 * VJ loop for SEM008 release https://soundcloud.com/semfree/sem008-offish-evasion-straight-shootingcalculated-risk-out-300922
 */
class Sem008 : PApplet(), KoinComponent, AnimationHandler {

    sealed class ExportConfig(
        val width: Int,
        val height: Int,
        val outputFile: String,
        val travelLength: Int
    ) {
        object Square : ExportConfig(1080, 1080, "sem008_loop_sqr.mp4", 200_000)
        object Story : ExportConfig(1080, 1920, "sem008_loop_story.mp4", 300_000)
    }

    private val exportConfig = ExportConfig.Square

    private lateinit var fx: PostFX

    private val beatCounter: BeatCounter by inject()
    private val videoExporter: VideoExporter by inject()

    override fun provideMillis(): Int = videoExporter.videoMillis()

    private var ures: Float = 3f
    private var uresTarget: Float = 3f
    private var vres: Float = 3f
    private var vresTarget: Float = 3f
    private var zoom: Float = 1f
    private var invert: Boolean = false

    override fun settings() {
        size(exportConfig.width, exportConfig.height, P3D)
    }

    override fun setup() {
        colorModeHsb()
        surface.setAlwaysOnTop(true)
        fx = PostFX(this)

        beatCounter.addListener(OnKick, 2) {
            uresTarget = random(1f, 5f)
            vresTarget = random(1f, 5f)
        }

        beatCounter.addListener(OnKick, 4) {
            invert = true
        }

        beatCounter.addListener(OnSnare, 2) {
            zoom = random(0.7f, 1f)
        }

        videoExporter.prepare(
            audioFilePath = "music/sem0081minclip.mp3",
            movieFps = 30f,
            dryRun = true,
            audioGain = 2f,
            outputVideoFileName = exportConfig.outputFile
        ) { drawSketch() }
    }

    override fun draw() = drawSketch()

    private fun PApplet.drawSketch() {
        ures = lerp(ures, uresTarget, 0.2f)
        vres = lerp(vres, vresTarget, 0.2f)

        clear()
        background(0f, 0f, 10f)

        sphereDetail(ures.toInt(), vres.toInt())

        pushPop {
            translateCenter()
            scale(zoom)

            for (travel in 0 until exportConfig.travelLength step 1000) {
                strokeWeight(4f)
                if (travel < saw(1 / 5f).mapp(0f, exportConfig.travelLength.toFloat())) {
                    fill(0f, 0f, 10f)
                    stroke(0f, 0f, 90f)
                } else {
                    fill(0f, 0f, 60f)
                    stroke(0f, 0f, 0f)
                }

                pushPop {
                    rotateZ(radianSeconds(20f) + travel)
                    translate(0f, 0f, millis().toFloat())

                    pushPop {
                        translate(-centerX() * 1.2f, 0f)
                        translate(0f, 0f, -travel.toFloat())
                        rotateY(radianSeconds(10f).quantize(0.05f))
                        rotateZ(-radianSeconds(30f))
                        sphere(shorterDimension() * 0.50f)
                    }

                    pushPop {
                        translate(centerX() * 1.2f, 0f)
                        translate(0f, 0f, -travel.toFloat())
                        rotateY(-radianSeconds(10f).quantize(0.05f))
                        rotateZ(radianSeconds(30f))
                        sphere(shorterDimension() * 0.50f)
                    }
                }
            }
        }

        fx.render().apply {
            noise(0.3f, 0.5f)
            rgbSplit(50f)
            bloom(0.5f, 20, 5f)
            pixelate(sin(radianSeconds(5f)).mapSin(width * 1f, width * 0.4f))
            if (invert) {
                invert()
                invert = false
            }
        }.compose()
    }
}