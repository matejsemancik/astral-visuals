package dev.matsem.astral.tools.video

import ddf.minim.AudioPlayer
import ddf.minim.Minim
import dev.matsem.astral.Config
import dev.matsem.astral.tools.audio.AudioProcessor
import dev.matsem.astral.tools.midi.MidiDevice
import dev.matsem.astral.tools.midi.MidiFileParser
import dev.matsem.astral.tools.midi.MidiPlayer
import dev.matsem.astral.tools.midi.MidiRecorder

class VideoPreparationTool(
        private val midiRecorder: MidiRecorder,
        private val midiPlayer: MidiPlayer,
        private val midiFileParser: MidiFileParser,
        private val minim: Minim,
        private val audioProcessor: AudioProcessor
) {

    private var audioPlayer: AudioPlayer? = null
    private var blacklistedCCs: List<Int> = listOf()

    val isPlaying: Boolean
        get() = midiPlayer.isPlaying

    fun plugInMidiDevice(device: MidiDevice) = apply {
        midiRecorder.plugIn(device)
        midiPlayer.plugIn(device)
    }

    fun setMusicFile(path: String) = apply {
        try {
            this.audioPlayer = minim.loadFile(path)
            this.audioPlayer?.addListener(audioProcessor)
        } catch (e: Throwable) {
            // ¯\_(ツ)_/¯
        }
    }

    fun setBlacklistedMessages(ccs: List<Int>) = apply {
        this.blacklistedCCs = ccs
    }

    fun startRecording() {
        midiRecorder.startRecording()
        midiPlayer.enqueue(midiRecorder.getMessages(excludedCCs = blacklistedCCs))
        midiPlayer.play()
        audioPlayer?.play()
    }

    fun stopRecording() {
        midiRecorder.stopRecording()
        midiPlayer.stop()
        audioPlayer?.pause()
        audioPlayer?.rewind()
    }

    fun startReplay() {
        midiPlayer.enqueue(midiRecorder.getMessages(excludedCCs = blacklistedCCs))
        midiPlayer.play()
        audioPlayer?.play()
    }

    fun stopReplay() {
        midiPlayer.stop()
        audioPlayer?.pause()
        audioPlayer?.rewind()
    }

    fun update() {
        midiPlayer.update()
    }

    fun saveIntoFile(fileName: String = Config.VideoExport.MIDI_AUTOMATION_FILE) =
            midiFileParser.saveFile(midiRecorder.getMessages(excludedCCs = blacklistedCCs), fileName)
}