package sketches.polygonal

import centerX
import centerY
import newLine
import processing.core.PApplet
import processing.core.PApplet.*
import processing.core.PConstants
import processing.event.KeyEvent
import sketches.BaseSketch
import sketches.polygonal.asteroid.Asteroid
import sketches.polygonal.star.Starfield
import tools.FFTLogger
import tools.audio.AudioProcessor
import tools.galaxy.Galaxy
import tools.galaxy.controls.Joystick
import tools.galaxy.controls.Pot
import tools.galaxy.controls.ToggleButton

class PolygonalSketch(override val sketch: PApplet,
                      val audioProcessor: AudioProcessor,
                      val galaxy: Galaxy)
    : BaseSketch(sketch, audioProcessor, galaxy) {

    companion object {
        const val NUMBER_ASTEROIDS = 3
    }

    // region params

    var starAccel = 0.4f
    var starMotion = Starfield.Motion.ZOOMING
    var starSpeed = 0.5f
    var starCount = 1200
    var starfieldRotation = 1f
    var shouldRegenerate = false
    var beatDetectEnabled = true
    var flickerEnabled = false // TODO midi
    var scaleByAudioEnabled = false // TODO midi
    var centerWeightEnabled = false // TODO midi
    var wiggleEnabled = false // TODO midi
    var starfieldRotationEnabled = true // TODO midi

    var hue = 130f
    var sat = 255f
    var bri = 255f

    // endregion

    // region TouchOSC

    lateinit var joystick: Joystick
    lateinit var beatDetectButton: ToggleButton
    lateinit var starSpeedPot: Pot
    lateinit var starCountPot: Pot
    lateinit var starfieldRotationPot: Pot
    lateinit var starAccelPot: Pot

    // endregion

    lateinit var starfield1: Starfield
    lateinit var starfield2: Starfield
    val triangloids = mutableListOf<Asteroid>()
    lateinit var fftLogger: FFTLogger

    var rmsSum = 0f
    var bassSum = 0f
    var vx = 0f
    var vy = 0f

    private fun regenerate() {
        triangloids.removeAt(0)
        triangloids.add(Asteroid(sketch, centerWeightEnabled, audioProcessor))
    }

    override fun setup() {
        starfield1 = Starfield(sketch, 300).apply { motion = starMotion }
        starfield2 = Starfield(sketch, 300).apply { motion = starMotion }
        repeat(NUMBER_ASTEROIDS, action = { triangloids.add(Asteroid(sketch, centerWeightEnabled, audioProcessor)) })
        fftLogger = FFTLogger(sketch, audioProcessor)

        // TouchOSC
        joystick = galaxy.createJoystick(0, 0, 1, 2).apply { flipped = true }
        galaxy.createPushButton(0, 6) { shouldRegenerate = true }
        galaxy.createPushButton(0, 8) { starMotion = Starfield.Motion.ZOOMING }
        galaxy.createPushButton(0, 9) { starMotion = Starfield.Motion.TRANSLATING_BACKWARD }
        galaxy.createPushButton(0, 11) { starMotion = Starfield.Motion.TRANSLATING_FORWARD }
        beatDetectButton = galaxy.createToggleButton(0, 7, beatDetectEnabled)
        starSpeedPot = galaxy.createPot(0, 3, 0.5f, 5f, starSpeed)
        starCountPot = galaxy.createPot(0, 4, 0f, 1200f, starCount.toFloat())
        starfieldRotationPot = galaxy.createPot(0, 5, 0f, 3f, starfieldRotation)
        starAccelPot = galaxy.createPot(0, 12, 0f, 1f, 0.6f)
    }

    override fun onBecameActive() {

    }

    override fun draw() {
        if (shouldRegenerate) {
            regenerate()
            shouldRegenerate = false
        }

        starAccel = starAccelPot.value
        starSpeed = starSpeedPot.value
        starCount = lerp(starCount.toFloat(), starCountPot.value, 0.1f).toInt()
        starfieldRotation = starfieldRotationPot.value
        vx += joystick.x * .01f
        vy += joystick.y * .01f
        vx *= 0.95f
        vy *= 0.95f
        beatDetectEnabled = beatDetectButton.isPressed

//        flickerEnabled = kontrol.pad(0, 0).state
//        scaleByAudioEnabled = kontrol.pad(0, 1).state
//        centerWeightEnabled = kontrol.pad(0, 2).state
//        wiggleEnabled = kontrol.pad(1, 0).state
//        autoMouseEnabled = kontrol.pad(1, 1).state
//        starfieldRotationEnabled = kontrol.pad(1, 2).state
//        starCount = lerp(starCount.toFloat(), kontrol.knob1.midiRange(500f), 0.04f).toInt()
//        hue = kontrol.encoder.midiRange(255f)starfieldRotation

        rmsSum += audioProcessor.audioInput.mix.level()
        rmsSum *= 0.2f

        if (audioProcessor.fft.avgSize() >= 2) {
            bassSum += audioProcessor.getFftAvg(0)
            bassSum *= 0.2f
        }

        if (beatDetectEnabled && audioProcessor.beatDetect.isSnare) {
            regenerate()
        }

        background(258f, 84f, 25f)

        if (isInDebugMode) {
            debugWindow()
        }

        if (flickerEnabled && sketch.frameCount % 4 == 0) {
            return
        }

        // Stars
        if (starfieldRotationEnabled) {
            starfield1.rotate(map(bassSum, 0f, 50f, 0f, 0.04f * starfieldRotation))
            starfield2.rotate(map(bassSum, 0f, 50f, 0f, 0.08f * starfieldRotation))
        }

        starfield1.setCount(starCount)
        starfield2.setCount(starCount)
        starfield1.update(speed = (2 * starSpeed).toInt() + (bassSum * starAccel).toInt())
        starfield2.update(speed = (4 * starSpeed).toInt())
        starfield1.setColor(hue, sat, bri)
        starfield2.setColor(hue, sat, bri)
        starfield1.motion = starMotion
        starfield2.motion = starMotion
        starfield1.draw()
        starfield2.draw()

        for ((index, triangloid) in triangloids.withIndex()) {
            triangloid.getShape().rotateY(0f + map(bassSum, 0f, 50f, 0f, 0.05f) + vx)
            triangloid.getShape().rotateX(0.005f * (index + 1) + vy)
            triangloid.getShape().rotateZ(0.010f)

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

            hud()
        }
    }

    fun hud() {
        pushMatrix()
        translate(centerX(), centerY())
        rotateX(sin(millis() * 0.0005f) * PConstants.PI / 100f)
        rotateZ(sin(millis() * 0.0005f) * PConstants.PI / 100f)
        rotateY(sin(millis() * 0.0005f) * PConstants.PI / 20f)
        translate(0f, 0f, sin(millis() * 0.001f) * PConstants.PI / 50f)

        translate(-centerX() / 2, -centerY() / 2)
        fftLogger.draw()
        popMatrix()

        pushMatrix()
        translate(centerX(), centerY())
        rotateX(sin(millis() * 0.0005f) * PConstants.PI / 100f)
        rotateZ(sin(millis() * 0.0005f) * PConstants.PI / 100f)
        rotateY(sin(millis() * 0.0005f) * PConstants.PI / 20f)
        translate(0f, 0f, sin(millis() * 0.001f) * PConstants.PI / 50f)

        translate(centerX() / 8, centerY() / 2)
        textSize(20f)
        text("Astral / Bop (RU) /\n09.11.2018", 0f, 0f)
        popMatrix()

        pushMatrix()
        translate(centerX(), centerY())
        rotateX(sin(millis() * 0.0005f) * PConstants.PI / 100f)
        rotateZ(sin(millis() * 0.0005f) * PConstants.PI / 100f)
        rotateY(sin(millis() * 0.0005f) * PConstants.PI / 80f)
        translate(0f, 0f, sin(millis() * 0.001f) * PConstants.PI / 50f)

        translate(centerX() / 2 + 40f, centerY() / 2 - 20)
        rect(0f, 0f, - bassSum * 4, - 5f)

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
        for (i in 0 until audioProcessor.fft.avgSize()) {
            // Draw frequency band
            fill(hue, sat, bri)
            rect(0f, i.toFloat() * rectHeight, audioProcessor.getFftAvg(i), rectHeight.toFloat())

            // Draw band frequency value
            fill(hue, sat, bri)
            textSize(10f)
            text("${audioProcessor.fft.getAverageCenterFrequency(i)} Hz", 0f, i.toFloat() * rectHeight + rectHeight)
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