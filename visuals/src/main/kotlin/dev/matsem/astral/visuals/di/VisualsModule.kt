package dev.matsem.astral.visuals.di

import dev.matsem.astral.visuals.Colorizer
import dev.matsem.astral.visuals.Effector
import dev.matsem.astral.visuals.EngineRoom
import dev.matsem.astral.visuals.Mixer
import dev.matsem.astral.visuals.layers.AttractorLayer
import dev.matsem.astral.visuals.layers.BackgroundLayer
import dev.matsem.astral.visuals.layers.BlobDetectionTerrainLayer
import dev.matsem.astral.visuals.layers.ConwayLayer
import dev.matsem.astral.visuals.layers.HexLayer
import dev.matsem.astral.visuals.layers.SphereLayer
import dev.matsem.astral.visuals.layers.TextOverlayLayer
import dev.matsem.astral.visuals.layers.debris.DebrisLayer
import dev.matsem.astral.visuals.layers.galaxy.GalaxyLayer
import dev.matsem.astral.visuals.layers.stars.StarsLayer
import dev.matsem.astral.visuals.legacy.boxes.BoxesSketch
import dev.matsem.astral.visuals.legacy.cubes.CubesSketch
import dev.matsem.astral.visuals.legacy.patterns.PatternsSketch
import dev.matsem.astral.visuals.tools.tapper.Tapper
import dev.matsem.astral.visuals.tools.video.VideoPreparationTool
import org.koin.dsl.bind
import org.koin.dsl.module
import processing.core.PApplet

fun visualsModule(provideSketch: () -> PApplet) = module {
    single { provideSketch() } bind PApplet::class

    factory { VideoPreparationTool(get(), get(), get(), get(), get(), get(), get(), get()) }

    // BPM
    single { Tapper(get()) }

    factory { PatternsSketch() }
    factory { BoxesSketch() }
    factory { CubesSketch() }

    single { Mixer(get()) }
    single { Effector(get(), get()) }
    single { Colorizer(get(), get()) }
    single { EngineRoom(get(), get(), get(), get(), get()) }

    factory { BackgroundLayer() }
    factory { StarsLayer() }
    factory { TextOverlayLayer() }
    factory { BlobDetectionTerrainLayer() }
    factory { AttractorLayer() }
    factory { ConwayLayer() }
    factory { GalaxyLayer() }
    factory { SphereLayer() }
    factory { HexLayer() }
    factory { DebrisLayer() }
}