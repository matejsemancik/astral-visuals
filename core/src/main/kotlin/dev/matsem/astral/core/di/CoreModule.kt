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
import dev.matsem.astral.core.tools.pixelsort.PixelSorter
import dev.matsem.astral.core.tools.shapes.ExtrusionCache
import dev.matsem.astral.core.tools.videoexport.FFTSerializer
import dev.matsem.astral.core.tools.videoexport.VideoExporter
import org.jbox2d.common.Vec2
import org.koin.dsl.module
import processing.core.PApplet
import shiffman.box2d.Box2DProcessing

val coreModule = module {
    single { KontrolF1() }
    single { Galaxy(get()) }

    single { MidiFileParser(get()) }
    factory { MidiRecorder(get()) }
    factory { MidiPlayer(get()) }
    factory { MidiAutomator(get(), get(), get()) }
    single { OscManager(get(), OscManager.INPUT_PORT, OscManager.OUTPUT_IP, OscManager.OUTPUT_PORT) }

    // Audio
    single { Minim(get() as PApplet) }
    single { (get() as Minim).lineOut }
    single { (get() as Minim).lineIn }
    single { Sink().apply { patch(get() as AudioOutput) } }
    single { AudioProcessor(get()) }
    factory { BeatCounter(get(), get()) }

    // Extrusion
    single { extruder.extruder(get()) }
    single { ExtrusionCache(get(), get()) }

    // Effects
    single { PixelSorter() }

    // VideoExporter
    single { VideoExport(get()) }
    single { FFTSerializer(get(), get()) }
    factory { VideoExporter(get(), get(), get(), get()) }

    factory {
        Box2DProcessing(get()).apply {
            createWorld(Vec2(0f, 0f))
            setContinuousPhysics(true)
        }
    }
}