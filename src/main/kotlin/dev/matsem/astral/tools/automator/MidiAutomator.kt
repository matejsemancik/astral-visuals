package dev.matsem.astral.tools.automator

import dev.matsem.astral.tools.galaxy.Galaxy
import dev.matsem.astral.tools.galaxy.controls.PushButton
import dev.matsem.astral.tools.galaxy.controls.ToggleButton
import dev.matsem.astral.tools.midi.MidiPlayer
import dev.matsem.astral.tools.midi.MidiRecorder

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

    private var recordButton: ToggleButton? = null
    private var playButton: ToggleButton? = null
    private var clearButton: PushButton? = null

    private val buttonList = mutableListOf<Int>()

    fun setupWithGalaxy(
            channel: Int,
            recordButtonCC: Int,
            playButtonCC: Int,
            clearButtonCC: Int
    ) {
        recordButton = galaxy
                .createToggleButton(channel, recordButtonCC, false) {
                    if (it) {
                        stopPlayer()
                        startRecording(startOnFirstNote = false)
                    } else {
                        stopRecording()
                    }
                }.also {
                    buttonList += it.cc
                }

        playButton = galaxy
                .createToggleButton(channel, playButtonCC, false) {
                    if (it) {
                        stopRecording()
                        playOneShot()
                    } else {
                        stopPlayer()
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

        midiRecorder.plugIn(galaxy)
    }

    private fun startRecording(startOnFirstNote: Boolean) {
        //TODO first note
        midiRecorder.startRecording()
        midiPlayer.enqueue(midiRecorder.getMessages(excludedCCs = buttonList))
        midiPlayer.play()
    }

    private fun stopRecording() {
        midiRecorder.stopRecording()
        midiPlayer.stop()
    }

    private fun playOneShot() {
        midiPlayer.enqueue(midiRecorder.getMessages(excludedCCs = buttonList))
        midiPlayer.play()
    }

    private fun playLoop() {
        // TODO()
    }

    private fun stopPlayer() {
        midiPlayer.stop()
        playButton?.isPressed
    }

    fun update() {
        midiPlayer.update()
    }

    private fun clear() {
        // TODO
    }
}