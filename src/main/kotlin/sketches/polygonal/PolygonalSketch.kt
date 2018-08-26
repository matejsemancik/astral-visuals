package sketches.polygonal

import centerX
import centerY
import ddf.minim.AudioInput
import ddf.minim.AudioListener
import ddf.minim.Minim
import ddf.minim.analysis.BeatDetect
import ddf.minim.analysis.FFT
import io.reactivex.subjects.PublishSubject
import newLine
import processing.core.PApplet
import processing.core.PConstants
import processing.event.KeyEvent
import sketches.polygonal.asteroid.Asteroid
import sketches.polygonal.star.Starfield
import tools.kontrol.KontrolF1
import tools.kontrol.Pad
import tools.kontrol.midiRange

class PolygonalSketch : PApplet(), AudioListener {

    // region AudioListener for input signal

    companion object {
        const val NUMBER_ASTEROIDS = 3
    }

    override fun samples(p0: FloatArray?) {
        beatDetect.detect(audioIn.mix)
        fft.forward(audioIn.mix)

        audioLevelObservable.onNext(audioIn.mix.level())
    }

    override fun samples(p0: FloatArray?, p1: FloatArray?) {
        beatDetect.detect(audioIn.mix)
        fft.forward(audioIn.mix)

        audioLevelObservable.onNext(audioIn.mix.level())
    }

    // endregion

    // region modes

    var debugWindowEnabled = true
    var flickerEnabled = false
    var scaleByAudioEnabled = false
    var centerWeightEnabled = false
    var beatDetectEnabled = false
    var wiggleEnabled = false
    var autoMouseEnabled = true
    var starfieldRotationEnabled = false
    var starCount = 400

    var hue = 0f
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
    lateinit var autoMouse: AutoMouse
    val kontrol = KontrolF1()

    val audioLevelObservable: PublishSubject<Float> = PublishSubject.create()
    var rmsSum = 0f
    var bassSum = 0f

    override fun settings() {
        size(1280, 720, PConstants.P3D)
        smooth(4)
    }

    private fun regenerate() {
        triangloids.removeAt(0)
        triangloids.add(Asteroid(this, centerWeightEnabled, fft))
    }

    override fun setup() {
        colorMode(HSB, 255f)

        minim = Minim(this)

        audioIn = minim.getLineIn()
        audioIn.addListener(this)

        fft = FFT(audioIn.bufferSize(), audioIn.sampleRate())
        fft.logAverages(22, 1)

        beatDetect = BeatDetect(audioIn.bufferSize(), audioIn.sampleRate())
        beatDetect.setSensitivity(150)

        autoMouse = AutoMouse(this, centerX(), centerY())

        starfield1 = Starfield(this, 300)
        starfield2 = Starfield(this, 300)

        repeat(NUMBER_ASTEROIDS, action = { triangloids.add(Asteroid(this, centerWeightEnabled, fft)) })

        kontrol.pad(0, 0).setMode(Pad.Mode.TRIGGER)
        kontrol.onEncoder { it ->
            val hue = it
            kontrol.pads.forEach { it.setColorOn(hue, 255, 255) }
        }
    }

    override fun draw() {
        flickerEnabled = kontrol.pad(0, 0).state
        scaleByAudioEnabled = kontrol.pad(0, 1).state
        centerWeightEnabled = kontrol.pad(0, 2).state
        beatDetectEnabled = kontrol.pad(0, 3).state
        wiggleEnabled = kontrol.pad(1, 0).state
        autoMouseEnabled = kontrol.pad(1, 1).state
        starfieldRotationEnabled = kontrol.pad(1, 2).state
        starCount = lerp(starCount.toFloat(), kontrol.knob1.midiRange(500f), 0.04f).toInt()
        hue = kontrol.encoder.midiRange(255f)

        rmsSum += audioIn.mix.level()
        rmsSum *= 0.2f

        if (fft.avgSize() >= 2) {
            bassSum += fft.getAvg(0)
            bassSum *= 0.2f
        }

        if (beatDetectEnabled && beatDetect.isSnare) {
            regenerate()
        }

        if (autoMouseEnabled) {
            autoMouse.move()
        }

        background(0f, 0f, 0f)

        if (debugWindowEnabled) {
            debugWindow()
        }

        if (flickerEnabled && frameCount % 4 == 0) {
            return
        }

        // Stars
        if (starfieldRotationEnabled) {
            starfield1.rotate(map(bassSum, 0f, 50f, 0f, 0.04f * kontrol.knob2.midiRange(0.0f, 3.0f)))
            starfield2.rotate(map(bassSum, 0f, 50f, 0f, 0.08f * kontrol.knob2.midiRange(0.0f, 3.0f)))
        }

//        starCount = map(mouseX.toFloat(), 0f, width.toFloat(), 0f, 400f).toInt()
        starfield1.setCount(starCount)
        starfield2.setCount(starCount)
        starfield1.update(speed = (2 * kontrol.slider1.midiRange(1f, 5f)).toInt())
        starfield2.update(speed = (4 * kontrol.slider1.midiRange(1f, 5f)).toInt())
        starfield1.setColor(hue, sat, bri)
        starfield2.setColor(hue, sat, bri)
        starfield1.draw()
        starfield2.draw()

        for ((index, triangloid) in triangloids.withIndex()) {
//            triangloid.getShape().rotateY(0.000f + map(autoMouse.xPos, width.toFloat() / 2f, width.toFloat(), 0f, 0.15f))
//            triangloid.getShape().rotateX(0.000f - map(autoMouse.yPos, height.toFloat() / 2f, height.toFloat(), 0f, 0.15f))
            triangloid.getShape().rotateY(0f + map(bassSum, 0f, 50f, 0f, 0.05f))
            triangloid.getShape().rotateX(0.005f * (index + 1))

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
            triangloid.setFillColor(0f, 0f, 0f)
            triangloid.draw()

            popMatrix()
        }

        pushMatrix()
        translate(centerX().toFloat(), centerY().toFloat())
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
                .append("[a] automouse: $autoMouseEnabled").newLine()
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

        // AutoMouse
        autoMouse.draw()

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
                'a' -> autoMouseEnabled = !autoMouseEnabled
                'r' -> starfieldRotationEnabled = !starfieldRotationEnabled
            }
        }

        super.keyPressed(event)
    }
}