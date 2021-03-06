package dev.matsem.astral.core.tools.midi

import dev.matsem.astral.core.tools.galaxy.Galaxy
import dev.matsem.astral.core.tools.galaxy.controls.PushButton
import dev.matsem.astral.core.tools.galaxy.controls.ToggleButton

/**
 * This class provides MIDI automation in live setup.
 * It is bound to TouchOSC controller buttons and it's main purpose is to simplify
 * automator implementation for each sketch.
 */
class MidiAutomator(
    private val midiRecorder: MidiRecorder,
    private val midiPlayer: MidiPlayer,
    private val galaxy: Galaxy
) {

    private lateinit var recordButton: ToggleButton
    private lateinit var playButton: ToggleButton
    private lateinit var loopButton: ToggleButton
    private lateinit var clearButton: PushButton

    private val buttonList = mutableListOf<Int>()

    fun setupWithGalaxy(
        channel: Int,
        recordButtonCC: Int,
        playButtonCC: Int,
        loopButtonCC: Int,
        clearButtonCC: Int,
        channelFilter: Int? = null
    ) {
        recordButton = galaxy
            .createToggleButton(channel, recordButtonCC, false) {
                if (it) {
                    startRecording()
                } else {
                    stopRecording()
                }
            }.also {
                buttonList += it.cc
            }

        loopButton = galaxy
            .createToggleButton(channel, loopButtonCC, false)
            .also {
                buttonList += it.cc
            }

        playButton = galaxy
            .createToggleButton(channel, playButtonCC, false) { isPressed ->
                when {
                    isPressed && loopButton.isPressed -> {
                        playLoop()
                    }
                    isPressed && !loopButton.isPressed -> {
                        playOneShot()
                    }
                    else -> stopPlayer()
                }
            }
            .also {
                buttonList += it.cc
            }

        clearButton = galaxy.createPushButton(channel, clearButtonCC) {
            clear()
        }.also {
            buttonList += it.cc
        }

        midiRecorder.plugIn(galaxy, channelFilter)
        midiPlayer.apply {
            plugIn(galaxy)
            doOnPlay { playButton.turnOn() }
            doOnStop { playButton.turnOff() }
        }
    }

    private fun startRecording() {
        midiRecorder.startRecording()
        recordButton.turnOn()

        playOneShot()
    }

    private fun stopRecording() {
        midiRecorder.stopRecording()
        recordButton.turnOff()
    }

    private fun playOneShot() {
        midiPlayer.enqueue(midiRecorder.getMessages(excludedCCs = buttonList))
        midiPlayer.play(loop = false)
    }

    private fun playLoop() {
        midiPlayer.enqueue(midiRecorder.getMessages(excludedCCs = buttonList))
        midiPlayer.play(loop = true)
    }

    private fun stopPlayer() = midiPlayer.stop()

    fun update() = midiPlayer.update()

    private fun clear() {
        stopRecording()
        stopPlayer()
        midiRecorder.clear()
    }
}