package dev.matsem.astral.core.di

import com.hamoid.VideoExport
import ddf.minim.AudioOutput
import ddf.minim.Minim
import ddf.minim.ugens.Sink
import dev.matsem.astral.core.VideoExportConfig
import dev.matsem.astral.core.tools.audio.AudioProcessor
import dev.matsem.astral.core.tools.audio.beatcounter.BeatCounter
import dev.matsem.astral.core.tools.galaxy.Galaxy
import dev.matsem.astral.core.tools.kontrol.KontrolF1
import dev.matsem.astral.core.tools.midi.MidiAutomator
import dev.matsem.astral.core.tools.midi.MidiFileParser
import dev.matsem.astral.core.tools.midi.MidiPlayer
import dev.matsem.astral.core.tools.midi.MidiRecorder
import org.jbox2d.common.Vec2
import org.koin.dsl.module
import processing.core.PApplet
import shiffman.box2d.Box2DProcessing

val coreModule = module {
    single { KontrolF1() }
    single { Galaxy() }

    single { MidiFileParser(get()) }
    factory { MidiRecorder(get()) }
    factory { MidiPlayer(get()) }
    factory { MidiAutomator(get(), get(), get()) }

    // Audio
    single { Minim(get() as PApplet) }
    single { (get() as Minim).lineOut }
    single { Sink().apply { patch(get() as AudioOutput) } }
    single { AudioProcessor(get(), VideoExportConfig.IS_IN_RENDER_MODE) }
    factory { BeatCounter(get()) }

    single {
        VideoExport(get()).apply {
            setFrameRate(VideoExportConfig.MOVIE_FPS)
            setAudioFileName(VideoExportConfig.AUDIO_FILE_PATH)
        }
    }

    factory {
        Box2DProcessing(get()).apply {
            createWorld(Vec2(0f, 0f))
            setContinuousPhysics(true)
        }
    }
}