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

class PolygonalSketch : PApplet(), AudioListener {

    // region AudioListener for input signal

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
    var flickerEnabled = true
    var scaleByAudioEnabled = true
    var centerWeightEnabled = false
    var beatDetectEnabled = true
    var wiggleEnabled = true;

    // endregion

    val triangloids = mutableListOf<Triangloid>()
    lateinit var minim: Minim
    lateinit var audioIn: AudioInput
    lateinit var fft: FFT
    lateinit var beatDetect: BeatDetect
    lateinit var autoMouse: AutoMouse

    val audioLevelObservable: PublishSubject<Float> = PublishSubject.create()
    var rmsSum = 0f

    override fun settings() {
        size(1280, 800, PConstants.P3D)
//        fullScreen(PConstants.P3D)
        smooth(4)
    }

    private fun regenerate() {
        triangloids.removeAt(0)
        triangloids.add(Triangloid(this, centerWeightEnabled))
    }

    override fun setup() {
        repeat(3, action = { triangloids.add(Triangloid(this, centerWeightEnabled)) })

        minim = Minim(this)

        audioIn = minim.getLineIn()
        audioIn.addListener(this)

        fft = FFT(audioIn.bufferSize(), audioIn.sampleRate())
        fft.logAverages(4, 1)

        beatDetect = BeatDetect(audioIn.bufferSize(), audioIn.sampleRate())
        beatDetect.setSensitivity(150)

        autoMouse = AutoMouse(this, centerX(), centerY())
    }

    override fun draw() {
        rmsSum += audioIn.mix.level()
        rmsSum *= 0.2f

        if (beatDetectEnabled && beatDetect.isSnare) {
            regenerate()
        }

        autoMouse.move()

        background(0f)

        if (debugWindowEnabled) {
            debugWindow()
        }

        if (flickerEnabled && frameCount % 4 == 0) {
            return
        }

        for (triangloid in triangloids) {
            triangloid.getShape().rotateY(0.000f + map(autoMouse.xPos, width.toFloat() / 2f, width.toFloat(), 0f, 0.15f))
            triangloid.getShape().rotateX(0.000f - map(autoMouse.yPos, height.toFloat() / 2f, height.toFloat(), 0f, 0.15f))
            triangloid.getShape().rotateZ(0.002f)

            if (wiggleEnabled) {
                triangloid.wiggle();
            }

            pushMatrix()
            translate(width / 2f, height / 2f)
            if (scaleByAudioEnabled) {
                scale(map(rmsSum, 0f, 1f, 0.5f, 1.5f))
            } else {
                scale(0.5f)
            }
            triangloid.draw()

            popMatrix()
        }
    }

    fun debugWindow() {
        // debug values
        val debugStr = StringBuilder()
                .append("resolution: ${width}x${height}").newLine()
                .append("frameRate: ${frameRate.toInt()}").newLine()
                .append("mouseX: ${mouseX - width / 2}").newLine()
                .append("mouseY: ${mouseY - height / 2}").newLine()
                .toString()

        noStroke()
        fill(0f, 255f, 100f)
        textSize(14f)
        text(debugStr, 12f, 24f)

        // menu
        val menuStr = StringBuilder()
                .append("[d] debug")
                .append(", [f] flicker: $flickerEnabled")
                .append(", [s] scale by audio: $scaleByAudioEnabled")
                .append(", [c] center-weighted triangloids: $centerWeightEnabled")
                .append(", [b] beat detect: $beatDetectEnabled")
                .append(", [w] wiggle: $wiggleEnabled")
                .toString()

        noStroke()
        fill(0f, 255f, 100f)
        textSize(14f)
        text(menuStr, 12f, height - 12f)

        // audio RMS
        val rectHeight = 2

        pushMatrix()
        translate(12f, 100f)
        rect(0f, 0f * rectHeight, audioIn.left.level() * 200, rectHeight.toFloat())

        fill(255f, 255f, 255f)
        rect(0f, 1f * rectHeight, rmsSum * 200, rectHeight.toFloat())

        fill(0f, 255f, 100f)
        rect(0f, 2f * rectHeight, audioIn.right.level() * 200, rectHeight.toFloat())
        popMatrix()

        // FFT
        pushMatrix()
        translate(12f, 130f)
        for (i in 0 until fft.specSize()) {
            rect(0f, i + rectHeight.toFloat(), fft.getBand(i) * 2, rectHeight.toFloat())
        }

        popMatrix()

        // AutoMouse
        autoMouse.draw()
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
            }
        }

        super.keyPressed(event)
    }
}