package dev.matsem.astral.di

import com.hamoid.VideoExport
import controlP5.ControlP5
import ddf.minim.Minim
import dev.matsem.astral.Config
import dev.matsem.astral.sketches.SketchLoader
import dev.matsem.astral.sketches.attractor.AttractorSketch
import dev.matsem.astral.sketches.blank.BlankSketch
import dev.matsem.astral.sketches.boxes.BoxesSketch
import dev.matsem.astral.sketches.cubes.CubesSketch
import dev.matsem.astral.sketches.fibonaccisphere.FibSphereSketch
import dev.matsem.astral.sketches.gameoflife.GameOfLifeSketch
import dev.matsem.astral.sketches.patterns.PatternsSketch
import dev.matsem.astral.sketches.polygonal.PolygonalSketch
import dev.matsem.astral.sketches.spikes.SpikesSketch
import dev.matsem.astral.sketches.galaxy.GalaxySketch
import dev.matsem.astral.sketches.oldskool.OldSkoolSketch
import dev.matsem.astral.sketches.starglitch.StarGlitchSketch
import dev.matsem.astral.sketches.terrain.TerrainSketch
import dev.matsem.astral.sketches.video.VideoSketch
import dev.matsem.astral.tools.audio.AudioProcessor
import dev.matsem.astral.tools.audio.beatcounter.BeatCounter
import dev.matsem.astral.tools.automator.MidiAutomator
import dev.matsem.astral.tools.galaxy.Galaxy
import dev.matsem.astral.tools.kontrol.KontrolF1
import dev.matsem.astral.tools.midi.MidiFileParser
import dev.matsem.astral.tools.midi.MidiPlayer
import dev.matsem.astral.tools.midi.MidiRecorder
import dev.matsem.astral.tools.video.VideoPreparationTool
import org.jbox2d.common.Vec2
import org.koin.dsl.bind
import org.koin.dsl.module
import processing.core.PApplet
import shiffman.box2d.Box2DProcessing

val appModule = module {
    single { SketchLoader() } bind PApplet::class

    // Controllers
    single { KontrolF1() }
    single { Galaxy() }
    single { ControlP5(get()).apply { isAutoDraw = false } }

    // Midi tools
    single { MidiFileParser(get()) }
    factory { MidiRecorder(get()) }
    factory { MidiPlayer(get()) }
    factory { MidiAutomator(get(), get(), get()) }

    // Video tools
    factory { VideoPreparationTool(get(), get(), get(), get(), get()) }

    // Audio
    single { Minim(get() as PApplet) }
    single { AudioProcessor(get(), Config.VideoExport.IS_IN_RENDER_MODE) }
    factory { BeatCounter(get()) }

    // Video Export
    single {
        VideoExport(get()).apply {
            setFrameRate(Config.VideoExport.MOVIE_FPS)
            setAudioFileName(Config.VideoExport.AUDIO_FILE_PATH)
        }
    }

    // Physics
    factory {
        Box2DProcessing(get()).apply {
            createWorld(Vec2(0f, 0f))
            setContinuousPhysics(true)
        }
    }

    // Sketches
    factory { BlankSketch() }
    factory { PolygonalSketch() }
    factory { TerrainSketch() }
    factory { FibSphereSketch() }
    factory { StarGlitchSketch() }
    factory { PatternsSketch() }
    factory { BoxesSketch() }
    factory { AttractorSketch() }
    factory { SpikesSketch() }
    factory { CubesSketch() }
    factory { VideoSketch() }
    factory { GalaxySketch() }
    factory { GameOfLifeSketch() }
    factory { OldSkoolSketch() }
}