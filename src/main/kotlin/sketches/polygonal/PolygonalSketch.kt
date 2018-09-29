package sketches.polygonal

import centerX
import centerY
import ddf.minim.AudioInput
import ddf.minim.AudioListener
import ddf.minim.Minim
import ddf.minim.analysis.BeatDetect
import ddf.minim.analysis.FFT
import midiRange
import newLine
import processing.core.PApplet
import processing.core.PConstants
import processing.event.KeyEvent
import sketches.polygonal.asteroid.Asteroid
import sketches.polygonal.star.Starfield
import tools.galaxy.Galaxy
import tools.galaxy.controls.Joystick
import tools.galaxy.controls.PushButton

class PolygonalSketch : PApplet(), AudioListener {

    companion object {
        const val NUMBER_ASTEROIDS = 3
    }

    // region AudioListener for input signal

    override fun samples(p0: FloatArray?) {
        beatDetect.detect(audioIn.mix)
        fft.forward(audioIn.mix)
    }

    override fun samples(p0: FloatArray?, p1: FloatArray?) {
        beatDetect.detect(audioIn.mix)
        fft.forward(audioIn.mix)
    }

    // endregion

    // region TouchOSC

    val galaxy = Galaxy()
    lateinit var joystick: Joystick
    lateinit var regenerateButton: PushButton
    var shouldRegenerate = false

    // endregion

    // region modes

    var debugWindowEnabled = true
    var flickerEnabled = false
    var scaleByAudioEnabled = false
    var centerWeightEnabled = false
    var beatDetectEnabled = true
    var wiggleEnabled = false
    var starfieldRotationEnabled = true
    var starSpeed = 1f
    var starCount = 400
    var starfieldRotation = 0f

    var hue = 130f
    var sat = 255f
    var bri = 255f

    // endregion

    lateinit var starfield1: Starfield
    lateinit var starfield2: Starfield
    val triangloids = mutableListOf<Asteroid>()

    lateinit var minim: Minim
    lateinit var audioIn: AudioInput
    lateinit var fft: FFT
    lateinit var beatDetect: BeatDetect

    var rmsSum = 0f
    var bassSum = 0f
    var vx = 0f
    var vy = 0f

    override fun settings() {
        size(1280, 720, PConstants.P3D)
        smooth(4)
    }

    private fun regenerate() {
        triangloids.removeAt(0)
        triangloids.add(Asteroid(this, centerWeightEnabled, fft))
    }

    override fun setup() {
        colorMode(HSB, 360f, 100f, 100f)

        minim = Minim(this)

        audioIn = minim.getLineIn()
        audioIn.addListener(this)

        fft = FFT(audioIn.bufferSize(), audioIn.sampleRate())
        fft.logAverages(22, 1)

        beatDetect = BeatDetect(audioIn.bufferSize(), audioIn.sampleRate())
        beatDetect.setSensitivity(150)

        starfield1 = Starfield(this, 300)
        starfield2 = Starfield(this, 300)

        repeat(NUMBER_ASTEROIDS, action = { triangloids.add(Asteroid(this, centerWeightEnabled, fft)) })

        // TouchOSC
        galaxy.connect()
        joystick = galaxy.createJoystick(0, 0, 1, 2).apply { flipped = true }
        regenerateButton = galaxy.createPushButton(0, 6) {
            shouldRegenerate = true
        }
    }

