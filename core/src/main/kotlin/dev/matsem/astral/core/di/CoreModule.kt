package dev.matsem.astral.core.di

import com.hamoid.VideoExport
import ddf.minim.AudioOutput
import ddf.minim.Minim
import ddf.minim.ugens.Sink
import dev.matsem.astral.core.tools.audio.AudioProcessor
import dev.matsem.astral.core.tools.audio.beatcounter.BeatCounter
import dev.matsem.astral.core.tools.galaxy.Galaxy
import dev.matsem.astral.core.tools.kontrol.KontrolF1
import dev.matsem.astral.core.tools.midi.MidiAutomator
import dev.matsem.astral.core.tools.midi.MidiFileParser
import dev.matsem.astral.core.tools.midi.MidiPlayer
import dev.matsem.astral.core.tools.midi.MidiRecorder
import dev.matsem.astral.core.tools.osc.OscManager
import dev.matsem.astral.core.tools.shapes.ExtrusionCache
import dev.matsem.astral.core.tools.videoexport.FFTSerializer
import dev.matsem.astral.core.tools.videoexport.VideoExporter
import org.jbox2d.common.Vec2
import org.koin.dsl.module
import processing.core.PApplet
import shiffman.box2d.Box2DProcessing

val coreModule = module {
    single { KontrolF1() }
    single { Galaxy(parent = get()) }

    single { MidiFileParser(sketch = get()) }
    factory { MidiRecorder(sketch = get()) }
    factory { MidiPlayer(sketch = get()) }
    factory { MidiAutomator(midiRecorder = get(), midiPlayer = get(), galaxy = get()) }
    single {
        OscManager(
            sketch = get(),
            inputPort = OscManager.INPUT_PORT,
            outputIp = OscManager.OUTPUT_IP,
            outputPort = OscManager.OUTPUT_PORT
        )
    }

    // Audio
    single { Minim(get() as PApplet) }
    single { (get() as Minim).lineOut }
    single { (get() as Minim).lineIn }
    single { Sink().apply { patch(get() as AudioOutput) } }
    single { AudioProcessor(lineIn = get()) }
    factory { BeatCounter(parent = get(), audioProcessor = get()) }

    // Extrusion
    single { extruder.extruder(get()) }
    single { ExtrusionCache(sketch = get(), ex = get()) }

    // VideoExporter
    single { VideoExport(get()) }
    single { FFTSerializer(parent = get(), minim = get()) }
    factory { VideoExporter(parent = get(), videoExport = get(), fftSerializer = get(), audioProcessor = get()) }

    factory {
        Box2DProcessing(get()).apply {
            createWorld(Vec2(0f, 0f))
            setContinuousPhysics(true)
        }
    }
}