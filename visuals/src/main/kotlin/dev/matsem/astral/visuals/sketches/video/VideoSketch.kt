package dev.matsem.astral.visuals.sketches.video

import dev.matsem.astral.core.tools.extensions.centerX
import dev.matsem.astral.core.tools.extensions.centerY
import dev.matsem.astral.core.tools.extensions.resizeRatioAware
import dev.matsem.astral.core.tools.extensions.shorterDimension
import dev.matsem.astral.core.tools.extensions.translateCenter
import dev.matsem.astral.visuals.sketches.BaseSketch
import dev.matsem.astral.visuals.sketches.SketchLoader
import dev.matsem.astral.core.tools.audio.AudioProcessor
import dev.matsem.astral.core.tools.galaxy.Galaxy
import org.koin.core.inject
import processing.video.Movie
import kotlin.random.Random

class VideoSketch : BaseSketch() {

    override val sketch: SketchLoader by inject()
    private val audioProcessor: AudioProcessor by inject()
    private val galaxy: Galaxy by inject()

    private val movie1 = Movie(sketch, sketch.dataPath("movies/astral03_BACKGROUND.mp4"))
    private val movie2 = Movie(sketch, sketch.dataPath("movies/astral04_BACKGROUND.mp4"))
    private val astralLogo = sketch.loadImage(sketch.dataPath("images/astrallogo_clean.png")).apply {
        resizeRatioAware(height = sketch.shorterDimension() / 2)
    }

    private val movies = arrayOf(movie1, movie2)
    private var activeMovie = movies[0]
    private var nextSwitch = 0
    private var logoVisible = false

    private val videoPushButtons = galaxy.createPushButtonGroup(9, listOf(0, 1)) {
        activeMovie = movies[it]
    }

    private val autoButton = galaxy.createToggleButton(9, 2, false)
    private val logoButton = galaxy.createToggleButton(9, 3, false)

    override fun onBecameActive() = Unit

    override fun setup() {
        movies.forEach { it.loop() }
    }

    override fun draw() = with(sketch) {
        background(0)

        if (millis() > nextSwitch) {
            nextSwitch = millis() + 15000
            if (autoButton.isPressed) {
                activeMovie = movies.random()
            }

            if (autoButton.isPressed && logoButton.isPressed) {
                logoVisible = Random.nextBoolean()
            }
        }

        if (autoButton.isPressed.not()) {
            logoVisible = logoButton.isPressed
        }

        if (activeMovie.available()) {
            activeMovie.read()
        }

        pushMatrix()
        translateCenter()
        scale(1 + audioProcessor.getRange(20f..100f) / 4000f)
        sketch.image(activeMovie, -centerX(), -centerY(), width.toFloat(), height.toFloat())
        popMatrix()

        if (logoVisible) {
            pushMatrix()
            translateCenter()
            scale(1 + audioProcessor.getRange(100f..200f) / 2000f)
            sketch.image(
                astralLogo,
                -astralLogo.width / 2f,
                -astralLogo.height / 2f,
                astralLogo.width.toFloat(),
                astralLogo.height.toFloat()
            )
            popMatrix()
        }
    }
}