    override fun draw() {
        if (shouldRegenerate) {
            regenerate()
            shouldRegenerate = false
        }

        starSpeed = galaxy.fader3.midiRange(1f, 5f)
        starCount = lerp(starCount.toFloat(), galaxy.pot4.midiRange(0f, 400f), 0.1f).toInt()
        starfieldRotation = galaxy.pot5.midiRange(0f, 3f)
        vx += joystick.x * .01f
        vy += joystick.y * .01f
        vx *= 0.95f
        vy *= 0.95f

//        flickerEnabled = kontrol.pad(0, 0).state
//        scaleByAudioEnabled = kontrol.pad(0, 1).state
//        centerWeightEnabled = kontrol.pad(0, 2).state
//        beatDetectEnabled = kontrol.pad(0, 3).state
//        wiggleEnabled = kontrol.pad(1, 0).state
//        autoMouseEnabled = kontrol.pad(1, 1).state
//        starfieldRotationEnabled = kontrol.pad(1, 2).state
//        starCount = lerp(starCount.toFloat(), kontrol.knob1.midiRange(500f), 0.04f).toInt()
//        hue = kontrol.encoder.midiRange(255f)starfieldRotation

        rmsSum += audioIn.mix.level()
        rmsSum *= 0.2f

        if (fft.avgSize() >= 2) {
            bassSum += fft.getAvg(0)
            bassSum *= 0.2f
        }

        if (beatDetectEnabled && beatDetect.isSnare) {
            regenerate()
        }

        background(258f, 84f, 25f)

        if (debugWindowEnabled) {
            debugWindow()
        }

        if (flickerEnabled && frameCount % 4 == 0) {
            return
        }

        // Stars
        if (starfieldRotationEnabled) {
            starfield1.rotate(map(bassSum, 0f, 50f, 0f, 0.04f * starfieldRotation))
            starfield2.rotate(map(bassSum, 0f, 50f, 0f, 0.08f * starfieldRotation))
        }

        starfield1.setCount(starCount)
        starfield2.setCount(starCount)
        starfield1.update(speed = (2 * starSpeed).toInt())
        starfield2.update(speed = (4 * starSpeed).toInt())
        starfield1.setColor(hue, sat, bri)
        starfield2.setColor(hue, sat, bri)
        starfield1.draw()
        starfield2.draw()

        for ((index, triangloid) in triangloids.withIndex()) {
            triangloid.getShape().rotateY(0f + map(bassSum, 0f, 50f, 0f, 0.05f) + vx)
            triangloid.getShape().rotateX(0.005f * (index + 1) + vy)

            if (wiggleEnabled) {
                triangloid.wiggle()
            }

            pushMatrix()
            translate(width / 2f, height / 2f)
            if (scaleByAudioEnabled) {
                scale(map(rmsSum, 0f, 1f, 0.5f, 1.5f))
            } else {
                scale(0.5f)
            }

            triangloid.setStrokeColor(hue, sat, bri)
            triangloid.setFillColor(258f, 84f, 25f)
            triangloid.draw()

            popMatrix()
        }

        pushMatrix()
        translate(centerX(), centerY())
        popMatrix()
    }

    fun debugWindow() {
        // debug values
        val basicInfoStr = StringBuilder()
                .append("resolution: ${width}x${height}").newLine()
                .append("frameRate: ${frameRate.toInt()}").newLine()
                .append("mouseX: ${mouseX - width / 2}").newLine()
                .append("mouseY: ${mouseY - height / 2}").newLine()
                .toString()

        noStroke()
        fill(hue, sat, bri)

        textSize(14f)
        text(basicInfoStr, 12f, 24f)

        // menu
        val menuStr = StringBuilder()
                .append("[d] toggle debug mode").newLine()
                .append("[f] flicker: $flickerEnabled").newLine()
                .append("[s] scale by audio: $scaleByAudioEnabled").newLine()
                .append("[c] center-weighted triangloids: $centerWeightEnabled").newLine()
                .append("[b] beat detect: $beatDetectEnabled").newLine()
                .append("[w] wiggle: $wiggleEnabled").newLine()
                .append("[r] starfield1 rotation: $starfieldRotationEnabled")
                .toString()

        noStroke()
        fill(hue, sat, bri)
        textSize(14f)
        text(menuStr, 12f, height - menuStr.lines().size * 20f)

        var rectHeight = 8

        pushMatrix()
        translate(12f, 100f)

        // audio RMS
        fill(hue, sat, bri)
        rect(0f, 1f * rectHeight, rmsSum * 200, rectHeight.toFloat())

        // bass RMS
        fill(hue, sat, bri)
        rect(0f, 2f * rectHeight, bassSum, rectHeight.toFloat())

        popMatrix()

        // FFT
        pushMatrix()
        translate(12f, 130f)
        for (i in 0 until fft.avgSize()) {
            // Draw frequency band
            fill(hue, sat, bri)
            rect(0f, i.toFloat() * rectHeight, fft.getAvg(i), rectHeight.toFloat())

            // Draw band frequency value
            fill(hue, sat, bri)
            textSize(10f)
            text("${fft.getAverageCenterFrequency(i)} Hz", 0f, i.toFloat() * rectHeight + rectHeight)
        }

        popMatrix()

        // Star count noStroke()
        fill(hue, sat, bri)
        textSize(14f)
        text("Star count: ${starCount}", mouseX.toFloat(), mouseY.toFloat())
    }

    override fun mouseClicked() {
        regenerate()
    }

    override fun keyPressed(event: KeyEvent?) {
        event?.let {
            when (event.key) {
                'd' -> debugWindowEnabled = !debugWindowEnabled
                'f' -> flickerEnabled = !flickerEnabled
                's' -> scaleByAudioEnabled = !scaleByAudioEnabled
                'c' -> centerWeightEnabled = !centerWeightEnabled
                'b' -> beatDetectEnabled = !beatDetectEnabled
                'w' -> wiggleEnabled = !wiggleEnabled
                'r' -> starfieldRotationEnabled = !starfieldRotationEnabled
            }
        }

        super.keyPressed(event)
    }
}