package dev.matsem.astral.sketches.playground

import dev.matsem.astral.tools.extensions.centerX
import dev.matsem.astral.tools.extensions.centerY
import dev.matsem.astral.tools.extensions.midiRange
import dev.matsem.astral.tools.kontrol.KontrolF1
import dev.matsem.astral.tools.kontrol.Pad
import org.koin.core.KoinComponent
import org.koin.core.inject
import processing.core.PApplet
import processing.core.PConstants
import kotlin.random.Random

// TODO to BaseSketch?
class PlaygroundSketch : PApplet(), KoinComponent {

    private var t: Float = 0f
    private val kontrol: KontrolF1 by inject()

    private val eq1 = ParametricEquation(listOf(SinStep(1 / 100f, 400f)))
    private val eq2 = ParametricEquation(listOf(CosStep(1 / 120f, -40f), SinStep(1 / 34f, 140f)))
    private val eq3 = ParametricEquation(listOf(SinStep(-1 / 67f, -40f), SinStep(1 / 100f, 20f), CosStep(1 / 50f, -20f)))
    private val eq4 = ParametricEquation(listOf(CosStep(1 / 80f, 200f)))

    override fun settings() {
        size(1280, 720, PConstants.P3D)
    }

    override fun setup() {
        colorMode(PConstants.HSB, 360f, 100f, 100f)
        kontrol.connect()

        repeat(4) { i ->
            kontrol.pad(0, i).apply {
                mode = Pad.Mode.TRIGGER
                colorOff = Triple(i * 10, 127, 48)
                colorOn = Triple(i * 10, 127, 127)
                setTriggerListener {
                    when (i) {
                        0 -> eq1.randomize(random(1f, 3f).toInt())
                        1 -> eq2.randomize(random(1f, 3f).toInt())
                        2 -> eq3.randomize(random(1f, 3f).toInt())
                        3 -> eq4.randomize(random(1f, 3f).toInt())
                    }
                }
            }
        }
    }

    override fun draw() {
        background(0f, 0f, 10f)
        stroke(kontrol.knob4.midiRange(360f), 100f, 100f)
        strokeWeight(5f)

        translate(centerX(), centerY())

        val spacing = kontrol.knob1.midiRange(10f)
        val tail = kontrol.slider1.midiRange(20f).toInt() + 1
        repeat(tail) { i ->
            line(x1(t - (i * spacing)), y1(t - (i * spacing)), x2(t - (i * spacing)), y2(t - (i * spacing)))
        }

        t += kontrol.knob2.midiRange(1f, 4f)
    }

    fun x1(t: Float): Float {
        return eq1.calculate(t)
    }

    fun y1(t: Float): Float {
        return eq2.calculate(t)
    }

    fun x2(t: Float): Float {
        return eq3.calculate(t)
    }

    fun y2(t: Float): Float {
        return eq4.calculate(t)
    }

    inner class ParametricEquation constructor(private var steps: List<Step>) {

        fun calculate(t: Float): Float {
            return steps.sumByDouble { it.calculate(t).toDouble() }.toFloat()
        }

        fun randomize(howMuchSteps: Int) {
            val newSteps = mutableListOf<Step>()

            repeat(howMuchSteps) {
                newSteps.add(getRandomStep())
            }

            steps = newSteps
        }

        private fun getRandomStep(): Step {
            val time = random(1 / 120f, 1 / 40f)
            val mult = random(-400f, 400f)
            return if (Random.nextBoolean()) {
                SinStep(time, mult)
            } else CosStep(time, mult)
        }
    }

    interface Step {
        fun calculate(t: Float): Float
    }

    class SinStep(val time: Float, val mult: Float) : Step {
        override fun calculate(t: Float) = sin(t * time) * mult
    }

    class CosStep(val time: Float, val mult: Float) : Step {
        override fun calculate(t: Float) = cos(t * time) * mult
    }
}