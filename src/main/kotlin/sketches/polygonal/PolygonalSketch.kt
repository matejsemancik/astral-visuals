package sketches.polygonal

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
        audioLevelObservable.onNext(audioIn.mix.level())
    }

    override fun samples(p0: FloatArray?, p1: FloatArray?) {
        beatDetect.detect(audioIn.mix)
        audioLevelObservable.onNext(audioIn.mix.level())
    }

    // endregion

    var showDebugWindow = true
    val triangloids = mutableListOf<Triangloid>()
    lateinit var minim: Minim
    lateinit var audioIn: AudioInput
    lateinit var fft: FFT
    lateinit var beatDetect: BeatDetect
    val audioLevelObservable: PublishSubject<Float> = PublishSubject.create()
    var rmsSum = 0f

    override fun settings() {
        size(1600, 900, PConstants.P3D)
//        fullScreen(PConstants.P3D)
        smooth(4)
    }

    private fun regenerate() {
        triangloids.removeAt(0)
        triangloids.add(Triangloid(this, width / 2f, height / 2f))
    }

    override fun setup() {
        repeat(3, action = { triangloids.add(Triangloid(this, width / 2f, height / 2f)) })

        minim = Minim(this)

        audioIn = minim.getLineIn()
        audioIn.addListener(this)

        fft = FFT(audioIn.bufferSize(), audioIn.sampleRate())
        fft.logAverages(4, 1)

        beatDetect = BeatDetect(audioIn.bufferSize(), audioIn.sampleRate())
        beatDetect.setSensitivity(150)
    }

    override fun draw() {
        rmsSum += audioIn.mix.level()
        rmsSum *= 0.8f

        if (beatDetect.isSnare) {
            regenerate()
        }

        background(0f)
        if (showDebugWindow) {
            debugWindow()
        }

        if (frameCount % 4 == 0) {
            return
        }

        for (triangloid in triangloids) {
            triangloid.getShape().rotateY(0.000f + map(mouseX.toFloat(), width.toFloat() / 2f, width.toFloat(), 0f, 0.15f))
            triangloid.getShape().rotateX(0.000f - map(mouseY.toFloat(), height.toFloat() / 2f, height.toFloat(), 0f, 0.15f))
            triangloid.getShape().rotateZ(0.002f)

            triangloid.wiggle()

            pushMatrix()
            translate(width / 2f, height / 2f)
            scale(map(rmsSum, 0f, 1f, 0.5f, 1.5f))

            triangloid.draw()

            popMatrix()
        }
    }

    fun debugWindow() {
        val str = StringBuilder()
                .append("resolution: ${width}x${height}").newLine()
                .append("frameRate: ${frameRate.toInt()}").newLine()
                .append("mouseX: ${mouseX - width / 2}").newLine()
                .append("mouseY: ${mouseY - height / 2}").newLine()
                .toString()

        noStroke()
        fill(0f, 255f, 100f)
        textSize(14f)
        text(str, 12f, 24f)

        // audio RMS
        val rectHeight = 5

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
        fft.forward(audioIn.mix)
        for (i in 0 until fft.specSize()) {
            rect(0f, i + rectHeight.toFloat(), fft.getBand(i) * 10, rectHeight.toFloat())
        }

        popMatrix()
    }

    override fun mouseClicked() {
        regenerate()
    }

    override fun keyPressed(event: KeyEvent?) {
        if (event?.key == 'd') {
            showDebugWindow = !showDebugWindow
        }

        super.keyPressed(event)
    }
}