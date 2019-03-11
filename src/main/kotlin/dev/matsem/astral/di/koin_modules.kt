package dev.matsem.astral.di

import dev.matsem.astral.sketches.SketchLoader
import dev.matsem.astral.tools.audio.AudioProcessor
import dev.matsem.astral.tools.galaxy.Galaxy
import dev.matsem.astral.tools.kontrol.KontrolF1
import org.koin.dsl.module

val appModule = module {
    single { SketchLoader() }
    single { KontrolF1() }
    single { Galaxy() }
    single { AudioProcessor(get(), SketchLoader.IS_IN_RENDER_MODE) }

    factory {  }
}