package dev.matsem.astral.visuals.di

import com.hamoid.VideoExport
import ddf.minim.Minim
import dev.matsem.astral.visuals.Config
import dev.matsem.astral.visuals.sketches.SketchLoader
import dev.matsem.astral.visuals.sketches.attractor.AttractorSketch
import dev.matsem.astral.visuals.sketches.boxes.BoxesSketch
import dev.matsem.astral.visuals.sketches.cubes.CubesSketch
import dev.matsem.astral.visuals.sketches.fibonaccisphere.FibSphereSketch
import dev.matsem.astral.visuals.sketches.galaxy.GalaxySketch
import dev.matsem.astral.visuals.sketches.gameoflife.GameOfLifeSketch
import dev.matsem.astral.visuals.sketches.oldskool.OldSkoolSketch
import dev.matsem.astral.visuals.sketches.patterns.PatternsSketch
import dev.matsem.astral.visuals.sketches.polygonal.PolygonalSketch
import dev.matsem.astral.visuals.sketches.spikes.SpikesSketch
import dev.matsem.astral.visuals.sketches.starglitch.StarGlitchSketch
import dev.matsem.astral.visuals.sketches.terrain.TerrainSketch
import dev.matsem.astral.visuals.sketches.tunnel.TunnelSketch
import dev.matsem.astral.visuals.sketches.video.VideoSketch
import dev.matsem.astral.visuals.tools.audio.AudioProcessor
import dev.matsem.astral.visuals.tools.audio.beatcounter.BeatCounter
import dev.matsem.astral.visuals.tools.automator.MidiAutomator
import dev.matsem.astral.visuals.tools.galaxy.Galaxy
import dev.matsem.astral.visuals.tools.kontrol.KontrolF1
import dev.matsem.astral.visuals.tools.midi.MidiFileParser
import dev.matsem.astral.visuals.tools.midi.MidiPlayer
import dev.matsem.astral.visuals.tools.midi.MidiRecorder
import dev.matsem.astral.visuals.tools.shapes.ExtrusionCache
import dev.matsem.astral.visuals.tools.tapper.Tapper
import dev.matsem.astral.visuals.tools.video.VideoPreparationTool
import org.jbox2d.common.Vec2
import org.koin.dsl.bind
import org.koin.dsl.module
import processing.core.PApplet
import shiffman.box2d.Box2DProcessing

object VisualsComponent {

    val visualsModule = module {
        single { SketchLoader() } bind PApplet::class // TODO how to handle multiple PApplets with dependencies?

        // Controllers TODO to core
        single { KontrolF1() }
        single { Galaxy() }

        // Midi tools TODO to core
        single { MidiFileParser(get()) }
        factory { MidiRecorder(get()) }
        factory { MidiPlayer(get()) }
        factory { MidiAutomator(get(), get(), get()) }

        // Video tools TODO visuals-only
        factory { VideoPreparationTool(get(), get(), get(), get(), get(), get(), get(), get()) }

        // Audio TODO to core
        single { Minim(get() as PApplet) }
        single { AudioProcessor(get(), Config.VideoExport.IS_IN_RENDER_MODE) }
        factory { BeatCounter(get()) }

        // BPM
        single { Tapper(get()) }

        // Shapes TODO visuals-only
        single { extruder.extruder(get()) }
        single { ExtrusionCache(get(), get()) }

        // Video Export TODO visuals-only
        single {
            VideoExport(get()).apply {
                setFrameRate(Config.VideoExport.MOVIE_FPS)
                setAudioFileName(Config.VideoExport.AUDIO_FILE_PATH)
            }
        }

        // Physics TODO to core
        factory {
            Box2DProcessing(get()).apply {
                createWorld(Vec2(0f, 0f))
                setContinuousPhysics(true)
            }
        }

        // Sketches TODO visuals-only
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
        factory { TunnelSketch() }
    }
}