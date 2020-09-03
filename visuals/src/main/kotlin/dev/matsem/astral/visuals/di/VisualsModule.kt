package dev.matsem.astral.visuals.di

import dev.matsem.astral.visuals.layers.Countdown
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

    factory { PolygonalSketch() }
    factory { TerrainSketch() }
    factory { FibSphereSketch() }
    factory { StarGlitchSketch() }
    factory { PatternsSketch() }
    factory { BoxesSketch() }
    factory { AttractorSketch() }
    factory { SpikesSketch() }
    factory { CubesSketch() }
    factory { GalaxySketch() }
    factory { GameOfLifeSketch() }
    factory { OldSkoolSketch() }
    factory { TunnelSketch() }
}