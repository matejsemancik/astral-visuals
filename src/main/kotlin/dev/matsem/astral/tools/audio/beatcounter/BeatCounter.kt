package dev.matsem.astral.tools.audio.beatcounter

import dev.matsem.astral.tools.audio.AudioProcessor

class BeatCounter(private val audioProcessor: AudioProcessor) {

    var kicksCount = 0
    var snaresCount = 0
    var hatsCount = 0

    private var listeners = mutableMapOf<BeatListener, () -> Unit>()

    fun update() {
        if (audioProcessor.beatDetectData.isKick) kicksCount++
        if (audioProcessor.beatDetectData.isSnare) snaresCount++
        if (audioProcessor.beatDetectData.isHat) hatsCount++

        listeners.forEach { listener, lambda ->
            when (listener.type) {
                OnKick -> if (kicksCount % listener.modulo == 0) {
                    if (listener.lastBeat != kicksCount) {
                        lambda()
                        listener.lastBeat = kicksCount
                    }
                }
                OnSnare -> if (snaresCount % listener.modulo == 0) {
                    if (listener.lastBeat != snaresCount) {
                        lambda()
                        listener.lastBeat = snaresCount
                    }
                }
                OnHat -> if (hatsCount % listener.modulo == 0) {
                    if (listener.lastBeat != hatsCount) {
                        lambda()
                        listener.lastBeat = hatsCount
                    }
                }
            }
        }
    }

    fun addListener(type: Type, modulo: Int, onBeat: () -> Unit): BeatListener {
        val listener = BeatListener(type, modulo)
        listeners[listener] = onBeat
        return listener
    }

    fun removeListener(listener: BeatListener) {
        listeners.remove(listener)
    }
}