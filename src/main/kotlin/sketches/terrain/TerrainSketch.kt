package sketches.terrain

import centerX
import centerY
import longerDimension
import newLine
import processing.core.PApplet
import processing.core.PApplet.map
import processing.core.PConstants
import processing.core.PConstants.TRIANGLE_STRIP
import processing.event.KeyEvent
import sketches.BaseSketch
import sketches.polygonal.star.Starfield
import tools.FFTLogger
import tools.audio.AudioProcessor
import tools.galaxy.Galaxy

class TerrainSketch(override val sketch: PApplet, val audioProcessor: AudioProcessor, val galaxy: Galaxy)
    : BaseSketch(sketch, audioProcessor, galaxy), PConstants {

    companion object {
        const val PADDING = 12f

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

    var rotationZEnabled = false
    var drawMode = 0
    var terrainMode = 0

    var rotX = 0f + PConstants.PI / 2f
    var rotY = 0f + PConstants.PI / 2f
    var rotZ = 0f
    val joystick = galaxy.createJoystick(1, 0, 1, 2, 3, 4, 5).flipped()
    val terrainAmpPot = galaxy.createPot(1, 6, 0f, 200f, 100f)
    val perlinAmpPot = galaxy.createPot(1, 7, 0f, 2f, 1.5f)
    val rotationResetButton = galaxy.createPushButton(1, 8) {
        rotX = 0f + PConstants.PI / 2f
        rotY = 0f
        rotZ = 0f
    }
    val perlinResPot = galaxy.createPot(1, 9, 0f, 0.5f, 0.4f)

    // endregion

    // region Terrain
    private val w = 1280f
    private val h = 1080f
    private var scale = 60f

    private var cols = (w / scale).toInt()
    private var rows = (h / scale).toInt()

    private val terrain = Array(rows) { FloatArray(cols) }
    private var musicTerrain = Array(rows) { FloatArray(cols) }

    private var flying = 0f

    // endregion

    // region other shit

    private lateinit var starfield: Starfield
    private lateinit var starfield2: Starfield
    val font = sketch.createFont("georgiab.ttf", 24f, true)

    // endregion

    private lateinit var fftLogger: FFTLogger

    override fun setup() {
        fftLogger = FFTLogger(sketch, audioProcessor)
        starfield = Starfield(sketch, 1200).apply {
            setColor(258f, 100f, 100f)
        }
        starfield2 = Starfield(sketch, 1200).apply {
            setColor(258f, 100f, 100f)
        }
    }

    override fun onBecameActive() {

    }

    override fun draw() {
        rotX += joystick.y * .02f + 0.005f
        rotY += joystick.x * .02f
        rotZ += joystick.z * .02f

        background(258f, 84f, 25f)

        starfield.update(3 + (audioProcessor.getRange(6000f..12000f) * 5f).toInt())
        starfield.draw()
        starfield2.update(3 + (audioProcessor.getRange(2500f..2600f) * 3f).toInt())
        starfield2.draw()

        stroke(130f, 255f, 255f)
        strokeWeight(1.4f)
        fill(258f, 84f, 25f)

        regenerate()
        flying -= 0.05f // TODO midi

        // First terrain
        pushMatrix()
        translate(centerX(), centerY())

        rotateX(rotX)
        rotateY(rotY)
        rotateZ(rotZ)

        translate(-w / 2, -h / 2, -longerDimension() / 4f)
        drawTerrain(1f)
        popMatrix()

        // Second terrain
        pushMatrix()
        translate(centerX(), centerY())

        rotateX(rotX)
        rotateY(rotY)
        rotateZ(rotZ)

        translate(-w / 2, -h / 2, longerDimension() / 4f)
        drawTerrain(-1f)
        popMatrix()

        pushMatrix()
        translate(centerX(), centerY())
        scale(1f + audioProcessor.getFftAvg(0) * 0.0004f)
        sketch.textFont(font)
        sketch.textAlign(PConstants.CENTER)
        noStroke()
        fill(130f, 100f, 100f)
        textSize(48f)
        text("BOP (RU) at Astral", 0f, 0f)
        textSize(32f)
        text("Lixx (SK) / Soul Ex Machina", 0f, 42f)
        textSize(32f)
        text("09/11/18 / Kabinet Múz", 0f, 92f)
        popMatrix()

        if (isInDebugMode) {
            debugWindow()
        }
    }

    fun drawTerrain(multiplier: Float) {
        for (y in 0 until rows - 1) {
            beginShape(TRIANGLE_STRIP)

            for (x in 0 until cols) {
                when (RENDER_MODES[drawMode]) {
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

    private fun regenerate() {
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
                        50f * perlinAmpPot.value) + musicTerrain[y][x]

                xoff += perlinResPot.value
            }

            yoff += 0.2f
        }
    }

    private fun debugWindow() {
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
                .append("[r] Z rotation: $rotationZEnabled").newLine()
                .append("[m] drawing mode: ${RENDER_MODES[drawMode]}").newLine()
                .append("[t] terrain mode: ${TERRAIN_MODES[terrainMode]}")
                .toString()

        noStroke()
        fill(0f, 255f, 100f)
        textSize(14f)
        text(menuStr, 12f, height - menuStr.lines().size * 20f)
    }

    override fun keyPressed(event: KeyEvent?) {
        event?.let {
            when (event.key) {
                'r' -> rotationZEnabled = !rotationZEnabled
                'm' -> {
                    drawMode++
                    if (drawMode >= RENDER_MODES.size) {
                        drawMode = 0
                    }
                }
                't' -> {
                    terrainMode++
                    if (terrainMode >= TERRAIN_MODES.size) {
                        terrainMode = 0
                    }
                }
            }
        }

        super.keyPressed(event)
    }
}