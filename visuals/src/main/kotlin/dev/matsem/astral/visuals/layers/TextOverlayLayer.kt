package dev.matsem.astral.visuals.layers

import dev.matsem.astral.core.Files
import dev.matsem.astral.core.tools.extensions.colorModeHsb
import dev.matsem.astral.core.tools.extensions.mapp
import dev.matsem.astral.core.tools.extensions.withAlpha
import dev.matsem.astral.core.tools.osc.OscHandler
import dev.matsem.astral.core.tools.osc.OscManager
import dev.matsem.astral.core.tools.osc.oscLabelIndicator
import dev.matsem.astral.core.tools.osc.oscPushButton
import dev.matsem.astral.visuals.Layer
import org.koin.core.KoinComponent
import org.koin.core.inject
import processing.core.PApplet
import processing.core.PGraphics
import java.time.Duration
import java.time.LocalDateTime
import java.time.Month
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.math.absoluteValue

class TextOverlayLayer : Layer(), KoinComponent, OscHandler {

    override val parent: PApplet by inject()
    override val oscManager: OscManager by inject()

    val lock = Any()
    val countdownText = "SYMBOL_LP_KŘEST"

    var deadline = LocalDateTime.of(2020, Month.SEPTEMBER, 11, 23, 0, 0)
    val jetbrainsMonoFont = parent.createFont(Files.Font.JETBRAINS_MONO, 24f, true)

    val minus1Button by oscPushButton("/text/minus1") {
        addMinutes(-1)
        updateLabel()
    }
    val minus10Button by oscPushButton("/text/minus10") {
        addMinutes(-10)
        updateLabel()
    }
    val plus10Button by oscPushButton("/text/plus10") {
        addMinutes(10)
        updateLabel()
    }
    val plus1Button by oscPushButton("/text/plus1") {
        addMinutes(1)
        updateLabel()
    }
    var deadlineLabel by oscLabelIndicator("/text/deadline", defaultValue = "--:--:--")

    private fun addMinutes(count: Long) {
        synchronized(lock) {
            deadline = deadline.plusMinutes(count)
        }
    }

    private fun updateLabel() {
        deadlineLabel = deadline.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM))
    }

    override fun PGraphics.draw() {
        synchronized(lock) {
            clear()
            colorModeHsb()
            textFont(jetbrainsMonoFont)
            textSize(24f)

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
                duration.seconds in (0L..60L) -> "$countdownText YEABOIIIIIIIII"
                duration.isNegative -> "$countdownText: T-$durationString @ ${parent.frameRate} FPS"
                else -> "$countdownText: T+$durationString @ ${parent.frameRate} FPS"
            }

            val textX = 10f
            val textY = height - 10f

            noStroke()
            fill(0x000000.withAlpha())
            rectMode(PApplet.CORNER)
            rect(textX, textY - textSize, textWidth(text), textSize + 4)

            noStroke()
            fill(0xffffff.withAlpha())

            if (duration.seconds in (0L..20L)) {
                text(
                    "$countdownText YEABOIIIIIIIII",
                    textX + parent.random(1f).mapp(-5f, 5f),
                    textY + parent.random(1f).mapp(-5f, 5f)
                )
            } else {
                text(text, textX, textY)
            }
        }
    }
}
