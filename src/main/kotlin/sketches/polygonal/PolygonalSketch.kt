package sketches.polygonal

import centerX
import centerY
import newLine
import processing.core.PApplet
import processing.core.PApplet.lerp
import processing.core.PApplet.map
import processing.event.KeyEvent
import sketches.BaseSketch
import sketches.polygonal.asteroid.Asteroid
import sketches.polygonal.star.Starfield
import tools.audio.AudioProcessor
import tools.galaxy.Galaxy
import tools.galaxy.controls.Joystick
import tools.galaxy.controls.Pot
import tools.galaxy.controls.PushButton
import tools.galaxy.controls.ToggleButton

class PolygonalSketch(override val sketch: PApplet,
                      val audioProcessor: AudioProcessor,
                      val galaxy: Galaxy)
    : BaseSketch(sketch, audioProcessor, galaxy) {

    companion object {
        const val NUMBER_ASTEROIDS = 3
    }

    // region params

    var starSpeed = 4f
    var starCount = 400
    var starfieldRotation = 2f
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
    lateinit var regenerateButton: PushButton
    lateinit var beatDetectButton: ToggleButton
    lateinit var starSpeedPot: Pot
    lateinit var starCountPot: Pot
    lateinit var starfieldRotationPot: Pot

    // endregion

    lateinit var starfield1: Starfield
    lateinit var starfield2: Starfield
    val triangloids = mutableListOf<Asteroid>()

    var rmsSum = 0f
    var bassSum = 0f
    var vx = 0f
    var vy = 0f

    private fun regenerate() {
        triangloids.removeAt(0)
        triangloids.add(Asteroid(sketch, centerWeightEnabled, audioProcessor))
    }

    override fun setup() {
        starfield1 = Starfield(sketch, 300).apply { motion = Starfield.Motion.TRANSLATING_FORWARD }
        starfield2 = Starfield(sketch, 300).apply { motion = Starfield.Motion.TRANSLATING_FORWARD }

        repeat(NUMBER_ASTEROIDS, action = { triangloids.add(Asteroid(sketch, centerWeightEnabled, audioProcessor)) })

        // TouchOSC
        joystick = galaxy.createJoystick(0, 0, 1, 2).apply { flipped = true }
        regenerateButton = galaxy.createPushButton(0, 6) { shouldRegenerate = true }
        beatDetectButton = galaxy.createToggleButton(0, 7, beatDetectEnabled)
        starSpeedPot = galaxy.createPot(0, 3, 0.5f, 5f, starSpeed)
        starCountPot = galaxy.createPot(0, 4, 0f, 400f, starCount.toFloat())
        starfieldRotationPot = galaxy.createPot(0, 5, 0f, 3f, starfieldRotation)
    }

    override fun onBecameActive() {

    }

    override fun draw() {
        if (shouldRegenerate) {
            regenerate()
            shouldRegenerate = false
        }

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

        val newMotion = when (starfield1.motion) {
            Starfield.Motion.ZOOMING -> Starfield.Motion.TRANSLATING_BACKWARD
            Starfield.Motion.TRANSLATING_BACKWARD -> Starfield.Motion.TRANSLATING_FORWARD
            Starfield.Motion.TRANSLATING_FORWARD -> Starfield.Motion.ZOOMING
        }

        starfield1.motion = newMotion
        starfield2.motion = newMotion
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