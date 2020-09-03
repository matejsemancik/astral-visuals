package dev.matsem.astral.visuals.layers

import dev.matsem.astral.core.Files
import dev.matsem.astral.core.tools.extensions.colorModeHsb
import dev.matsem.astral.visuals.Layer
import org.koin.core.KoinComponent
import org.koin.core.inject
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PGraphics
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.math.absoluteValue

class Countdown : Layer(), KoinComponent {

    override val parent: PApplet by inject()

    val countdownText = "SYMBOL_LP_KŘEST"
    val deadline = LocalDateTime.of(LocalDate.now(), LocalTime.of(22, 30, 0))
    val jetbrainsMonoFont = parent.createFont(Files.Font.JETBRAINS_MONO, 24f, true)

    override fun PGraphics.draw() {
        clear()
        colorModeHsb()
        textFont(jetbrainsMonoFont)
        textSize(24f)
        textAlign(PConstants.LEFT, PConstants.BOTTOM)

        val now = LocalDateTime.now()
        val duration = Duration.between(deadline, now)
        val durationString = String.format(
            "%02d:%02d:%02d:%03d",
            duration.seconds.absoluteValue / 3600,
            duration.seconds.absoluteValue % 3600 / 60,
            duration.seconds.absoluteValue % 60,
            duration.toMillis().absoluteValue % 1000
        )

        val text = when {
            duration.isNegative -> "$countdownText: T-$durationString"
            else -> "$countdownText: T+$durationString"
        }
        text(text, 10f, height - 10f)
    }
}
