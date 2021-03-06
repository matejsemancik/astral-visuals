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
import dev.matsem.astral.visuals.tools.tapper.Tapper
import org.koin.dsl.bind
import org.koin.dsl.module
import processing.core.PApplet

fun visualsModule(provideSketch: () -> PApplet) = module {
    single { provideSketch() } bind PApplet::class

    // BPM
    single { Tapper(get()) }

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