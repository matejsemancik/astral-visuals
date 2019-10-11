package dev.matsem.astral.sketches.terrain

import dev.matsem.astral.sketches.BaseSketch
import dev.matsem.astral.sketches.SketchLoader
import dev.matsem.astral.tools.audio.AudioProcessor
import dev.matsem.astral.tools.automator.MidiAutomator
import dev.matsem.astral.tools.extensions.centerX
import dev.matsem.astral.tools.extensions.centerY
import dev.matsem.astral.tools.extensions.newLine
import dev.matsem.astral.tools.galaxy.Galaxy
import dev.matsem.astral.tools.logging.FFTLogger
import org.koin.core.inject
import processing.core.PApplet.map
import processing.core.PConstants
import processing.core.PConstants.TRIANGLE_STRIP

class TerrainSketch : BaseSketch() {

    override val sketch: SketchLoader by inject()
    private val audioProcessor: AudioProcessor by inject()
    private val galaxy: Galaxy by inject()
    private val automator: MidiAutomator by inject()

    companion object {
        const val RENDER_MODE_TRIANGLE_STRIP = "RENDER_MODE_TRIANGLE_STRIP"
        const val RENDER_MODE_LINES_Z = "RENDER_MODE_LINES_Z"
        const val RENDER_MODE_LINES_Y = "RENDER_MODE_LINES_Y"

        val RENDER_MODES = mapOf(
                0 to RENDER_MODE_TRIANGLE_STRIP,
                1 to RENDER_MODE_LINES_Z,
                2 to RENDER_MODE_LINES_Y
        )

        const val TERRAIN_MODE_BASS_CORNER = "TERRAIN_MODE_BASS_CORNER"
        const val TERRAIN_MODE_BASS_CENTER = "TERRAIN_MODE_BASS_CENTER"
        const val TERRAIN_MODE_BASS_LEFT_ALIGNED = "TERRAIN_MODE_BASS_LEFT_ALIGNED"
        const val TERRAIN_MODE_BASS_RIGHT_ALIGNED = "TERRAIN_MODE_BASS_RIGHT_ALIGNED"

        val TERRAIN_MODES = mapOf(
                0 to TERRAIN_MODE_BASS_CORNER,
                1 to TERRAIN_MODE_BASS_CENTER,
                2 to TERRAIN_MODE_BASS_LEFT_ALIGNED,
                3 to TERRAIN_MODE_BASS_RIGHT_ALIGNED
        )
    }

    // region prefs

    var renderMode = 0
    var terrainMode = 0

    var rotX = 0f + PConstants.PI / 2f
    var rotY = 0f
    var rotZ = 0f
    val joystick = galaxy.createJoystick(1, 0, 1, 2, 3, 4, 5).flipped()
    val terrainAmpPot = galaxy.createPot(1, 6, 0f, 200f, 100f)
    val perlinAmpPot = galaxy.createPot(1, 7, 0f, 4f, 1.5f)
    val rotationResetButton = galaxy.createPushButton(1, 8) {
        rotX = 0f + PConstants.PI / 2f
        rotY = 0f
        rotZ = 0f
    }
    val perlinResPot = galaxy.createPot(1, 9, 0f, 0.5f, 0.4f)
    val distancePot = galaxy.createPot(1, 10, -sketch.width / 1.5f, sketch.width / 1.5f, 0f).lerp(0.05f)
    val flyingPot = galaxy.createPot(1, 11, -0.1f, 0.1f, 0f).lerp(0.05f)
    val perlinBoostPot = galaxy.createPot(1, 12, 0f, 5f, 0f)
    val secondTerrainButton = galaxy.createToggleButton(1, 13, true)
    val renderModeButtons = galaxy.createButtonGroup(1, listOf(14, 15, 16), listOf(14))
    val terrainModeButtons = galaxy.createButtonGroup(1, listOf(17, 18, 19, 20), listOf(17))

    // endregion

    // region Terrain
    private val w = 1600f
    private val h = 1920f
    private var scale = 50f

    private var cols = (w / scale).toInt()
    private var rows = (h / scale).toInt()

    private val terrain = Array(rows) { FloatArray(cols) }
    private var musicTerrain = Array(rows) { FloatArray(cols) }

    private var flying = 0f

    // endregion

    private lateinit var fftLogger: FFTLogger

    override fun setup() {
        fftLogger = FFTLogger(sketch, audioProcessor)
        automator.setupWithGalaxy(
                channel = 1,
                recordButtonCC = 21,
                playButtonCC = 22,
                loopButtonCC = 23,
                clearButtonCC = 24,
                channelFilter = null
        )
    }

