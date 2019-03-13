package dev.matsem.astral.di

import com.hamoid.VideoExport
import controlP5.ControlP5
import dev.matsem.astral.Config
import dev.matsem.astral.sketches.SketchLoader
import dev.matsem.astral.sketches.attractor.AttractorSketch
import dev.matsem.astral.sketches.blank.BlankSketch
import dev.matsem.astral.sketches.boxes.BoxesSketch
import dev.matsem.astral.sketches.fibonaccisphere.FibSphereSketch
import dev.matsem.astral.sketches.machina.MachinaSketch
import dev.matsem.astral.sketches.patterns.PatternsSketch
import dev.matsem.astral.sketches.polygonal.PolygonalSketch
import dev.matsem.astral.sketches.starglitch.StarGlitchSketch
import dev.matsem.astral.sketches.terrain.TerrainSketch
import dev.matsem.astral.tools.audio.AudioProcessor
import dev.matsem.astral.tools.galaxy.Galaxy
import dev.matsem.astral.tools.kontrol.KontrolF1
import org.koin.dsl.bind
import org.koin.dsl.module
import processing.core.PApplet

val appModule = module {
    single { SketchLoader() } bind PApplet::class
    single { KontrolF1() }
    single { Galaxy() }
    single { AudioProcessor(get(), Config.Sketch.IS_IN_RENDER_MODE) }
    single {
        VideoExport(get()).apply {
            setFrameRate(Config.VideoExport.MOVIE_FPS)
            setAudioFileName(Config.VideoExport.AUDIO_FILE_PATH)
        }
    }
    single {
        ControlP5(get()).apply {
            isAutoDraw = false
        }
    }

    factory { BlankSketch() }
    factory { PolygonalSketch() }
    factory { TerrainSketch() }
    factory { FibSphereSketch() }
    factory { StarGlitchSketch() }
    factory { PatternsSketch() }
    factory { MachinaSketch() }
    factory { BoxesSketch() }
    factory { AttractorSketch() }
}