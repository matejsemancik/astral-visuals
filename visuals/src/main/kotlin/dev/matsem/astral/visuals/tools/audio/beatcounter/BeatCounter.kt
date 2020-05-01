package dev.matsem.astral.visuals.tools.audio.beatcounter

import dev.matsem.astral.visuals.tools.audio.AudioProcessor

class BeatCounter(private val audioProcessor: AudioProcessor) {

    private var kicksCount = 0
    private var snaresCount = 0
    private var hatsCount = 0

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

    fun sync() {
        kicksCount = 0
        snaresCount = 0
        hatsCount = 0
    }

    fun addListener(type: Type, modulo: Int, onBeat: () -> Unit) {
        val listener = BeatListener(type, modulo)
        listeners[listener] = onBeat
    }

    fun removeListener(listener: BeatListener) {
        listeners.remove(listener)
    }
}