    override fun draw() = with(sketch) {
        automator.update()
        renderMode = renderModeButtons.activeButtonsIndices().first()
        terrainMode = terrainModeButtons.activeButtonsIndices().first()

        rotX += joystick.y * .02f
        rotY += joystick.x * .02f
        rotZ += joystick.z * .02f

        background(bgColor)

        stroke(fgColor)
        strokeWeight(6f)
        fill(accentColor)

        regenerate()
        flying += flyingPot.value

        // First terrain (upper)
        if (secondTerrainButton.isPressed) {
            pushMatrix()
            translate(centerX(), centerY())

            rotateX(rotX)
            rotateY(rotY)
            rotateZ(rotZ)

            translate(-w / 2, -h / 2, -distancePot.value)
            drawTerrain(1f)
            popMatrix()
        }

        // Second terrain
        pushMatrix()
        translate(centerX(), centerY())

        rotateX(rotX)
        rotateY(rotY)
        rotateZ(rotZ)

        translate(-w / 2, -h / 2, distancePot.value)
        drawTerrain(-1f)
        popMatrix()

        if (isInDebugMode) {
            debugWindow()
        }
    }

    fun drawTerrain(multiplier: Float) = with(sketch) {
        for (y in 0 until rows - 1) {
            beginShape(TRIANGLE_STRIP)

            for (x in 0 until cols) {
                when (RENDER_MODES[renderMode]) {
                    RENDER_MODE_TRIANGLE_STRIP -> {
                        vertex(x * scale, y * scale, terrain[y][x] * multiplier)
                        vertex(x * scale, (y + 1) * scale, terrain[y + 1][x] * multiplier)
                    }

                    RENDER_MODE_LINES_Z -> {
                        vertex(x * scale, y * scale, terrain[y][x] * multiplier)
                    }

                    RENDER_MODE_LINES_Y -> {
                        vertex(x * scale, y * scale + map(terrain[y][x], 0f, 10f, 0f, 5f), 0f * multiplier)
                    }
                }
            }

            endShape()
        }
    }

    private fun regenerate() = with(sketch) {
        val buff = musicTerrain.toMutableList()
        buff.removeAt(buff.size - 1)
        buff.add(0, FloatArray(cols))
        for (x in 0 until cols) {
            val amp = if (x < audioProcessor.fft.avgSize()) {
                map(audioProcessor.getFftAvg(x), 0f, 80f, 0f, 20f + terrainAmpPot.value)
            } else {
                0f
            }

            when (TERRAIN_MODES[terrainMode]) {
                TERRAIN_MODE_BASS_CORNER -> {
                    buff[0][x] += amp
                    buff[0][cols - x - 1] += amp
                }

                TERRAIN_MODE_BASS_CENTER -> {
                    if (x < cols / 2) {
                        buff[0][x + cols / 2] += amp
                        buff[0][(cols / 2) - x] += amp
                    }
                }

                TERRAIN_MODE_BASS_LEFT_ALIGNED -> {
                    buff[0][x] += amp
                    if (x < cols / 2) {
                        buff[0][x + cols / 2] += amp
                    }
                }

                TERRAIN_MODE_BASS_RIGHT_ALIGNED -> {
                    buff[0][cols - x - 1] += amp
                    if (x < cols / 2) {
                        buff[0][(cols / 2) - x] += amp
                    }
                }
            }
        }

        musicTerrain = buff.toTypedArray()

        var yoff = flying
        for (y in 0 until rows) {
            var xoff = 0f
            for (x in 0 until cols) {
                terrain[y][x] = map(
                        noise(xoff, yoff),
                        0f,
                        1f,
                        -20f * perlinAmpPot.value,
                        50f * perlinAmpPot.value) + musicTerrain[y][x] + audioProcessor.getRange(6000f..12000f) * perlinBoostPot.value

                xoff += perlinResPot.value
            }

            yoff += 0.2f
        }
    }

    private fun debugWindow() = with(sketch) {
        // debug values
        val basicInfoStr = StringBuilder()
                .append("resolution: ${width}x$height").newLine()
                .append("frameRate: ${frameRate.toInt()}").newLine()
                .append("mouseX: ${mouseX - width / 2}").newLine()
                .append("mouseY: ${mouseY - height / 2}").newLine()
                .toString()

        noStroke()
        fill(0f, 255f, 100f)

        textSize(14f)
        text(basicInfoStr, 12f, 24f)

        fftLogger.draw(12f, 96f)

        // menu
        val menuStr = StringBuilder()
                .append("[d] toggle debug mode").newLine()
                .append("[m] drawing mode: ${RENDER_MODES[renderMode]}").newLine()
                .append("[t] terrain mode: ${TERRAIN_MODES[terrainMode]}")
                .toString()

        noStroke()
        fill(0f, 255f, 100f)
        textSize(14f)
        text(menuStr, 12f, height - menuStr.lines().size * 20f)
    }
}