package dev.matsem.astral.playground.sketches

import ch.bildspur.postfx.builder.PostFX
import dev.matsem.astral.core.tools.animations.AnimationHandler
import dev.matsem.astral.core.tools.animations.radianSeconds
import dev.matsem.astral.core.tools.audio.AudioProcessor
import dev.matsem.astral.core.tools.audio.beatcounter.BeatCounter
import dev.matsem.astral.core.tools.audio.beatcounter.OnHat
import dev.matsem.astral.core.tools.audio.beatcounter.OnKick
import dev.matsem.astral.core.tools.audio.beatcounter.OnSnare
import dev.matsem.astral.core.tools.extensions.*
import dev.matsem.astral.core.tools.videoexport.VideoExporter
import org.koin.core.KoinComponent
import org.koin.core.inject
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PImage
import processing.video.Movie
import kotlin.random.Random

/**
 * Visual for SEM004 release by Daniel Weirdone.
 */
class Sem004 : PApplet(), AnimationHandler, KoinComponent {

    sealed class ExportConfig(val fileName: String, val audioFile: String, val width: Int, val height: Int) {
        object Square : ExportConfig("sem004_loop_sqr.mp4", "music/sem004clipVID.wav", 1080, 1080)
        object Portrait : ExportConfig("sem004_loop_port.mp4", "music/sem004clipVID.wav", 1080, 1920)
        object SquareSC : ExportConfig("sem004_loop_sqr_SC.mp4", "music/sem004clipsc.wav", 1080, 1080)
    }

    private val exportConfig = ExportConfig.SquareSC

    private val videoExporter: VideoExporter by inject()
    private val beatCounter: BeatCounter by inject()
    private val audioProcessor: AudioProcessor by inject()

    private lateinit var mov: Movie
    private lateinit var semlogo: PImage
    private lateinit var fx: PostFX

    private var shuffleSeed = 0
    private var tileSize = 300

    fun movieEvent(movie: Movie) {
        mov.read()
    }

    private fun randomize() {
        shuffleSeed = random(0f, 100f).toInt()
        tileSize = random(200f, 400f).toInt()
    }

    override fun provideMillis(): Int {
        return videoExporter.videoMillis()
    }

    override fun settings() {
        size(exportConfig.width, exportConfig.height, P2D)
    }

    override fun setup() {
        colorModeHsb()
        semlogo = loadImage("images/semlogo_notext.png").apply { resizeRatioAware(width = 200) }
        mov = Movie(this, "movie/PRAY_STATE.mp4") // by @beeple
        mov.loop()
        mov.volume(0f)
        mov.speed(0.1f)
        fx = PostFX(this)
        beatCounter.addListener(OnSnare, 1) { randomize() }
        beatCounter.addListener(OnKick, 1) { randomize() }
        beatCounter.addListener(OnHat, 1) { randomize() }
        videoExporter.prepare(
            audioFilePath = exportConfig.audioFile,
            movieFps = 24f,
            dryRun = true,
            audioGain = 1f,
            outputVideoFileName = exportConfig.fileName
        ) {
            renderFrame()
        }
    }

    override fun draw() = Unit

    private fun PApplet.renderFrame() {
        background(0)
        val tiles = mutableListOf<PImage>()
        for (x in 0 until mov.pixelWidth step tileSize) {
            for (y in 0 until mov.pixelHeight step tileSize) {
                val tile = mov.get(x, y, tileSize, tileSize)
                tiles += tile
            }
        }

        tiles.shuffle(Random(shuffleSeed))

        var index = 0
        for (x in 0 until width / 2 step tileSize) {
            for (y in 0 until height step tileSize) {
                val tile = tiles[index % (tiles.count() - 1)]
                index++
                image(tile, x.toFloat(), y.toFloat())
            }
        }

        // Mirroring
        val leftHalf = get(0, 0, width / 2, height)
        pushPop {
            translate(width.toFloat(), height.toFloat())
            scale(-1f, -1f)
            image(leftHalf, 0f, 0f)
        }

        // Logo & Text
        pushPop {
            translateCenter()
            rectMode(PConstants.CENTER)
            imageMode(PConstants.CENTER)
            noStroke()
            fill(0x000000.withAlpha())
            tint(0xffffff.withAlpha())
            rect(0f, 0f, 300f, 300f)
            image(semlogo, 0f, 0f)

            noStroke()
            fill(0xffffff.withAlpha())
            textSize(24f)
            textAlign(PConstants.LEFT)
            text("SEM004", -100f, 132f)

            noStroke()
            fill(0xffffff.withAlpha())
            rectMode(PConstants.CORNER)
            rect(0f, 118f, 100f, 8f)
        }

        // PostFX
        filter(PConstants.GRAY)
        filter(PConstants.THRESHOLD, 0.4f)

        fx
            .render()
            .apply {
                bloom(0.3f, 100, 10f)
                bloom(0.3f, 100, 10f)
                noise(0.4f, 0.1f)
                pixelate(sin(radianSeconds(5f)).mapSin(width * 1f, width * 0.4f))
            }
            .compose()
    }

    override fun mouseClicked() {
        randomize()
    }
}