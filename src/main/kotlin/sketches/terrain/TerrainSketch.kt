package sketches.terrain

import ddf.minim.AudioInput
import ddf.minim.AudioListener
import ddf.minim.Minim
import ddf.minim.analysis.FFT
import newLine
import processing.core.PApplet
import processing.core.PConstants
import processing.event.KeyEvent
import sketches.polygonal.star.Starfield
import tools.FFTLogger

class TerrainSketch : PApplet(), AudioListener {

    companion object {
        val MODE_TRIANGLE_STRIP = "MODE_TRIANGLE_STRIP"
        val MODE_LINES_Z = "MODE_LINES_Z"
        val MODE_LINES_ZY = "MODE_LINES_ZY"

        val MODES = mapOf(
                0 to MODE_TRIANGLE_STRIP,
                1 to MODE_LINES_Z,
                2 to MODE_LINES_ZY
        )
    }

    override fun samples(p0: FloatArray?) {
        fft.forward(p0)
    }

    override fun samples(p0: FloatArray?, p1: FloatArray?) {
        fft.forward(p0, p1)
    }

    // region prefs

    var debugEnabled = false
    var rotationZEnabled = false
    var drawMode = 0
    var rotationX = 0f
    var rotationZ = 0f

    // endregion

    // region Terrain
    val w = 600f
    val h = 800f
    var scale = 18f

    var cols = (w / scale).toInt()
    var rows = (h / scale).toInt()

    //    val terrain = Array(rows, { FloatArray(cols) })
    val terrain = Array(rows, { FloatArray(cols) })
    var musicTerrain = Array(rows, { FloatArray(cols) })

    var flying = 0f

    // endregion

    // region stars

    lateinit var starfield: Starfield

    // endregion

    lateinit var minim: Minim
    lateinit var audioIn: AudioInput
    lateinit var fft: FFT
    lateinit var fftLogger: FFTLogger

    override fun settings() {
        size(1280, 720, PConstants.P3D)
//        fullScreen(PConstants.P3D)
        smooth(4)
    }

    override fun setup() {
        minim = Minim(this)
        audioIn = minim.lineIn
        audioIn.addListener(this)
        fft = FFT(audioIn.bufferSize(), audioIn.sampleRate())
        fft.logAverages(22, 3)
        fftLogger = FFTLogger(this, fft)

        starfield = Starfield(this, 800)
    }

    override fun draw() {
        background(32f, 32f, 32f)

        starfield.update(3)
        starfield.draw()

        stroke(0f, 255f, 100f)
        strokeWeight(1.4f)
        fill(32f, 32f, 32f)

        pushMatrix()
        translate(width.toFloat() / 2, height.toFloat() / 2)

        if (mousePressed) {
            rotationX = map(mouseY.toFloat(), height.toFloat(), 0f, PConstants.PI, 0f)
        }

        if (rotationZEnabled) {
            rotationZ += 0.002f
        } else {
            rotationZ = 0f
        }

        rotateX(rotationX)
        rotateZ(rotationZ)

        translate(-w / 2, -h / 2)

        regenerate()
        flying -= 0.05f

        for (y in 0 until rows - 1) {
            beginShape(PConstants.TRIANGLE_STRIP)

            for (x in 0 until cols) {
                when (MODES[drawMode]) {
                    MODE_TRIANGLE_STRIP -> {
                        vertex(x * scale, y * scale, terrain[y][x])
                        vertex(x * scale, (y + 1) * scale, terrain[y + 1][x])
                    }

                    MODE_LINES_Z -> {
                        vertex(x * scale, y * scale, terrain[y][x])
                    }

                    MODE_LINES_ZY -> {
                        vertex(x * scale, y * scale + map(terrain[y][x], 0f, 10f, 0f, 5f), terrain[y][x])
                    }
                }
            }

            endShape()
        }

        popMatrix()

        if (debugEnabled) {
            debugWindow()
        }
    }

    private fun regenerate() {
        val buff = musicTerrain.toMutableList()
        buff.removeAt(buff.size - 1)
        buff.add(0, FloatArray(cols))
        for (x in 0 until cols) {
            val amp = if (x < fft.avgSize()) map(fft.getAvg(x), 0f, 80f, 0f, 20f) else 0f

            buff[0][x] += amp
            buff[0][cols - x - 1] += amp
        }

        musicTerrain = buff.toTypedArray()

        var yoff = flying
        for (y in 0 until rows) {
            var xoff = 0f
            for (x in 0 until cols) {
                terrain[y][x] = map(noise(xoff, yoff), 0f, 1f, -20f, 50f) + musicTerrain[y][x]
                xoff += map(mouseX.toFloat(), 0f, width.toFloat(), 0f, 0.5f)
            }

            yoff += 0.2f
        }
    }

    private fun debugWindow() {
        // debug values
        val basicInfoStr = StringBuilder()
                .append("resolution: ${width}x${height}").newLine()
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
                .append("[m] drawing mode: ${MODES[drawMode]}")
                .toString()

        noStroke()
        fill(0f, 255f, 100f)
        textSize(14f)
        text(menuStr, 12f, height - menuStr.lines().size * 20f)
    }

    override fun keyPressed(event: KeyEvent?) {
        event?.let {
            when (event.key) {
                'd' -> debugEnabled = !debugEnabled
                'r' -> rotationZEnabled = !rotationZEnabled
                'm' -> {
                    drawMode++
                    if (drawMode >= MODES.size) {
                        drawMode = 0
                    }
                }
            }
        }

        super.keyPressed(event)
    }
}